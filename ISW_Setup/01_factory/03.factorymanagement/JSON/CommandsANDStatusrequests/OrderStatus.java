package JSON.CommandsANDStatusrequests;

import JSON.EnumsAndParameters.ExecutionStatus;

import java.time.ZonedDateTime;

public class OrderStatus {
    private final String yoghurtId;
    private final ZonedDateTime timestamp;
    private final int orderId;
    private final ExecutionStatus status;

    public OrderStatus(String yoghurtId, ZonedDateTime timestamp, int orderId, ExecutionStatus status) {
        this.yoghurtId = yoghurtId;
        this.timestamp = timestamp;
        this.orderId = orderId;
        this.status = status;
    }

    public String getYoghurtId() {
        return yoghurtId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public int getOrderId() {
        return orderId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "OrderStatus{" +
                "yoghurtId='" + yoghurtId + '\'' +
                ", timestamp=" + timestamp +
                ", orderId=" + orderId +
                ", status=" + status +
                '}';
    }
}
