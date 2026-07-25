# Implementation Plan - Apply Custom Fonts and UI Best Practices

This plan outlines the steps to integrate the newly added Poppins and Urbanist fonts into the Jetpack Compose theme and apply UI styling best practices.

## User Review Required

> [!IMPORTANT]
> I have chosen **Urbanist** for Display, Headline, and Title roles (headers) and **Poppins** for Body and Label roles (content). This is a common design pattern for modern apps. Please let me know if you prefer a different mapping.

## Proposed Changes

### UI Theme

#### [MODIFY] [Type.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/theme/Type.kt)
- Define `Poppins` and `Urbanist` `FontFamily` using the provided `.ttf` files.
- Update the `Typography` object to use these families across all Material 3 typography scales (Display, Headline, Title, Body, Label).

#### [MODIFY] [Theme.kt](file:///C:/Users/AhmedElgohary/AndroidStudioProjects/ElSahra/app/src/main/java/com/example/elsahra/ui/theme/Theme.kt)
- Ensure the `MaterialTheme` is correctly initialized with the updated `Typography`.
- (Optional) Refine the status bar and system UI styling for better consistency.

## Verification Plan

### Manual Verification
- Deploy the app to a device/emulator.
- Inspect different UI elements to ensure they reflect the new typography.
- Verify that both Poppins and Urbanist are being used as intended.
- Check light and dark mode consistency.
