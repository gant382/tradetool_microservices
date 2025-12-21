package com.saicon.games.callcard.entity;

import com.saicon.games.entities.shared.ItemTypes;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by user101 on 9/2/2016.
 */
@Entity
@Table(name = "CALL_CARD_TEMPLATE_ENTRY")
public class CallCardTemplateEntry {

    @Id
    @Basic(optional = false)
    @Column(name = "ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String id;

    @JoinColumn(name = "CALL_CARD_TEMPLATE_ID", referencedColumnName = "CALL_CARD_TEMPLATE_ID", nullable = false)
    @ManyToOne(optional = false)
    private CallCardTemplate callCardTemplateId;

    @Column(name = "ITEM_ID", nullable = true, columnDefinition = "uniqueidentifier")
    private String itemId;

    @JoinColumn(name = "ITEM_TYPE_ID", referencedColumnName = "ITEM_TYPE_ID", nullable = false)
    @ManyToOne(optional = false)
    private ItemTypes itemTypeId;

    @Column(name = "PROPERTIES", nullable = false, columnDefinition = "nvarchar(max)")
    private String properties;

    @Column(name = "ORDERING", nullable = false)
    private int ordering;

    public CallCardTemplateEntry() {
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

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }


    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }
}
