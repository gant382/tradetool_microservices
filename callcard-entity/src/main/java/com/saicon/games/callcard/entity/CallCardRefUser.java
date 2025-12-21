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
@Table(name = "CALL_CARD_REFUSER")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUser.deleteByIds", query = "DELETE FROM CallCardRefUser u WHERE u.callCardRefUserId IN ?1"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUser.deleteCallCardRefUserByCallCardId", query = "DELETE FROM CallCardRefUser u WHERE u.callCardId.callCardId = ?1"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUser.listByCallCardId", query = "SELECT u FROM CallCardRefUser u WHERE u.callCardId.callCardId = ?1"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUser.listByCallCardIdEndDate", query = "SELECT u FROM CallCardRefUser u WHERE u.callCardId IN (?1) AND u.endDate > ?2"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUser.listByCallCardIdRefUserId", query = "SELECT u FROM CallCardRefUser u WHERE u.callCardId.callCardId = ?1 AND u.refUserId.userId = ?2"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUser.listByCallCardIdInternalRefNo", query = "SELECT u FROM CallCardRefUser u WHERE u.callCardId.callCardId = ?1 AND u.internalRefNo = ?2")
})
public class CallCardRefUser {

    @Id
    @Column(name = "CALL_CARD_REFUSER_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String callCardRefUserId;

    @JoinColumn(name = "CALL_CARD_ID", referencedColumnName = "CALL_CARD_ID", nullable = false)
    @ManyToOne(optional = false)
    private CallCard callCardId;

    @JoinColumn(name = "REF_USER_ID", referencedColumnName = "USER_ID", nullable = false)
    @ManyToOne(optional = false)
    private Users refUserId;

    @JoinColumn(name = "SOURCE_USER_ID", referencedColumnName = "USER_ID", nullable = true)
    @ManyToOne(optional = true)
    private Users sourceUserId;

    @Column(name = "START_DATE", nullable = true, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "END_DATE", nullable = true, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @Column(name = "LAST_UPDATED", nullable = true, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "COMMENT", nullable = true, columnDefinition = "nvarchar(100)")
    private String comment;

    @Column(name = "STATUS", nullable = true, columnDefinition = "integer")
    private Integer status;

    @Column(name = "REF_NO", nullable = true, columnDefinition = "nvarchar(100)")
    private String refNo;

    @Column(name = "INTERNAL_REF_NO", nullable = true, columnDefinition = "nvarchar(100)")
    private String internalRefNo;

    @OneToMany(mappedBy = "callCardRefUserId")
    private List<CallCardRefUserIndex> CallCardRefUserIndexes = new ArrayList<CallCardRefUserIndex>();

    public CallCardRefUser() {
    }

    public String getCallCardRefUserId() {
        return callCardRefUserId;
    }

    public void setCallCardRefUserId(String callCardRefUserId) {
        this.callCardRefUserId = callCardRefUserId;
    }

    public CallCard getCallCardId() {
        return callCardId;
    }

    public void setCallCardId(CallCard callCardId) {
        this.callCardId = callCardId;
    }

    public Users getRefUserId() {
        return refUserId;
    }

    public void setRefUserId(Users refUserId) {
        this.refUserId = refUserId;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Users getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(Users sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getInternalRefNo() {
        return internalRefNo;
    }

    public void setInternalRefNo(String internalRefNo) {
        this.internalRefNo = internalRefNo;
    }

    public List<CallCardRefUserIndex> getCallCardRefUserIndexes() {
        return CallCardRefUserIndexes;
    }

    public void setCallCardRefUserIndexes(List<CallCardRefUserIndex> callCardRefUserIndexes) {
        CallCardRefUserIndexes = callCardRefUserIndexes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Alias methods for compatibility with legacy code
    public String getIssuerUserId() {
        return sourceUserId != null ? sourceUserId.getUserId() : null;
    }

    public void setIssuerUserId(String userId) {
        // Stub - issuer user ID mapping
    }

    public String getRecipientUserId() {
        return refUserId != null ? refUserId.getUserId() : null;
    }

    public void setRecipientUserId(String userId) {
        // Stub - recipient user ID mapping
    }

    public Date getDateCreated() {
        return startDate;
    }

    public void setDateCreated(Date date) {
        this.startDate = date;
    }

    public Date getDateUpdated() {
        return lastUpdated;
    }

    public void setDateUpdated(Date date) {
        this.lastUpdated = date;
    }

    public List<CallCardRefUserIndex> getItems() {
        return CallCardRefUserIndexes;
    }

    public void setItems(List<CallCardRefUserIndex> items) {
        this.CallCardRefUserIndexes = items;
    }
}
