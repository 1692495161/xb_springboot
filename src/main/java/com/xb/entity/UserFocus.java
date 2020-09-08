package com.xb.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "userfocus")
@IdClass(UserFocus.class)
public class UserFocus implements Serializable {

    @Id
    private Long userId;

    @Id
    private Long userFocusId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserFocusId() {
        return userFocusId;
    }

    public void setUserFocusId(Long userFocusId) {
        this.userFocusId = userFocusId;
    }
}
