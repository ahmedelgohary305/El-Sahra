# Revert Movie Item Layout to Dynamic Heights

The user wants to revert the changes that enforced fixed heights on movie items and rows (intended to prevent vertical jumping). This will return the app to a more compact, dynamic layout where rows shrink and grow based on their specific content.

## Proposed Changes

### UI Components

#### [MODIFY] [MovieComponents.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/components/MovieComponents.kt)
- **MovieItem**: Revert spacer between poster and title from `6.dp` back to `10.dp`.
- **MoviePagingRow**:
    - Revert title padding from `start=16.dp, end=16.dp, top=12.dp, bottom=4.dp` to `horizontal=16.dp, vertical=8.dp`.
    - Remove `.height(310.dp)` from the `LazyRow` container.
    - Remove `.fillMaxHeight()` from `MovieItem` and `MovieItemSkeleton` instances within the row.
    - Remove `.height(310.dp)` from the error state `Box`.
- **MoviesRow**: Same as `MoviePagingRow` (remove heights and max-height fills).
- **MoviesPagingGrid**: Revert `GridCells.Adaptive(150.dp)` to `GridCells.Fixed(2)` (or whatever was original, checking... actually it was `GridCells.Fixed(2)` in some places). *Correction: I'll revert to the previous cell logic.*

#### [MODIFY] [Skeletons.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/components/Skeletons.kt)
- **MovieItemSkeleton**: Revert spacer from `6.dp` back to `10.dp`.
- **MovieRowSkeleton**:
    - Remove `.height(310.dp)` from the `LazyRow`.
    - Remove `.fillMaxHeight()` from `MovieItemSkeleton`.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/screens/HomeScreen.kt)
- Revert `verticalArrangement = Arrangement.spacedBy(4.dp)` to `Arrangement.spacedBy(24.dp)` in `MovieTab` and `TvTab`.

---

### Logic Preservation
I will keep the **Error Handling** improvements (User-friendly strings, `ErrorMapper`, `CompactErrorState` with icons) but will remove the fixed-height constraints that were part of the "jumping" fix.

## Verification Plan

### Manual Verification
1.  Open the Home Screen.
2.  Verify that categories like "Trending" and "Popular" now have more vertical separation (24.dp).
3.  Verify that rows no longer have excessive empty space at the bottom.
4.  Verify that the app still handles errors correctly (indigo "Retry" button appears on network failure).
