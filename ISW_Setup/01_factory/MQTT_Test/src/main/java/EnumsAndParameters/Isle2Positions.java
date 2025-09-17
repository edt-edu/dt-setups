package EnumsAndParameters;

public enum Isle2Positions
{
    // 2.1-Grip
    GRIP21IO27START(new ThreeDPosition(PositionMeaning.START, 0,0,0)),
    GRIP21CONV23CENTEREND(new ThreeDPosition(PositionMeaning.END, 0,0,0)),

    // 2.2-Grip
    GRIP22CONV24CENTERSTART(new ThreeDPosition(PositionMeaning.START, 0,0,0)),
    GRIP22IO28END(new ThreeDPosition(PositionMeaning.END, 0,0,0)),

    // 2.5-Vac
    VAC25CONV23VACSTART(new ThreeDPosition(PositionMeaning.START, 0,0,0)),
    VAC25FREEZE26END(new ThreeDPosition(PositionMeaning.START, 0,0,0));

    private ThreeDPosition threeDPosition;

    Isle2Positions(ThreeDPosition threeDPosition)
    {
        this.threeDPosition = threeDPosition;
    }

    public ThreeDPosition getPosition()
    {
        return threeDPosition;
    }
}
