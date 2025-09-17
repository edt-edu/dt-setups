import EnumsAndParameters.Command;
import EnumsAndParameters.Direction;

public class Conveyor extends Station {
    private Direction direction;
    private int stepCount;

    public Conveyor(String stationID) {
        super(stationID);
    }

    void setCommand(Command command){
        this.command = command;
    }

    void setDirection(Direction direction){
        this.direction = direction;
    }

    void setStepCount(int stepCount){
        this.stepCount = stepCount;
    }
}
