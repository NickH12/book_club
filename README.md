# Book Club

An Android app for tracking the books you read: search titles via the Google Books
API, save them to your personal library with a cover photo, rating and review, sync
across devices with Firebase, and see stats about your reading habits.

Built with Kotlin and modern Android Jetpack libraries as a personal/portfolio
project. Fully localized in English and Hebrew (RTL).

## Features

- **Search** — look up books by title/author through the Google Books API, sorted by
  relevance or newest.
- **Personal library** — add a book with your own rating, written review, and a
  cover photo (camera or gallery), stored locally with Room and synced to Firestore.
- **Favorites** — star books and view them in a dedicated list.
- **Statistics** — total books read, average rating, and top-rated books, charted
  with MPAndroidChart.
- **Accounts** — email/username + password auth via Firebase Authentication, with
  password reset.
- **Recommendations** — similar-book suggestions surfaced after rating a book highly.

## Tech stack

| Layer            | Choice                                              |
|-------------------|-----------------------------------------------------|
| Language          | Kotlin                                               |
| Architecture      | MVVM, Repository pattern                             |
| DI                | Hilt                                                 |
| UI                | XML layouts, ViewBinding, Navigation Component       |
| Local storage     | Room                                                 |
| Remote sync/auth  | Firebase (Firestore, Auth, Storage)                  |
| Networking        | Retrofit + Gson (Google Books API)                   |
| Images            | Glide                                                |
| Charts            | MPAndroidChart                                       |

## Screens

Login/Register → Book List → Book Detail / Edit, Favorites, Statistics, User Profile.

## Getting started

The project won't build out of the box — it depends on a Firebase project and a
Google Books API key that aren't checked in. To run it yourself:

1. **Firebase**: create a project in the
   [Firebase console](https://console.firebase.google.com/), add an Android app with
   package name `com.example.bookclub`, enable **Authentication** (Email/Password),
   **Firestore**, and **Storage**, then download `google-services.json` and place it
   at `app/src/google-services.json` (see `app/src/google-services.json.example` for
   the expected shape).
2. **Google Books API key**: get a key from the
   [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
   (enable the "Books API"), then add it to your `local.properties`:
   ```properties
   GOOGLE_BOOKS_API_KEY=your_key_here
   ```
3. Open the project in Android Studio and run the `app` module (min SDK 23).

## Project status

This started as a coursework project and is being incrementally modernized
(secret handling, tests, CI, and a partial Compose migration) as a Kotlin/Android
portfolio piece.
