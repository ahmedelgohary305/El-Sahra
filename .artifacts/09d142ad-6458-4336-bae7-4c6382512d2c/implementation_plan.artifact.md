# Unify Error and Empty States UI

This plan aims to unify the UI design for error and empty states across the application by creating reusable Jetpack Compose components and refactoring existing screens to use them.

## User Review Required

> [!IMPORTANT]
> The unified design will follow the style currently used in `SearchScreen`'s `EmptySearchState`, which includes a large icon in a circular background, a bold title, an optional description, and an optional action button.

## Proposed Changes

### [Component Layer]

#### [NEW] [StateComponents.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/components/StateComponents.kt)
Create a new file containing:
- `StateComponent`: The base building block for state messages.
- `EmptyState`: A convenience component for empty states.
- `ErrorState`: A convenience component for error states with a retry button.

#### [MODIFY] [strings.xml](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/res/values/strings.xml)
- Add a generic `retry` string resource.
- Add a generic `error_description` string resource (optional).

### [Feature Screens]

#### [MODIFY] [SearchScreen.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/screens/SearchScreen.kt)
- Replace `EmptySearchState` with the new `EmptyState` component.
- Remove the local `EmptySearchState` function.

#### [MODIFY] [MovieDetailScreen.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/screens/MovieDetailScreen.kt)
- Replace the inline error handling with the new `ErrorState` component.

#### [MODIFY] [WatchlistScreen.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/screens/WatchlistScreen.kt)
- Replace the inline empty state with the new `EmptyState` component.

#### [MODIFY] [SeeAllScreen.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/screens/SeeAllScreen.kt)
- Replace the inline error handling with the new `ErrorState` component.

## Verification Plan

### Automated Tests
- Run existing UI tests if available.
- Build the project to ensure no compilation errors.

### Manual Verification
- Navigate to the Search screen and search for a non-existent movie to verify the empty state.
- Navigate to the Watchlist screen with an empty watchlist to verify the empty state.
- Simulate an error (e.g., by disabling internet) on the Movie Detail and See All screens to verify the error state.
