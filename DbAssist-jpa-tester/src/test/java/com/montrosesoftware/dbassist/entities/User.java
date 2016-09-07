package com.montrosesoftware.dbassist.entities;

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

    @Column(name = "created_at_utc")
    private Date createdAtUtc;

    @Column(name = "updated_at_utc")
    private Timestamp updatedAtUtc;

    @Column(name = "last_logged_at_utc")
    private Date lastLoggedAtUtc;

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

    public User(int id, String name, Date createdAtUtc){
        this.id = id;
        this.name = name;
        this.createdAtUtc = createdAtUtc;
        this.updatedAtUtc = new Timestamp(createdAtUtc.getTime());
        this.lastLoggedAtUtc = new Date(createdAtUtc.getTime());
    }

    public User(int id, String name, Date createdAtUtc, Timestamp updatedAtUtc, Date lastLoggedAtUtc) {
        this.id = id;
        this.name = name;
        this.createdAtUtc = createdAtUtc;
        this.updatedAtUtc = updatedAtUtc;
        this.lastLoggedAtUtc = lastLoggedAtUtc;
    }

    public User(int id, String name, Date createdAtUtc, Double salary, String category) {
        this.id = id;
        this.name = name;
        this.createdAtUtc = createdAtUtc;
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
