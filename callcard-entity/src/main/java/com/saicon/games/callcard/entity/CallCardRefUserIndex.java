package com.saicon.games.callcard.entity;

import com.saicon.games.entities.shared.ItemTypes;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by user101 on 9/2/2016.
 */
@Entity
@Table(name = "CALL_CARD_REFUSER_INDEX")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUserIndex.deleteByIds", query = "DELETE FROM CallCardRefUserIndex u WHERE u.callCardRefUserIndexId IN ?1"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUserIndex.listByCallCardRefUserId", query = "SELECT u FROM CallCardRefUserIndex u WHERE u.callCardRefUserId.callCardRefUserId = ?1"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUserIndex.listByCallCardRefUserIds", query = "SELECT u FROM CallCardRefUserIndex u WHERE u.callCardRefUserId.callCardRefUserId IN ?1"),
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardRefUserIndex.listByCallCardRefUserIdsSubmitDate", query = "SELECT u FROM CallCardRefUserIndex u WHERE u.callCardRefUserId IN (?1) AND u.submitDate > ?2")
})

@SqlResultSetMapping(
        name = "RefUserIndexMapping",
        entities = @EntityResult(
                entityClass = CallCardRefUserIndex.class,
                fields = {
                        @FieldResult(name = "callCardRefUserIndexId", column = "call_card_refuser_index_id"),
                        @FieldResult(name = "callCardRefUserId", column = "call_card_refuser_id"),
                        @FieldResult(name = "itemId", column = "item_id"),
                        @FieldResult(name = "itemTypeId", column = "item_type_id"),
                        @FieldResult(name = "propertyId", column = "property_id"),
                        @FieldResult(name = "propertyValue", column = "property_value"),
                        @FieldResult(name = "status", column = "status"),
                        @FieldResult(name = "submitDate", column = "submit_date"),
                        @FieldResult(name = "amount", column = "amount"),
                        @FieldResult(name = "type", column = "type")}),
        columns = @ColumnResult(name = "refUserId"))


public class CallCardRefUserIndex {

    @Id
    @Column(name = "CALL_CARD_REFUSER_INDEX_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String callCardRefUserIndexId;

    @JoinColumn(name = "CALL_CARD_REFUSER_ID", referencedColumnName = "CALL_CARD_REFUSER_ID", nullable = false)
    @ManyToOne(optional = false)
    private CallCardRefUser callCardRefUserId;

    @Column(name = "ITEM_ID", nullable = true, columnDefinition = "uniqueidentifier")
    private String itemId;

    @JoinColumn(name = "ITEM_TYPE_ID", referencedColumnName = "ITEM_TYPE_ID", nullable = false)
    @ManyToOne(optional = false)
    private ItemTypes itemTypeId;

    @Column(name = "PROPERTY_ID", nullable = false, columnDefinition = "nvarchar(max)")
    private String propertyId;

    @Column(name = "PROPERTY_VALUE", nullable = false, columnDefinition = "nvarchar(max)")
    private String propertyValue;

    @Column(name = "STATUS", nullable = false)
    private int status;

    @Column(name = "SUBMIT_DATE", nullable = false, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submitDate;

    @Column(name = "AMOUNT", nullable = true, columnDefinition = "decimal(7,2)")
    private BigDecimal amount;

    @Column(name = "TYPE", nullable = false)
    private int type;

    public CallCardRefUserIndex() {
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

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCallCardRefUserIndexId() {
        return callCardRefUserIndexId;
    }

    public void setCallCardRefUserIndexId(String callCardRefUserIndexId) {
        this.callCardRefUserIndexId = callCardRefUserIndexId;
    }

    public CallCardRefUser getCallCardRefUserId() {
        return callCardRefUserId;
    }

    public void setCallCardRefUserId(CallCardRefUser callCardRefUserId) {
        this.callCardRefUserId = callCardRefUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
