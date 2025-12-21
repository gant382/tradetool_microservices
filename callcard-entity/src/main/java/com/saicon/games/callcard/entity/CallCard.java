package com.saicon.games.callcard.entity;

import com.saicon.games.entities.shared.Users;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by user101 on 9/2/2016.
 */
@Entity
@Table(name = "CALL_CARD")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCard.filterByUsersEndDate", query = "SELECT c FROM CallCard c WHERE c.userId IN (?1) AND c.endDate > ?2"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCard.listByUserInternalRefNo", query = "SELECT c FROM CallCard c WHERE c.userId.userId = ?1 AND c.internalRefNo = ?2")
})
public class CallCard {

    @Id
    @Column(name = "CALL_CARD_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String callCardId;

    @JoinColumn(name = "CALL_CARD_TEMPLATE_ID",referencedColumnName = "CALL_CARD_TEMPLATE_ID", nullable = false)
    @ManyToOne(optional = false)
    private CallCardTemplate callCardTemplateId;

    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
    @ManyToOne(optional = false)
    private Users userId;

    @Column(name = "START_DATE", nullable = false, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "END_DATE", nullable = true, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @Column(name = "COMMENTS", nullable = true, columnDefinition = "nvarchar(100)")
    private String comments;

    @OneToMany(mappedBy = "callCardId")
    private List<CallCardRefUser> callCardIndices = new ArrayList<CallCardRefUser>();

    @Column(name = "LAST_UPDATED", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "INTERNAL_REF_NO", nullable = true, columnDefinition = "nvarchar(100)")
    private String internalRefNo;

    public CallCard() {
    }

    public String getCallCardId() {
        return callCardId;
    }

    public void setCallCardId(String callCardId) {
        this.callCardId = callCardId;
    }

    public CallCardTemplate getCallCardTemplateId() {
        return callCardTemplateId;
    }

    public void setCallCardTemplateId(CallCardTemplate callCardTemplateId) {
        this.callCardTemplateId = callCardTemplateId;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<CallCardRefUser> getCallCardIndices() {
        return callCardIndices;
    }

    public void setCallCardIndices(List<CallCardRefUser> callCardIndices) {
        this.callCardIndices = callCardIndices;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getInternalRefNo() {
        return internalRefNo;
    }

    public void setInternalRefNo(String internalRefNo) {
        this.internalRefNo = internalRefNo;
    }
}
