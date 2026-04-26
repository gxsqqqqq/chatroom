package com.chat.server;

import com.chat.common.Message;
import com.chat.common.DatabaseUtil;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler implements Runnable {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private Socket socket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private boolean connected;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.connected = true;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Failed to initialize input/output streams: " + e.getMessage());
            close();
        }
    }

    @Override
    public void run() {
        try {
            while (connected) {
                Message message = (Message) in.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                System.err.println("Error reading message from client: " + e.getMessage());
            }
        } finally {
            close();
        }
    }

    private void processMessage(Message message) {
        switch (message.getType()) {
            case LOGIN:
                handleLogin(message);
                break;
            case BROADCAST:
                handleBroadcast(message);
                break;
            case PRIVATE:
                handlePrivate(message);
                break;
            case LOGOUT:
                handleLogout();
                break;
        }
    }

    private void handleLogin(Message message) {
        this.username = message.getSender();
        server.addOnlineUser(username);
        System.out.println("User " + username + " logged in");
    }

    private void handleBroadcast(Message message) {
        executorService.submit(() -> {
            String sql = "INSERT INTO chat_history (name, text, time) VALUES (?, ?, ?)";
            DatabaseUtil.executeUpdate(sql, message.getSender(), message.getContent(), message.getTimestamp());
        });

        server.broadcastMessage(message);
    }

    private void handlePrivate(Message message) {
        executorService.submit(() -> {
            String sql = "INSERT INTO chat_history (name, text, time) VALUES (?, ?, ?)";
            DatabaseUtil.executeUpdate(sql, message.getSender(), "[Private to " + message.getRecipient() + "] " + message.getContent(), message.getTimestamp());
        });

        server.sendPrivateMessage(message);
        sendMessage(message);
    }

    private void handleLogout() {
        server.removeOnlineUser(username);
        connected = false;
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send message to client: " + e.getMessage());
            close();
        }
    }

    public void close() {
        if (connected) {
            connected = false;

            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Failed to close resources: " + e.getMessage());
            }

            server.removeClientHandler(this);
            System.out.println("Client connection closed: " + (username != null ? username : "unknown"));
        }
    }

    public String getUsername() {
        return username;
    }
}