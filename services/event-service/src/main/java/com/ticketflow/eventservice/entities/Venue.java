package com.ticketflow.eventservice.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    private String address;

    @Column(nullable = false)
    private int capacity;

    protected Venue() { }

    public Venue(String name, String city, String address, int capacity) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public int getCapacity() { return capacity; }
}