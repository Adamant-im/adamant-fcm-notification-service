package core.responses;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import core.entities.Transaction;
import core.entities.transaction_assets.TransactionAsset;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionWasNormalized<T extends TransactionAsset> {
    private int nodeTimestamp;
    private boolean success;
    private Transaction<T> transaction;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Transaction<T> getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction<T> transaction) {
        this.transaction = transaction;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getNodeTimestamp() {
        return nodeTimestamp;
    }

    public void setNodeTimestamp(int nodeTimestamp) {
        this.nodeTimestamp = nodeTimestamp;
    }
}
