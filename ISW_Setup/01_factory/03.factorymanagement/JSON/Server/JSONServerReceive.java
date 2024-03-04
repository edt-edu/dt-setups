package JSON.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Empfängt JSON Strings
 */
public class JSONServerReceive implements Runnable {

    private final ServerSocket server;
    Socket socket = null;
    LinkedList<String> jsonList;

    public JSONServerReceive(int port, LinkedList<String> jsonList) throws IOException {
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

    public void receiveData() throws IOException {
        BufferedReader inputData = new BufferedReader(new InputStreamReader(socket
                .getInputStream()));
            while(inputData.ready()){
                //make sure feedback strings end with \n in python so that they can be separated
                String s = inputData.readLine();
                this.jsonList.add(s);
                System.out.println(s);
                System.out.println(System.currentTimeMillis());
            }
    }

    public static void main(String[] args) throws Exception {
        JSONServerReceive serverE = new JSONServerReceive(6014, new LinkedList<>());
        long now = System.currentTimeMillis() + 50000;
        while(System.currentTimeMillis() < now){
            serverE.receiveData();
        }
        System.out.println("finished");
    }

    public void run(){
        while(true){
            try {
                receiveData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

