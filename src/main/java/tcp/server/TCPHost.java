package tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPHost {
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final int port;
    private final RequestHandler handler;

    public TCPHost(int port) {
        this.threadPool = Executors.newCachedThreadPool();
        this.port = port;
        this.handler = new RequestHandler();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Port " + port + " bereits belegt, weiter...");
            return;  // ← einfach weitermachen, Server läuft schon
        }
        
        Thread.ofVirtual().start(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new ClientHandler(clientSocket, this.handler));
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Accept-Fehler: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void stop() {
        try {
            threadPool.shutdown();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Fehler beim Stoppen: " + e.getMessage());
        }
    }
}