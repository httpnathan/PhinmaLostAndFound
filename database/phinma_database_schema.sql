-- ============================================================================
-- PHINMA LOST AND FOUND - MariaDB Database Schema
-- ============================================================================
-- This is a TEMPLATE database structure
-- You can easily add/remove tables and fields as needed
-- Comments indicate where and how to modify
-- ============================================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS phinma_lost_found;
USE phinma_lost_found;

-- ============================================================================
-- USERS TABLE
-- ============================================================================
-- This table stores user account information
-- 🔧 TO ADD NEW USER FIELDS: Add columns below (example: phone_number VARCHAR(20))
-- 🔧 TO REMOVE FIELDS: Delete the column line you don't need

CREATE TABLE IF NOT EXISTS users (
    -- Primary Key
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Login Credentials
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- Password is hashed for security
    
    -- User Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    
    -- 🔧 ADD YOUR CUSTOM USER FIELDS HERE:
    -- student_id VARCHAR(50),
    -- department VARCHAR(100),
    -- year_level INT,
    -- contact_number VARCHAR(20),
    
    -- Account Status
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    
    -- Indexes for faster queries
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- POSTS TABLE
-- ============================================================================
-- This table stores lost and found item posts
-- 🔧 TO ADD POST FIELDS: Add columns below (example: reward_amount DECIMAL(10,2))
-- 🔧 TO REMOVE FIELDS: Delete the column line

CREATE TABLE IF NOT EXISTS posts (
    -- Primary Key
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Foreign Key (links to users table)
    user_id INT NOT NULL,
    
    -- Post Type
    post_type ENUM('lost', 'found') NOT NULL,  -- 🔧 Can add more types like 'claimed'
    
    -- Item Information
    item_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100),  -- 🔧 e.g., 'Electronics', 'Documents', 'Accessories'
    
    -- 🔧 ADD YOUR CUSTOM POST FIELDS HERE:
    -- color VARCHAR(50),
    -- brand VARCHAR(100),
    -- size VARCHAR(50),
    -- reward_amount DECIMAL(10,2),
    
    -- Location Information
    location_found VARCHAR(255),  -- Where item was lost/found
    building VARCHAR(100),
    floor_number VARCHAR(20),
    
    -- Media
    image_url VARCHAR(500),  -- Path to uploaded image
    
    -- Status
    status ENUM('active', 'claimed', 'expired', 'deleted') DEFAULT 'active',
    
    -- Timestamps
    date_lost_found DATE,  -- When item was lost/found
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraint
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_post_type (post_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MESSAGES TABLE
-- ============================================================================
-- This table stores chat messages between users
-- 🔧 TO ADD MESSAGE FIELDS: Add columns below

CREATE TABLE IF NOT EXISTS messages (
    -- Primary Key
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Foreign Keys
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    
    -- Message Content
    message_text TEXT NOT NULL,
    
    -- 🔧 ADD YOUR CUSTOM MESSAGE FIELDS HERE:
    -- attachment_url VARCHAR(500),
    -- message_type ENUM('text', 'image', 'file'),
    
    -- Status
    is_read BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    
    -- Foreign Key Constraints
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_sender_receiver (sender_id, receiver_id),
    INDEX idx_sent_at (sent_at),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- ADMINS TABLE
-- ============================================================================
-- This table stores admin/moderator accounts
-- 🔧 TO ADD ADMIN FIELDS: Add columns below

CREATE TABLE IF NOT EXISTS admins (
    -- Primary Key
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Login Credentials
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    
    -- Admin Information
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    
    -- 🔧 ADD YOUR CUSTOM ADMIN FIELDS HERE:
    -- role ENUM('super_admin', 'moderator', 'support'),
    -- department VARCHAR(100),
    -- permissions JSON,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    
    -- Indexes
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- NOTIFICATIONS TABLE (OPTIONAL)
-- ============================================================================
-- This table stores user notifications
-- 🔧 YOU CAN DELETE THIS ENTIRE TABLE if you don't need notifications

CREATE TABLE IF NOT EXISTS notifications (
    -- Primary Key
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Foreign Key
    user_id INT NOT NULL,
    
    -- Notification Content
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),  -- 🔧 e.g., 'match_found', 'message', 'claim'
    
    -- Related Data
    related_post_id INT NULL,  -- Link to related post if applicable
    
    -- Status
    is_read BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraints
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (related_post_id) REFERENCES posts(post_id) ON DELETE SET NULL,
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- CATEGORIES TABLE (OPTIONAL)
-- ============================================================================
-- This table stores predefined categories for items
-- 🔧 YOU CAN DELETE THIS if you want to use free-text categories

CREATE TABLE IF NOT EXISTS categories (
    -- Primary Key
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Category Information
    category_name VARCHAR(100) UNIQUE NOT NULL,
    category_icon VARCHAR(100),  -- Icon name or URL
    
    -- 🔧 ADD YOUR CUSTOM CATEGORY FIELDS HERE:
    -- parent_category_id INT,  -- For subcategories
    -- display_order INT,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SAMPLE DATA (FOR TESTING)
-- ============================================================================
-- 🔧 REMOVE THIS SECTION in production
-- This creates sample data for testing

-- Sample Admin Account
-- Username: admin, Password: admin123
INSERT INTO admins (username, password_hash, full_name, email) VALUES
('admin', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin User', 'admin@phinma.edu.ph');

-- Sample User Account
-- Email: user@test.com, Password: 1234
INSERT INTO users (email, password_hash, first_name, last_name, is_verified) VALUES
('user@test.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Test', 'User', TRUE);

-- Sample Categories
INSERT INTO categories (category_name, category_icon) VALUES
('Electronics', 'ic_phone'),
('Documents', 'ic_document'),
('Accessories', 'ic_accessory'),
('Clothing', 'ic_shirt'),
('Books', 'ic_book'),
('Keys', 'ic_key'),
('Others', 'ic_more');

-- ============================================================================
-- USEFUL QUERIES (FOR REFERENCE)
-- ============================================================================

-- Get all active posts with user information
-- SELECT p.*, u.first_name, u.last_name, u.email 
-- FROM posts p 
-- JOIN users u ON p.user_id = u.user_id 
-- WHERE p.status = 'active' 
-- ORDER BY p.created_at DESC;

-- Get unread messages for a user
-- SELECT m.*, u.first_name, u.last_name 
-- FROM messages m 
-- JOIN users u ON m.sender_id = u.user_id 
-- WHERE m.receiver_id = ? AND m.is_read = FALSE 
-- ORDER BY m.sent_at DESC;

-- Search posts by keyword
-- SELECT * FROM posts 
-- WHERE (item_name LIKE '%keyword%' OR description LIKE '%keyword%') 
-- AND status = 'active' 
-- ORDER BY created_at DESC;

-- ============================================================================
-- MODIFICATION GUIDE
-- ============================================================================

-- 📝 HOW TO ADD A NEW TABLE:
-- 1. Copy the template structure from an existing table
-- 2. Change the table name
-- 3. Add your custom fields
-- 4. Add appropriate indexes
-- 5. Add foreign keys if needed

-- 📝 HOW TO ADD A NEW FIELD TO EXISTING TABLE:
-- ALTER TABLE table_name ADD COLUMN new_field_name VARCHAR(100);

-- 📝 HOW TO REMOVE A FIELD:
-- ALTER TABLE table_name DROP COLUMN field_name;

-- 📝 HOW TO CHANGE A FIELD TYPE:
-- ALTER TABLE table_name MODIFY COLUMN field_name NEW_TYPE;

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
