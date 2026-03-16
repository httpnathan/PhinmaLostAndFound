# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PHINMA Lost and Found — an Android app (Kotlin) paired with a PHP/MariaDB REST API backend. Users can post lost or found items, browse/search posts with images, and message other users.

**Working directory**: `android-app/`
**Backend**: `../backend/`
**Database**: `../database/`

---

## Build & Run Commands

From the `android-app/` directory:

```bash
# Build debug APK
./gradlew assembleDebug

# Build and install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.example.phinmalostandfound.ExampleUnitTest"

# Clean build
./gradlew clean assembleDebug
```

**Note**: On Windows use `gradlew.bat` instead of `./gradlew`.

---

## API Configuration

The base URL is hardcoded in `app/src/main/java/com/example/phinmalostandfound/ApiConfig.kt`:

```
http://10.20.173.34/phinma-api/backend/
```

Change `BASE_URL` in `ApiConfig.kt` when deploying to a different server. The app uses HTTP (cleartext traffic is enabled in the manifest).

**Backend credential defaults** (in `../backend/config.php`):
- DB: `phinma_lost_found` on `localhost`, user `root`, no password
- API key: `phinma_secret_key_2024`
- Image uploads: stored in `../backend/uploads/`

---

## Architecture

### Android App

**Pattern**: Activity-based (no Fragments/ViewModel for most screens). Networking via Volley. Images loaded with Glide.

**Session management**: SharedPreferences (`"PhinmaLostAndFound"`) stores `isLoggedIn`, `userId`, `userFirstName`, `userLastName`, `userEmail`. `MainActivity` is a splash screen that routes to `HomeActivity` or `LoginActivity` based on login state.

**Navigation flow**:
- `MainActivity` (2s splash) → `LoginActivity` / `SignUpActivity` → `HomeActivity`
- `HomeActivity` hosts a bottom navigation bar linking to: Search, PostItemActivity (create post), ChatSectionActivity, MenuActivity

**Key source files**:
| File | Role |
|------|------|
| `ApiConfig.kt` | All API endpoint constants and `buildUrl()` helper |
| `Post.kt` | Post data class (fields: postId, postType, itemName, imageUrls, status, etc.) |
| `PostAdapter.kt` | RecyclerView adapter for the post feed |
| `VolleyMultipartRequest.kt` | Custom Volley request for multipart image uploads |
| `HomeActivity.kt` | Main feed with filter (All/Lost/Found) and pagination (50/page) |
| `PostItemActivity.kt` | Create post form with image picker and multipart upload |
| `PostDetailActivity.kt` | ViewPager2 image slider + post details |

### Backend (PHP)

Three PHP files handle all API logic:
- `auth.php` — register, login, admin_login
- `posts.php` — create, get_all (with filter/pagination), get_one, update, delete, search, get_user_posts
- `messages.php` — send, get_conversation, get_chats, mark_read

All responses use the `sendResponse($success, $message, $data)` wrapper defined in `config.php`, returning JSON with `{"success": bool, "message": "...", "data": ...}`.

### Database

MariaDB database `phinma_lost_found`. Key tables:
- `users` — email auth, bcrypt passwords
- `posts` — post_type enum `('lost','found')`, status enum `('active','claimed','resolved','archived')`
- `post_images` — multiple images per post (cascade delete with post)
- `messages` — sender/receiver FK to users
- `admins` — separate admin table

Schema file: `../database/phinma_database_schema.sql`

---

## Important Implementation Notes

- **Image upload**: `PostItemActivity` converts a gallery URI to a byte array and sends it via `VolleyMultipartRequest`. Currently supports a single image per post creation; the DB supports multiple via `post_images`.
- **Search**: The UI in `SearchActivity` is implemented, but the search logic (calling `posts.php?action=search`) is marked TODO in the code.
- **Messaging**: UI activities (`ChatSectionActivity`, `PrivateMessageActivity`) exist but the backend messaging integration is partially implemented.
- **Firebase**: Dependencies are commented out in `app/build.gradle` — ready to enable for push notifications.
- **Cleartext traffic**: Enabled in `AndroidManifest.xml` for the development server IP. Must be addressed for production.
- **Post status flow**: Posts start as `active`; can transition to `claimed`, `resolved`, or `archived`.
