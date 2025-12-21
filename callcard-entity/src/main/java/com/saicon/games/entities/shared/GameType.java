package com.saicon.games.entities.shared;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Simplified GameType entity for CallCard microservice
 */
@Entity
@Immutable
@Table(name = "GAME_TYPE")
@NamedQueries({
        @NamedQuery(name = "GameType.findAll",
                query = "SELECT g FROM GameType g"),
        @NamedQuery(name = "com.saicon.games.entities.shared.GameType.listAll",
                query = "SELECT a FROM GameType a"),
        @NamedQuery(name = "com.saicon.games.entities.shared.GameType.findByIds",
                query = "SELECT a FROM GameType a WHERE a.gameTypeId IN ?1",
                hints = {@QueryHint(name = "org.hibernate.cacheable", value = "true")}),
        @NamedQuery(name = "com.saicon.games.entities.shared.GameType.findById",
                query = "SELECT a FROM GameType a WHERE a.gameTypeId = ?1",
                hints = {@QueryHint(name = "org.hibernate.cacheable", value = "true")})
})
@Cacheable(true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class GameType implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "GAME_TYPE_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String gameTypeId;

    @Basic(optional = false)
    @Column(name = "NAME", nullable = false, columnDefinition = "nvarchar(255)")
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "nvarchar(50)")
    private String description;

    @Column(name = "VERSION", columnDefinition = "nvarchar(50)")
    private String version;

    @Column(name = "MIN_PARTICIPANTS")
    private int minParticipants;

    @Column(name = "MAX_PARTICIPANTS")
    private int maxParticipants;

    @Column(name = "DEFAULT_PARTICIPANTS")
    private int defaultParticipants;

    @Column(name = "BOT_APPLICABLE", nullable = false, columnDefinition = "bit")
    private boolean botApplicable;

    public GameType() {
    }

    public GameType(String gameTypeId) {
        this.gameTypeId = gameTypeId == null ? null : gameTypeId.toUpperCase();
    }

    public GameType(String gameTypeId, String name) {
        this.gameTypeId = gameTypeId == null ? null : gameTypeId.toUpperCase();
        this.name = name;
    }

    public String getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(String gameTypeId) {
        this.gameTypeId = gameTypeId == null ? null : gameTypeId.toUpperCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getDefaultParticipants() {
        return defaultParticipants;
    }

    public void setDefaultParticipants(int defaultParticipants) {
        this.defaultParticipants = defaultParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public void setMinParticipants(int minParticipants) {
        this.minParticipants = minParticipants;
    }

    public boolean isBotApplicable() {
        return botApplicable;
    }

    public void setBotApplicable(boolean botApplicable) {
        this.botApplicable = botApplicable;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (gameTypeId != null ? gameTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GameType)) {
            return false;
        }
        GameType other = (GameType) object;
        if ((this.gameTypeId == null && other.gameTypeId != null) || (this.gameTypeId != null && !this.gameTypeId.equalsIgnoreCase(other.gameTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GameType[gameTypeId=" + gameTypeId + ", name=" + name + "]";
    }
}
