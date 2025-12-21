package com.saicon.games.callcard.ws.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * User engagement metrics DTO for call card analytics.
 */
public class UserEngagementDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String userName;
    private int totalCallCards;
    private int completedCallCards;
    private int pendingCallCards;
    private Date lastActivityDate;
    private double completionRate;

    public UserEngagementDTO() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getTotalCallCards() { return totalCallCards; }
    public void setTotalCallCards(int totalCallCards) { this.totalCallCards = totalCallCards; }

    public int getCompletedCallCards() { return completedCallCards; }
    public void setCompletedCallCards(int completedCallCards) { this.completedCallCards = completedCallCards; }

    public int getPendingCallCards() { return pendingCallCards; }
    public void setPendingCallCards(int pendingCallCards) { this.pendingCallCards = pendingCallCards; }

    public Date getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(Date lastActivityDate) { this.lastActivityDate = lastActivityDate; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
}
