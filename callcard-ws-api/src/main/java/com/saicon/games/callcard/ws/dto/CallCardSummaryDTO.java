package com.saicon.games.callcard.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saicon.games.callcard.util.DTOParam;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * CallCard Summary DTO - Ultra-minimal for list views
 *
 * Optimizations:
 * - Only essential fields for list/grid displays
 * - No nested objects
 * - Primitive types only
 * - ISO 8601 string dates
 * - ~90% smaller payload than full CallCardDTO
 *
 * Use case: Mobile app list screens, table views, search results
 * For full details, call getSimplifiedCallCard(id) or getCallCard(id)
 */
@XmlRootElement(name = "CallCardSummary")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallCardSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @DTOParam(1)
    private String id;

    @DTOParam(2)
    private String name;

    @DTOParam(3)
    private String status;

    @DTOParam(4)
    private String templateName;

    @DTOParam(5)
    private int userCount;

    @DTOParam(6)
    private String lastModified; // ISO 8601 format

    @DTOParam(7)
    private boolean active;

    @DTOParam(8)
    private boolean submitted;

    public CallCardSummaryDTO() {
    }

    public CallCardSummaryDTO(String id, String name, String status, String templateName,
                              int userCount, String lastModified, boolean active, boolean submitted) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.templateName = templateName;
        this.userCount = userCount;
        this.lastModified = lastModified;
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

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
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

    @Override
    public String toString() {
        return "CallCardSummaryDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", templateName='" + templateName + '\'' +
                ", userCount=" + userCount +
                ", lastModified='" + lastModified + '\'' +
                '}';
    }
}
