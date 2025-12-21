package com.saicon.games.callcard.entity;

import com.saicon.games.entities.shared.GameType;
import com.saicon.games.entities.shared.UserGroups;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by user101 on 9/2/2016.
 */
@Entity
@Table(name = "CALL_CARD_TEMPLATE")
public class CallCardTemplate {

    @Id
    @Column(name = "CALL_CARD_TEMPLATE_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String callCardTemplateId;

    @Column(name = "START_DATE", nullable = false, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "END_DATE", nullable = false, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @JoinColumn(name = "USER_GROUP_ID", referencedColumnName = "GROUP_ID", nullable = false)
    @ManyToOne(optional = false)
    private UserGroups userGroupId;

    @JoinColumn(name = "GAME_TYPE_ID", referencedColumnName = "GAME_TYPE_ID", nullable = false)
    @ManyToOne(optional = false)
    private GameType gameTypeId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToMany(mappedBy = "callCardTemplateId")
    private List<CallCardTemplateEntry> entries = new ArrayList<CallCardTemplateEntry>();

    @OneToMany(mappedBy = "callCardTemplateId")
    private List<CallCardTemplatePOS> pos = new ArrayList<CallCardTemplatePOS>();

    public CallCardTemplate() {
    }

    public String getCallCardTemplateId() { return callCardTemplateId; }

    public Date getStartDate() { return startDate; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }

    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCallCardTemplateId(String callCardTemplateId) {
        this.callCardTemplateId = callCardTemplateId;
    }

    public UserGroups getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(UserGroups userGroupId) {
        this.userGroupId = userGroupId;
    }

    public GameType getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(GameType gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public List<CallCardTemplateEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<CallCardTemplateEntry> entries) {
        this.entries = entries;
    }

    public List<CallCardTemplatePOS> getPos() {
        return pos;
    }

    public void setPos(List<CallCardTemplatePOS> pos) {
        this.pos = pos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.name;
    }

    public void setDescription(String description) {
        this.name = description;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof CallCardTemplate)) {
            return false;
        }
        CallCardTemplate other = (CallCardTemplate) object;
        if ((this.callCardTemplateId == null && other.callCardTemplateId != null) ||
                (this.callCardTemplateId != null && !this.callCardTemplateId.equalsIgnoreCase(other.callCardTemplateId))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (callCardTemplateId != null ? callCardTemplateId.hashCode() : 0);
        return hash;
    }
}
