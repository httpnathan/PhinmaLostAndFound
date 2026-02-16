<?php
/**
 * ============================================================================
 * POSTS API ENDPOINTS
 * ============================================================================
 * This file handles creating, reading, updating, and deleting posts
 * 
 * 🔧 TO ADD NEW POST FIELDS:
 * 1. Add them to the CREATE and UPDATE queries
 * 2. Add them to the response in GET query
 * 3. Update Android app to send/receive those fields
 * ============================================================================
 */

require_once 'config.php';

$database = new Database();
$db = $database->getConnection();

$request_method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

// ============================================================================
// CREATE NEW POST
// ============================================================================
// 🔧 TO ADD POST CREATION FIELDS: Add to INSERT query and required fields

if ($action === 'create' && $request_method === 'POST') {
    $data = getJsonInput();
    
    // Validate required fields
    // 🔧 ADD YOUR CUSTOM REQUIRED FIELDS HERE
    $required_fields = ['user_id', 'post_type', 'item_name', 'description'];
    validateRequired($data, $required_fields);
    
    $user_id = $data['user_id'];
    $post_type = $data['post_type'];
    $item_name = trim($data['item_name']);
    $description = trim($data['description']);
    
    // Optional fields
    $category = isset($data['category']) ? trim($data['category']) : null;
    $location_found = isset($data['location_found']) ? trim($data['location_found']) : null;
    $building = isset($data['building']) ? trim($data['building']) : null;
    $floor_number = isset($data['floor_number']) ? trim($data['floor_number']) : null;
    $image_url = isset($data['image_url']) ? $data['image_url'] : null;
    $date_lost_found = isset($data['date_lost_found']) ? $data['date_lost_found'] : date('Y-m-d');
    
    // 🔧 GET YOUR CUSTOM OPTIONAL FIELDS HERE
    // $color = isset($data['color']) ? $data['color'] : null;
    // $brand = isset($data['brand']) ? $data['brand'] : null;
    
    // Validate post type
    if (!in_array($post_type, ['lost', 'found'])) {
        sendResponse(false, 'Invalid post type. Must be "lost" or "found"');
    }
    
    // Insert post
    // 🔧 ADD YOUR CUSTOM FIELDS TO THIS QUERY
    $insert_query = "INSERT INTO posts 
                     (user_id, post_type, item_name, description, category, 
                      location_found, building, floor_number, image_url, date_lost_found) 
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    $stmt = $db->prepare($insert_query);
    
    try {
        $stmt->execute([
            $user_id, $post_type, $item_name, $description, $category,
            $location_found, $building, $floor_number, $image_url, $date_lost_found
        ]);
        
        $post_id = $db->lastInsertId();
        
        sendResponse(true, 'Post created successfully', ['post_id' => $post_id]);
    } catch (PDOException $e) {
        sendResponse(false, 'Failed to create post: ' . $e->getMessage());
    }
}

// ============================================================================
// GET ALL POSTS (with filtering)
// ============================================================================
// 🔧 TO ADD RESPONSE FIELDS: Add columns to SELECT query

elseif ($action === 'get_all' && $request_method === 'GET') {
    // Optional filters
    $post_type = isset($_GET['post_type']) ? $_GET['post_type'] : null;
    $category = isset($_GET['category']) ? $_GET['category'] : null;
    $status = isset($_GET['status']) ? $_GET['status'] : 'active';
    $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
    $offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;
    
    // Build query
    // 🔧 ADD YOUR CUSTOM FIELDS TO SELECT
    $query = "SELECT p.*, u.first_name, u.last_name, u.email 
              FROM posts p 
              JOIN users u ON p.user_id = u.user_id 
              WHERE 1=1";
    
    $params = [];
    
    if ($post_type) {
        $query .= " AND p.post_type = ?";
        $params[] = $post_type;
    }
    
    if ($category) {
        $query .= " AND p.category = ?";
        $params[] = $category;
    }
    
    if ($status) {
        $query .= " AND p.status = ?";
        $params[] = $status;
    }
    
    // 🔧 ADD YOUR CUSTOM FILTERS HERE
    // if (isset($_GET['building'])) {
    //     $query .= " AND p.building = ?";
    //     $params[] = $_GET['building'];
    // }
    
    $query .= " ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
    $params[] = $limit;
    $params[] = $offset;
    
    $stmt = $db->prepare($query);
    $stmt->execute($params);
    
    $posts = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    sendResponse(true, 'Posts retrieved successfully', $posts);
}

// ============================================================================
// GET SINGLE POST
// ============================================================================

elseif ($action === 'get_one' && $request_method === 'GET') {
    if (!isset($_GET['post_id'])) {
        sendResponse(false, 'Post ID is required');
    }
    
    $post_id = $_GET['post_id'];
    
    // 🔧 ADD YOUR CUSTOM FIELDS TO SELECT
    $query = "SELECT p.*, u.first_name, u.last_name, u.email 
              FROM posts p 
              JOIN users u ON p.user_id = u.user_id 
              WHERE p.post_id = ?";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$post_id]);
    
    if ($stmt->rowCount() === 0) {
        sendResponse(false, 'Post not found');
    }
    
    $post = $stmt->fetch(PDO::FETCH_ASSOC);
    sendResponse(true, 'Post retrieved successfully', $post);
}

