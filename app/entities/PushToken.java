package entities;

import io.ebean.Finder;
import io.ebean.annotation.CreatedTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "tokens")
public class PushToken {
    public static final Finder<Long, PushToken> finder = new Finder<>(PushToken.class);

    @Id
    @SequenceGenerator(name="tokens_id_seq",
            sequenceName="tokens_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tokens_id_seq")
    private long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    private String provider;

    private String address;

    private boolean extendedPush;

    @CreatedTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isExtendedPush() {
        return extendedPush;
    }

    public void setExtendedPush(boolean extendedPush) {
        this.extendedPush = extendedPush;
    }
}
