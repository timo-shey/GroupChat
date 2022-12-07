package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static chat.ChatColour.*;

public class Server implements Runnable{

    public static void main(String[] args) {
        System.out.println(ANSI_BLUE + "Starting up the Chat Server...");
        Server server = new Server();
        System.out.println(ANSI_BLUE + "Chat Server is up and running!");
        server.run();
    }

    public ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                System.out.println(ANSI_GREEN + "A new client has connected.");
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) { shutdown(); }
    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) server.close();
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) { /*ignore*/ }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ConnectionHandler(Socket client) { this.client = client; }


        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println(ANSI_BLUE + "Enter your username: ");
                username = in.readLine();
                System.out.println(ANSI_BLUE + username + " connected!");
                broadcast(ANSI_YELLOW + username + " joined the chat!");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/rename ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(ANSI_PURPLE + username + " renamed themselves to " + messageSplit[1]);
                            System.out.println(ANSI_PURPLE + username + " renamed themselves to " + messageSplit[1]);
                            username = messageSplit[1];
                            out.println(ANSI_PURPLE + "Successfully changed username to " + username);
                        } else out.println(ANSI_RED + "No username was provided!");
                    } else if (message.startsWith("/exit")) {
                        broadcast(ANSI_RED + username + " left the chat");
                        shutdown();
                    } else broadcast(ANSI_CYAN + username + ": " + message);
                }
            } catch (IOException e) { shutdown(); }
        }

        public void broadcast(String message) {
            for (ConnectionHandler ch : connections) {
                if (ch.username != null) {
                    if (!ch.username.equals(username)) ch.sendMessage(message);
                }
            }
        }

        public void sendMessage(String message) { out.println(message); }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) client.close();
            } catch (IOException e) { /*ignore*/ }
        }
    }
}
