package com.montrosesoftware.entities;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", schema = "jpa")
public class User {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "last_logged_at")
    private Date lastLoggedAt; //TODO think about it

    @Column(name = "salary")
    private Double salary;

    @Column(name = "category")
    private String category;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_certificates",
            schema = "jpa",
            joinColumns=@JoinColumn(name="user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="cert_id", referencedColumnName = "id"))
    private Set<Certificate> certificates = new HashSet<>();

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "main_cert_id")
    private Certificate mainCertificate;

    public User(){}

    public User(int id, String name, Date createdAt){
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = new Timestamp(createdAt.getTime());
        this.lastLoggedAt = new Date(createdAt.getTime());
    }

    public User(int id, String name, Date createdAt, Timestamp updatedAt, Date lastLoggedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoggedAt = lastLoggedAt;
    }

    public User(int id, String name, Date createdAt, Double salary, String category) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.salary = salary;
        this.category = category;
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

    public Double getSalary() { return salary; }

    public void setSalary(Double salary) { this.salary = salary; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public Set<Certificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(Set<Certificate> certificates) {
        this.certificates = certificates;
    }

    public void addCertificate(Certificate cert){
        this.certificates.add(cert);
        if(!cert.getUsers().contains(this)){
            cert.getUsers().add(this);
        }
    }

    public Certificate getMainCertificate() {
        return mainCertificate;
    }

    public void setMainCertificate(Certificate cert) {
        this.mainCertificate = cert;
        if(!cert.getUsersOfMainCert().contains(this))
            cert.getUsersOfMainCert().add(this);
    }
}
