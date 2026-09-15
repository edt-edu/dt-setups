package JSON.Server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/**
 * sendet JSON strings
 */
public class JSONServerSend implements Runnable {

    private final ServerSocket server;
    Socket socket = null;
    LinkedList<String> jsonList = new LinkedList<>();

    public JSONServerSend(int port) throws IOException {
        server = new ServerSocket(port);
        connectToClient();
    }

    private void connectToClient() {
        try {
            System.out.println("running");
            this.socket = server.accept();
            System.out.println("Socket connected: " + socket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(String s) throws IOException {
        PrintStream dataToSend = new PrintStream(socket.getOutputStream());
        dataToSend.println(s);
        dataToSend.flush();
        System.out.println("sent: " + s);
        System.out.println(System.currentTimeMillis());
    }

    public void run() {
        while(true) {
            if (!jsonList.isEmpty()) {
                try {
                    sendData(jsonList.removeFirst());
                    System.out.println("sent data");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}