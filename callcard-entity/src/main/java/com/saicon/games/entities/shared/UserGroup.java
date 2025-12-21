package com.saicon.games.entities.shared;

import javax.persistence.*;
import java.io.Serializable;

/**
 * User Group entity stub.
 * Represents organizational grouping of users.
 */
@Entity
@Table(name = "USER_GROUP")
public class UserGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_GROUP_ID")
    private String userGroupId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    public UserGroup() {}

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
