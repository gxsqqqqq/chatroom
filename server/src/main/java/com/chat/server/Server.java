package com.chat.server;

import com.chat.common.Message;
import com.chat.common.DatabaseUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers;
    private List<String> onlineUsers;
    private boolean running;

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientHandlers = new CopyOnWriteArrayList<>();
            onlineUsers = new CopyOnWriteArrayList<>();
            running = true;
            System.out.println("Server started, listening on port: " + port);
            start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }

    private void start() {
        new Thread(this::consoleCommand).start();

        while (running) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientHandlers.add(clientHandler);

                new Thread(clientHandler).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    private void consoleCommand() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            System.out.print("Server> ");
            String command = scanner.nextLine().trim();

            switch (command) {
                case "list":
                    listOnlineUsers();
                    break;
                case "stop":
                    stop();
                    break;
                case "help":
                    System.out.println("Commands: list - show online users, stop - shutdown server, help - show help");
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for available commands.");
            }
        }
        scanner.close();
    }

    private void listOnlineUsers() {
        System.out.println("Online users (" + onlineUsers.size() + "):");
        for (String user : onlineUsers) {
            System.out.println("- " + user);
        }
    }

    public void stop() {
        running = false;
        System.out.println("Stopping server...");

        for (ClientHandler handler : clientHandlers) {
            handler.close();
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close server socket: " + e.getMessage());
        }

        DatabaseUtil.shutdown();

        System.out.println("Server stopped");
        System.exit(0);
    }

    public void addOnlineUser(String username) {
        onlineUsers.add(username);
        broadcastUserList();

        String message = username + " joined the chat room";
        broadcastMessage(new Message("SERVER", "", message, Message.Type.BROADCAST));

        System.out.println("User " + username + " joined, online users: " + onlineUsers.size());
    }

    public void removeOnlineUser(String username) {
        onlineUsers.remove(username);
        broadcastUserList();

        String message = username + " left the chat room";
        broadcastMessage(new Message("SERVER", "", message, Message.Type.BROADCAST));

        System.out.println("User " + username + " left, online users: " + onlineUsers.size());
    }

    public void broadcastUserList() {
        Message userListMessage = new Message(new ArrayList<>(onlineUsers));
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(userListMessage);
        }
    }

    public void broadcastMessage(Message message) {
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(message);
        }
    }

    public void sendPrivateMessage(Message message) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.getUsername().equals(message.getRecipient())) {
                handler.sendMessage(message);
                break;
            }
        }
    }

    public void removeClientHandler(ClientHandler handler) {
        clientHandlers.remove(handler);
        if (handler.getUsername() != null) {
            removeOnlineUser(handler.getUsername());
        }
    }

    public static void main(String[] args) {
        int port = 8888;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number, using default: " + port);
            }
        }

        new Server(port);
    }
}