# 🎯 PRESET POSTS GUIDE

## ✅ What You Got

You now have **3 preset posts** that display in the app without needing database!

### Features:
- ✅ **Photo Scroller** (Shopee-style swipe)
- ✅ **Title/Header** 
- ✅ **Description**
- ✅ **Last Seen Location**
- ✅ **Last Seen Time**
- ✅ **Contact Person**
- ✅ **LOST/FOUND Badge**
- ✅ **Image Counter** (1/3, 2/3, etc.)

---

## 📱 Current Preset Posts

### Post 1: Lost iPhone 13 Pro
- Type: LOST (red badge)
- 3 images
- Location: Main Library - 2nd Floor
- Time: Today, 2:30 PM
- Contact: Juan Dela Cruz - 0917-123-4567

### Post 2: Found PHINMA Student ID
- Type: FOUND (green badge)
- 2 images
- Location: University Cafeteria
- Time: Yesterday, 4:15 PM
- Contact: Security Office

### Post 3: Lost Black Backpack
- Type: LOST (red badge)
- 4 images
- Location: Room 304 - Engineering Building
- Time: Feb 8, 2024 - 11:00 AM
- Contact: Pedro Martinez - 0998-765-4321

---

## 🔧 How to Add Your Own Images

### Step 1: Add Images to Project

1. **Prepare your images:**
   - Format: JPG or PNG
   - Recommended size: 800x800 pixels or similar
   - Name them: `post1_image1.jpg`, `post1_image2.jpg`, etc.

2. **Add to Android Studio:**
   - Right-click `res/drawable` folder
   - Select "Show in Explorer" (or Finder on Mac)
   - Copy your images into the `drawable` folder
   - Or drag and drop directly in Android Studio

### Step 2: Update Post.kt

**File:** `Post.kt`  
**Location:** `app/src/main/java/com/example/phinmalostandfound/Post.kt`

Find the preset posts section (around line 50) and update:

```kotlin
// POST 1 - Lost iPhone
Post(
    id = 1,
    title = "Lost iPhone 13 Pro",  // 🔧 Change title here
    images = listOf(
        R.drawable.post1_image1,   // 🔧 Change to your image name
        R.drawable.post1_image2,   // 🔧 Add your images
        R.drawable.post1_image3
    ),
    description = """
        Your description here...   // 🔧 Change description
    """.trimIndent(),
    lastSeenLocation = "Your location",      // 🔧 Change location
    lastSeenTime = "Your time",              // 🔧 Change time
    contactPerson = "Your contact",          // 🔧 Change contact
    postType = "lost",  // or "found"
    category = "Electronics",
    postedBy = "Your Name"
)
```

---

## 🎨 Customization Options

### Change Post Type Color

**File:** `PostAdapter.kt`  
**Line:** ~50

```kotlin
if (post.postType == "lost") {
    holder.postTypeBadge.setBackgroundColor(Color.parseColor("#D32F2F")) // 🔧 Red for LOST
} else {
    holder.postTypeBadge.setBackgroundColor(Color.parseColor("#1B5E20")) // 🔧 Green for FOUND
}
```

### Add More Posts

**File:** `Post.kt`  
**After Post 3:**

```kotlin
,  // Don't forget comma!

// POST 4 - Your New Post
Post(
    id = 4,
    title = "Your Title",
    images = listOf(R.drawable.your_image),
    description = "Your description",
    lastSeenLocation = "Your location",
    lastSeenTime = "Your time",
    contactPerson = "Your contact",
    postType = "lost", // or "found"
)
```

---

## 📝 Quick Edit Checklist

To update a post, edit these fields in `Post.kt`:

- [ ] **title** - Item name
- [ ] **images** - List of drawable resources
- [ ] **description** - Full details
- [ ] **lastSeenLocation** - Where it was lost/found
- [ ] **lastSeenTime** - When it was lost/found
- [ ] **contactPerson** - Who to contact
- [ ] **postType** - "lost" or "found"
- [ ] **category** - Optional category
- [ ] **postedBy** - Optional poster name

---

## 🖼️ Image Best Practices

### Recommended:
✅ Square or landscape images (800x600, 1000x750, etc.)
✅ Good lighting and clear focus
✅ JPG format for photos
✅ PNG format for documents/IDs (better quality)

### Avoid:
❌ Very large files (>2MB)
❌ Portrait orientation (gets cropped)
❌ Blurry or dark images

---

## 🔍 How It Works

### Image Scroller:
- Swipe left/right to see all images
- Counter shows current image (1/3, 2/3, etc.)
- Like Shopee product viewer!

### Card Layout:
- Modern Material Design
- Rounded corners
- Shadow for depth
- Clean, professional look

### LOST vs FOUND:
- LOST = Red badge
- FOUND = Green badge
- Automatic color coding

---

## 🚀 Testing Your Changes

1. **Update Post.kt**
2. **Sync Project:** File → Sync Project with Gradle Files
3. **Clean Build:** Build → Clean Project
4. **Rebuild:** Build → Rebuild Project
5. **Run App** ▶️
6. **Check HomeActivity** - Your posts should appear!

---

## 💡 Pro Tips

### Tip 1: Use Placeholder for Testing
If you don't have images yet, use:
```kotlin
images = listOf(
    R.drawable.ic_image_placeholder,
    R.drawable.ic_image_placeholder
)
```

### Tip 2: Bulk Add Images
Name your images consistently:
- `lost_phone_1.jpg`, `lost_phone_2.jpg`
- `found_id_1.jpg`, `found_id_2.jpg`

### Tip 3: Compress Images
Use online tools to compress:
- TinyPNG.com
- Compressor.io
- Keep under 500KB each

### Tip 4: Quick Description Formatting
Use triple quotes for multi-line:
```kotlin
description = """
    Line 1
    Line 2
    Line 3
""".trimIndent()
```

---

## 🎯 Example: Complete Post Update

**Before:**
```kotlin
Post(
    id = 1,
    title = "Lost iPhone 13 Pro",
    images = listOf(R.drawable.ic_image_placeholder),
    description = "Lost my phone...",
    ...
)
```

**After (with your content):**
```kotlin
Post(
    id = 1,
    title = "Lost Blue Wallet",  // ✅ Your title
    images = listOf(
        R.drawable.wallet_front,  // ✅ Your images
        R.drawable.wallet_open,
        R.drawable.wallet_contents
    ),
    description = """
        Lost my blue leather wallet near the gym.
        
        Contains:
        - Student ID
        - ATM card
        - Some cash
        
        Please contact if found!
    """.trimIndent(),  // ✅ Your description
    lastSeenLocation = "University Gym - Locker Area",  // ✅ Your location
    lastSeenTime = "Today, 9:00 AM",  // ✅ Your time
    contactPerson = "John Doe - 0912-345-6789",  // ✅ Your contact
    postType = "lost",
    category = "Personal Items"
)
```

---

## 📞 Need Help?

### Common Issues:

**Images not showing:**
- Check drawable folder
- Verify image names (no spaces, lowercase)
- Clean and rebuild project

**App crashes:**
- Check for typos in Post.kt
- Ensure all commas are in place
- Check image resources exist

**Scroller not working:**
- ViewPager2 dependency added? (Check build.gradle)
- Sync Gradle files

---

<div align="center">

# ✅ Ready to Demo!

**Your posts are live in the app!**

Just update the text and images in `Post.kt`

No database needed! 🎉

</div>
