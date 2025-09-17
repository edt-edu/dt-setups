import EnumsAndParameters.*;
import EnumsAndParameters.ThreeDPosition;

public class ThreeDGripper extends Station {
    private ThreeDPosition pickPosition;
    private ThreeDPosition placePosition;

    public ThreeDGripper(String stationID) {
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
