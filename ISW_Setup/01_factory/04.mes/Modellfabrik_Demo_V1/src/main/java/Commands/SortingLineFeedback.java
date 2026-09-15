package Commands;

public class SortingLineFeedback
{
    private String state;
    private Boolean isWhiteStored;
    private Boolean isRedStored;
    private Boolean isBlueStored;
    private Boolean isAtStart;

    public SortingLineFeedback(String state, Boolean isWhiteStored, Boolean isRedStored, Boolean isBlueStored, Boolean isAtStart)
    {
        this.state = state;
        this.isWhiteStored = isWhiteStored;
        this.isRedStored = isRedStored;
        this.isBlueStored = isBlueStored;
        this.isAtStart = isAtStart;
    }

    public String getState()
    {
        return state;
    }

    public Boolean getWhiteStored()
    {
        return isWhiteStored;
    }

    public Boolean getRedStored()
    {
        return isRedStored;
    }

    public Boolean getBlueStored()
    {
        return isBlueStored;
    }

    public Boolean getAtStart()
    {
        return isAtStart;
    }
}
