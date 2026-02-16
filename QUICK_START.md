# ⚡ QUICK START - Get Running in 10 Minutes!

## What You Got

✅ MariaDB Database (ready to import)  
✅ PHP REST API (fully functional)  
✅ Android App (with PHINMA colors)  
✅ Everything documented with 🔧 markers

---

## 🚀 10-Minute Setup

### 1. Install XAMPP (2 minutes)
- Download: https://www.apachefriends.org/
- Install → Start Apache & MySQL

### 2. Import Database (2 minutes)
- Open: http://localhost/phpmyadmin
- Create database: `phinma_lost_found`
- Import: `database/phinma_database_schema.sql`

### 3. Setup API (1 minute)
- Copy `backend` folder to `C:\xampp\htdocs\phinma-api`
- Done! API ready at: http://localhost/phinma-api/

### 4. Find Your IP (1 minute)
Windows: CMD → `ipconfig` → IPv4 Address  
Mac: System Preferences → Network  
Example: 192.168.1.100

### 5. Update Android App (2 minutes)
Open `ApiConfig.kt`:
```kotlin
private const val BASE_URL = "http://YOUR_IP_HERE/phinma-api/"
```

### 6. Run App! (2 minutes)
- Open in Android Studio
- Connect phone or start emulator
- Click Run ▶️

---

## ✅ Test Login

**Email:** user@test.com  
**Password:** 1234

---

## 🎨 PHINMA Colors Applied

- Primary: Dark Green #1B5E20
- Accent: Yellow #FDD835
- Modern gradients & shadows

---

## 🔧 Need to Customize?

**All editable code marked with:** 🔧

Search for 🔧 in:
- Database schema
- API files  
- Android code

Read: `CUSTOMIZATION_GUIDE.md`

---

## 📁 Package Structure

```
├── backend/           ← Copy to xampp/htdocs/
├── database/          ← Import to phpMyAdmin
├── android-app/       ← Open in Android Studio
└── documentation/     ← Read these!
```

---

## 🐛 Troubleshooting

**Can't connect to API?**
- Check XAMPP is running
- Verify IP address in ApiConfig.kt
- Phone/emulator on same WiFi

**Database error?**
- Check MySQL running in XAMPP
- Verify database name: phinma_lost_found

---

## 📞 What's Next?

1. ✅ Get it running (follow steps above)
2. 🎨 Customize colors if needed
3. 🔧 Add your custom fields
4. 📱 Build features
5. 🚀 Deploy!

---

<div align="center">

# Everything is Ready!

**Just follow the 6 steps above.**

*Full setup guide: `SETUP_GUIDE.md`*

</div>
