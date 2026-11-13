# RentEase - Android Studio + Firebase

A Java/XML Android property rental application matching the supplied RentEase screen concept.

## Before running
1. Open this folder in Android Studio.
2. Let Gradle Sync finish.
3. In Firebase Console create a project.
4. Add an Android app with package name `com.rentease.app`.
5. Download your real `google-services.json` and replace `app/google-services.json`.
6. Enable Authentication > Email/Password.
7. Create Firestore Database.
8. Create collections: `users`, `properties`, `bookings`, `messages`.
9. Apply `firebase/firestore.rules`.
10. Run on an emulator/device with Android 7.0 (API 24) or newer.

The included google-services.json is only a build placeholder. Firebase features will not work until it is replaced with the file from your own Firebase project.
