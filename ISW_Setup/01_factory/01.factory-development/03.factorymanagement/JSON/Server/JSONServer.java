package JSON.Server;

import JSON.Exceptions.JsonApiExcpetion;
import JSON.Parsing.JSONInput;
import JSON.Parsing.JSONParser;
import JSON.Parsing.JSONReadable;
import JSON.Test.JSONBuilderTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * for testing
 */
public class JSONServer {

    private final ServerSocket server;
    Socket socket = null;

    public JSONServer(int port) throws IOException {
        server = new ServerSocket(port);
        //socket.bind(new InetSocketAddress("169.254.106.212", 6000));
    }

    public void connectToClient() {
        while (true) {
            try {
                System.out.println("running");
                socket = server.accept();
                //socket.bind(new InetSocketAddress("169.254.106.213", 6000));
                System.out.println("Socket connected: " + socket.isConnected());
                sendDataTest(socket);
                //receiveDataTest(socket);
            } catch (IOException | JsonApiExcpetion e) {
                e.printStackTrace();
            } finally {
                if (socket != null)
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    private void sendDataTest(Socket socket) throws IOException, JsonApiExcpetion { //(Socket socket)
        PrintStream dataToSend = new PrintStream(socket.getOutputStream());
        JSONBuilderTest test = new JSONBuilderTest();
        //dataToSend.println(test.testMethode());
        dataToSend.flush();

        System.out.println("sent");
    }

    private void receiveDataTest(Socket socket) throws IOException {
        BufferedReader inputData = new BufferedReader(new InputStreamReader(socket
                .getInputStream()));
        if(inputData.ready()) {
            while(inputData.ready()){
                String s = inputData.readLine();
                System.out.println(s);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        JSONServer server = new JSONServer(6001);
        server.connectToClient();

    }
}


