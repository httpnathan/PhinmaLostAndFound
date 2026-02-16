<?php
/**
 * ============================================================================
 * MESSAGES API ENDPOINTS
 * ============================================================================
 * This file handles chat messages between users
 * 
 * 🔧 TO ADD MESSAGE FEATURES:
 * - Add fields to send/receive queries
 * - Implement read receipts
 * - Add message types (text, image, etc.)
 * ============================================================================
 */

require_once 'config.php';

$database = new Database();
$db = $database->getConnection();

$request_method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

// ============================================================================
// SEND MESSAGE
// ============================================================================

if ($action === 'send' && $request_method === 'POST') {
    $data = getJsonInput();
    
    validateRequired($data, ['sender_id', 'receiver_id', 'message_text']);
    
    $sender_id = $data['sender_id'];
    $receiver_id = $data['receiver_id'];
    $message_text = trim($data['message_text']);
    
    // Insert message
    $insert_query = "INSERT INTO messages (sender_id, receiver_id, message_text) 
                     VALUES (?, ?, ?)";
    
    $stmt = $db->prepare($insert_query);
    
    try {
        $stmt->execute([$sender_id, $receiver_id, $message_text]);
        $message_id = $db->lastInsertId();
        
        sendResponse(true, 'Message sent', ['message_id' => $message_id]);
    } catch (PDOException $e) {
        sendResponse(false, 'Failed to send message: ' . $e->getMessage());
    }
}

// ============================================================================
// GET CONVERSATION
// ============================================================================

elseif ($action === 'get_conversation' && $request_method === 'GET') {
    if (!isset($_GET['user_id']) || !isset($_GET['other_user_id'])) {
        sendResponse(false, 'Both user IDs are required');
    }
    
    $user_id = $_GET['user_id'];
    $other_user_id = $_GET['other_user_id'];
    
    $query = "SELECT m.*, 
              u1.first_name as sender_first_name, u1.last_name as sender_last_name,
              u2.first_name as receiver_first_name, u2.last_name as receiver_last_name
              FROM messages m
              JOIN users u1 ON m.sender_id = u1.user_id
              JOIN users u2 ON m.receiver_id = u2.user_id
              WHERE (m.sender_id = ? AND m.receiver_id = ?)
              OR (m.sender_id = ? AND m.receiver_id = ?)
              ORDER BY m.sent_at ASC";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$user_id, $other_user_id, $other_user_id, $user_id]);
    
    $messages = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    sendResponse(true, 'Conversation retrieved', $messages);
}

// ============================================================================
// GET ALL CHATS (Chat List)
// ============================================================================

elseif ($action === 'get_chats' && $request_method === 'GET') {
    if (!isset($_GET['user_id'])) {
        sendResponse(false, 'User ID is required');
    }
    
    $user_id = $_GET['user_id'];
    
    // Get latest message with each user
    $query = "SELECT DISTINCT
              CASE 
                WHEN m.sender_id = ? THEN m.receiver_id
                ELSE m.sender_id
              END as other_user_id,
              u.first_name, u.last_name, u.email,
              (SELECT message_text FROM messages 
               WHERE (sender_id = ? AND receiver_id = other_user_id)
               OR (sender_id = other_user_id AND receiver_id = ?)
               ORDER BY sent_at DESC LIMIT 1) as last_message,
              (SELECT sent_at FROM messages 
               WHERE (sender_id = ? AND receiver_id = other_user_id)
               OR (sender_id = other_user_id AND receiver_id = ?)
               ORDER BY sent_at DESC LIMIT 1) as last_message_time,
              (SELECT COUNT(*) FROM messages 
               WHERE receiver_id = ? AND sender_id = other_user_id 
               AND is_read = FALSE) as unread_count
              FROM messages m
              JOIN users u ON u.user_id = CASE 
                WHEN m.sender_id = ? THEN m.receiver_id
                ELSE m.sender_id
              END
              WHERE m.sender_id = ? OR m.receiver_id = ?
              ORDER BY last_message_time DESC";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$user_id, $user_id, $user_id, $user_id, $user_id, $user_id, $user_id, $user_id, $user_id]);
    
    $chats = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    sendResponse(true, 'Chats retrieved', $chats);
}

// ============================================================================
// MARK AS READ
// ============================================================================

elseif ($action === 'mark_read' && $request_method === 'POST') {
    $data = getJsonInput();
    
    validateRequired($data, ['user_id', 'other_user_id']);
    
    $user_id = $data['user_id'];
    $other_user_id = $data['other_user_id'];
    
    $update_query = "UPDATE messages 
                     SET is_read = TRUE, read_at = NOW() 
                     WHERE receiver_id = ? AND sender_id = ? AND is_read = FALSE";
    
    $stmt = $db->prepare($update_query);
    
    try {
        $stmt->execute([$user_id, $other_user_id]);
        sendResponse(true, 'Messages marked as read');
    } catch (PDOException $e) {
        sendResponse(false, 'Failed to mark as read: ' . $e->getMessage());
    }
}

// ============================================================================
// INVALID ACTION
// ============================================================================

else {
    sendResponse(false, 'Invalid action or request method');
}

?>
