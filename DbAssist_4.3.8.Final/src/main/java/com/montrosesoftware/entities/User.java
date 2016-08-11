package com.montrosesoftware.entities;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @Column(name = "Id")
    private int id;

    @Column(name = "Name")
    private String name;

    @Column(name = "createdat")
    private Date createdAt;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_certificates",
            joinColumns=@JoinColumn(name="user_id", referencedColumnName = "Id"),
            inverseJoinColumns = @JoinColumn(name="cert_id", referencedColumnName = "id"))
    private List<Certificate> certificates = new ArrayList<>();

    public User(){}

    public User(int id, String name, Date createdAt){
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
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

    public List<Certificate> getCertificates() {
        return certificates;
    }

    //TODO verify
    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
    }

    public void addCertificate(Certificate cert){
        this.certificates.add(cert);
        if(!cert.getUsers().contains(this)){
            cert.getUsers().add(this);
        }
    }
}
