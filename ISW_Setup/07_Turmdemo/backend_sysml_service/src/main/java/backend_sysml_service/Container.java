package backend_sysml_service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Container {
    private int id;
    private String containerName;
    private int unitId;
    private String itemType;
    private int itemQty;
    private double itemWeightKg;
    private LocalDate itemDateIn;
    private LocalDateTime statusUpdatedAt;

    public Container(int id, String containerName, int unitId, String itemType, int itemQty,
                     double itemWeightKg, LocalDate itemDateIn, LocalDateTime statusUpdatedAt) {
        this.id = id;
        this.containerName = containerName;
        this.unitId = unitId;
        this.itemType = itemType;
        this.itemQty = itemQty;
        this.itemWeightKg = itemWeightKg;
        this.itemDateIn = itemDateIn;
        this.statusUpdatedAt = statusUpdatedAt;
    }

    // Getter und Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContainerName() { return containerName; }
    public void setContainerName(String containerName) { this.containerName = containerName; }

    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getItemQty() { return itemQty; }
    public void setItemQty(int itemQty) { this.itemQty = itemQty; }

    public double getItemWeightKg() { return itemWeightKg; }
    public void setItemWeightKg(double itemWeightKg) { this.itemWeightKg = itemWeightKg; }

    public LocalDate getItemDateIn() { return itemDateIn; }
    public void setItemDateIn(LocalDate itemDateIn) { this.itemDateIn = itemDateIn; }

    public LocalDateTime getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(LocalDateTime statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }
}
