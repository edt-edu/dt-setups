package Commands;

public class WarehouseFeedback
{
    private String state;
    private Boolean pickup;
    private Boolean conveyor;

    public WarehouseFeedback(String state, Boolean pickup, Boolean conveyor)
    {
        this.state = state;
        this.pickup = pickup;
        this.conveyor = conveyor;
    }

    public String getState()
    {
        return state;
    }

    public Boolean getPickup()
    {
        return pickup;
    }

    public Boolean getConveyor()
    {
        return conveyor;
    }
}
