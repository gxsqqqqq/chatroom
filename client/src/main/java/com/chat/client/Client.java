package com.chat.client;

import com.chat.common.Message;
import com.chat.common.DatabaseUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private JFrame loginFrame;
    private JFrame mainFrame;
    private JTextArea publicChatArea;
    private JTextField messageField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private SimpleDateFormat dateFormat;
    private Map<String, JFrame> privateChatFrames;
    private Map<String, JTextArea> privateChatAreas;

    public Client() {
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        privateChatFrames = new HashMap<>();
        privateChatAreas = new HashMap<>();
        createLoginFrame();
    }

    private void createLoginFrame() {
        loginFrame = new JFrame("Login - Java Chat");
        loginFrame.setSize(400, 250);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Java Multi-Client Chat System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 18));
        titlePanel.add(titleLabel);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("SimHei", Font.PLAIN, 14));
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("SimHei", Font.PLAIN, 14));

        JLabel serverLabel = new JLabel("Server Address:");
        serverLabel.setFont(new Font("SimHei", Font.PLAIN, 14));
        JTextField serverField = new JTextField("localhost");
        serverField.setFont(new Font("SimHei", Font.PLAIN, 14));

        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("SimHei", Font.PLAIN, 14));
        JTextField portField = new JTextField("8888");
        portField.setFont(new Font("SimHei", Font.PLAIN, 14));

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(serverLabel);
        formPanel.add(serverField);
        formPanel.add(portLabel);
        formPanel.add(portField);

        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("SimHei", Font.BOLD, 14));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(loginFrame, "Please enter username", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String server = serverField.getText().trim();
                int port;
                try {
                    port = Integer.parseInt(portField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (connectToServer(server, port)) {
                    loginFrame.dispose();
                    createMainFrame();
                    startListening();
                    loadHistoryMessages();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Failed to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(loginButton);

        loginFrame.add(titlePanel, BorderLayout.NORTH);
        loginFrame.add(formPanel, BorderLayout.CENTER);
        loginFrame.add(buttonPanel, BorderLayout.SOUTH);
        loginFrame.setVisible(true);
    }

    private boolean connectToServer(String server, int port) {
        try {
            socket = new Socket(server, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Message loginMessage = new Message(username, "", "", Message.Type.LOGIN);
            out.writeObject(loginMessage);
            out.flush();

            System.out.println("Connected to server: " + server + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    private void createMainFrame() {
        mainFrame = new JFrame("Chat - " + username);
        mainFrame.setSize(900, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel userListLabel = new JLabel("Online Users", SwingConstants.CENTER);
        userListLabel.setFont(new Font("SimHei", Font.BOLD, 14));
        userListLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setFont(new Font("SimHei", Font.PLAIN, 12));
        
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(username)) {
                        openPrivateChat(selectedUser);
                    }
                }
            }
        });
        
        JScrollPane userListScrollPane = new JScrollPane(userList);
        userListScrollPane.setPreferredSize(new Dimension(180, 0));
        leftPanel.add(userListLabel, BorderLayout.NORTH);
        leftPanel.add(userListScrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel chatTitleLabel = new JLabel("Public Chat Room - All Users", SwingConstants.CENTER);
        chatTitleLabel.setFont(new Font("SimHei", Font.BOLD, 16));
        chatTitleLabel.setBackground(new Color(135, 206, 235));
        chatTitleLabel.setOpaque(true);
        chatTitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        publicChatArea = new JTextArea();
        publicChatArea.setEditable(false);
        publicChatArea.setFont(new Font("SimHei", Font.PLAIN, 13));
        publicChatArea.setLineWrap(true);
        publicChatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(publicChatArea);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messageField = new JTextField();
        messageField.setFont(new Font("SimHei", Font.PLAIN, 14));
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPublicMessage();
            }
        });

        JButton sendButton = new JButton("Send Public");
        sendButton.setFont(new Font("SimHei", Font.BOLD, 12));
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPublicMessage();
            }
        });

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        rightPanel.add(chatTitleLabel, BorderLayout.NORTH);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(5);

        mainFrame.add(splitPane);
        mainFrame.setVisible(true);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendLogoutMessage();
                close();
            }
        });
    }

    private void openPrivateChat(String targetUser) {
        if (privateChatFrames.containsKey(targetUser)) {
            JFrame frame = privateChatFrames.get(targetUser);
            frame.toFront();
            return;
        }

        JFrame chatFrame = new JFrame("Chat with " + targetUser);
        chatFrame.setSize(600, 500);
        chatFrame.setLocationRelativeTo(mainFrame);
        chatFrame.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Private Chat with " + targetUser, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 14));
        titleLabel.setBackground(new Color(255, 182, 193));
        titleLabel.setOpaque(true);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SimHei", Font.PLAIN, 13));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField privateMessageField = new JTextField();
        privateMessageField.setFont(new Font("SimHei", Font.PLAIN, 14));
        privateMessageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPrivateMessage(targetUser, privateMessageField.getText().trim());
                privateMessageField.setText("");
            }
        });

        JButton sendPrivateButton = new JButton("Send");
        sendPrivateButton.setFont(new Font("SimHei", Font.BOLD, 12));
        sendPrivateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPrivateMessage(targetUser, privateMessageField.getText().trim());
                privateMessageField.setText("");
            }
        });

        inputPanel.add(privateMessageField, BorderLayout.CENTER);
        inputPanel.add(sendPrivateButton, BorderLayout.EAST);

        chatFrame.add(titleLabel, BorderLayout.NORTH);
        chatFrame.add(chatScrollPane, BorderLayout.CENTER);
        chatFrame.add(inputPanel, BorderLayout.SOUTH);

        chatFrame.setVisible(true);

        privateChatFrames.put(targetUser, chatFrame);
        privateChatAreas.put(targetUser, chatArea);

        chatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                privateChatFrames.remove(targetUser);
                privateChatAreas.remove(targetUser);
            }
        });
    }

    private void sendPublicMessage() {
        String content = messageField.getText().trim();
        if (!content.isEmpty()) {
            try {
                Message message = new Message(username, "", content, Message.Type.BROADCAST);
                out.writeObject(message);
                out.flush();
                messageField.setText("");
                displayPublicMessage("You", content);
            } catch (IOException e) {
                System.err.println("Failed to send broadcast message: " + e.getMessage());
            }
        }
    }

    private void sendPrivateMessage(String recipient, String content) {
        if (!content.isEmpty()) {
            try {
                Message message = new Message(username, recipient, content, Message.Type.PRIVATE);
                out.writeObject(message);
                out.flush();
                
                if (privateChatAreas.containsKey(recipient)) {
                    JTextArea chatArea = privateChatAreas.get(recipient);
                    displayPrivateMessage(chatArea, "You", content);
                }
            } catch (IOException e) {
                System.err.println("Failed to send private message: " + e.getMessage());
            }
        }
    }

    private void sendLogoutMessage() {
        try {
            Message logoutMessage = new Message(username, "", "", Message.Type.LOGOUT);
            out.writeObject(logoutMessage);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send logout message: " + e.getMessage());
        }
    }

    private void startListening() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Message message = (Message) in.readObject();
                        processMessage(message);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Connection closed by server: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(mainFrame, "Connection closed by server", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    });
                }
            }
        }).start();
    }

    private void processMessage(Message message) {
        switch (message.getType()) {
            case BROADCAST:
                displayPublicMessage(message.getSender(), message.getContent());
                break;
            case PRIVATE:
                displayPrivateMessageFromSender(message.getSender(), message.getContent());
                break;
            case USER_LIST:
                updateUserList(message.getUserList());
                break;
        }
    }

    private void displayPublicMessage(String sender, String content) {
        String time = dateFormat.format(new Date());
        String displayText = "[" + time + "] " + sender + ": " + content + "\n";

        SwingUtilities.invokeLater(() -> {
            publicChatArea.append(displayText);
            publicChatArea.setCaretPosition(publicChatArea.getDocument().getLength());
        });
    }

    private void displayPrivateMessage(JTextArea chatArea, String sender, String content) {
        String time = dateFormat.format(new Date());
        String displayText = "[" + time + "] " + sender + ": " + content + "\n";

        SwingUtilities.invokeLater(() -> {
            chatArea.append(displayText);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void displayPrivateMessageFromSender(String sender, String content) {
        if (privateChatAreas.containsKey(sender)) {
            JTextArea chatArea = privateChatAreas.get(sender);
            displayPrivateMessage(chatArea, sender, content);
            
            JFrame frame = privateChatFrames.get(sender);
            if (frame != null) {
                frame.toFront();
            }
        } else {
            openPrivateChat(sender);
            if (privateChatAreas.containsKey(sender)) {
                JTextArea chatArea = privateChatAreas.get(sender);
                displayPrivateMessage(chatArea, sender, content);
            }
        }
    }

    private void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }

    private void loadHistoryMessages() {
        new Thread(() -> {
            String sql = "SELECT name, text, time FROM chat_history ORDER BY time DESC LIMIT 50";
            List<String[]> results = DatabaseUtil.executeQuery(sql);
            try {
                java.util.ArrayList<String> messages = new java.util.ArrayList<>();
                for (String[] row : results) {
                    String name = row[0];
                    String text = row[1];
                    long time = Long.parseLong(row[2]);
                    String timeStr = dateFormat.format(new Date(time));
                    messages.add("[" + timeStr + "] " + name + ": " + text);
                }

                final List<String> finalMessages = messages;
                SwingUtilities.invokeLater(() -> {
                    publicChatArea.append("=== History Messages ===\n");
                    for (int i = finalMessages.size() - 1; i >= 0; i--) {
                        publicChatArea.append(finalMessages.get(i) + "\n");
                    }
                    publicChatArea.append("=== End of History ===\n\n");
                    publicChatArea.setCaretPosition(publicChatArea.getDocument().getLength());
                });
            } catch (Exception e) {
                System.err.println("Failed to load history messages: " + e.getMessage());
            }
        }).start();
    }

    private void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }
}