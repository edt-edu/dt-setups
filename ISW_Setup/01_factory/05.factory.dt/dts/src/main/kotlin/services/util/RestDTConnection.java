package services.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class RestDTConnection implements IInboundConnection{


    // http client

    final private HttpClient httpClient;

    final int inboundPort;

    HttpServer server;

    public RestDTConnection(String digitalTwinUri, int inboundPort) {   
        this.inboundPort = inboundPort;
        this.httpClient = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @Override
    public void connect() throws IOException {           
        server = HttpServer.create(new InetSocketAddress(this.inboundPort), 0);
        server.createContext("/inbox", new InboxHandler());
        server.setExecutor(null);
        server.start();    
    }

    @Override
    public void disconnect() {
        server.stop(0);	
    }  

    @Override
    public void receive(InboundMessage message) throws IOException  {
        System.out.println("Received message: " + message.getMessage());
    }

    private class InboxHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream body = exchange.getRequestBody();
            byte[] bytes = body.readAllBytes();
            String message = new String(bytes);
            receive(new InboundMessage(message));
        }
    }
    
}
