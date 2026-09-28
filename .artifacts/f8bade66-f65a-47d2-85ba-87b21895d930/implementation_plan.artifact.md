# Rename Package Name to `com.maibu.reelshortscounter`

To comply with the Google Play Console requirement, the application package name (applicationId) must be changed from `com.maibu.scrollsense` to `com.maibu.reelshortscounter`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts](file:///Users/bmaibu/Documents/Reelsandshorts/app/build.gradle.kts)
- Update `namespace` and `applicationId` to `com.maibu.reelshortscounter`.

### Source Code Refactoring

#### [MODIFY] All Kotlin Files
- Update package declarations from `com.maibu.scrollsense` to `com.maibu.reelshortscounter`.
- Update all internal imports to reflect the new package name.
- Update hardcoded broadcast action strings: `com.maibu.scrollsense.UPDATE_DATA` -> `com.maibu.reelshortscounter.UPDATE_DATA`.

#### [MOVE] Directory Structure
- Move all files from `app/src/main/java/com/maibu/scrollsense` to `app/src/main/java/com/maibu/reelshortscounter`.
- Move test files from `app/src/test/java/com/maibu/scrollsense` to `app/src/test/java/com/maibu/reelshortscounter`.
- Move instrumented test files from `app/src/androidTest/java/com/maibu/scrollsense` to `app/src/androidTest/java/com/maibu/reelshortscounter`.

### Release Generation

- Regenerate the signed Android App Bundle (.aab) with the new package name.

## Verification Plan

### Automated Tests
- Run `gradle_build` to ensure the project compiles and builds successfully with the new package name.
- Verify the generated AAB has the correct package name.

### Manual Verification
- Confirm that the App Bundle is accepted by the Google Play Console.
