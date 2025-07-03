package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.cache.ProductCache;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCache productCache;

    public List<Product> getProducts() {
        // 1. Redis 캐시 확인
        List<Product> cached = productCache.getAllProducts();
        if (cached != null) {
            return cached;
        }

        // 2. DB 조회
        List<Product> products = productRepository.findAll();

        // 3. 캐시에 저장
        productCache.saveAllProducts(products);

        // 4. 반환
        return products;
    }
}
