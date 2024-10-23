package com.colak.springtutorial.repository;

import com.colak.springtutorial.jpa.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

