package de.unistuttgart.isw.dtservices.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import de.unistuttgart.isw.dts.gateway.TGatewayRequest;
import de.unistuttgart.isw.dts.services.ObjectFactory;
import de.unistuttgart.isw.dts.services.TServiceRequest;
import de.unistuttgart.isw.dtservices.configs.Config;
import de.unistuttgart.isw.dtservices.services.query.Callable;
import de.unistuttgart.isw.dtservices.services.query.Deletable;
import de.unistuttgart.isw.dtservices.services.query.Selectable;
import de.unistuttgart.isw.dtservices.services.query.Updateable;

public abstract class CrudService implements IBootable, IPermissionRestricted, GatewayInboundConnection, Callable, Deletable, Updateable, Selectable {

    private Config config;

    private ServiceMailbox<TServiceRequest, ObjectFactory> mailbox;     


    @Override
    public final void start(){   	
        try {            
            this.mailbox = new ServiceMailbox(TServiceRequest.class,ObjectFactory.class, mailboxName(), config.getRabbitMQUri());
            this.mailbox.onMessageListeners.add(this::onMessage);
            this.mailbox.onParsedMessageListeners.add(this::onParsedMessage);
            this.mailbox.startRabbitMQConsumer();
            // call the afterStart method
            this.afterStart();
        } catch (IOException | TimeoutException | URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    protected void sendMessagetoGateway(TGatewayRequest message){
     //   this.gatewayOutboundConnection.sendMessage(message); 
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

  

    /**
     * This method can used to define a service dependent behaviour after the service is started
     */
    protected void afterStart(){

    }

    /**
     * This method is called when the service is stopped
     * @return
     */
    protected String mailboxName() {
        return this.config.getQueueName();
    }


}