package com.saicon.games.callcard.entity;

import com.saicon.games.entities.shared.Users;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by user101 on 25/2/2016.
 */

@Entity
@Table(name = "CALL_CARD_TEMPLATE_USER")
@NamedQueries({
        @NamedQuery(name = "com.saicon.games.callcard.entity.CallCardTemplateUser.findTemplateUserByUserId", query = "SELECT tu FROM CallCardTemplateUser tu WHERE tu.userId.userId = ?1")
})
public class CallCardTemplateUser implements Serializable {

    @Id
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @OneToOne(optional = false)
    private Users userId;

    @JoinColumn(name = "CALL_CARD_TEMPLATE_ID",referencedColumnName = "CALL_CARD_TEMPLATE_ID", nullable = false)
    @ManyToOne(optional = false)
    private CallCardTemplate callCardTemplateId;

    public CallCardTemplateUser() {
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public CallCardTemplate getCallCardTemplateId() {
        return callCardTemplateId;
    }

    public void setCallCardTemplateId(CallCardTemplate callCardTemplateId) {
        this.callCardTemplateId = callCardTemplateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallCardTemplateUser that = (CallCardTemplateUser) o;

        if (!userId.equals(that.userId)) return false;
        return callCardTemplateId.equals(that.callCardTemplateId);
    }

    @Override
    public int hashCode() {
        int result = userId.getUserId().hashCode();
        result = 31 * result + callCardTemplateId.getCallCardTemplateId().hashCode();
        return result;
    }
}
