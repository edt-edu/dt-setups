import EnumsAndParameters.Command;

public class Multiprocessing extends Station {
    private int freezeDuration;

    public Multiprocessing(String stationID) {
        super(stationID);
    }


    void setCommand(Command command){
        this.command = command;
    }
    void setFreezeDuration(int duration){
        this.freezeDuration = duration;
    }
}
//jsontype -> messagetype, in message rein
