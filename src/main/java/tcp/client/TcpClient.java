package tcp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public String send(String message) throws IOException {
        if (socket == null || socket.isClosed()) {
            connect();  // ← automatisch verbinden
        }
        out.println(message);
        return in.readLine();
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Fehler beim Schließen: " + e.getMessage());
        }
    }
}