package com.saicon.games.callcard.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saicon.games.callcard.util.DTOParam;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * Template Usage DTO - Statistics for specific CallCard templates
 * FIXED VERSION - Copy to: callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/dto/TemplateUsageDTO.java
 *
 * Added: getSubmittedCallCards() as alias for getSubmittedCount()
 */
@XmlRootElement(name = "TemplateUsage")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateUsageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @DTOParam(1)
    private String templateId;

    @DTOParam(2)
    private String templateName;

    @DTOParam(3)
    private String userGroupId;

    @DTOParam(4)
    private Date dateFrom;

    @DTOParam(5)
    private Date dateTo;

    @DTOParam(6)
    private long usageCount;

    @DTOParam(7)
    private long uniqueUsers;

    @DTOParam(8)
    private long activeCount;

    @DTOParam(9)
    private long submittedCount;

    @DTOParam(10)
    private Double completionRate; // Percentage (0-100)

    @DTOParam(11)
    private Long averageCompletionTimeMinutes;

    @DTOParam(12)
    private Date lastUsedDate;

    @DTOParam(13)
    private Date firstUsedDate;

    @DTOParam(14)
    private long totalRefUsers;

    @DTOParam(15)
    private Double averageRefUsersPerCallCard;

    public TemplateUsageDTO() {
    }

    public TemplateUsageDTO(String templateId, String templateName, String userGroupId,
                            Date dateFrom, Date dateTo, long usageCount, long uniqueUsers,
                            long activeCount, long submittedCount, Double completionRate,
                            Long averageCompletionTimeMinutes, Date lastUsedDate,
                            Date firstUsedDate, long totalRefUsers, Double averageRefUsersPerCallCard) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.userGroupId = userGroupId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.usageCount = usageCount;
        this.uniqueUsers = uniqueUsers;
        this.activeCount = activeCount;
        this.submittedCount = submittedCount;
        this.completionRate = completionRate;
        this.averageCompletionTimeMinutes = averageCompletionTimeMinutes;
        this.lastUsedDate = lastUsedDate;
        this.firstUsedDate = firstUsedDate;
        this.totalRefUsers = totalRefUsers;
        this.averageRefUsersPerCallCard = averageRefUsersPerCallCard;
    }

    // Getters and Setters
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

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(long usageCount) {
        this.usageCount = usageCount;
    }

    public long getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(long uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }

    public long getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(long activeCount) {
        this.activeCount = activeCount;
    }

    public long getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(long submittedCount) {
        this.submittedCount = submittedCount;
    }

    /**
     * Alias for getSubmittedCount() for backward compatibility
     */
    public long getSubmittedCallCards() {
        return submittedCount;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Long getAverageCompletionTimeMinutes() {
        return averageCompletionTimeMinutes;
    }

    public void setAverageCompletionTimeMinutes(Long averageCompletionTimeMinutes) {
        this.averageCompletionTimeMinutes = averageCompletionTimeMinutes;
    }

    public Date getLastUsedDate() {
        return lastUsedDate;
    }

    public void setLastUsedDate(Date lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    public Date getFirstUsedDate() {
        return firstUsedDate;
    }

    public void setFirstUsedDate(Date firstUsedDate) {
        this.firstUsedDate = firstUsedDate;
    }

    public long getTotalRefUsers() {
        return totalRefUsers;
    }

    public void setTotalRefUsers(long totalRefUsers) {
        this.totalRefUsers = totalRefUsers;
    }

    public Double getAverageRefUsersPerCallCard() {
        return averageRefUsersPerCallCard;
    }

    public void setAverageRefUsersPerCallCard(Double averageRefUsersPerCallCard) {
        this.averageRefUsersPerCallCard = averageRefUsersPerCallCard;
    }

    @Override
    public String toString() {
        return "TemplateUsageDTO{" +
                "templateId='" + templateId + '\'' +
                ", templateName='" + templateName + '\'' +
                ", userGroupId='" + userGroupId + '\'' +
                ", usageCount=" + usageCount +
                ", uniqueUsers=" + uniqueUsers +
                ", completionRate=" + completionRate +
                ", lastUsedDate=" + lastUsedDate +
                '}';
    }
}
