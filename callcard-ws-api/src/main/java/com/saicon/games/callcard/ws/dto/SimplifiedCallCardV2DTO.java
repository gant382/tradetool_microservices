package com.saicon.games.callcard.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saicon.games.callcard.util.DTOParam;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Simplified CallCard DTO V2 - Optimized for mobile clients
 *
 * Optimizations:
 * - Removed AbstractDTOWithResources inheritance (reduces payload)
 * - Primitive types instead of wrapper classes where possible
 * - ISO 8601 string dates instead of Date objects (better for JSON)
 * - User count instead of full user list (60%+ payload reduction)
 * - Excludes null fields in JSON serialization
 * - Minimal fields for list operations
 *
 * Typical use case: Mobile app list views and detail views
 */
@XmlRootElement(name = "SimplifiedCallCardV2")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimplifiedCallCardV2DTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @DTOParam(1)
    private String id;

    @DTOParam(2)
    private String name;

    @DTOParam(3)
    private String status;

    @DTOParam(4)
    private String templateId;

    @DTOParam(5)
    private String templateName;

    @DTOParam(6)
    private String createdDate; // ISO 8601 format: yyyy-MM-dd'T'HH:mm:ss'Z'

    @DTOParam(7)
    private String lastModified; // ISO 8601 format

    @DTOParam(8)
    private int assignedUserCount;

    @DTOParam(9)
    private boolean active;

    @DTOParam(10)
    private boolean submitted;

    @DTOParam(11)
    private String comments;

    @DTOParam(12)
    private String internalRefNo;

    public SimplifiedCallCardV2DTO() {
    }

    public SimplifiedCallCardV2DTO(String id, String name, String status, String templateId,
                                   String templateName, String createdDate, String lastModified,
                                   int assignedUserCount, boolean active, boolean submitted) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.templateId = templateId;
        this.templateName = templateName;
        this.createdDate = createdDate;
        this.lastModified = lastModified;
        this.assignedUserCount = assignedUserCount;
        this.active = active;
        this.submitted = submitted;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public int getAssignedUserCount() {
        return assignedUserCount;
    }

    public void setAssignedUserCount(int assignedUserCount) {
        this.assignedUserCount = assignedUserCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getInternalRefNo() {
        return internalRefNo;
    }

    public void setInternalRefNo(String internalRefNo) {
        this.internalRefNo = internalRefNo;
    }

    @Override
    public String toString() {
        return "SimplifiedCallCardV2DTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", templateId='" + templateId + '\'' +
                ", assignedUserCount=" + assignedUserCount +
                ", active=" + active +
                ", submitted=" + submitted +
                '}';
    }
}
