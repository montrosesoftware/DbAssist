package com.montrosesoftware;

import java.sql.Timestamp;
import java.util.Date;

public class User {

    private int id;
    private String name;
    private Date createdAtUtc;
    private Timestamp updatedAtUtc;
    private Date lastLoggedAtUtc;

    public User(){}

    public User(int id, String name, Date createdAtUtc, Timestamp updatedAtUtc, Date lastLoggedAtUtc) {
        this.id = id;
        this.name = name;
        this.createdAtUtc = createdAtUtc;
        this.updatedAtUtc = updatedAtUtc;
        this.lastLoggedAtUtc = lastLoggedAtUtc;
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

    public Date getCreatedAtUtc() {
        return createdAtUtc;
    }

    public void setCreatedAtUtc(Date createdAtUtc) {
        this.createdAtUtc = createdAtUtc;
    }

    public Timestamp getUpdatedAtUtc() {
        return updatedAtUtc;
    }

    public void setUpdatedAtUtc(Timestamp updatedAtUtc) {
        this.updatedAtUtc = updatedAtUtc;
    }

    public Date getLastLoggedAtUtc() {
        return lastLoggedAtUtc;
    }

    public void setLastLoggedAtUtc(Date lastLoggedAtUtc) {
        this.lastLoggedAtUtc = lastLoggedAtUtc;
    }
}