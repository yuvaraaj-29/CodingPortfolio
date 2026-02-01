package chatbot;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;  
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.io.*;
import java.net.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.databaseconnection;
import java.sql.*;  

public class api extends javax.swing.JFrame {
    private static final String GEMINI_API_KEY = "AIzaSyCApHFLt7OtAm0vtSJ1C4pjQN0Y58baXwI";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final String SYSTEM_INSTRUCTION = "You are Criczz GPT, expert cricket coach. Be concise, 3-4 sentences max.";
    private String loginEmail = "testuser@criczz.com";
    private int currentSessionId = -1;
    private List<Map<String, String>> conversationHistory = new ArrayList<>();
    private DefaultListModel<String> historyListModel = new DefaultListModel<>();
    private CriczzBot criczzBot = new CriczzBot();
    private SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
    private SimpleDateFormat SESSION_FORMAT = new SimpleDateFormat("MMM dd, HH:mm");
    public api(String userEmail) {
        this.loginEmail = userEmail; 
        System.out.println("👤 Logged in as: " + loginEmail);
        initComponents();
        setupUI();
        loadHistorySessions();  
        startNewSession();     
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setLocationRelativeTo(null);    
   }
    public api() {
        this("test@criczz.com");
        
    }
private void setupUI() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setLocationRelativeTo(null);
    jTextArea1.setEditable(false);
    jTextArea1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    jTextArea1.setLineWrap(true);
    jTextArea1.setWrapStyleWord(true);
    jTextArea1.setBackground(Color.WHITE);
    jTextArea1.setForeground(Color.BLACK);
    jTextField1.setBackground(Color.WHITE);
    jTextField1.setForeground(Color.BLACK);
    jList1.setModel(historyListModel);
    jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jList1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    jList1.setBackground(Color.WHITE);
    jList1.setForeground(Color.BLACK);
    jList1.addListSelectionListener(this::loadSelectedSession);
    jTextField1.addActionListener(e -> sendMessage());
    jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
    jButton1.setBackground(new Color(0, 120, 215));
    jButton1.setForeground(Color.WHITE);
    jButton2.setBackground(new Color(0, 120, 215));
    jButton2.setForeground(Color.WHITE);
    jButton3.setBackground(new Color(220, 53, 69));
    jButton3.setForeground(Color.WHITE);
    jButton4.setBackground(new Color(0, 120, 215));
    jButton4.setForeground(Color.WHITE);
    jButton5.setBackground(new Color(220, 53, 69));
    jButton5.setForeground(Color.WHITE);
}
private void startNewSession() {
        String sessionName = "Chat " + SESSION_FORMAT.format(new java.util.Date());
        currentSessionId = createNewSession(sessionName);
        conversationHistory.clear();
        criczzBot.resetState(); 
        updateChatDisplay();
        appendSystemMessage("👋 Welcome to Criczz GPT! 🏏");
        if (historyListModel.getSize() > 0) {
            jList1.setSelectedIndex(historyListModel.getSize() - 1);
        }
    }
