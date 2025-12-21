package com.saicon.games.callcard.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saicon.games.callcard.util.DTOParam;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * CallCard Statistics DTO - Overall statistics
 *
 * Provides comprehensive statistics for CallCards within a userGroup and date range:
 * - Total CallCards created
 * - Active vs Submitted CallCards
 * - Total users engaged
 * - Most used templates
 * - Average completion time
 *
 * Use case: Dashboard overview, reporting, analytics
 */
@XmlRootElement(name = "CallCardStats")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallCardStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @DTOParam(1)
    private String userGroupId;

    @DTOParam(2)
    private Date dateFrom;

    @DTOParam(3)
    private Date dateTo;

    @DTOParam(4)
    private long totalCallCards;

    @DTOParam(5)
    private long activeCallCards;

    @DTOParam(6)
    private long submittedCallCards;

    @DTOParam(7)
    private long totalUsers;

    @DTOParam(8)
    private long totalTemplates;

    @DTOParam(9)
    private long totalRefUsers;

    @DTOParam(10)
    private Double averageUsersPerCallCard;

    @DTOParam(11)
    private Double averageCallCardsPerUser;

    @DTOParam(12)
    private Long averageCompletionTimeMinutes;

    public CallCardStatsDTO() {
    }

    public CallCardStatsDTO(String userGroupId, Date dateFrom, Date dateTo,
                            long totalCallCards, long activeCallCards, long submittedCallCards,
                            long totalUsers, long totalTemplates, long totalRefUsers,
                            Double averageUsersPerCallCard, Double averageCallCardsPerUser,
                            Long averageCompletionTimeMinutes) {
        this.userGroupId = userGroupId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.totalCallCards = totalCallCards;
        this.activeCallCards = activeCallCards;
        this.submittedCallCards = submittedCallCards;
        this.totalUsers = totalUsers;
        this.totalTemplates = totalTemplates;
        this.totalRefUsers = totalRefUsers;
        this.averageUsersPerCallCard = averageUsersPerCallCard;
        this.averageCallCardsPerUser = averageCallCardsPerUser;
        this.averageCompletionTimeMinutes = averageCompletionTimeMinutes;
    }

    // Getters and Setters
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

    public long getTotalCallCards() {
        return totalCallCards;
    }

    public void setTotalCallCards(long totalCallCards) {
        this.totalCallCards = totalCallCards;
    }

    public long getActiveCallCards() {
        return activeCallCards;
    }

    public void setActiveCallCards(long activeCallCards) {
        this.activeCallCards = activeCallCards;
    }

    public long getSubmittedCallCards() {
        return submittedCallCards;
    }

    public void setSubmittedCallCards(long submittedCallCards) {
        this.submittedCallCards = submittedCallCards;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalTemplates() {
        return totalTemplates;
    }

    public void setTotalTemplates(long totalTemplates) {
        this.totalTemplates = totalTemplates;
    }

    public long getTotalRefUsers() {
        return totalRefUsers;
    }

    public void setTotalRefUsers(long totalRefUsers) {
        this.totalRefUsers = totalRefUsers;
    }

    public Double getAverageUsersPerCallCard() {
        return averageUsersPerCallCard;
    }

    public void setAverageUsersPerCallCard(Double averageUsersPerCallCard) {
        this.averageUsersPerCallCard = averageUsersPerCallCard;
    }

    public Double getAverageCallCardsPerUser() {
        return averageCallCardsPerUser;
    }

    public void setAverageCallCardsPerUser(Double averageCallCardsPerUser) {
        this.averageCallCardsPerUser = averageCallCardsPerUser;
    }

    public Long getAverageCompletionTimeMinutes() {
        return averageCompletionTimeMinutes;
    }

    public void setAverageCompletionTimeMinutes(Long averageCompletionTimeMinutes) {
        this.averageCompletionTimeMinutes = averageCompletionTimeMinutes;
    }

    @Override
    public String toString() {
        return "CallCardStatsDTO{" +
                "userGroupId='" + userGroupId + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", totalCallCards=" + totalCallCards +
                ", activeCallCards=" + activeCallCards +
                ", submittedCallCards=" + submittedCallCards +
                ", totalUsers=" + totalUsers +
                ", totalTemplates=" + totalTemplates +
                ", totalRefUsers=" + totalRefUsers +
                ", averageUsersPerCallCard=" + averageUsersPerCallCard +
                ", averageCallCardsPerUser=" + averageCallCardsPerUser +
                ", averageCompletionTimeMinutes=" + averageCompletionTimeMinutes +
                '}';
    }
}
