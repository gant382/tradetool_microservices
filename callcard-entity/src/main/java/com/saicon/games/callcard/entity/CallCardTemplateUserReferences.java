package com.saicon.games.callcard.entity;

import com.saicon.games.entities.shared.ItemTypes;
import com.saicon.games.entities.shared.Users;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by user101 on 9/2/2016.
 */
@Entity
@Table(name = "CALL_CARD_TEMPLATE_USER_REFERENCES")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardTemplateUserReferences.listByCallCardTemplateId", query = "SELECT u FROM CallCardTemplateUserReferences u WHERE u.callCardTemplateId.callCardTemplateId = ?1 AND u.active = 1 ORDER BY u.ordering ASC")
})
public class CallCardTemplateUserReferences {

    @Id
    @Column(name = "ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String id;

    @JoinColumn(name = "CALL_CARD_TEMPLATE_ID", referencedColumnName = "CALL_CARD_TEMPLATE_ID", nullable = false)
    @ManyToOne(optional = false)
    private CallCardTemplate callCardTemplateId;

    @JoinColumn(name = "REF_USER_ID", referencedColumnName = "USER_ID", nullable = false)
    @ManyToOne(optional = false)
    private Users refUserId;

    @Column(name = "ITEM_ID", nullable = true, columnDefinition = "uniqueidentifier")
    private String itemId;

    @JoinColumn(name = "ITEM_TYPE_ID", referencedColumnName = "ITEM_TYPE_ID", nullable = false)
    @ManyToOne(optional = false)
    private ItemTypes itemTypeId;

    @Column(name = "MANDATORY", nullable = false)
    private boolean mandatory;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active;

    @Column(name = "ORDERING", nullable = false)
    private int ordering;

    public CallCardTemplateUserReferences() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CallCardTemplate getCallCardTemplateId() {
        return callCardTemplateId;
    }

    public void setCallCardTemplateId(CallCardTemplate callCardTemplateId) {
        this.callCardTemplateId = callCardTemplateId;
    }

    public Users getRefUserId() {
        return refUserId;
    }

    public void setRefUserId(Users refUserId) {
        this.refUserId = refUserId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public ItemTypes getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(ItemTypes itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }
}
