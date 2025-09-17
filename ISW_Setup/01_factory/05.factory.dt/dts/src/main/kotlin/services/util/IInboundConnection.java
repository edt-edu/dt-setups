package services.util;

import java.io.IOException;

public interface IInboundConnection {
    
    public void connect() throws Exception;
    public void disconnect() throws Exception;
    public void receive(InboundMessage message) throws IOException ;


}
