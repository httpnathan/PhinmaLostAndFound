<?php
/**
 * ============================================================================
 * AUTHENTICATION API ENDPOINTS
 * ============================================================================
 * This file handles user login and registration
 * 
 * 🔧 TO ADD NEW AUTH FIELDS:
 * 1. Add them to the validateRequired() call
 * 2. Add them to the INSERT/UPDATE query
 * 3. Update the Android app to send those fields
 * ============================================================================
 */

require_once 'config.php';

$database = new Database();
$db = $database->getConnection();

$request_method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

// ============================================================================
// REGISTER NEW USER
// ============================================================================
// 🔧 TO ADD REGISTRATION FIELDS: Add to $required_fields and INSERT query

if ($action === 'register' && $request_method === 'POST') {
    $data = getJsonInput();
    
    // Validate required fields
    // 🔧 ADD YOUR CUSTOM FIELDS HERE (e.g., 'student_id', 'department')
    $required_fields = ['email', 'password', 'first_name', 'last_name'];
    validateRequired($data, $required_fields);
    
    $email = trim($data['email']);
    $password = $data['password'];
    $first_name = trim($data['first_name']);
    $last_name = trim($data['last_name']);
    
    // 🔧 GET YOUR CUSTOM FIELDS HERE
    // $student_id = trim($data['student_id']);
    // $department = trim($data['department']);
    
    // Validate email format
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        sendResponse(false, 'Invalid email format');
    }
    
    // Check if email already exists
    $check_query = "SELECT user_id FROM users WHERE email = ?";
    $stmt = $db->prepare($check_query);
    $stmt->execute([$email]);
    
    if ($stmt->rowCount() > 0) {
        sendResponse(false, 'Email already registered');
    }
    
    // Hash password
    $password_hash = password_hash($password, PASSWORD_DEFAULT);
    
    // Insert new user
    // 🔧 ADD YOUR CUSTOM FIELDS TO THIS QUERY
    $insert_query = "INSERT INTO users (email, password_hash, first_name, last_name) 
                     VALUES (?, ?, ?, ?)";
    $stmt = $db->prepare($insert_query);
    
    try {
        $stmt->execute([$email, $password_hash, $first_name, $last_name]);
        
        $user_id = $db->lastInsertId();
        
        sendResponse(true, 'Registration successful', [
            'user_id' => $user_id,
            'email' => $email,
            'first_name' => $first_name,
            'last_name' => $last_name
        ]);
    } catch (PDOException $e) {
        sendResponse(false, 'Registration failed: ' . $e->getMessage());
    }
}

// ============================================================================
// LOGIN USER
// ============================================================================
// 🔧 TO ADD LOGIN RESPONSE FIELDS: Add to the response data array

elseif ($action === 'login' && $request_method === 'POST') {
    $data = getJsonInput();
    
    validateRequired($data, ['email', 'password']);
    
    $email = trim($data['email']);
    $password = $data['password'];
    
    // Get user from database
    // 🔧 ADD YOUR CUSTOM FIELDS TO SELECT if you want them in login response
    $query = "SELECT user_id, email, password_hash, first_name, last_name, is_active, is_verified 
              FROM users 
              WHERE email = ?";
    $stmt = $db->prepare($query);
    $stmt->execute([$email]);
    
    if ($stmt->rowCount() === 0) {
        sendResponse(false, 'Invalid email or password');
    }
    
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    // Verify password
    if (!password_verify($password, $user['password_hash'])) {
        sendResponse(false, 'Invalid email or password');
    }
    
    // Check if account is active
    if (!$user['is_active']) {
        sendResponse(false, 'Account is deactivated. Please contact support.');
    }
    
    // Update last login time
    $update_query = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
    $stmt = $db->prepare($update_query);
    $stmt->execute([$user['user_id']]);
    
    // Remove password hash from response
    unset($user['password_hash']);
    
    // 🔧 YOU CAN ADD MORE FIELDS TO THE RESPONSE HERE
    sendResponse(true, 'Login successful', $user);
}

// ============================================================================
// ADMIN LOGIN
// ============================================================================
// 🔧 TO CUSTOMIZE ADMIN LOGIN: Modify the query and response

elseif ($action === 'admin_login' && $request_method === 'POST') {
    $data = getJsonInput();
    
    validateRequired($data, ['username', 'password']);
    
    $username = trim($data['username']);
    $password = $data['password'];
    
    // Get admin from database
    $query = "SELECT admin_id, username, password_hash, full_name, email, is_active 
              FROM admins 
              WHERE username = ?";
    $stmt = $db->prepare($query);
    $stmt->execute([$username]);
    
    if ($stmt->rowCount() === 0) {
        sendResponse(false, 'Invalid credentials');
    }
    
    $admin = $stmt->fetch(PDO::FETCH_ASSOC);
    
    // Verify password
    if (!password_verify($password, $admin['password_hash'])) {
        sendResponse(false, 'Invalid credentials');
    }
    
    // Check if account is active
    if (!$admin['is_active']) {
        sendResponse(false, 'Admin account is deactivated');
    }
    
    // Update last login
    $update_query = "UPDATE admins SET last_login = NOW() WHERE admin_id = ?";
    $stmt = $db->prepare($update_query);
    $stmt->execute([$admin['admin_id']]);
    
    unset($admin['password_hash']);
    
    sendResponse(true, 'Admin login successful', $admin);
}

// ============================================================================
// INVALID ACTION
// ============================================================================

else {
    sendResponse(false, 'Invalid action or request method');
}

?>
