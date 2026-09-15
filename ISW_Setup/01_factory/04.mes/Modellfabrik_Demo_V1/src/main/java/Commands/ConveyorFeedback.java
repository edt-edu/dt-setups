package Commands;

public class ConveyorFeedback
{
    private String state;
    private String reason;
    private Boolean leftSensor;
    private Boolean rightSensor;
    private int position;

    public ConveyorFeedback(String state, String reason, Boolean leftSensor, Boolean rightSensor, int position)
    {
        this.state = state;
        this.reason = reason;
        this.leftSensor = leftSensor;
        this.rightSensor = rightSensor;
        this.position = position;
    }

    public String getState()
    {
        return this.state;
    }
    public String getReason()
    {
        return this.reason;
    }
    public Boolean getLeftSensor()
    {
        return this.leftSensor;
    }
    public Boolean getRightSensor()
    {
        return this.rightSensor;
    }
    public int getPosition()
    {
        return this.position;
    }
}
