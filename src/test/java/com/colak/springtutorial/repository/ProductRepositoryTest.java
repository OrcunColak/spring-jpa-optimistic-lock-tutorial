package com.colak.springtutorial.repository;

import com.colak.springtutorial.jpa.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testOptimisticLockingWithForcedConcurrency() {
        // Latch to synchronize both threads
        CountDownLatch latch = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Long productId = 1L;

            // Thread 1: This transaction will update the product first
            executor.submit(() -> updateProductConcurrently1(productId, 1100.0, latch));

            // Thread 2: This transaction will attempt to update the product concurrently
            Future<Void> future2 = executor.submit(() -> {
                updateProductConcurrently2(productId, 1200.0, latch); // This should fail due to version mismatch
                return null;
            });


            // Expect OptimisticLockingFailureException to be thrown, but unwrap ExecutionException first
            ExecutionException thrownException = Assertions.assertThrows(ExecutionException.class, future2::get);

            // Now check if the cause of the ExecutionException is the expected OptimisticLockingFailureException
            Assertions.assertInstanceOf(OptimisticLockingFailureException.class, thrownException.getCause());
        }
    }

    @Transactional
    public void updateProductConcurrently1(Long productId, Double newPrice, CountDownLatch latch) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.setPrice(newPrice);
        productRepository.saveAndFlush(product); // This will trigger the version check

        // Release the latch, allowing thread2 to start
        latch.countDown();
    }

    @Transactional
    public void updateProductConcurrently2(Long productId, Double newPrice, CountDownLatch latch) throws InterruptedException {
        Product product = productRepository.findById(productId).orElseThrow();

        // Wait for transaction 1 to finish
        latch.await();
        product.setPrice(newPrice);
        productRepository.saveAndFlush(product); // This will trigger the version check
    }
}

