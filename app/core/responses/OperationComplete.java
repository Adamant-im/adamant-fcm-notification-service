package core.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationComplete {
    private int nodeTimestamp;
    private boolean success;
    private String transactionId;

    public int getNodeTimestamp() {
        return nodeTimestamp;
    }

    public void setNodeTimestamp(int nodeTimestamp) {
        this.nodeTimestamp = nodeTimestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