public String callAPI_OneShot(String input) {
    try {
        URL url = new URL(GEMINI_API_URL + "?key=" + GEMINI_API_KEY);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        String jsonInputString = "{"
            + "\"contents\": ["
            + "{\"role\": \"user\", \"parts\": [{\"text\": \"" + input.replace("\"", "\\\"") + "\"}]}"
            + "]"
            + "}";       
        try (OutputStream os = con.getOutputStream()) {
            byte[] byteInput = jsonInputString.getBytes("utf-8");
            os.write(byteInput, 0, byteInput.length);
        }
        return getResponseFromConnection(con);
    } catch (Exception e) {
        e.printStackTrace();
        return "{\"error\": \"API connection failed\"}";
    }
}
public String callAPI(boolean isConversation) {
    if (!isConversation) return callAPI_OneShot("Error: Invalid mode");  
    try {
        URL url = new URL(GEMINI_API_URL + "?key=" + GEMINI_API_KEY);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        StringBuilder contentsJson = new StringBuilder();
        String escapedInstruction = SYSTEM_INSTRUCTION.replace("\"", "\\\"");
        contentsJson.append("{\"role\": \"user\", \"parts\": [{\"text\": \"").append(escapedInstruction).append("\"}]},");
        int startIndex = Math.max(0, conversationHistory.size() - 10);
        for (int i = startIndex; i < conversationHistory.size(); i++) {
            Map<String, String> message = conversationHistory.get(i);
            String role = message.get("role");
            String text = message.get("text").replace("\"", "\\\"");
            contentsJson.append("{\"role\": \"").append(role).append("\", \"parts\": [{\"text\": \"")
                        .append(text).append("\"}]},");
        }
        if (contentsJson.length() > 0 && contentsJson.charAt(contentsJson.length() - 1) == ',') {
            contentsJson.deleteCharAt(contentsJson.length() - 1);
        }
        String jsonInputString = "{ \"contents\": [" + contentsJson.toString() + "] }";
        try (OutputStream os = con.getOutputStream()) {
            byte[] byteInput = jsonInputString.getBytes("utf-8");
            os.write(byteInput, 0, byteInput.length);
        }
        return getResponseFromConnection(con);
    } catch (Exception e) {
        e.printStackTrace();
        return "{\"error\": \"API Error: " + e.getMessage() + "\"}";
    }
}
private String getResponseFromConnection(HttpURLConnection con) throws IOException {
    int responseCode = con.getResponseCode();
    InputStream is = (responseCode == 200) ? con.getInputStream() : con.getErrorStream();
    StringBuilder response = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
    }
    return responseCode == 200 ? response.toString() : "ERROR (" + responseCode + "): " + response.toString();
}
private String parseGeminiResponse(String jsonResponse) {
    if (jsonResponse.startsWith("ERROR") || jsonResponse.contains("error")) {
        return "❌ Sorry! I'm having trouble connecting. Please try again.";
    }
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);
        JsonNode textNode = rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode()) {
            return "🤔 Hmm, let me think about that differently...";
        }
        return textNode.asText().trim();
    } catch (Exception e) {
        return "⚠️ Response parsing issue. Please try again!";
    }
}
private int createNewSession(String sessionName) {
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
        con = databaseconnection.getConnection();
        if (con == null) return -1;
        con.setAutoCommit(false); 
        String sql = "INSERT INTO chat_sessions (user_email, session_name, created_at) VALUES (?, ?, NOW())";
        pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, loginEmail);
        pst.setString(2, sessionName);
        pst.executeUpdate();
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
            int sessionId = rs.getInt(1);
            historyListModel.addElement(sessionName + "  ⏰ " + DATE_FORMAT.format(new java.util.Date()));
            con.commit(); 
            return sessionId;
        }
    } catch (SQLException ex) {
        System.err.println("Session create error: " + ex.getMessage());
        if (con != null) {
            try { con.rollback(); } catch (SQLException e) { e.printStackTrace(); }
        }
    } finally {
        closeResources(rs, pst, con);
    }
    return -1;
}
private void loadHistorySessions() {
    historyListModel.clear();
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
        con = databaseconnection.getConnection();
        if (con == null) return;
        String sql = "SELECT session_name, created_at, id FROM chat_sessions WHERE user_email = ? ORDER BY created_at DESC";
        pst = con.prepareStatement(sql);
        pst.setString(1, loginEmail);
        rs = pst.executeQuery();
        while (rs.next()) {
            String name = rs.getString("session_name");
            java.sql.Timestamp ts = rs.getTimestamp("created_at");
            java.util.Date created = new java.util.Date(ts.getTime());
            String dateStr = DATE_FORMAT.format(created);
            int sessionId = rs.getInt("id");
            String displayText = name + "  ⏰ " + dateStr;
            historyListModel.addElement(displayText);
            System.out.println("📝 Added: '" + displayText + "' (ID: " + sessionId + ")");
        }
        System.out.println("✅ Loaded " + historyListModel.size() + " sessions for " + loginEmail);
    } catch (SQLException ex) {
        System.err.println("Sessions load ERROR: " + ex.getMessage());
        ex.printStackTrace();
    } finally {
        closeResources(rs, pst, con);
    }
}
private void loadSelectedSession(ListSelectionEvent e) {
    if (!e.getValueIsAdjusting() && jList1.getSelectedIndex() >= 0) {
        String selectedSession = historyListModel.getElementAt(jList1.getSelectedIndex());
        System.out.println("📋 CLICKED: " + selectedSession);
        int sessionId = getSessionIdFromName(selectedSession);
        if (sessionId != -1) {
            currentSessionId = sessionId;
            loadSessionByIndex(sessionId);
            updateChatDisplay();
            System.out.println("✅ LOADED Session ID: " + sessionId + " | Messages: " + conversationHistory.size());
        } else {
            JOptionPane.showMessageDialog(this, 
                "❌ Failed to load: " + selectedSession, 
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}   
private void loadSessionByIndex(int sessionId) {
    conversationHistory.clear();  
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
        con = databaseconnection.getConnection();
        String sql = "SELECT role, message_text FROM chat_history WHERE session_id = ? ORDER BY created_at ASC";
        pst = con.prepareStatement(sql);
        pst.setInt(1, sessionId);
        rs = pst.executeQuery();
        while (rs.next()) {
            Map<String, String> message = new HashMap<>();
            message.put("role", rs.getString("role"));
            message.put("text", rs.getString("message_text"));
            conversationHistory.add(message);
        }
        System.out.println("✅ LOADED " + conversationHistory.size() + " messages for ID: " + sessionId);
    } catch (SQLException ex) {
        System.err.println("Load ERROR: " + ex.getMessage());
    } finally {
        closeResources(rs, pst, con);
    }
}
private void loadMessagesForSession(int sessionId) {
    conversationHistory.clear();
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    try {
        con = databaseconnection.getConnection();
        String sql = "SELECT role, message_text FROM chat_history WHERE session_id = ? ORDER BY created_at ASC";
        pst = con.prepareStatement(sql);
        pst.setInt(1, sessionId);
        rs = pst.executeQuery();
        while (rs.next()) {
            Map<String, String> message = new HashMap<>();
            message.put("role", rs.getString("role"));
            message.put("text", rs.getString("message_text"));
            conversationHistory.add(message);
        }
    } catch (SQLException ex) {
        System.err.println("Messages load error: " + ex.getMessage());
    } finally {
        closeResources(rs, pst, con);  
    }
}
private void sendMessage() {
    String userInput = jTextField1.getText().trim();
    if (userInput.isEmpty()) return;
    
    saveAndDisplayMessage("user", userInput);
    jTextField1.setText("");
    if (isGreeting(userInput)) {
        if (isCricketQuery(userInput)) {
            String botResponse = criczzBot.processGreeting(userInput);
            if (botResponse != null) {
                saveAndDisplayMessage("model", botResponse);
                return;
            }
        }
        saveAndDisplayMessage("model", "👋 Welcome to Criczz GPT! 🏏 Ask me about cricket dismissals, shots, or strategies!");
        return;
    }
    if (isDismissalQuery(userInput)) {
        String botResponse = criczzBot.processRecommendation(userInput);
        if (botResponse != null) {
            saveAndDisplayMessage("model", botResponse);
            return; 
        }
    }
    if (isCricketQuery(userInput)) {
        appendSystemMessage("⏳ Thinking...");
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                String apiResponse = callGeminiAPI(userInput);
                SwingUtilities.invokeLater(() -> {
                    String currentText = jTextArea1.getText();
                    jTextArea1.setText(currentText.replaceAll("⏳ Thinking\\.\\.\\.", ""));
                    saveAndDisplayMessage("model", apiResponse);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> saveAndDisplayMessage("model", "❌ Sorry, API error. Try again!"));
            }
        }).start();
        return;
    }
    saveAndDisplayMessage("model", "🏏 CRICKET ONLY! Ask about dismissals, shots, or strategies...");
}
private boolean isGreeting(String input) {
    String lower = input.toLowerCase().trim();
    String[] greetings = {
        "hi", "hello", "hey", "good morning", "good afternoon", 
        "good evening", "greetings", "namaste", "hola"
    };
    
    for (String greeting : greetings) {
        if (lower.contains(greeting)) {
            return true;
        }
    }
    return false;
}
private String callGeminiAPI(String userInput) {
    try {
        StringBuilder context = new StringBuilder();
        context.append(SYSTEM_INSTRUCTION).append("\n\nRecent chat:\n");
        for (int i = Math.max(0, conversationHistory.size() - 5); i < conversationHistory.size(); i++) {
            Map<String, String> msg = conversationHistory.get(i);
            context.append(msg.get("role")).append(": ").append(msg.get("text")).append("\n");
        }
        context.append("\nUser: ").append(userInput);
        String rawResponse = callAPI(true);
        String formattedResponse = parseGeminiResponse(rawResponse);
        if (!formattedResponse.toLowerCase().startsWith("🏏 **")) {
            String query = userInput.toUpperCase().replace("?", "");
            formattedResponse = "🏏 **" + query + "**: " + formattedResponse.trim() + ". Ask me about another cricket topic!";
        }
        return formattedResponse;
    } catch (Exception e) {
        return "🏏 **API ERROR**: Couldn't fetch response. Try again!";
    }
}
private boolean isDismissalQuery(String input) {
    String lower = input.toLowerCase();
    if (lower.contains("out") || lower.contains("got out") || lower.contains("dismissed")) return true;
    String[] dismissalTypes = {"caught", "bowled", "lbw", "run out", "stumped", "hit wicket"};
    for (String type : dismissalTypes) {
        if (lower.contains(type)) return true;
    }
    String[] followUpDetails = {
        "yorker", "bouncer", "fuller", "full", "length",
        "edge", "keeper", "slips", "lofted", "outfield",
        "forward", "back", "line", "off", "stumps"
    };
    for (String detail : followUpDetails) {
        if (lower.contains(detail)) return true;
    }
    
    return false;
}
    
    
private boolean isCricketQuery(String input) {
    String lower = input.toLowerCase();
    if (isGreeting(input) && !containsCricketWords(lower)) {
        return false;
    }
    
    String[] cricketWords = {
        "cricket", "bat", "bowl", "pitch", "wicket", "run", "six", "four", 
        "spin", "pace", "batting", "bowling", "fielding", "cover drive", 
        "googly", "slog", "flick", "cut", "pull", "hook", "yorker", 
        "bouncer", "lbw", "caught", "bowled", "stumped", "run out"
    };
    
    for (String word : cricketWords) {
        if (lower.contains(word)) {
            return true;
        }
    }
    return false;
}
private boolean containsCricketWords(String lower) {
    String[] cricketKeywords = {
        "cricket", "bat", "bowl", "wicket", "run", "six", "four", "shot"
    };
    for (String keyword : cricketKeywords) {
        if (lower.contains(keyword)) return true;
    }
    return false;
}
    
