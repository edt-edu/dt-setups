import EnumsAndParameters.MessageType;

import java.util.UUID;

public class Message {
    private final long timestamp;
    private MessageType messageType;
    private Station station;
    private String messageID;
    public Message(MessageType messageType, Station station){
        this.messageID = UUID.randomUUID().toString();
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis()/1000L;
        this.station = station;
    }

    void setMessageType(MessageType messageType){
        this.messageType = messageType;
    }
}
