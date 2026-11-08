package com.ticketflow.eventservice.repos;

import com.ticketflow.eventservice.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, Long> { }