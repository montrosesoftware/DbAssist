package com.montrosesoftware;

import java.sql.Timestamp;
import java.util.Date;

public class User {

    private int id;
    private String name;
    private Date createdAt;
    private Timestamp updatedAt;
    private Date lastLoggedAt;

    public User(){}

    public User(int id, String name, Date createdAt, Timestamp updatedAt, Date lastLoggedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoggedAt = lastLoggedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getLastLoggedAt() {
        return lastLoggedAt;
    }

    public void setLastLoggedAt(Date lastLoggedAt) {
        this.lastLoggedAt = lastLoggedAt;
    }
}