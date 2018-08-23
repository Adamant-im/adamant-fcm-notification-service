package entities;

import io.ebean.Finder;
import io.ebean.annotation.CreatedTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "messages")
public class MessageData {
    public static final Finder<Long, MessageData> finder = new Finder<>(MessageData.class);

    @Id
    @SequenceGenerator(name="messages_id_seq",
            sequenceName="messages_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messages_id_seq")
    private long id;

    @ManyToOne
    private PushToken pushToken;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "created_at")
    @CreatedTimestamp
    private Timestamp createdAt;

    private int attempts;

    @Column(name = "locked_at")
    private Timestamp lockedAt;


    private boolean sended;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PushToken getPushToken() {
        return pushToken;
    }

    public void setPushToken(PushToken pushToken) {
        this.pushToken = pushToken;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Timestamp getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Timestamp lockedAt) {
        this.lockedAt = lockedAt;
    }

    public boolean isSended() {
        return sended;
    }

    public void setSended(boolean sended) {
        this.sended = sended;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
