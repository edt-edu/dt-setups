package JSON.Server;

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
            } catch (IOException e) {
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

    private void sendDataTest(Socket socket) throws IOException { //(Socket socket)
        PrintStream dataToSend = new PrintStream(socket.getOutputStream());
        String t = "{\n" +
                "  \"topicName\" : \"4.5-Store\",\n" +
                "  \"timestamp\" : 1688031412.685000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"COMMAND\",\n" +
                "    \"type\" : \"WAREHOUSE\",\n" +
                "    \"name\" : \"STORE\",\n" +
                "    \"parameters\" : [ {\n" +
                "      \"passableType\" : \"BOXNUMBER\",\n" +
                "      \"passable\" : \"BOX1\"\n" +
                "    } ],\n" +
                "    \"outputId\" : 2\n" +
                "  }\n" +
                "}";
        String s = "{\n" +
                "  \"topicName\" : \"4.1-Grip\",\n" +
                "  \"timestamp\" : 1687952422.090000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"COMMAND\",\n" +
                "    \"type\" : \"GRIPPER\",\n" +
                "    \"name\" : \"MOVE\",\n" +
                "    \"parameters\" : [ {\n" +
                "      \"passableType\" : \"POSITIONPARAMETERTHREED\",\n" +
                "      \"passable\" : {\n" +
                "        \"meaning\" : \"START\",\n" +
                "        \"vertical\" : 1000,\n" +
                "        \"rot\" : 500,\n" +
                "        \"horizontal\" : 5\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"passableType\" : \"POSITIONPARAMETERTHREED\",\n" +
                "      \"passable\" : {\n" +
                "        \"meaning\" : \"END\",\n" +
                "        \"vertical\" : 200,\n" +
                "        \"rot\" : 200,\n" +
                "        \"horizontal\" : 4\n" +
                "      }\n" +
                "    } ],\n" +
                "    \"outputId\" : 1\n" +
                "  }\n" +
                "}";
        String z = "{\n" +
                "  \"topicName\" : \"4.4-Vac\",\n" +
                "  \"timestamp\" : 1687952422.090000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"COMMAND\",\n" +
                "    \"type\" : \"VACUUM\",\n" +
                "    \"name\" : \"MOVE\",\n" +
                "    \"parameters\" : [ {\n" +
                "      \"passableType\" : \"POSITIONPARAMETERTHREED\",\n" +
                "      \"passable\" : {\n" +
                "        \"meaning\" : \"START\",\n" +
                "        \"vertical\" : 1000,\n" +
                "        \"rot\" : 500,\n" +
                "        \"horizontal\" : 105\n" +
                "      }\n" +
                "    }, {\n" +
                "      \"passableType\" : \"POSITIONPARAMETERTHREED\",\n" +
                "      \"passable\" : {\n" +
                "        \"meaning\" : \"END\",\n" +
                "        \"vertical\" : 200,\n" +
                "        \"rot\" : 200,\n" +
                "        \"horizontal\" : 24\n" +
                "      }\n" +
                "    } ],\n" +
                "    \"outputId\" : 1\n" +
                "  }\n" +
                "}";
        String v = "{\n" +
                "  \"topicName\" : \"1.4-Sort\",\n" +
                "  \"timestamp\" : 1688039459.574000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"COMMAND\",\n" +
                "    \"type\" : \"SORTING\",\n" +
                "    \"name\" : \"EJECT\",\n" +
                "    \"parameters\" : [ {\n" +
                "      \"passableType\" : \"COLOUR\",\n" +
                "      \"passable\" : \"RED\"\n" +
                "    } ],\n" +
                "    \"outputId\" : 2\n" +
                "  }\n" +
                "}";
        String w = "{\n" +
                "  \"topicName\" : \"1.7-Indexed\",\n" +
                "  \"timestamp\" : 1688040551.032000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"COMMAND\",\n" +
                "    \"type\" : \"INDEXEDLINE\",\n" +
                "    \"name\" : \"STIRR\",\n" +
                "    \"parameters\" : [ {\n" +
                "      \"passableType\" : \"NUMBERNATURAL\",\n" +
                "      \"passable\" : {\n" +
                "        \"number\" : 2\n" +
                "      }\n" +
                "    } ],\n" +
                "    \"outputId\" : 3\n" +
                "  }\n" +
                "}\n";
        String x = "{\n" +
                "  \"topicName\" : \"4.2-Conv\",\n" +
                "  \"timestamp\" : 1688047170.146000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"COMMAND\",\n" +
                "    \"type\" : \"CONVEYOR\",\n" +
                "    \"name\" : \"MOVE\",\n" +
                "    \"parameters\" : [ {\n" +
                "      \"passableType\" : \"DIRECTION\",\n" +
                "      \"passable\" : \"BACKWARD\"\n" +
                "    } ],\n" +
                "    \"outputId\" : 4\n" +
                "  }\n" +
                "}";
        String a = "{\n" +
                "  \"topicName\" : \"2.6-Freeze\",\n" +
                "  \"timestamp\" : 1688047622.854000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"COMMAND\",\n" +
                "    \"type\" : \"MULTIPROCESSING\",\n" +
                "    \"name\" : \"FREEZE\",\n" +
                "    \"parameters\" : [ {\n" +
                "      \"passableType\" : \"NUMBERNATURAL\",\n" +
                "      \"passable\" : {\n" +
                "        \"number\" : 2\n" +
                "      }\n" +
                "    } ],\n" +
                "    \"outputId\" : 4\n" +
                "  }\n" +
                "}\n";
        String b = "{\n" +
                "  \"topicName\" : \"3.7-Press\",\n" +
                "  \"timestamp\" : 1688047170.146000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"COMMAND\",\n" +
                "    \"type\" : \"PUNCHING\",\n" +
                "    \"name\" : \"PRESS\",\n" +
                "    \"parameters\" : [ ],\n" +
                "    \"outputId\" : 4\n" +
                "  }\n" +
                "}";
        dataToSend.println(t);
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
        JSONServer server = new JSONServer(6000);
        server.connectToClient();
    }
}

