# 🔧 Template Customization Guide

## Where to Edit Code to Add/Remove Database Fields

This guide shows you **EXACTLY** which lines to edit when customizing the database.

---

## 📝 Adding a New User Field (Example: student_id)

### Step 1: Update Database Schema
**File:** `database/phinma_database_schema.sql`  
**Line:** ~35 (in users table, after last_name)

```sql
-- 🔧 ADD YOUR CUSTOM USER FIELDS HERE:
student_id VARCHAR(50),  -- ← ADD THIS LINE
department VARCHAR(100),
```

### Step 2: Update Registration API
**File:** `backend/auth.php`

**Line ~25:** Add to required fields
```php
$required_fields = ['email', 'password', 'first_name', 'last_name', 'student_id'];  // ← ADD student_id
```

**Line ~40:** Get the field value
```php
$student_id = trim($data['student_id']);  // ← ADD THIS LINE
```

**Line ~55:** Add to INSERT query
```php
$insert_query = "INSERT INTO users (email, password_hash, first_name, last_name, student_id) 
                 VALUES (?, ?, ?, ?, ?)";  // ← ADD student_id to both parts
```

**Line ~60:** Add to execute parameters
```php
$stmt->execute([$email, $password_hash, $first_name, $last_name, $student_id]);  // ← ADD $student_id
```

### Step 3: Update Login Response
**File:** `backend/auth.php`

**Line ~90:** Add to SELECT query
```php
$query = "SELECT user_id, email, password_hash, first_name, last_name, student_id, is_active, is_verified 
          FROM users WHERE email = ?";  // ← ADD student_id
```

### Step 4: Update Android App
Add EditText to `activity_signup.xml` and handle in `SignUpActivity.kt`

---

## 📮 Adding a New Post Field (Example: color)

### Step 1: Update Database
**File:** `database/phinma_database_schema.sql`  
**Line:** ~90 (in posts table)

```sql
-- 🔧 ADD YOUR CUSTOM POST FIELDS HERE:
color VARCHAR(50),  -- ← ADD THIS
brand VARCHAR(100),
```

### Step 2: Update Create Post API
**File:** `backend/posts.php`

**Line ~42:** Get the field
```php
$color = isset($data['color']) ? $data['color'] : null;  // ← ADD THIS
```

**Line ~67:** Add to INSERT query
```php
$insert_query = "INSERT INTO posts 
                 (user_id, post_type, item_name, description, color) 
                 VALUES (?, ?, ?, ?, ?)";  // ← ADD color
```

**Line ~75:** Add to execute
```php
$stmt->execute([$user_id, $post_type, $item_name, $description, $color]);  // ← ADD $color
```

### Step 3: Update Get Posts Query
**File:** `backend/posts.php`  
**Line ~100:** (in get_all action)

```php
$query = "SELECT p.*, u.first_name, u.last_name 
          FROM posts p ...";  // p.* already includes color, no change needed
```

### Step 4: Update Search
**File:** `backend/posts.php`  
**Line ~185:** Add to searchable fields

```php
WHERE (p.item_name LIKE ? 
       OR p.description LIKE ? 
       OR p.color LIKE ?)  // ← ADD THIS LINE
```

Then update execute:
```php
$stmt->execute([$keyword, $keyword, $keyword]);  // ← ADD third $keyword
```

---

## 💬 Adding Message Features

### Adding Message Types (text, image, file)

**File:** `database/phinma_database_schema.sql`  
**Line:** ~135

```sql
-- 🔧 ADD YOUR CUSTOM MESSAGE FIELDS HERE:
message_type ENUM('text', 'image', 'file') DEFAULT 'text',
attachment_url VARCHAR(500),
```

**File:** `backend/messages.php`  
**Line ~30:** Get fields
```php
$message_type = isset($data['message_type']) ? $data['message_type'] : 'text';
$attachment_url = isset($data['attachment_url']) ? $data['attachment_url'] : null;
```

**Line ~35:** Update INSERT
```php
$insert_query = "INSERT INTO messages 
                 (sender_id, receiver_id, message_text, message_type, attachment_url) 
                 VALUES (?, ?, ?, ?, ?)";
                 
$stmt->execute([$sender_id, $receiver_id, $message_text, $message_type, $attachment_url]);
```

---

## 🗑️ Removing Fields

### To Remove a Field:

1. **Database:** Comment out or delete the line
```sql
-- color VARCHAR(50),  ← Comment with --
```

2. **API:** Remove from all queries
3. **Android:** Remove from UI and data models

---

## 🎨 Adding New Categories

### Step 1: Insert Category
Run this SQL in phpMyAdmin:
```sql
INSERT INTO categories (category_name, category_icon) 
VALUES ('Sports Equipment', 'ic_sports');
```

### Step 2: Use in App
Categories are automatically available in get_all_posts

---

## 📊 Common Customizations

### Add User Role Field

**Database:**
```sql
ALTER TABLE users ADD COLUMN role ENUM('student', 'faculty', 'staff') DEFAULT 'student';
```

**API (auth.php):**
```php
// In register:
$role = isset($data['role']) ? $data['role'] : 'student';

// In INSERT:
$insert_query = "INSERT INTO users (..., role) VALUES (..., ?)";
```

### Add Post Status Field

Already exists! Just use:
- 'active'
- 'claimed'
- 'expired'  
- 'deleted'

Update via:
```php
$data = ['post_id' => 1, 'user_id' => 1, 'status' => 'claimed'];
// POST to posts.php?action=update
```

---

## 🔍 Finding Code Locations

### Quick Search:
All customizable sections are marked with:
```php
// 🔧 ADD YOUR CUSTOM FIELDS HERE:
```

**Search for:** `🔧` in all files to find editable sections

### File Organization:
```
Database Fields    → database/phinma_database_schema.sql
API Logic          → backend/*.php
Android UI         → app/src/main/res/layout/*.xml
Android Logic      → app/src/main/java/**/*.kt
```

---

## ✅ Testing Changes

After adding fields:

1. **Test Database:**
   - Run SQL in phpMyAdmin
   - Check table structure

2. **Test API:**
   - Use Postman or browser
   - Check JSON response

3. **Test Android:**
   - Build and run app
   - Check Logcat for errors

---

## 💡 Pro Tips

1. **Always backup database before changes**
   ```sql
   mysqldump -u root phinma_lost_found > backup.sql
   ```

2. **Test API before Android integration**
   - Use Postman to test endpoints
   - Verify response format

3. **Use consistent naming**
   - Database: snake_case (student_id)
   - PHP: camelCase ($studentId)
   - Kotlin: camelCase (studentId)

4. **Add validation**
   - Database: Use appropriate data types
   - API: Validate in PHP
   - Android: Validate before sending

---

## 📝 Checklist for Adding Fields

- [ ] Update database schema (.sql file)
- [ ] Run ALTER TABLE or reimport database
- [ ] Update API endpoint (add to query)
- [ ] Update API validation if required
- [ ] Add to Android data model
- [ ] Add to Android UI (if visible)
- [ ] Test with Postman
- [ ] Test in Android app

---

<div align="center">

**All customizable code is marked with 🔧**

*Search for this emoji to find where to edit!*

</div>
