package com.saicon.games.entities.shared;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Simplified UserGroups entity for CallCard microservice
 */
@Entity
@Immutable
@Table(name = "USER_GROUPS")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.entities.shared.UserGroups.findAll", query = "SELECT ug FROM UserGroups ug"),
        @NamedQuery(name = "com.saicon.games.entities.shared.UserGroups.listAll", query = "SELECT ug FROM UserGroups ug"),
        @NamedQuery(name = "com.saicon.games.entities.shared.UserGroups.findByUserGroupId", query = "SELECT ug FROM UserGroups ug WHERE ug.groupId = ?"),
        @NamedQuery(name = "com.saicon.games.entities.shared.UserGroups.findByIds", query = "SELECT ug FROM UserGroups ug where groupId IN ?1"),
        @NamedQuery(name = "com.saicon.games.entities.shared.UserGroups.listByUserGroupId", query = "SELECT ug FROM UserGroups ug WHERE ug.groupId = ?1 AND ug.isActive = true ")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class UserGroups implements Serializable {

    public static final String REGISTRATION_REQUIRED_FIELD_FIRST_NAME = "firstName";
    public static final String REGISTRATION_REQUIRED_FIELD_LAST_NAME = "lastName";
    public static final String REGISTRATION_REQUIRED_FIELD_EMAIL = "email";

    @Id
    @Column(name = "GROUP_ID", nullable = false, columnDefinition = "uniqueIdentifier(36)")
    private String groupId;

    @Column(name = "GROUP_NAME", nullable = false, columnDefinition = "varchar(50)")
    private String groupName;

    @Column(name = "DATE_ADDED", nullable = false, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAdded;

    @Column(name = "IS_ACTIVE", nullable = false, columnDefinition = "tinyint")
    private boolean isActive;

    @Column(name = "IS_FACEBOOK", nullable = false)
    private boolean isFacebook;

    @Column(name = "REQUIRED_REGISTRATION_FIELDS", columnDefinition = "nvarchar(200)")
    private String requiredRegistrationFields;

    @Column(name = "REGISTRATION_WITH_ACTIVATION", nullable = false, columnDefinition = "bit")
    private boolean registrationWithActivation;

    @Column(name = "SESSION_ELEVATION_REQUIRED", nullable = false, columnDefinition = "bit")
    private boolean sessionElevationRequired;

    @Column(name = "IMPORT_EXTERNAL_SYSTEM_USER_DETAILS_ON_LOGIN", nullable = false, columnDefinition = "bit")
    private boolean importExternalSystemUserDetailsOnLogin;

    @Column(name = "CHECK_PASSWORD_COMPLEXITY", nullable = false, columnDefinition = "bit")
    private boolean checkPasswordComplexity;

    @Column(name = "MAX_FAILED_LOGINS", nullable = true, columnDefinition = "int")
    private Integer maxFailedLogins;

    @Column(name = "REQUIRES_EMAIL_VERIFICATION", nullable = false, columnDefinition = "bit")
    private boolean requiresEmailVerification;

    @Column(name = "REQUIRES_MOBILE_VERIFICATION", nullable = false, columnDefinition = "bit")
    private boolean requiresMobileVerification;

    @Column(name = "UNIQUE_USER_BY_EMAIL", nullable = false, columnDefinition = "bit")
    private boolean uniqueUserByEmail;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isFacebook() {
        return isFacebook;
    }

    public void setIsFacebook(boolean isFacebook) {
        this.isFacebook = isFacebook;
    }

    public String getRequiredRegistrationFields() {
        return requiredRegistrationFields;
    }

    public void setRequiredRegistrationFields(String requiredRegistrationFields) {
        this.requiredRegistrationFields = requiredRegistrationFields;
    }

    public boolean isRegistrationWithActivation() {
        return registrationWithActivation;
    }

    public void setRegistrationWithActivation(boolean registrationWithActivation) {
        this.registrationWithActivation = registrationWithActivation;
    }

    public boolean isSessionElevationRequired() {
        return sessionElevationRequired;
    }

    public void setSessionElevationRequired(boolean sessionElevationRequired) {
        this.sessionElevationRequired = sessionElevationRequired;
    }

    public boolean isImportExternalSystemUserDetailsOnLogin() {
        return importExternalSystemUserDetailsOnLogin;
    }

    public void setImportExternalSystemUserDetailsOnLogin(boolean importExternalSystemUserDetailsOnLogin) {
        this.importExternalSystemUserDetailsOnLogin = importExternalSystemUserDetailsOnLogin;
    }

    public Integer getMaxFailedLogins() {
        return maxFailedLogins;
    }

    public void setMaxFailedLogins(Integer maxFailedLogins) {
        this.maxFailedLogins = maxFailedLogins;
    }

    public boolean isCheckPasswordComplexity() {
        return checkPasswordComplexity;
    }

    public void setCheckPasswordComplexity(boolean checkPasswordComplexity) {
        this.checkPasswordComplexity = checkPasswordComplexity;
    }

    public boolean isRequiresEmailVerification() {
        return requiresEmailVerification;
    }

    public void setRequiresEmailVerification(boolean requiresEmailVerification) {
        this.requiresEmailVerification = requiresEmailVerification;
    }

    public boolean isRequiresMobileVerification() {
        return requiresMobileVerification;
    }

    public void setRequiresMobileVerification(boolean requiresMobileVerification) {
        this.requiresMobileVerification = requiresMobileVerification;
    }

    public boolean isUniqueUserByEmail() {
        return uniqueUserByEmail;
    }

    public void setUniqueUserByEmail(boolean uniqueUserByEmail) {
        this.uniqueUserByEmail = uniqueUserByEmail;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserGroups)) {
            return false;
        }
        UserGroups other = (UserGroups) object;
        if ((this.groupId == null && other.groupId != null) || (this.groupId != null && !this.groupId.equalsIgnoreCase(other.groupId))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (groupId != null ? groupId.hashCode() : 0);
        return hash;
    }
}
