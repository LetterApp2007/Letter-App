package com.ariyo.chatapp;

import java.util.List;

public class UserAccount {
    String name;
    String age;
    String phone;
    String image;



    String uid;
    List contactList;


    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    public String getImage() {
        return image;
    }

    public List getContactList() {
        return contactList;
    }

    public String getUid() {
        return uid;
    }

    public UserAccount(String name, String age, String phone, String image, List contactList, String uid) {
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.image = image;
        this.contactList=contactList;
        this.uid=uid;
    }
}
