package Layout_new;

/**
 * a 3d position, which is used to configure important points on stations in the perspective from one specific machine
 */
public class Position {


    private int vertical;
    private int rot;
    private int horizontal;

    public Position(int vertical, int rot, int horizontal) {
        this.vertical = vertical;
        this.rot = rot;
        this.horizontal = horizontal;
    }

    public int getVertical() {
        return vertical;
    }

    public void setVertical(int vertical) {
        this.vertical = vertical;
    }

    public int getRot() {
        return rot;
    }

    public void setRot(int rot) {
        this.rot = rot;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(int horizontal) {
        this.horizontal = horizontal;
    }
}
