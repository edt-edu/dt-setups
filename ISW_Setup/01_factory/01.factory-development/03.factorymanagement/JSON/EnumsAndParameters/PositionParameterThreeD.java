package JSON.EnumsAndParameters;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * a position parameter object used with GRIPPER
 */
@JsonPropertyOrder({"meaning", "vertical", "rot", "horizontal"})
public class PositionParameterThreeD implements Passable{

    private final PositionMeaning meaning;
    private final int vertical;
    private final int rot;
    private final int horizontal;

    public PositionParameterThreeD(PositionMeaning meaning, int vertical, int rot, int horizontal) {
        this.meaning = meaning;
        this.vertical = vertical;
        this.rot = rot;
        this.horizontal = horizontal;
    }

    public PositionMeaning getMeaning() {
        return meaning;
    }

    public int getVertical() {
        return vertical;
    }

    public int getRot() {
        return rot;
    }

    public int getHorizontal() {
        return horizontal;
    }
}
