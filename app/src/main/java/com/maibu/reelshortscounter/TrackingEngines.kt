package com.maibu.reelshortscounter

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

interface PlatformTracker {
    val packageName: String
    var lastIndex: Int
    
    fun isTargetPlatform(activePackage: String): Boolean {
        return activePackage == packageName
    }

    fun checkForNewSwipe(rootNode: AccessibilityNodeInfo, event: AccessibilityEvent): Boolean

    fun reset() {
        lastIndex = -1
    }
}

// 📸 Instagram Logic
class InstagramTracker : PlatformTracker {
    override val packageName = "com.instagram.android"
    override var lastIndex = -1

    override fun checkForNewSwipe(rootNode: AccessibilityNodeInfo, event: AccessibilityEvent): Boolean {
        val className = event.className?.toString() ?: ""
        if (!className.contains("RecyclerView") && !className.contains("ViewPager")) return false

        val isReels = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_video_container").isNotEmpty()
        if (!isReels) return false

        val currentIndex = event.fromIndex
        if (currentIndex == -1) return false 

        // Instagram indices increment cleanly upwards
        if (lastIndex != -1 && currentIndex > lastIndex) {
            lastIndex = currentIndex
            return true 
        }
        lastIndex = currentIndex
        return false
    }
}

// 📺 YouTube Logic
class YouTubeTracker : PlatformTracker {
    override val packageName = "com.google.android.youtube"
    override var lastIndex = -1
    private var lastScrollTime = 0L

    override fun checkForNewSwipe(rootNode: AccessibilityNodeInfo, event: AccessibilityEvent): Boolean {
        // 1. Isolate the Shorts UI strictly
        val hasReelRecycler = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler").isNotEmpty()
        val hasReelViewer = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_viewer_page").isNotEmpty()
        val hasShortsPlayer = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/shorts_player_view").isNotEmpty()
        
        // If we are watching a normal horizontal video, ignore completely
        if (!hasReelRecycler && !hasReelViewer && !hasShortsPlayer) return false

        // 2. Ignore scrolls if the user is reading the comments!
        val hasCommentPanel = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/engagement_panel_root").isNotEmpty()
        val hasBottomSheet = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/bottom_sheet").isNotEmpty()
        if (hasCommentPanel || hasBottomSheet) return false

        // 3. Smart Index Reading
        var currentIndex = event.fromIndex
        if (currentIndex < 0) {
            currentIndex = event.toIndex
        }

        val currentTime = System.currentTimeMillis()

        // 4. Handle the swipe logic
        if (currentIndex > -1) {
            // YouTube recycles views in a loop (e.g., 0, 1, 2, 0, 1, 2)
            // If the index CHANGED, they moved to a new video.
            if (lastIndex != -1 && currentIndex != lastIndex) {
                // 400ms debounce prevents double-counting weird Android multi-touch bounces
                if (currentTime - lastScrollTime > 400) {
                    lastIndex = currentIndex
                    lastScrollTime = currentTime
                    return true
                }
            }
            lastIndex = currentIndex
            if (lastScrollTime == 0L) lastScrollTime = currentTime
        } else {
            // 5. Bulletproof Fallback
            // If lastScrollTime is 0, it means it's the first time detecting a video.
            // We set the time but don't count it yet.
            if (lastScrollTime != 0L && currentTime - lastScrollTime > 1200) {
                lastScrollTime = currentTime
                return true
            }
            lastScrollTime = currentTime
        }

        return false
    }

    override fun reset() {
        super.reset()
        lastScrollTime = 0L
    }
}

// 🔵 Facebook Logic
class FacebookTracker : PlatformTracker {
    override val packageName = "com.facebook.katana"
    override var lastIndex = -1
    private var lastScrollTime = 0L
    private var lastContentId = ""

    fun isTargetPlatformVariant(activePackage: String): Boolean {
        return activePackage == "com.facebook.katana" || 
               activePackage == "com.facebook.lite" || 
               activePackage == "com.facebook.wakizashi"
    }

    override fun checkForNewSwipe(rootNode: AccessibilityNodeInfo, event: AccessibilityEvent): Boolean {
        // Facebook Reels detection via presence of engagement buttons or player labels
        val hasLike = rootNode.findAccessibilityNodeInfosByText("Like").isNotEmpty()
        val hasComment = rootNode.findAccessibilityNodeInfosByText("Comment").isNotEmpty()
        val hasShare = rootNode.findAccessibilityNodeInfosByText("Share").isNotEmpty()
        val isReels = hasLike || hasComment || hasShare || 
                     rootNode.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/reels_video_view_container").isNotEmpty()
        
        if (!isReels) return false

        // Unique Content Check: Try to find a stable label that identifies this specific reel
        // e.g., the creator's name is usually a button or text node
        val contentId = findFacebookContentId(rootNode)
        val currentTime = System.currentTimeMillis()

        var currentIndex = event.fromIndex
        if (currentIndex < 0) currentIndex = event.toIndex

        if (contentId.isNotEmpty() && contentId != lastContentId) {
            if (lastContentId.isNotEmpty() && currentTime - lastScrollTime > 500) {
                lastContentId = contentId
                lastScrollTime = currentTime
                return true
            }
            lastContentId = contentId
            lastScrollTime = currentTime
        } else if (currentIndex > -1 && currentIndex != lastIndex) {
            if (lastIndex != -1 && currentTime - lastScrollTime > 500) {
                lastIndex = currentIndex
                lastScrollTime = currentTime
                return true
            }
            lastIndex = currentIndex
            lastScrollTime = currentTime
        } else if (lastScrollTime != 0L && currentTime - lastScrollTime > 1500 && event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            // Fallback for missing index and stable ID
            lastScrollTime = currentTime
            return true
        } else if (lastScrollTime == 0L) {
            lastScrollTime = currentTime
        }

        return false
    }

    private fun findFacebookContentId(node: AccessibilityNodeInfo): String {
        // Look for common content markers like "View * profile"
        val nodes = node.findAccessibilityNodeInfosByText("View ")
        var foundId = ""
        for (n in nodes) {
            val desc = n.contentDescription?.toString() ?: ""
            if (desc.startsWith("View ") && desc.endsWith(" profile")) {
                foundId = desc
                break
            }
        }
        nodes.forEach { it.recycle() } // Stability improvement: recycle nodes
        return foundId
    }

    override fun reset() {
        super.reset()
        lastContentId = ""
        lastScrollTime = 0L
    }
}
