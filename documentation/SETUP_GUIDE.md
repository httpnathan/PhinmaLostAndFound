# 🚀 PHINMA Lost and Found - Full MariaDB Package Setup Guide

## 📦 Package Contents

You received a **COMPLETE** production-ready package with:

### ✅ Backend (PHP REST API)
- `config.php` - Database configuration
- `auth.php` - Login & Registration
- `posts.php` - Lost/Found items management  
- `messages.php` - Chat system

### ✅ Database (MariaDB/MySQL)
- `phinma_database_schema.sql` - Complete database structure
- Includes: Users, Posts, Messages, Admins, Categories

### ✅ Android App
- Updated with PHINMA dark green & yellow colors
- Modern Material Design UI
- API integration ready
- Clean, commented code

---

## 🎨 PHINMA Brand Colors

**Primary:** Dark Green `#1B5E20`  
**Accent:** Yellow `#FDD835`  
**Gradient:** Dark → Light Green  

---

## ⚡ Quick Start (5 Steps)

### Step 1: Install XAMPP
1. Download XAMPP: https://www.apachefriends.org/
2. Install it (default settings)
3. Start **Apache** and **MySQL** from XAMPP Control Panel

### Step 2: Import Database
1. Open browser: `http://localhost/phpmyadmin`
2. Click "New" to create database
3. Name it: `phinma_lost_found`
4. Click "Import" tab
5. Choose file: `phinma_database_schema.sql`
6. Click "Go"

✅ Database is now ready!

### Step 3: Setup PHP API
1. Copy `backend` folder to: `C:\xampp\htdocs\`
2. Rename it to: `phinma-api`
3. Edit `config.php`:
   ```php
   define('DB_HOST', 'localhost');
   define('DB_NAME', 'phinma_lost_found');
   define('DB_USER', 'root');
   define('DB_PASS', '');  // Empty for XAMPP
   ```

✅ API is ready at: `http://localhost/phinma-api/`

### Step 4: Test API
Open browser:
```
http://localhost/phinma-api/auth.php?action=login
```
Should see: `{"success":false,"message":"Invalid action or request method"}`

✅ API is working!

### Step 5: Update Android App
1. Open project in Android Studio
2. Create file: `ApiConfig.kt`
   ```kotlin
   object ApiConfig {
       // 🔧 CHANGE THIS to your computer's IP address
       const val BASE_URL = "http://192.168.1.100/phinma-api/"
   }
   ```

3. **Find Your IP Address:**
   - Windows: Open CMD, type `ipconfig`, look for IPv4
   - Mac: System Preferences → Network
   - Linux: `ifconfig` or `ip addr`

✅ App is ready to run!

---

## 🔧 Database Template Guide

### Adding New User Fields

**1. Update Database:**
```sql
ALTER TABLE users ADD COLUMN student_id VARCHAR(50);
```

**2. Update `auth.php` (Line 25):**
```php
$required_fields = ['email', 'password', 'first_name', 'last_name', 'student_id'];
```

**3. Update `auth.php` (Line 55):**
```php
$insert_query = "INSERT INTO users 
                 (email, password_hash, first_name, last_name, student_id) 
                 VALUES (?, ?, ?, ?, ?)";
```

**4. Update Android App:**
Add field to registration form

---

### Adding New Post Fields

**1. Update Database:**
```sql
ALTER TABLE posts ADD COLUMN color VARCHAR(50);
```

**2. Update `posts.php` (Line 42):**
```php
$color = isset($data['color']) ? $data['color'] : null;
```

**3. Update `posts.php` (Line 67):**
```php
$insert_query = "INSERT INTO posts 
                 (user_id, post_type, item_name, description, color) 
                 VALUES (?, ?, ?, ?, ?)";
```

---

## 📱 Android App - API Integration

### Login Example:
```kotlin
// File: LoginActivity.kt
// Line: ~60 (in performLogin function)

val apiUrl = ApiConfig.BASE_URL + "auth.php?action=login"

val jsonBody = JSONObject().apply {
    put("email", email)
    put("password", password)
}

// Make API call here
```

