package com.luisgenesius.chawy.Chat;

import com.luisgenesius.chawy.User.User;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    private String chatId;

    private ArrayList<User> userArrayList = new ArrayList<>();

    public Chat(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public ArrayList<User> getUserArrayList() { return userArrayList; }

    public void addUserToUserList(User user) {
        userArrayList.add(user);
    }
}
