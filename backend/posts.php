<?php
/**
 * ============================================================================
 * POSTS API ENDPOINTS
 * ============================================================================
 * This file handles creating, reading, updating, and deleting posts
 */

require_once 'config.php';

$database = new Database();
$db = $database->getConnection();

$request_method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

// ============================================================================
// CREATE NEW POST
// ============================================================================
if ($action === 'create' && $request_method === 'POST') {
    // Merge all input sources: JSON body, $_POST (multipart fields), and $_GET (URL params).
    // getJsonInput() alone misses $_GET, which is where user_id lands when appended to the URL.
    $json_body = json_decode(file_get_contents('php://input'), true) ?? [];
    $data = array_merge($json_body, $_POST, $_GET);
    // Remove routing params so they don't interfere with validation
    unset($data['action']);
    
    $required_fields = ['user_id', 'post_type', 'item_name', 'description'];
    validateRequired($data, $required_fields);
    
    $user_id = $data['user_id'];
    $post_type = $data['post_type'];
    $item_name = trim($data['item_name']);
    $description = trim($data['description']);
    
    $category = isset($data['category']) ? trim($data['category']) : 'General';
    $location_found = isset($data['location_found']) ? trim($data['location_found']) : 'Campus';
    $building = isset($data['building']) ? trim($data['building']) : 'Main';
    $floor_number = isset($data['floor_number']) ? trim($data['floor_number']) : '1';
    $date_lost_found = isset($data['date_lost_found']) ? $data['date_lost_found'] : date('Y-m-d H:i:s');

    $image_url = null;

    // Handle File Upload
    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        if (!is_dir(UPLOAD_DIR)) {
            mkdir(UPLOAD_DIR, 0777, true);
        }

        $file_extension = pathinfo($_FILES['image']['name'], PATHINFO_EXTENSION);
        $file_name = 'img_' . time() . '_' . rand(1000, 9999) . '.' . $file_extension;
        $target_file = UPLOAD_DIR . $file_name;

        if (move_uploaded_file($_FILES['image']['tmp_name'], $target_file)) {
            // Store the full URL or relative path. The Android app might need the full URL.
            // For now, we store the relative path.
            $image_url = $target_file;
        }
    } else if (isset($data['image_url'])) {
        $image_url = $data['image_url'];
    }

    if (!in_array($post_type, ['lost', 'found'])) {
        sendResponse(false, 'Invalid post type. Must be "lost" or "found"');
    }
    
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
elseif ($action === 'get_all' && $request_method === 'GET') {
    $post_type = isset($_GET['post_type']) ? $_GET['post_type'] : null;
    $category = isset($_GET['category']) ? $_GET['category'] : null;
    $status = isset($_GET['status']) ? $_GET['status'] : 'active';
    $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
    $offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;
    
    $query = "SELECT p.*, CONCAT(u.first_name, ' ', u.last_name) as posted_by, u.email as contact_number
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

    // LIMIT and OFFSET should be added carefully to avoid SQL injection
    $query .= " ORDER BY p.created_at DESC LIMIT " . (int)$limit . " OFFSET " . (int)$offset;
    
    try {
        $stmt = $db->prepare($query);
        $stmt->execute($params);
        $posts = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Base URL for images
        $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
        $host = $_SERVER['HTTP_HOST'];
        $script_path = dirname($_SERVER['SCRIPT_NAME']);
        $base_url = $protocol . "://" . $host . $script_path . "/";

        // Format for Android App
        foreach ($posts as &$post) {
            if ($post['image_url'] && !filter_var($post['image_url'], FILTER_VALIDATE_URL)) {
                $post['image_url'] = $base_url . $post['image_url'];
            }
            $post['image_urls'] = $post['image_url'] ? [$post['image_url']] : [];
        }

        sendResponse(true, 'Posts retrieved successfully', $posts);
    } catch (PDOException $e) {
        sendResponse(false, 'Database error: ' . $e->getMessage());
    }
}