---

## 🎨 Customizing UI Colors

### Change Primary Color:
**File:** `res/values/colors.xml`  
**Line:** 4
```xml
<color name="phinma_dark_green">#YOUR_COLOR</color>
```

### Change Accent Color:
**File:** `res/values/colors.xml`  
**Line:** 7
```xml
<color name="phinma_yellow">#YOUR_COLOR</color>
```

---

## 🧪 Testing the System

### Test User Login:
**Email:** `user@test.com`  
**Password:** `1234`

### Test Admin Login:
**Username:** `admin`  
**Password:** `admin123`

### API Test URLs:
```
# Login
POST http://localhost/phinma-api/auth.php?action=login
Body: {"email":"user@test.com","password":"1234"}

# Get Posts
GET http://localhost/phinma-api/posts.php?action=get_all

# Create Post
POST http://localhost/phinma-api/posts.php?action=create
Body: {
  "user_id":1,
  "post_type":"lost",
  "item_name":"Phone",
  "description":"Lost iPhone"
}
```

---

## 📂 File Structure

```
phinma-complete-mariadb/
├── backend/
│   ├── config.php          🔧 Edit DB credentials here
│   ├── auth.php            🔧 Login/Register logic
│   ├── posts.php           🔧 Posts CRUD operations
│   └── messages.php        🔧 Chat functionality
│
├── database/
│   └── phinma_database_schema.sql  🔧 Import this to MySQL
│
├── android-app/
│   └── [Full Android Studio Project]
│
└── documentation/
    └── SETUP_GUIDE.md (this file)
```

---

## 🔐 Security Notes

### For Production:
1. **Change Database Password:**
   ```php
   define('DB_PASS', 'strong_password_here');
   ```

2. **Change API Key:**
   ```php
   define('API_KEY', 'your_secret_key');
   ```

3. **Enable HTTPS:**
   Use SSL certificate for API

4. **Validate All Inputs:**
   Already done in template

---

## 🐛 Troubleshooting

### "Database connection failed"
- Check XAMPP MySQL is running
- Verify database name in `config.php`
- Check username/password

### "Cannot connect to API"
- Check Apache is running in XAMPP
- Verify IP address in Android app
- Make sure phone/emulator on same WiFi

### "404 Not Found"
- Check backend folder is in `htdocs`
- Verify URL: `http://localhost/phinma-api/`

---

## 📝 Modification Checklist

### ✅ Where to Edit for Custom Fields:

**Database Fields:**
- `database/phinma_database_schema.sql` (Lines marked with 🔧)

**API Endpoints:**
- `backend/auth.php` (Lines 25, 55, 85)
- `backend/posts.php` (Lines 42, 67, 120)
- `backend/messages.php` (As needed)

**Android App:**
- Data models (create in `app/src/main/java/.../models/`)
- API service (create `ApiService.kt`)
- Activities (update forms)

---

## 🎯 Next Steps

1. ✅ Complete basic setup (Steps 1-5 above)
2. 📱 Test login on Android app
3. 🎨 Customize colors if needed
4. 🔧 Add custom fields to database
5. 📝 Update API to handle new fields
6. 📱 Update Android app UI
7. 🧪 Test everything
8. 🚀 Deploy!

---

## 💡 Pro Tips

1. **Use Postman** to test API endpoints before Android integration
2. **Keep comments** in code - they guide you where to edit
3. **Backup database** before making changes
4. **Test on real device** for best results
5. **Read code comments** - every template section is marked with 🔧

---

## 📞 Support

If you encounter issues:
1. Check all comments in code (marked with 🔧)
2. Verify XAMPP services are running
3. Check Android Logcat for errors
4. Review API responses in browser/Postman

---

<div align="center">

# 🎉 You're All Set!

**Everything is configured and ready to use!**

Just follow Steps 1-5 above and you'll have a working system.

**PHINMA Lost and Found v1.0**  
*Built with ❤️ for PHINMA Education Network*

</div>
