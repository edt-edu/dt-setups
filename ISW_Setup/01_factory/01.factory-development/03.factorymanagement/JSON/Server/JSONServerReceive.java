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
    LinkedList<String> jsonList = new LinkedList<>();

    public JSONServerReceive(int port) throws IOException {
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
    public LinkedList<String> getJsonList(){
        return this.jsonList;
    }

    public void receiveData() throws IOException {
        BufferedReader inputData = new BufferedReader(new InputStreamReader(socket
                .getInputStream()));
            while(inputData.ready()){
                //make sure feedback strings end with \n in python so that they can be separated
                String s = inputData.readLine();
                this.jsonList.add(s);
                //System.out.println(System.currentTimeMillis());
            }
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
