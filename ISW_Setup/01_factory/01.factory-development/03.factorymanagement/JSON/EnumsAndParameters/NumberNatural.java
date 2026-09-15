package JSON.EnumsAndParameters;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * wrapper for an integer greater zero - necessary for jsonParsing
 */
@JsonPropertyOrder({"number"})
public class NumberNatural implements Passable{

    private final int number;

    public NumberNatural(int i){
        if (i >= 0){
            this.number = i;
        } else {
            throw new IllegalArgumentException("Should only be greater or equal zero");
        }

    }

    public int getNumber(){
        return this.number;
    }
}
