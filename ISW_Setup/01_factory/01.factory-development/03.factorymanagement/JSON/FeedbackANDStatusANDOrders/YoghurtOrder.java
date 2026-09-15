package JSON.FeedbackANDStatusANDOrders;

import JSON.EnumsAndParameters.Colour;
import JSON.EnumsAndParameters.Flavour;
import JSON.EnumsAndParameters.JSONOutputType;
import JSON.EnumsAndParameters.Topping;
import JSON.Exceptions.JsonApiExcpetion;
import JSON.Parsing.JSONReadable;
import Layout_new.Processable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Objects;

/**
 * Class to hold yoghurt orders that come in from the webshop
 */
@JsonPropertyOrder({"jsonType", "orderId", "cup", "yoghurtFlavour", "toppings", "frozen", "stirred", "forStoring"})
public class YoghurtOrder implements JSONReadable {
    private final JSONOutputType jsonType;
    private final int orderId;
    private final Colour cup;
    private final Flavour yoghurtFlavour;
    private final List<Topping> toppings;
    private final boolean frozen;
    private final boolean stirred;
    private final boolean forStoring;

    /**
     * Constructor for a Yoghurt Order object
     *
     * Contains all the necessary information for production decisions
     *
     * @param jsonType
     * @param orderId
     * @param cup
     * @param yoghurtFlavour
     * @param toppings
     * @param frozen
     * @param stirred
     * @param forStoring
     */
    @JsonCreator
    public YoghurtOrder(@JsonProperty("jsonType") JSONOutputType jsonType,
                        @JsonProperty("orderId") int orderId,
                        @JsonProperty("cup") Colour cup,
                        @JsonProperty("yoghurtFlavour") Flavour yoghurtFlavour,
                        @JsonProperty("toppings") List<Topping> toppings,
                        @JsonProperty("frozen") boolean frozen,
                        @JsonProperty("stirred") boolean stirred,
                        @JsonProperty("forStoring") boolean forStoring) throws JsonApiExcpetion {
        if(jsonType != JSONOutputType.ORDER){
            throw new JsonApiExcpetion(); //TODO change to something more clear
        }
        this.jsonType = jsonType;
        this.orderId = orderId;
        this.cup = cup;
        this.yoghurtFlavour = yoghurtFlavour;
        this.toppings = toppings;
        this.frozen = frozen;
        this.stirred = stirred;
        this.forStoring = forStoring;
    }

    public int getOrderId() {
        return orderId;
    }

    public JSONOutputType getJsonType(){
        return jsonType;
    }

    public Colour getCup() {
        return cup;
    }

    public Flavour getYoghurtFlavour() {
        return yoghurtFlavour;
    }

    public List<Topping> getToppings() {
        return toppings;
    }

    public boolean getisFrozen() {
        return frozen;
    }

    public boolean getisStirred() {
        return stirred;
    }

    public boolean getisForStoring() {
        return forStoring;
    }

    @Override
    public String toString() {
        return "YoghurtOrder{" +
                "jsonType=" + jsonType +
                ", orderId=" + orderId +
                ", cup=" + cup +
                ", yoghurtFlavour=" + yoghurtFlavour +
                ", toppings=" + toppings +
                ", frozen=" + frozen +
                ", stirred=" + stirred +
                ", forStoring=" + forStoring +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YoghurtOrder)) return false;
        YoghurtOrder that = (YoghurtOrder) o;
        return orderId == that.orderId && frozen == that.frozen && stirred == that.stirred && forStoring == that.forStoring && Objects.equals(jsonType, that.jsonType) && cup == that.cup && yoghurtFlavour == that.yoghurtFlavour && Objects.equals(toppings, that.toppings);
    }
}