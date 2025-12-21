package com.saicon.games.entities.shared;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Simplified ItemTypes entity for CallCard microservice
 */
@Entity
@Immutable
@Table(name = "ITEM_TYPES")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.entities.shared.ItemTypes.findByItemTypeDesc", query = "SELECT i FROM ItemTypes i WHERE i.itemTypeDesc = ?1"),
        @NamedQuery(name = "com.saicon.games.entities.shared.ItemTypes.findAll", query = "SELECT i FROM ItemTypes i")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ItemTypes implements Serializable {
    private static final long serialVersionUID = -9120387373543101263L;

    @Id
    @Basic(optional = false)
    @Column(name = "ITEM_TYPE_ID", nullable = false)
    private Integer itemTypeId;

    @Basic(optional = false)
    @Column(name = "ITEM_TYPE_DESC", nullable = false, columnDefinition = "nvarchar(255)")
    private String itemTypeDesc;

    public ItemTypes() {
    }

    public ItemTypes(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public ItemTypes(Integer itemTypeId, String itemTypeDesc) {
        this.itemTypeId = itemTypeId;
        this.itemTypeDesc = itemTypeDesc;
    }

    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public String getItemTypeDesc() {
        return itemTypeDesc;
    }

    public void setItemTypeDesc(String itemTypeDesc) {
        this.itemTypeDesc = itemTypeDesc;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (itemTypeId != null ? itemTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ItemTypes)) {
            return false;
        }
        ItemTypes other = (ItemTypes) object;
        if ((this.itemTypeId == null && other.itemTypeId != null) || (this.itemTypeId != null && !this.itemTypeId.equals(other.itemTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ItemTypes[itemTypeId=" + itemTypeId + "]";
    }
}
