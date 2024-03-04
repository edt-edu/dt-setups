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
    LinkedList<String> jsonList;

    public JSONServerSend(int port, LinkedList<String> jsonList) throws IOException {
        server = new ServerSocket(port);
        connectToClient();
        this.jsonList = jsonList;
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

    public static void main(String[] args) throws Exception {
        JSONServerSend serverE = new JSONServerSend(6005, new LinkedList<>());
        fakeJsonListGenerator fakeL = new fakeJsonListGenerator();
        long now = System.currentTimeMillis() + 50000;
        while(System.currentTimeMillis() < now){
            if(!serverE.jsonList.isEmpty()){
                serverE.sendData(serverE.jsonList.removeFirst());
            }
            if(serverE.jsonList.isEmpty()){
                Thread.sleep(10000);
                serverE.jsonList.add(fakeL.call());
            }
        }
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

