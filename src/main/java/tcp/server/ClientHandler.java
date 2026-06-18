package tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RequestHandler requestHandler;

    public ClientHandler(Socket socket , RequestHandler handler) {
        this.socket = socket;
        this.requestHandler = handler; // würde hier auch ein handler reichen, statt für jeden Client eine neue Instanz zu erstellen?
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Empfangen: " + line);
                try{
                String response = requestHandler.callMethodRemotely(line);
                out.println(response);
                }
            catch (Exception e) {
                System.err.println(e.getMessage());
                out.println("Error: " + e.getMessage()); // ← Antwort zurückschicken!
            }
            }
        } catch (IOException e) {
            System.err.println("Client-Fehler: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Fehler beim Schließen: " + e.getMessage());
            }
        }
    }
}