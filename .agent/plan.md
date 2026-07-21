# Project Plan

Build a modern Android movie browsing application called ElSahra using Jetpack Compose and TMDB API. The app features a multi-section home screen, search functionality, movie details, and Arabic language support. It uses Clean Architecture, Retrofit, Coroutines, Coil, Material 3, and Dagger Hilt for DI.

## Project Brief

# ElSahra - Project Brief

## Features

- **Categorized Discovery:** A home screen with a hero banner and horizontally scrollable rows for Trending, Popular, Top Rated, and Upcoming movies.
- **Search & Filter:** A dedicated search interface with a grid layout and genre-based filtering to quickly find specific content.
- **Movie Details:** Comprehensive detail screens featuring posters, backdrops, and essential info with a "Watch Now" call to action.
- **Arabic Localization:** Full support for the Arabic language and Right-to-Left (RTL) layouts for a seamless local experience.

## High-Level Tech Stack
- **Kotlin:** The primary language for modern Android development.
- **Jetpack Compose (Material 3):** For building a responsive, vibrant, and energetic user interface.
- **Jetpack Navigation 3:** Implementing state-driven navigation for reliable screen flow.
- **Compose Material Adaptive:** Ensuring the layout adapts elegantly across different screen sizes.
- **Dagger Hilt:** For robust and scalable dependency injection.
- **Retrofit & OkHttp:** For handling API communication with TMDB.
- **Kotlin Coroutines & Flow:** Managing background tasks and reactive data streams.
- **Coil:** For efficient and smooth image loading.


## UI Design Image
![UI Design](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/input_images/image_0.png)

## Implementation Steps
**Total Duration:** 43m 33s

### Task_1_Foundation_Home: Setup the project foundation including Material 3 theme, TMDB API integration, and the Home Screen.
- **Status:** COMPLETED
- **Updates:** Completed the project foundation setup including Material 3 theme, TMDB API integration via Retrofit, and the Home Screen UI. Implemented Edge-to-Edge display and handled API key configuration from local.properties. UI matches image_0.png and image_1.png.
- **Acceptance Criteria:**
  - Material 3 theme with vibrant light/dark color schemes implemented
  - TMDB API integrated with Retrofit and API_KEY configuration
  - Home screen featuring hero banner and movie categories implemented
  - The implemented UI must match the design provided in image_0.png and image_1.png
  - Project builds successfully
- **Duration:** 2m 11s

### Task_5_Hilt_Setup_Refactor: Setup Dagger Hilt for dependency injection and refactor the existing API integration and Home Screen to use it.
- **Status:** COMPLETED
- **Updates:** Setup Dagger Hilt for dependency injection. Configured Application class, NetworkModule, and RepositoryModule. Refactored Home Screen ViewModel to use @HiltViewModel. UI maintained and verified.
- **Acceptance Criteria:**
  - Hilt dependencies added and @HiltAndroidApp configured
  - Retrofit and TMDB API service provided via Hilt modules
  - Home Screen ViewModel refactored to use @HiltViewModel
  - Existing UI logic in Home Screen maintained and verified against image_0.png and image_1.png
  - Project builds and runs successfully
- **Duration:** 17m 33s

### Task_2_Search_Details_Localization: Implement navigation, search functionality, movie details, and Arabic localization using the Hilt-based architecture.
- **Status:** COMPLETED
- **Updates:** Fixed crash in Movie Details screen by correctly annotating DetailsViewModel with @HiltViewModel(assistedFactory = ...) and ensuring hiltViewModel(creationCallback = ...) is used in AppNavigation. Stable navigation verified.
- **Acceptance Criteria:**
  - Navigation 3 implemented for screen transitions
  - Search screen with grid layout and genre filtering functional
  - Movie details screen with backdrop and 'Watch Now' action implemented
  - Full Arabic localization for UI strings and content
  - The implemented UI must match the design provided in image_2.png, image_3.png, image_4.png, and image_5.png
- **Duration:** 7m 9s

### Task_3_Refinements_Assets: Apply final UI refinements, adaptive layouts, and create the app icon.
- **Status:** COMPLETED
- **Updates:** Implemented adaptive navigation (Rail/Bar), scaling content layouts, polished edge-to-edge, and a custom adaptive app icon. Verified visual consistency with Material 3.
- **Acceptance Criteria:**
  - Full Edge-to-Edge display implemented
  - Adaptive app icon matching the movie theme created
  - Adaptive layouts scaling across device form factors implemented
  - App does not crash during feature navigation
- **Duration:** 16m 40s

### Task_4_Run_and_Verify: Perform a final run to verify stability, requirements alignment, and UI fidelity.
- **Status:** IN_PROGRESS
- **Updates:** Verification failed due to a crash in Movie Details screen navigation. Reopening Task 2 for fix.
- **Acceptance Criteria:**
  - Build pass
  - App does not crash
  - All features (Home, Search, Details, Localization, DI) verified for stability
  - Final UI matches all provided design images
- **StartTime:** 2026-05-01 16:48:28 EEST