// ============================================================================
// GET SINGLE POST
// ============================================================================
elseif ($action === 'get_one' && $request_method === 'GET') {
    if (!isset($_GET['post_id'])) {
        sendResponse(false, 'Post ID is required');
    }
    
    $post_id = $_GET['post_id'];
    
    $query = "SELECT p.*, CONCAT(u.first_name, ' ', u.last_name) as posted_by, u.email as contact_number
              FROM posts p 
              JOIN users u ON p.user_id = u.user_id 
              WHERE p.post_id = ?";

    try {
        $stmt = $db->prepare($query);
        $stmt->execute([$post_id]);

        if ($stmt->rowCount() === 0) {
            sendResponse(false, 'Post not found');
        }

        $post = $stmt->fetch(PDO::FETCH_ASSOC);

        $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
        $host = $_SERVER['HTTP_HOST'];
        $script_path = dirname($_SERVER['SCRIPT_NAME']);
        $base_url = $protocol . "://" . $host . $script_path . "/";

        if ($post['image_url'] && !filter_var($post['image_url'], FILTER_VALIDATE_URL)) {
            $post['image_url'] = $base_url . $post['image_url'];
        }
        $post['image_urls'] = $post['image_url'] ? [$post['image_url']] : [];

        sendResponse(true, 'Post retrieved successfully', $post);
    } catch (PDOException $e) {
        sendResponse(false, 'Database error: ' . $e->getMessage());
    }
}

// ============================================================================
// SEARCH POSTS
// ============================================================================
elseif ($action === 'search' && $request_method === 'GET') {
    if (!isset($_GET['keyword'])) {
        sendResponse(false, 'Search keyword is required');
    }
    
    $keyword = '%' . $_GET['keyword'] . '%';
    
    $query = "SELECT p.*, CONCAT(u.first_name, ' ', u.last_name) as posted_by
              FROM posts p 
              JOIN users u ON p.user_id = u.user_id 
              WHERE p.status = 'active' 
              AND (p.item_name LIKE ? 
                   OR p.description LIKE ? 
                   OR p.category LIKE ?
                   OR p.location_found LIKE ?)
              ORDER BY p.created_at DESC 
              LIMIT 50";

    try {
        $stmt = $db->prepare($query);
        $stmt->execute([$keyword, $keyword, $keyword, $keyword]);
        $posts = $stmt->fetchAll(PDO::FETCH_ASSOC);

        $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
        $host = $_SERVER['HTTP_HOST'];
        $script_path = dirname($_SERVER['SCRIPT_NAME']);
        $base_url = $protocol . "://" . $host . $script_path . "/";

        foreach ($posts as &$post) {
            if ($post['image_url'] && !filter_var($post['image_url'], FILTER_VALIDATE_URL)) {
                $post['image_url'] = $base_url . $post['image_url'];
            }
            $post['image_urls'] = $post['image_url'] ? [$post['image_url']] : [];
        }

        sendResponse(true, 'Search completed', $posts);
    } catch (PDOException $e) {
        sendResponse(false, 'Database error: ' . $e->getMessage());
    }
}

// ============================================================================
// UPDATE POST
// ============================================================================
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
    
    try {
        $stmt = $db->prepare($query);
        $stmt->execute([$user_id]);
        $posts = $stmt->fetchAll(PDO::FETCH_ASSOC);

        $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
        $host = $_SERVER['HTTP_HOST'];
        $script_path = dirname($_SERVER['SCRIPT_NAME']);
        $base_url = $protocol . "://" . $host . $script_path . "/";

        foreach ($posts as &$post) {
            if ($post['image_url'] && !filter_var($post['image_url'], FILTER_VALIDATE_URL)) {
                $post['image_url'] = $base_url . $post['image_url'];
            }
            $post['image_urls'] = $post['image_url'] ? [$post['image_url']] : [];
        }

        sendResponse(true, 'User posts retrieved', $posts);
    } catch (PDOException $e) {
        sendResponse(false, 'Database error: ' . $e->getMessage());
    }
}

else {
    sendResponse(false, 'Invalid action or request method');
}
?>