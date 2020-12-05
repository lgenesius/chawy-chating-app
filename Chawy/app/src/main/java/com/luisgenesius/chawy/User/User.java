package com.luisgenesius.chawy.User;

import java.io.Serializable;

public class User implements Serializable {

    private String uId, name, phone, notificationKey;

    private Boolean selected = false;

    public User(String uId, String name, String phone) {
        this.uId = uId;
        this.name = name;
        this.phone = phone;
    }

    public User(String uId, String name, String phone, String notificationKey) {
        this.uId = uId;
        this.name = name;
        this.phone = phone;
        this.notificationKey = notificationKey;
    }

    public String getuId() {
        return uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public String getNotificationKey() { return notificationKey; }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public Boolean getSelected() { return selected; }

    public void setSelected(Boolean selected) { this.selected = selected; }
}
