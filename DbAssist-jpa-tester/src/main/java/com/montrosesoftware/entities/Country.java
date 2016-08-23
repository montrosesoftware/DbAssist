package com.montrosesoftware.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "countries", schema = "jpa")
public class Country {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "country")
    private Set<Provider> providers = new HashSet<>();

    public Country() {}

    public Country(int id, String name) {
        this.id = id;
        this.name = name;
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

    public Set<Provider> getProviders() {
        return providers;
    }

    public void setProviders(Set<Provider> providers) {
        this.providers = providers;
    }

    public void addProviders(Provider provider){
        this.providers.add(provider);
        if(provider.getCountry() != this){
            provider.setCountry(this);
        }
    }
}
