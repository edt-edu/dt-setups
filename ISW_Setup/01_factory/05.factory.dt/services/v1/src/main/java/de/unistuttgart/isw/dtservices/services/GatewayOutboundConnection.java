package de.unistuttgart.isw.dtservices.services;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.unistuttgart.isw.dts.gateway.ObjectFactory;
import de.unistuttgart.isw.dts.gateway.TGatewayRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class GatewayOutboundConnection {

    private final String messageQueue;
    private final String rabbitMQUri;
    private Connection conn;
    private Channel channel;

    GatewayOutboundConnection(final String messageQueue, final String rabbitMQUri) {
        this.messageQueue = messageQueue;
        this.rabbitMQUri = rabbitMQUri;
    }

    public void connect() throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException{
        ConnectionFactory factory = new ConnectionFactory();      
        factory.setUri(this.rabbitMQUri);
        this.conn = factory.newConnection();
        this.channel = conn.createChannel();   
    }

    public void sendMessage(TGatewayRequest message) {
         
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(message, writer);
            this.channel.basicPublish("", this.messageQueue, null, message.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
