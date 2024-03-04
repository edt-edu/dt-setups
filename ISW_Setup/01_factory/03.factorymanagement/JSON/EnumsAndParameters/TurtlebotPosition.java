package JSON.EnumsAndParameters;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * used with turtlebot, aktuell beispielhaft TODO
 */
@JsonPropertyOrder({"meaning", "number"})
public class TurtlebotPosition implements Passable {

    private final PositionMeaning meaning;
    private final IOStationNumber number;


    public TurtlebotPosition(PositionMeaning meaning, IOStationNumber number) {
        this.meaning = meaning;
        this.number = number;
    }

    public PositionMeaning getMeaning() {
        return meaning;
    }

    public IOStationNumber getNumber() {
        return number;
    }
}
