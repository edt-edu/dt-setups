package Commands;

public class IndexedLineFeedback
{
    private String state;
    private String reason;
    private Boolean package_at_drill;
    private Boolean package_at_mill;
    private Boolean package_at_slider;
    private Boolean package_at_end;
    private Boolean package_at_start;

    public IndexedLineFeedback(String state, String reason, Boolean package_at_drill, Boolean package_at_mill, Boolean package_at_slider, Boolean package_at_end, Boolean package_at_start)
    {
        this.state = state;
        this.reason = reason;
        this. package_at_drill = package_at_drill;
        this.package_at_mill = package_at_mill;
        this.package_at_slider = package_at_slider;
        this.package_at_end = package_at_end;
        this.package_at_start = package_at_start;
    }

    public String getState()
    {
        return state;
    }

    public String getReason()
    {
        return reason;
    }

    public Boolean getPackage_at_drill()
    {
        return package_at_drill;
    }

    public Boolean getPackage_at_mill()
    {
        return package_at_mill;
    }

    public Boolean getPackage_at_slider()
    {
        return package_at_slider;
    }

    public Boolean getPackage_at_end()
    {
        return package_at_end;
    }

    public Boolean getPackage_at_start()
    {
        return package_at_start;
    }
}
