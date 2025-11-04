package backend_sysml_service;
public class StorageUnit {
    private int id;
    private String unitName;
    private int[] relativePosition; // [x, y, z]

    public StorageUnit(int id, String unitName, int[] relativePosition) {
        this.id = id;
        this.unitName = unitName;
        this.relativePosition = relativePosition;
    }

    // Getter und Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public int[] getRelativePosition() { return relativePosition; }
    public void setRelativePosition(int[] relativePosition) { this.relativePosition = relativePosition; }
}