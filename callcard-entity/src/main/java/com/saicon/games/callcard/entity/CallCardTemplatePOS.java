package com.saicon.games.callcard.entity;

import com.saicon.games.entities.shared.Users;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;


/**
 * Created by user101 on 16/2/2016.
 */
@Entity
@Table(name = "CALL_CARD_TEMPLATE_POS")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardTemplatePOS.listByCallCardTemplateId", query = "SELECT u FROM CallCardTemplatePOS u WHERE u.callCardTemplateId.callCardTemplateId = ?1 AND u.active = 1 ORDER BY u.groupId DESC, u.ordering ASC")
})
public class CallCardTemplatePOS {

    @Id
    @Column(name = "CALL_CARD_TEMPLATE_POS_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String callCardTemplatePOSId;

    @JoinColumn(name = "CALL_CARD_TEMPLATE_ID", referencedColumnName = "CALL_CARD_TEMPLATE_ID", nullable = false)
    @ManyToOne(optional = false)
    private CallCardTemplate callCardTemplateId;

    @JoinColumn(name = "REF_USER_ID", referencedColumnName = "USER_ID", nullable = false)
    @ManyToOne(optional = false)
    private Users refUserId;

    @Column(name = "MANDATORY", nullable = false)
    private boolean mandatory;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @Column(name = "ORDERING", nullable = false)
    private int ordering;

    @Column(name = "GROUP_ID", nullable = true)
    private Integer groupId;

    public CallCardTemplatePOS() {
    }

    public String getCallCardTemplatePOSId() { return callCardTemplatePOSId; }

    public void setCallCardTemplatePOSId(String callCardTemplatePOSId) { this.callCardTemplatePOSId = callCardTemplatePOSId; }

    public CallCardTemplate getCallCardTemplateId() { return callCardTemplateId; }

    public void setCallCardTemplateId(CallCardTemplate callCardTemplateId) { this.callCardTemplateId = callCardTemplateId; }

    public Users getRefUserId() { return refUserId; }

    public void setRefUserId(Users refUserId) { this.refUserId = refUserId; }

    public boolean isMandatory() { return mandatory; }

    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
}
