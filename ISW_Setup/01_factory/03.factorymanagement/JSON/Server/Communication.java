package JSON.Server;

import Layout_new.StationExecutor.RevPiNumber;

import java.io.IOException;
import java.util.LinkedList;


/**
 * class that manages all communication with the RevPis (TODO and later all Turtlebots)
 * whenever the constructor is called, it creates an input and an output buffer as well as sockets for sending and receiving data
 */
public class Communication {
    LinkedList<String> input;
    LinkedList<String> output;
    private final int port;
    private Thread sendRevPi;
    private Thread receiveRevPi;

    /**
     * sets up the specific sockets and starts them
     * @param revPiNumber
     */
    public Communication(RevPiNumber revPiNumber) {
        this.input = new LinkedList<>();
        this.output = new LinkedList<>();
        this.port = 6000;
        //PORT 6001 für senden an RevPi 1, PORT 6011 für empfangen von RevPi 1, usw.
        try {
            if(revPiNumber == RevPiNumber.CORE1) {
                this.sendRevPi = new Thread(new JSONServerSend(port + 1, this.output));
                this.receiveRevPi = new Thread(new JSONServerReceive(port + 11, this.input));
            } else if (revPiNumber == RevPiNumber.CORE2) {
                this.sendRevPi = new Thread(new JSONServerSend(port + 2, this.output));
                this.receiveRevPi = new Thread(new JSONServerReceive(port + 12, this.input));
            } else if (revPiNumber == RevPiNumber.CORE3) {
                this.sendRevPi = new Thread(new JSONServerSend(port + 3, this.output));
                this.receiveRevPi = new Thread(new JSONServerReceive(port + 13, this.input));
            } else if (revPiNumber == RevPiNumber.CORE4) {
                this.sendRevPi = new Thread(new JSONServerSend(port + 4, this.output));
                this.receiveRevPi = new Thread(new JSONServerReceive(port + 14, this.input));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.sendRevPi.start();
        this.receiveRevPi.start();
    }

    /**
     * add whatever needs to be Sent to this list - will be automatically removed after its sent
     * @param send
     */
    public void addToSendList(String send){
        this.output.add(send);
    }

    /**
     * get an item from the receive List
     * @return
     */
    public String removeFromReceiveList() {
        return this.input.removeFirst();
    }

    /**
     * indicates that the input buffer is not empty
     * @return
     */
    public boolean receivedAvailable() {
        return (!this.input.isEmpty());
    }

}