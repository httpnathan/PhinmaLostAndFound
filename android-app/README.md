# PHINMA Lost and Found App - Kotlin Files

This package contains preset Kotlin activity files and their corresponding XML layouts for the PHINMA Lost and Found application based on the Figma design.

## 📁 File Structure

### Activities & Layouts

1. **MainActivity.kt** + **activity_main.xml**
   - Splash screen with app logo and branding
   - Session check (auto-login if user is already logged in)
   - Navigation to Login or Home based on session state

2. **LoginActivity.kt** + **activity_login.xml**
   - Email/Password login
   - Google and Apple sign-in options
   - Link to Sign Up page

3. **SignUpActivity.kt** + **activity_signup.xml**
   - First Name, Last Name, Email, Password fields
   - Google and Apple sign-up options
   - Link to Login page

4. **HomeActivity.kt** + **activity_home.xml**
   - Main feed with lost and found item posts
   - Bottom navigation bar
   - Notification icon

5. **ProfileActivity.kt** + **activity_profile.xml**
   - User profile with avatar
   - Username and email display
   - Posts/Followers/Following stats
   - Edit Profile and Sign Out buttons

6. **ChatSectionActivity.kt** + **activity_chat_section.xml**
   - List of all chats/conversations
   - Navigation to individual chat

7. **PrivateMessageActivity.kt** + **activity_private_message.xml**
   - One-on-one messaging interface
   - Message input and send button

8. **SearchActivity.kt** + **activity_search.xml**
   - Search bar
   - Recent searches
   - Search results for lost and found items

9. **PostItemActivity.kt** + **activity_post_item.xml**
   - Create new lost/found item post
   - Image upload
   - Description input

10. **MenuActivity.kt** + **activity_menu.xml**
    - Navigation to Profile, Settings, About, Report, FAQs

11. **SettingsActivity.kt** + **activity_settings.xml**
    - Notifications toggle
    - Dark mode toggle
    - Location services toggle
    - Privacy policy and terms

12. **AdditionalActivities.kt**
    - FAQsActivity
    - AboutActivity
    - ReportActivity

### Resources

13. **bottom_navigation_menu.xml**
    - Menu resource for bottom navigation with 5 items (Home, Search, Post, Chat, Menu)

## 🎨 Required Drawable Resources

You'll need to add these icon files to your `res/drawable/` directory:

- ic_app_logo.xml (Main app logo for splash screen)
- ic_google.xml
- ic_apple.xml
- ic_notifications.xml
- ic_home.xml
- ic_search.xml
- ic_add.xml
- ic_chat.xml
- ic_menu.xml
- ic_profile.xml
- ic_settings.xml
- ic_back.xml
- ic_send.xml
- ic_arrow_right.xml
- ic_info.xml
- ic_report.xml
- ic_help.xml
- ic_profile_placeholder.xml
- ic_image_placeholder.xml

## 🎨 Required Drawable Backgrounds

- edittext_background.xml
- search_background.xml
- image_placeholder_background.xml

## 🎨 Required Colors (colors.xml)

```xml
<color name="white">#FFFFFF</color>
<color name="black">#000000</color>
<color name="green">#4CAF50</color>
<color name="gray">#9E9E9E</color>
<color name="light_gray">#F5F5F5</color>
<color name="red">#F44336</color>
```

## 📦 Required Dependencies (build.gradle)

Add these to your app's build.gradle:

```gradle
dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    
    // Circle ImageView for profile pictures
    implementation 'de.hdodenhof:circleimageview:3.1.0'
}
```

## 🔧 AndroidManifest.xml

Add all activities to your AndroidManifest.xml:

```xml
<activity android:name=".MainActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity android:name=".LoginActivity" />
<activity android:name=".SignUpActivity" />
<activity android:name=".HomeActivity" />
<activity android:name=".ProfileActivity" />
<activity android:name=".ChatSectionActivity" />
<activity android:name=".PrivateMessageActivity" />
<activity android:name=".SearchActivity" />
<activity android:name=".PostItemActivity" />
<activity android:name=".MenuActivity" />
<activity android:name=".SettingsActivity" />
<activity android:name=".FAQsActivity" />
<activity android:name=".AboutActivity" />
<activity android:name=".ReportActivity" />
```

## ✅ TODO Implementation Tasks

Each file contains TODO comments for features that need implementation:

1. **Authentication**
   - Implement Firebase Authentication or custom backend
   - Google Sign-In integration
   - Apple Sign-In integration
   - Session management in SharedPreferences

2. **Database**
   - Set up Firebase Firestore or Room Database
   - User profiles
   - Lost and found item posts
   - Messages
   - Search functionality

3. **RecyclerView Adapters**
   - PostsAdapter for home feed (lost/found items)
   - ChatsAdapter for chat list
   - MessagesAdapter for individual messages
   - SearchResultsAdapter for search results

4. **Image Handling**
   - Implement image picking from gallery/camera
   - Image upload to cloud storage (Firebase Storage, etc.)
   - Image loading library (Glide or Picasso)

5. **Real-time Features**
   - WebSocket or Firebase for real-time messaging
   - Push notifications for found items
   - Location-based search for nearby lost items

## 🚀 Getting Started

1. Copy all .kt files to your `app/src/main/java/com/example/phinmalostandfound/` directory
2. Copy all .xml files to appropriate `res/` subdirectories:
   - activity_*.xml → `res/layout/`
   - bottom_navigation_menu.xml → `res/menu/`
3. Create required drawable resources
4. Add colors to colors.xml
5. Update AndroidManifest.xml with MainActivity as launcher
6. Add required dependencies
7. Start implementing TODO items

## 📱 Navigation Flow

```
MainActivity (Splash)
    ↓
Login → Home ←→ Profile
  ↓       ↓       ↓
SignUp  Search  Settings
        ↓       ↓
      Post    About
        ↓       ↓
      Chat    FAQs
        ↓       ↓
    Messages  Report
```

## 🔐 Permissions (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
```

## 📝 Notes

- All layouts use ConstraintLayout for flexible responsive design
- Bottom navigation is consistent across main screens
- Material Design components are used throughout
- Each activity has proper navigation between screens
- Image placeholders and icons need to be created
- Package name: `com.example.phinmalostandfound`
- MainActivity implements splash screen with 2-second delay
- Session management uses SharedPreferences with key "PhinmaLostAndFound"

## 🎓 About PHINMA Lost and Found

This is a lost and found application designed for the PHINMA community to help students and staff report and locate lost items on campus. Users can:
- Post about lost items with photos and descriptions
- Report found items to help others
- Search for specific lost items
- Communicate through built-in messaging
- Get notifications about potentially matching items

Good luck with your PHINMA Lost and Found app! 📚🔍
