package JSON.EnumsAndParameters;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Wraps around the passable and specifies the type of the passable - important for reading function in python
 */
@JsonPropertyOrder({"passableType", "passable"})
public class Parameter {

    private final PassableType passableType;
    private final Passable passable;

    public Parameter(Passable passable){
        this.passable = passable;
        if (this.passable == null) {
            throw new NullPointerException();
        } else {
            //passable übergeben, automatische ergänzung um passableType
            String passableTypeRaw = this.passable.getClass().toString().toLowerCase();
            System.out.println(passableTypeRaw);
            if (passableTypeRaw.contains("positionparameterthreed")) {
                passableType = PassableType.POSITIONPARAMETERTHREED;
            } else if (passableTypeRaw.contains("colour")){
                passableType = PassableType.COLOUR;
            } else if (passableTypeRaw.contains("direction")){
                passableType = PassableType.DIRECTION;
            } else if (passableTypeRaw.contains("topping")) {
                passableType = PassableType.TOPPING;
            } else if (passableTypeRaw.contains("boxnumber")){
                passableType = PassableType.BOXNUMBER;
            } else if (passableTypeRaw.contains("numbernatural")) {
                passableType = PassableType.NUMBERNATURAL;
            } else if (passableTypeRaw.contains("turtlebotposition")) {
                passableType = PassableType.TUTRTLEBOTPOSITION;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public PassableType getPassableType() {
        return passableType;
    }

    public Passable getPassable() {
        return passable;
    }

    public static void main(String[] args) {
        Parameter p = new Parameter(new PositionParameterThreeD(PositionMeaning.START, 0, 1, 2));
    }
}
