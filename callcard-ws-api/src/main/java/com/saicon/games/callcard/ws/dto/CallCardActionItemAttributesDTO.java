package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;
import com.saicon.games.client.data.DecimalDTO;

import java.util.Date;

/**
 * Created by user101 on 12/2/2016.
 */
public class CallCardActionItemAttributesDTO {

    @DTOParam(1)
    private String callCardRefUserIndexId;

    @DTOParam(2)
    private String propertyId;

    @DTOParam(3)
    private String propertyName;

    @DTOParam(4)
    private String propertyTypeId;

    @DTOParam(5)
    private String propertyValue;

    @DTOParam(6)
    private Date dateSubmitted;

    @DTOParam(7)
    private Integer status;

    @DTOParam(8)
    private Integer type;

    @DTOParam(9)
    private DecimalDTO amount;

    @DTOParam(10)
    private String refPropertyValue;

    public CallCardActionItemAttributesDTO() {
    }

    public CallCardActionItemAttributesDTO(String callCardRefUserIndexId, String propertyId, String propertyName, String propertyTypeId, String propertyValue, Date dateSubmitted, Integer status, Integer type, DecimalDTO amount,String refPropertyValue) {
        this.callCardRefUserIndexId = callCardRefUserIndexId;
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.propertyTypeId = propertyTypeId;
        this.propertyValue = propertyValue;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
        this.type = type;
        this.amount = amount;
        this.setRefPropertyValue(refPropertyValue);
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyTypeId() {
        return propertyTypeId;
    }

    public void setPropertyTypeId(String propertyTypeId) {
        this.propertyTypeId = propertyTypeId;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCallCardRefUserIndexId() {
        return callCardRefUserIndexId;
    }

    public void setCallCardRefUserIndexId(String callCardRefUserIndexId) {
        this.callCardRefUserIndexId = callCardRefUserIndexId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public DecimalDTO getAmount() {
        return amount;
    }

    public void setAmount(DecimalDTO amount) {
        this.amount = amount;
    }

    public String getRefPropertyValue() {
        return refPropertyValue;
    }

    public void setRefPropertyValue(String refPropertyValue) {
        this.refPropertyValue = refPropertyValue;
    }
}