private void saveAndDisplayMessage(String role, String text) {
    saveMessage(role, text);
    String prefix = "user".equals(role) ? "👤 You" : "🤖 Criczz GPT";
    jTextArea1.append(prefix + ": " + text + "\n\n");
    autoScroll();
}
    
    private void appendSystemMessage(String text) {
        String time = getCurrentTime();
        jTextArea1.append(String.format("[%s] 💬: %s\n", time, text));
        autoScroll();
    }
    
    private void saveMessage(String role, String text) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("text", text);
        conversationHistory.add(message);
        
        if (currentSessionId != -1) {
            Connection con = null;
            PreparedStatement pst = null;
            try {
                con = databaseconnection.getConnection();
                String sql = "INSERT INTO chat_history (session_id, role, message_text) VALUES (?, ?, ?)";
                pst = con.prepareStatement(sql);
                pst.setInt(1, currentSessionId);
                pst.setString(2, role);
                pst.setString(3, text);
                pst.executeUpdate();
            } catch (SQLException ex) {
                System.err.println("Save error: " + ex.getMessage());
            } finally {
                closeResources(null, pst, con);
            }
        }
    }
    
private void updateChatDisplay() {
    jTextArea1.setText("");  
    
    if (conversationHistory.isEmpty()) {
        jTextArea1.append("💬 No messages yet...\n");
    } else {
        for (Map<String, String> message : conversationHistory) {
            String role = message.get("role");
            String text = message.get("text");
            String prefix = role.equals("user") ? "👤 You" : "🤖 Criczz GPT";
            jTextArea1.append(prefix + ": " + text + "\n\n");  
        }
    }
    
    autoScroll();  
    System.out.println("📱 Displayed " + conversationHistory.size() + " messages");
}
    
    private String getCurrentTime() {
        return TIME_FORMAT.format(new java.util.Date());
    }
    
    private void autoScroll() {
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
    }
    
    private void closeResources(ResultSet rs, PreparedStatement pst, Connection con) {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (pst != null) pst.close(); } catch (Exception e) {}
        try { if (con != null) con.close(); } catch (Exception e) {}
    }
