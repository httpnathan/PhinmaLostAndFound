<?php
/**
 * ============================================================================
 * PHINMA LOST AND FOUND - REST API
 * ============================================================================
 * This is the main configuration file for the API
 */

// Start output buffering to catch any accidental output (like warnings) before JSON
ob_start();

// ============================================================================
// DATABASE CONFIGURATION
// ============================================================================
define('DB_HOST', 'localhost');
define('DB_NAME', 'phinma_lost_found');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_CHARSET', 'utf8mb4');

// ============================================================================
// API CONFIGURATION
// ============================================================================
define('API_KEY', 'phinma_secret_key_2024');
define('UPLOAD_DIR', 'uploads/');

// Error reporting - set to 0 in production
error_reporting(E_ALL);
ini_set('display_errors', 1);

// CORS Headers
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

class Database {
    private $conn;
    public function getConnection() {
        $this->conn = null;
        try {
            $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
            $this->conn = new PDO($dsn, DB_USER, DB_PASS);
            $this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        } catch(PDOException $e) {
            sendResponse(false, 'Database connection failed: ' . $e->getMessage());
        }
        return $this->conn;
    }
}

function sendResponse($success, $message, $data = null) {
    // Clear any previous output (like PHP warnings)
    if (ob_get_length()) ob_clean();

    $response = [
        'success' => $success,
        'message' => $message
    ];
    
    if ($data !== null) {
        $action = isset($_GET['action']) ? $_GET['action'] : '';
        // If data is the posts array, we wrap it in a 'posts' key for the HomeActivity
        if (in_array($action, ['get_all', 'get_user_posts', 'search'])) {
            $response['posts'] = $data;
        } else if ($action === 'get_one') {
            $response['post'] = $data;
        } else {
            $response['data'] = $data;
        }
    }
    
    echo json_encode($response);
    exit();
}

function validateRequired($data, $fields) {
    foreach ($fields as $field) {
        if (!isset($data[$field]) || (is_string($data[$field]) && empty(trim($data[$field])))) {
            sendResponse(false, "Field '$field' is required");
        }
    }
}

function getJsonInput() {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    return !empty($data) ? $data : $_POST;
}
?>