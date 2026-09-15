public class Yoghurt
{
    private String flavour;
    private String[] toppings;
    private Boolean frozen;

    public Yoghurt(String flavour, String[] toppings, Boolean frozen)
    {
        this.flavour = flavour;
        this.toppings = toppings;
        this.frozen = frozen;
    }

    public String getFlavour()
    {
        return this.flavour;
    }

    public String[] getToppings()
    {
        return this. toppings;
    }

    public Boolean getFrozen()
    {
        return this.frozen;
    }
}