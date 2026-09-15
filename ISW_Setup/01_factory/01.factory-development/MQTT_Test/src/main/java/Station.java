import EnumsAndParameters.Command;

public abstract class Station {
    String stationType;
    String stationID;
    Command command;

    public Station(String stationID){
        this.stationID = stationID;
        this.stationType = this.getClass().getSimpleName();
    }
}
