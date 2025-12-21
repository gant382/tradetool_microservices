package com.saicon.games.entities.shared;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Simplified Application entity for CallCard microservice
 */
@Entity
@Immutable
@Table(name = "APPLICATION")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.entities.shared.Application.listAll",
                query = "SELECT a FROM Application a"),
        @NamedQuery(name = "com.saicon.games.entities.shared.Application.findByGameTypeId",
                query = "SELECT a FROM Application a WHERE a.gameTypeId.gameTypeId = ?1"),
        @NamedQuery(name = "com.saicon.games.entities.shared.Application.listByApplicationIds",
                query = "SELECT a FROM Application a WHERE a.applicationId in ?1 AND a.active = ?2"),
        @NamedQuery(name = "com.saicon.games.entities.shared.Application.findByApplicationIds",
                query = "SELECT a FROM Application a WHERE a.applicationId in ?1")
})
@Cacheable(true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "APPLICATION_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String applicationId;

    @Basic(optional = false)
    @Column(name = "API_NAME", nullable = false, columnDefinition = "nvarchar(50)")
    private String apiName;

    @Basic(optional = false)
    @Column(name = "DESCRIPTION", nullable = false, columnDefinition = "nvarchar(50)")
    private String description;

    @JoinColumn(name = "GAME_TYPE_ID", referencedColumnName = "GAME_TYPE_ID", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private GameType gameTypeId;

    @Column(name = "EXPIRES_IN_SECONDS", columnDefinition = "numeric(19,0)")
    private Long expirationSeconds;

    @Column(name = "IS_ACTIVE", columnDefinition = "bit", insertable = false, nullable = false, updatable = false)
    private Boolean active;

    @Column(name = "MAGIC_KEY", columnDefinition = "nvarchar(50)", insertable = false, nullable = false, updatable = false)
    private String magicKey;

    public Application() {
    }

    public Application(String applicationId) {
        this.applicationId = applicationId == null ? null : applicationId.toUpperCase();
    }

    public Application(String applicationId, String apiName) {
        this.applicationId = applicationId == null ? null : applicationId.toUpperCase();
        this.apiName = apiName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId.toUpperCase();
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public GameType getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(GameType gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMagicKey() {
        return magicKey;
    }

    public void setMagicKey(String magicKey) {
        this.magicKey = magicKey;
    }

    public Long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(Long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Stub method for client type compatibility.
     * @return Stub client type (0)
     */
    public int getClientType() {
        return 0; // Stub - returns default client type
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (applicationId != null ? applicationId.toUpperCase().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Application)) {
            return false;
        }
        Application other = (Application) object;
        if ((this.applicationId == null && other.applicationId != null) || (this.applicationId != null && !this.applicationId.equalsIgnoreCase(other.applicationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Application[applicationId=" + applicationId + "]";
    }
}
