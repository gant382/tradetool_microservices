package com.saicon.games.entities.shared;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Simplified Users entity for CallCard microservice
 * Contains only essential fields needed for CallCard operations
 */
@Entity
@Table(name = "USERS")
@NamedQueries({
        @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
        @NamedQuery(name = "com.saicon.games.entities.shared.Users.findByUserId", query = "SELECT u FROM Users u WHERE u.userId = ?"),
        @NamedQuery(name = "com.saicon.games.entities.shared.Users.listByUserIds", query = "SELECT u FROM Users u WHERE u.userId IN ?1"),
        @NamedQuery(name = "com.saicon.games.entities.shared.Users.findByUsername", query = "SELECT u FROM Users u WHERE u.username = ?")
})
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_ID", nullable = false, columnDefinition = "uniqueIdentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.entities.common.UUIDGenerator")
    private String userId;

    @Column(name = "EXTERNAL_SYSTEM_ID", nullable = true, columnDefinition = "nvarchar(100)")
    private String externalSystemId;

    @Basic(optional = false)
    @Column(name = "LOGIN_NAME", nullable = false, columnDefinition = "nvarchar(255)")
    private String username;

    @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID", nullable = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private UserGroups userGroup;

    @Column(name = "USERNAME", columnDefinition = "nvarchar(255)")
    private String nickname;

    @Column(name = "IS_GUEST", nullable = false, columnDefinition = "bit")
    private boolean guest;

    @Column(name = "EMAIL", columnDefinition = "nvarchar(250)")
    private String email;

    @Column(name = "MOBILE", columnDefinition = "nvarchar(100)")
    private String mobile;

    @Column(name = "FIRST_NAME", columnDefinition = "nvarchar(100)")
    private String firstName;

    @Column(name = "LAST_NAME", columnDefinition = "nvarchar(100)")
    private String lastName;

    @Column(name = "IS_BOT", nullable = false, updatable = false, insertable = true)
    private Boolean isBot = Boolean.FALSE;

    @Column(name = "CREATED_ON", nullable = true, columnDefinition = "datetime", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "LAST_UPDATED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    public Users() {
    }

    public Users(String userId) {
        this.userId = userId;
    }

    public Users(String userId, String username) {
        this.userId = userId;
        this.username = username;
        guest = false;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupId() {
        return userGroup.getGroupId();
    }

    public UserGroups getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroups userGroup) {
        this.userGroup = userGroup;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getBot() {
        return isBot;
    }

    public void setBot(Boolean bot) {
        isBot = bot;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public java.util.List<Object> getUserAddress() {
        return new java.util.ArrayList<>(); // Stub implementation for CallCard microservice
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equalsIgnoreCase(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Users{" +
                "userId=" + userId +
                ", username=" + username +
                ", nickname=" + nickname +
                ", email=" + email +
                '}';
    }
}
