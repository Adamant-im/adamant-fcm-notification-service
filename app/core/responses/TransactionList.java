package core.responses;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import core.entities.Transaction;
import core.entities.transaction_assets.TransactionAsset;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionList<AT extends TransactionAsset> {
    private int nodeTimestamp;
    private boolean success;
    private List<Transaction<AT>> transactions;
    private int count;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Transaction<AT>> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction<AT>> transactions) {
        this.transactions = transactions;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
