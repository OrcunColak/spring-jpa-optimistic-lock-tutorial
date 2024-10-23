package com.colak.springtutorial.service;

import com.colak.springtutorial.jpa.Product;
import com.colak.springtutorial.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public void updateProductPrice(Long productId, Double newPrice) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setPrice(newPrice);
            productRepository.save(product);

        } catch (OptimisticLockException exception) {
            // Handle the conflict, e.g., by showing an error message or retrying the transaction
            throw new RuntimeException("Conflict detected: Product was modified concurrently.");
        }
    }
}

