package com.codewithpaolo.frischcheck.repository;

import com.codewithpaolo.frischcheck.model.Product;
import com.codewithpaolo.frischcheck.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** FEFO: was zuerst abläuft, steht oben. */
    List<Product> findByStatusOrderByExpiryDateAscNameAsc(ProductStatus status);

    List<Product> findAllByOrderByExpiryDateAscNameAsc();
}
