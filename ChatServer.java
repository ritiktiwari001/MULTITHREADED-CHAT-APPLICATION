import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ChatServer
 *
 * A multi-client chat server using Java sockets and multithreading.
 * This server accepts multiple client connections, handles each in its own thread,
 * and broadcasts messages to all connected clients in real time.
 */
public class ChatServer {
    private static final int PORT = 12345;
    // Set of all connected clients
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Accept new client connection
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Create handler for client and start new thread
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     *
     * @param message       the message to send
     * @param excludeClient the client to exclude from broadcast
     */
    public static void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clientHandlers) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Removes a client from the set of active clients.
     *
     * @param client the client handler to remove
     */
    public static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
        System.out.println("Client disconnected: " + client.getClientName());
    }

    /**
     * ClientHandler
     *
     * A runnable class that manages communication with a single client.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Initialize input and output streams
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Ask client for their name
                out.println("Enter your name:");
                clientName = in.readLine();

                System.out.println(clientName + " has joined the chat.");
                broadcast(clientName + " has joined the chat.", this);

                // Handle messages from this client
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    if (clientMessage.equalsIgnoreCase("exit")) {
                        break;
                    }
                    System.out.println(clientName + ": " + clientMessage);
                    broadcast(clientName + ": " + clientMessage, this);
                }
            } catch (IOException e) {
                System.out.println("Error in ClientHandler: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Couldn't close a socket: " + e.getMessage());
                }
                removeClient(this);
                broadcast(clientName + " has left the chat.", this);
            }
        }

        /**
         * Sends a message to this client.
         *
         * @param message the message to send
         */
        public void sendMessage(String message) {
            out.println(message);
        }

        /**
         * Returns the client's name.
         *
         * @return client name
         */
        public String getClientName() {
            return clientName;
        }
    }
}
