package com.montrosesoftware;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Certificates")
public class Certificate {

    @Id @GeneratedValue
    @Column(name = "Id")
    private int id;

    @Column(name = "Name")
    private String name;

    @Column(name = "ExpirationDate")
    private Date expirationDate;

    public Certificate(){}

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

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
