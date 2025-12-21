package com.saicon.games.callcard.ws.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * User engagement metrics DTO for call card analytics.
 * FIXED VERSION - Copy this to: callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/dto/UserEngagementDTO.java
 */
public class UserEngagementDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String userName;
    private String userGroupId;
    private Date dateFrom;
    private Date dateTo;
    private long totalCallCards;
    private long activeCallCards;
    private long submittedCallCards;
    private int completedCallCards;
    private int pendingCallCards;
    private Date lastActivityDate;
    private Date firstActivityDate;
    private Integer activityDaysCount;
    private double completionRate;
    private long totalRefUsers;
    private Double averageRefUsersPerCallCard;
    private long uniqueTemplatesUsed;
    private Long averageCompletionTimeMinutes;
    private String mostUsedTemplateId;
    private String mostUsedTemplateName;

    public UserEngagementDTO() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserGroupId() { return userGroupId; }
    public void setUserGroupId(String userGroupId) { this.userGroupId = userGroupId; }

    public Date getDateFrom() { return dateFrom; }
    public void setDateFrom(Date dateFrom) { this.dateFrom = dateFrom; }

    public Date getDateTo() { return dateTo; }
    public void setDateTo(Date dateTo) { this.dateTo = dateTo; }

    public long getTotalCallCards() { return totalCallCards; }
    public void setTotalCallCards(long totalCallCards) { this.totalCallCards = totalCallCards; }

    public long getActiveCallCards() { return activeCallCards; }
    public void setActiveCallCards(long activeCallCards) { this.activeCallCards = activeCallCards; }

    public long getSubmittedCallCards() { return submittedCallCards; }
    public void setSubmittedCallCards(long submittedCallCards) { this.submittedCallCards = submittedCallCards; }

    public int getCompletedCallCards() { return completedCallCards; }
    public void setCompletedCallCards(int completedCallCards) { this.completedCallCards = completedCallCards; }

    public int getPendingCallCards() { return pendingCallCards; }
    public void setPendingCallCards(int pendingCallCards) { this.pendingCallCards = pendingCallCards; }

    public Date getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(Date lastActivityDate) { this.lastActivityDate = lastActivityDate; }

    public Date getFirstActivityDate() { return firstActivityDate; }
    public void setFirstActivityDate(Date firstActivityDate) { this.firstActivityDate = firstActivityDate; }

    public Integer getActivityDaysCount() { return activityDaysCount; }
    public void setActivityDaysCount(Integer activityDaysCount) { this.activityDaysCount = activityDaysCount; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public long getTotalRefUsers() { return totalRefUsers; }
    public void setTotalRefUsers(long totalRefUsers) { this.totalRefUsers = totalRefUsers; }

    public Double getAverageRefUsersPerCallCard() { return averageRefUsersPerCallCard; }
    public void setAverageRefUsersPerCallCard(Double averageRefUsersPerCallCard) {
        this.averageRefUsersPerCallCard = averageRefUsersPerCallCard;
    }

    public long getUniqueTemplatesUsed() { return uniqueTemplatesUsed; }
    public void setUniqueTemplatesUsed(long uniqueTemplatesUsed) { this.uniqueTemplatesUsed = uniqueTemplatesUsed; }

    public Long getAverageCompletionTimeMinutes() { return averageCompletionTimeMinutes; }
    public void setAverageCompletionTimeMinutes(Long averageCompletionTimeMinutes) {
        this.averageCompletionTimeMinutes = averageCompletionTimeMinutes;
    }

    public String getMostUsedTemplateId() { return mostUsedTemplateId; }
    public void setMostUsedTemplateId(String mostUsedTemplateId) { this.mostUsedTemplateId = mostUsedTemplateId; }

    public String getMostUsedTemplateName() { return mostUsedTemplateName; }
    public void setMostUsedTemplateName(String mostUsedTemplateName) { this.mostUsedTemplateName = mostUsedTemplateName; }
}