// ============================================================================
// SEARCH POSTS
// ============================================================================
// 🔧 TO ADD SEARCHABLE FIELDS: Add to WHERE clause with OR

elseif ($action === 'search' && $request_method === 'GET') {
    if (!isset($_GET['keyword'])) {
        sendResponse(false, 'Search keyword is required');
    }
    
    $keyword = '%' . $_GET['keyword'] . '%';
    
    // 🔧 ADD YOUR CUSTOM SEARCHABLE FIELDS HERE
    $query = "SELECT p.*, u.first_name, u.last_name 
              FROM posts p 
              JOIN users u ON p.user_id = u.user_id 
              WHERE p.status = 'active' 
              AND (p.item_name LIKE ? 
                   OR p.description LIKE ? 
                   OR p.category LIKE ?
                   OR p.location_found LIKE ?)
              ORDER BY p.created_at DESC 
              LIMIT 50";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$keyword, $keyword, $keyword, $keyword]);
    
    $posts = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    sendResponse(true, 'Search completed', $posts);
}

// ============================================================================
// UPDATE POST
// ============================================================================
// 🔧 TO ADD UPDATABLE FIELDS: Add to UPDATE query

elseif ($action === 'update' && $request_method === 'POST') {
    $data = getJsonInput();
    
    validateRequired($data, ['post_id', 'user_id']);
    
    $post_id = $data['post_id'];
    $user_id = $data['user_id'];
    
    // Verify ownership
    $check_query = "SELECT user_id FROM posts WHERE post_id = ?";
    $stmt = $db->prepare($check_query);
    $stmt->execute([$post_id]);
    
    if ($stmt->rowCount() === 0) {
        sendResponse(false, 'Post not found');
    }
    
    $post = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($post['user_id'] != $user_id) {
        sendResponse(false, 'Unauthorized to update this post');
    }
    
    // Build update query dynamically
    $update_fields = [];
    $update_values = [];
    
    // 🔧 ADD YOUR CUSTOM UPDATABLE FIELDS HERE
    $allowed_fields = ['item_name', 'description', 'category', 'location_found', 
                       'building', 'floor_number', 'status', 'image_url'];
    
    foreach ($allowed_fields as $field) {
        if (isset($data[$field])) {
            $update_fields[] = "$field = ?";
            $update_values[] = $data[$field];
        }
    }
    
    if (empty($update_fields)) {
        sendResponse(false, 'No fields to update');
    }
    
    $update_values[] = $post_id;
    
    $update_query = "UPDATE posts SET " . implode(', ', $update_fields) . " WHERE post_id = ?";
    $stmt = $db->prepare($update_query);
    
    try {
        $stmt->execute($update_values);
        sendResponse(true, 'Post updated successfully');
    } catch (PDOException $e) {
        sendResponse(false, 'Failed to update post: ' . $e->getMessage());
    }
}

// ============================================================================
// DELETE POST
// ============================================================================

elseif ($action === 'delete' && $request_method === 'POST') {
    $data = getJsonInput();
    
    validateRequired($data, ['post_id', 'user_id']);
    
    $post_id = $data['post_id'];
    $user_id = $data['user_id'];
    
    // Verify ownership
    $check_query = "SELECT user_id FROM posts WHERE post_id = ?";
    $stmt = $db->prepare($check_query);
    $stmt->execute([$post_id]);
    
    if ($stmt->rowCount() === 0) {
        sendResponse(false, 'Post not found');
    }
    
    $post = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($post['user_id'] != $user_id) {
        sendResponse(false, 'Unauthorized to delete this post');
    }
    
    // Delete post (or set status to 'deleted')
    // 🔧 CHOOSE: Hard delete OR soft delete
    // Hard delete (removes from database):
    // $delete_query = "DELETE FROM posts WHERE post_id = ?";
    
    // Soft delete (keeps in database but marks as deleted):
    $delete_query = "UPDATE posts SET status = 'deleted' WHERE post_id = ?";
    
    $stmt = $db->prepare($delete_query);
    
    try {
        $stmt->execute([$post_id]);
        sendResponse(true, 'Post deleted successfully');
    } catch (PDOException $e) {
        sendResponse(false, 'Failed to delete post: ' . $e->getMessage());
    }
}

// ============================================================================
// GET USER'S POSTS
// ============================================================================

elseif ($action === 'get_user_posts' && $request_method === 'GET') {
    if (!isset($_GET['user_id'])) {
        sendResponse(false, 'User ID is required');
    }
    
    $user_id = $_GET['user_id'];
    
    $query = "SELECT * FROM posts 
              WHERE user_id = ? 
              AND status != 'deleted' 
              ORDER BY created_at DESC";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$user_id]);
    
    $posts = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    sendResponse(true, 'User posts retrieved', $posts);
}

// ============================================================================
// INVALID ACTION
// ============================================================================

else {
    sendResponse(false, 'Invalid action or request method');
}

?>