private void deleteSelectedSession() {
    int selectedIndex = jList1.getSelectedIndex();
    if (selectedIndex < 0) {
        JOptionPane.showMessageDialog(this, "❌ Select a session to delete!", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    String selectedSession = historyListModel.getElementAt(selectedIndex);
    int sessionId = getSessionIdFromName(selectedSession);
    if (sessionId == currentSessionId) {
        JOptionPane.showMessageDialog(this, "❌ Cannot delete the current session!", "Invalid Action", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int choice = JOptionPane.showConfirmDialog(
        this, 
        "🗑️ DELETE THIS ENTIRE CHAT?\n\n⚠️ This cannot be undone!",
        "Confirm Delete", 
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );
    
    if (choice == JOptionPane.YES_OPTION) {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            con = databaseconnection.getConnection();
            String sql = "SELECT id FROM chat_sessions WHERE user_email = ? ORDER BY created_at DESC LIMIT ?, 1";
            pst = con.prepareStatement(sql);
            pst.setString(1, loginEmail);
            pst.setInt(2, selectedIndex);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                int deleteSessionId = rs.getInt("id");
                
                // Delete messages first
                String deleteMsgSql = "DELETE FROM chat_history WHERE session_id = ?";
                try (PreparedStatement deleteMsgPst = con.prepareStatement(deleteMsgSql)) {
                    deleteMsgPst.setInt(1, deleteSessionId);
                    deleteMsgPst.executeUpdate();
                }
                
                // Delete session
                String deleteSessionSql = "DELETE FROM chat_sessions WHERE id = ?";
                try (PreparedStatement deleteSessionPst = con.prepareStatement(deleteSessionSql)) {
                    deleteSessionPst.setInt(1, deleteSessionId);
                    deleteSessionPst.executeUpdate();
                }
                
                // Refresh history
                loadHistorySessions();
                JOptionPane.showMessageDialog(this, "🗑️ Chat deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            System.err.println("Delete error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "❌ Delete failed: " + ex.getMessage());
        } finally {
            closeResources(rs, pst, con);
        }
    }
}
private int getSessionIdFromName(String displayName) {
    try {
        Connection conn = databaseconnection.getConnection();
        String sessionName = displayName.split("  ⏰ ")[0];
        System.out.println("🔍 Looking for session: '" + sessionName + "'");
        String sql = "SELECT id FROM chat_sessions WHERE session_name = ? AND user_email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            pstmt.setString(2, loginEmail);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("✅ Found ID: " + id + " for '" + sessionName + "'");
                return id;
            }
        }
        System.out.println("❌ NO ID found for: '" + sessionName + "'");
        return -1;
    } catch (Exception e) {
        System.err.println("ERROR getSessionIdFromName: " + e.getMessage());
        e.printStackTrace();
        return -1;
    }
}

public void deleteSession(int sessionId) throws SQLException {
    try (Connection conn = databaseconnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM chat_sessions WHERE id = ?")) {
        pstmt.setInt(1, sessionId);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        throw e; 
    }
}
private void saveCurrentSession() {
    if (currentSessionId == -1) {
        System.err.println("No valid session ID to save.");
        return;
    }
    Connection con = null;
    PreparedStatement pst = null;
    try {
        con = databaseconnection.getConnection();
        con.setAutoCommit(false); // Start transaction
        for (Map<String, String> message : conversationHistory) {
            String role = message.get("role");
            String text = message.get("text");
            String sql = "INSERT IGNORE INTO chat_history (session_id, role, message_text) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, currentSessionId);
                pstmt.setString(2, role);
                pstmt.setString(3, text);
                pstmt.executeUpdate();
            }
        }
        con.commit(); // Commit transaction
        System.out.println("Current session saved for session ID: " + currentSessionId);
    } catch (SQLException ex) {
        System.err.println("Save session error: " + ex.getMessage());
        if (con != null) {
            try { con.rollback(); } catch (SQLException e) { e.printStackTrace(); }
        }
        JOptionPane.showMessageDialog(this, "❌ Failed to save current session: " + ex.getMessage());
    } finally {
        closeResources(null, pst, con);
    }
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jScrollBar1 = new javax.swing.JScrollBar();
        jButton4 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 70, 1150, 610));
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 690, 700, 50));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setText("Send");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton1MouseEntered(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1260, 690, 90, 50));

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton2.setText("New Chat");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 260, 50));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Criczz GPT");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 140, -1));

        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton3.setText("Logout");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 260, 40));
        getContentPane().add(jScrollBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1470, 80, -1, 600));

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton4.setText("History");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 203, 260, 50));

        jScrollPane3.setViewportView(jList1);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 260, 410));

        jButton5.setText("Delete");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 690, 110, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        sendMessage();
    
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
int choice = JOptionPane.showConfirmDialog(
        this, 
        "🆕 Start NEW CHAT?\n💾 Current session will be SAVED!", 
        "New Session", 
        JOptionPane.YES_NO_OPTION
    );
    if (choice == JOptionPane.YES_OPTION) {
        // Save current session before starting new one
        if (currentSessionId != -1) {
            saveCurrentSession();
        }
        startNewSession(); // Start fresh session
    }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
int choice = JOptionPane.showConfirmDialog(
            this,
            "👋 Are you sure you want to logout?\n💾 All your chats are automatically saved!",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new login().setVisible(true);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseEntered
        // TODO add your handling code here:
        sendMessage();
    }//GEN-LAST:event_jButton1MouseEntered

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
//private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {                                         
    // ✅ RELOAD FRESH FROM DATABASE
    
    loadHistorySessions();
        jList1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
deleteSelectedSession();
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(chatbot.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new api().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
