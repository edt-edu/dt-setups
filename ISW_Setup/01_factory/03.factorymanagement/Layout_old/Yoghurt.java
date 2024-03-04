package Layout_old;

import java.util.List;
import java.util.Optional;

public class Yoghurt {
    private String name = "";
    private Optional<List<Wish>> wishes;

    public Yoghurt(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
