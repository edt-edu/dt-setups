package backend_sysml_service;

import java.time.LocalDateTime;

public class ArmOperation {
    private int armId;
    private int containerId;
    private String operationType;
    private LocalDateTime operationTimestamp;
    private String triggerSource;
    private int[] relativePosition; // [x, y, z]

    public ArmOperation(int armId, int containerId, String operationType, LocalDateTime operationTimestamp,
                     String triggerSource, int[] relativePosition) {
        this.armId = armId;
        this.containerId = containerId;
        this.operationType = operationType;
        this.operationTimestamp = operationTimestamp;
        this.triggerSource = triggerSource;
        this.relativePosition = relativePosition;
    }

    // Getter und Setter
    public int getArmId() { return armId; }
    public void setArmId(int armId) { this.armId = armId; }

    public int getContainerId() { return containerId; }
    public void setContainerId(int containerId) { this.containerId = containerId; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public LocalDateTime getOperationTimestamp() { return operationTimestamp; }
    public void setOperationTimestamp(LocalDateTime operationTimestamp) { this.operationTimestamp = operationTimestamp; }

    public String getTriggerSource() { return triggerSource; }
    public void setTriggerSource(String triggerSource) { this.triggerSource = triggerSource; }

    public int[] getRelativePosition() { return relativePosition; }
    public void setRelativePosition(int[] relativePosition) { this.relativePosition = relativePosition; }
}
