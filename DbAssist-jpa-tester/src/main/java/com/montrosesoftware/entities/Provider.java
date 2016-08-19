package com.montrosesoftware.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "providers", schema = "jpa")
public class Provider {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private boolean active;

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "provider")
    private List<Certificate> certificates = new ArrayList<>();

    public Provider(){}

    public Provider(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Certificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
    }

    public void addCertificate(Certificate certificate){
        this.certificates.add(certificate);
        if(certificate.getProvider() != this){
            certificate.setProvider(this);
        }
    }
}
