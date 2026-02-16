<?php
/**
 * ============================================================================
 * PHINMA LOST AND FOUND - REST API
 * ============================================================================
 * This is the main configuration file for the API
 * 
 * 🔧 WHAT YOU NEED TO EDIT:
 * 1. Database credentials below
 * 2. That's it! Everything else is ready to use
 * ============================================================================
 */

// ============================================================================
// DATABASE CONFIGURATION
// ============================================================================
// 🔧 EDIT THESE VALUES to match your database setup

define('DB_HOST', 'localhost');           // Database server (usually 'localhost')
define('DB_NAME', 'phinma_lost_found');   // Database name (from SQL file)
define('DB_USER', 'root');                // Database username (default: 'root')
define('DB_PASS', '');                    // Database password (default: empty for XAMPP)
define('DB_CHARSET', 'utf8mb4');          // Character set (don't change)

// ============================================================================
// API CONFIGURATION
// ============================================================================
// 🔧 YOU CAN EDIT THESE if needed

define('API_KEY', 'phinma_secret_key_2024');  // API security key
define('UPLOAD_DIR', 'uploads/');              // Where to store uploaded images

// ============================================================================
// ERROR REPORTING (for development)
// ============================================================================
// 🔧 SET TO FALSE in production

error_reporting(E_ALL);
ini_set('display_errors', 1);

// ============================================================================
// CORS HEADERS (Allow Android app to connect)
// ============================================================================

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Content-Type: application/json');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// ============================================================================
// DATABASE CONNECTION CLASS
// ============================================================================

class Database {
    private $conn;
    
    public function getConnection() {
        $this->conn = null;
        
        try {
            $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
            $this->conn = new PDO($dsn, DB_USER, DB_PASS);
            $this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        } catch(PDOException $e) {
            echo json_encode([
                'success' => false,
                'message' => 'Database connection failed: ' . $e->getMessage()
            ]);
        }
        
        return $this->conn;
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Send JSON response
 */
function sendResponse($success, $message, $data = null) {
    $response = [
        'success' => $success,
        'message' => $message
    ];
    
    if ($data !== null) {
        $response['data'] = $data;
    }
    
    echo json_encode($response);
    exit();
}

/**
 * Validate required fields
 */
function validateRequired($data, $fields) {
    foreach ($fields as $field) {
        if (!isset($data[$field]) || empty(trim($data[$field]))) {
            sendResponse(false, "Field '$field' is required");
        }
    }
}

/**
 * Get JSON input
 */
function getJsonInput() {
    $json = file_get_contents('php://input');
    return json_decode($json, true);
}

?>
