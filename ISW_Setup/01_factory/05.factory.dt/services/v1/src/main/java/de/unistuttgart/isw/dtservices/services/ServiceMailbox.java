package de.unistuttgart.isw.dtservices.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

public class ServiceMailbox<RequestType, ObjectFactory> {
    final private String mailboxQueueName;
    final private String rabbitMQConnectionString;

    // lister for onMessage and onParsedMessage
    public final List<Consumer<String>> onMessageListeners = new ArrayList<>();
    public final List<Consumer<RequestType>> onParsedMessageListeners = new ArrayList<>();
    public final Class<RequestType> reqType;
    public final Class<ObjectFactory> objFactory;

    public ServiceMailbox(Class<RequestType> reqType,Class<ObjectFactory> objFactory, String mailboxQueueName, String rabbitMQConnectionString) {
        this.mailboxQueueName = mailboxQueueName;
        this.rabbitMQConnectionString = rabbitMQConnectionString;
        this.reqType = reqType;
        this.objFactory = objFactory;
    }
   

    /**
     * start the rabbitMQ consumer and add the listener for inbound messages
     */
    public void startRabbitMQConsumer() throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(rabbitMQConnectionString);
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();
        channel.queueDeclare(this.mailboxQueueName, true, true, true, null);
        channel.basicConsume(this.mailboxQueueName, true, (consumerTag, message) -> {
        String input = new String(message.getBody());
        System.setProperty("com.sun.xml.bind.v2.runtime.unmarshaller", "DEBUG");
        this.onMessageListeners.forEach(listener -> listener.accept(input));
        try {
            XMLInputFactory xif = XMLInputFactory.newFactory();
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(input));
            JAXBContext context = JAXBContext.newInstance(this.objFactory);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<RequestType> o = (JAXBElement<RequestType>) unmarshaller.unmarshal(xsr);
            RequestType se = this.reqType.cast(o.getValue());
            this.onParsedMessageListeners
                .forEach(listener -> listener.accept(se));
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }, consumerTag -> {});
    }
}
