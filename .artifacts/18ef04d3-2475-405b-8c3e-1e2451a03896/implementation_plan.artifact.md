# Fix redundant requestLayout calls during layout pass

Eliminate the "requestLayout() improperly called" warning by ensuring that layout-altering properties (padding, visibility, layout parameters) are only updated when their values actually change.

## Proposed Changes

### Tools & Extensions

#### [MODIFY] [Extensions.kt](file:///C:/GitHub/EntourageApp/android/app/src/main/java/social/entourage/android/tools/Extensions.kt)
- Add conditional checks in `updatePaddingTopForEdgeToEdge` and `updatePaddingForEdgeToEdge` to verify if the padding values are different before applying them.

---

### Home Feature

#### [MODIFY] [HomeFragment.kt](file:///C:/GitHub/EntourageApp/android/app/src/main/java/social/entourage/android/home/HomeFragment.kt)
- Update `setRecyclerViewScrollListener` and `startAnimation` to check if `topMargin` or `visibility` actually needs to be updated before calling `layoutParams = ...` or `visibility = ...`.

## Verification Plan

### Automated Tests
- Run the app and monitor Logcat for the `View: requestLayout() improperly called` warning while scrolling the home screen or navigating between fragments.

### Manual Verification
- Verify that the Home screen header animation still works correctly (hiding/showing the title on scroll).
- Verify that the status bar padding is correctly applied on screens using `updatePaddingTopForEdgeToEdge`.
