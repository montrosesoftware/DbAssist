package com.montrosesoftware.entities;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "certificates", schema = "jpa")
public class Certificate {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @ManyToMany(mappedBy="certificates")
    private Set<User> users = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "mainCertificate")
    private Set<User> usersOfMainCert = new HashSet<>();

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    public Certificate () {}

    public Certificate(int id, String name, Date expirationDate) {
        this.id = id;
        this.name = name;
        this.expirationDate = expirationDate;
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

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user){
        this.users.add(user);
        if(!user.getCertificates().contains(this)){
            user.getCertificates().add(this);
        }
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        if(!provider.getCertificates().contains(this))
            provider.getCertificates().add(this);
    }

    public Set<User> getUsersOfMainCert() {
        return usersOfMainCert;
    }

    public void setUsersOfMainCert(Set<User> usersOfMainCert) {
        this.usersOfMainCert = usersOfMainCert;
    }

    public void addUserOfMainCert(User user){
        this.usersOfMainCert.add(user);
        if(user.getMainCertificate()!= this)
            user.setMainCertificate(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Certificate that = (Certificate) o;

        if (id != that.id) return false;
        if (!name.equals(that.name)) return false;
        return expirationDate != null ? expirationDate.equals(that.expirationDate) : that.expirationDate == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + (expirationDate != null ? expirationDate.hashCode() : 0);
        return result;
    }
}
