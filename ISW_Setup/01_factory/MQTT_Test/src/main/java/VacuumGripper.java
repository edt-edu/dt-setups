import EnumsAndParameters.*;
import EnumsAndParameters.ThreeDPosition;

public class VacuumGripper extends Station {
    private ThreeDPosition pickPosition;
    private ThreeDPosition placePosition;

    public VacuumGripper(String stationID) {
        super(stationID);
    }

    void setCommand(Command command){
        this.command = command;
    }

    void setPickPosition(ThreeDPosition pick){
        this.pickPosition = pick;
    }

    void setPlacePosition(ThreeDPosition place){
        this.placePosition = place;
    }
}