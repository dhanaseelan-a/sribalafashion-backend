package com.sribalafashion.product;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#category ?: 'ALL'")
    public List<Product> getAllProducts(String category) {
        if (category != null && !category.isEmpty()) {
            return productRepository.findByCategory(category);
        }
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProductsPaginated(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @CacheEvict(value = { "products", "product" }, allEntries = true)
    public Product createProduct(Product product) {
        if (product.getSizeVariants() != null) {
            for (ProductSizeVariant variant : product.getSizeVariants()) {
                variant.setProduct(product);
            }
        }
        return productRepository.save(product);
    }

    @CacheEvict(value = { "products", "product" }, allEntries = true)
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStock(productDetails.getStock());
        product.setDiscountPercent(productDetails.getDiscountPercent());

        // Update size variants
        product.getSizeVariants().clear();
        if (productDetails.getSizeVariants() != null) {
            for (ProductSizeVariant variant : productDetails.getSizeVariants()) {
                variant.setProduct(product);
                product.getSizeVariants().add(variant);
            }
        }

        return productRepository.save(product);
    }

    @CacheEvict(value = { "products", "product" }, allEntries = true)
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
