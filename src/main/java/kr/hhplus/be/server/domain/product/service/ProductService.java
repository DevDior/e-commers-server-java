package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.cache.ProductCache;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCache productCache;

    public List<Product> getAllProducts() {
        log.info("[ProductService] getProducts() 호출");

        // 1. Redis 캐시 확인
        List<Product> cached = productCache.getAllProducts();
        if (cached != null) {
            log.info("[ProductService] 캐시 HIT: {}개의 상품 반환", cached.size());
            return cached;
        }
        log.info("[ProductService] 캐시 MISS: 캐시에 상품 데이터가 없음");

        // 2. DB 조회
        List<Product> products = productRepository.findAll();
        log.info("[ProductService] DB에서 {}개의 상품 조회 완료", products.size());

        // 3. 캐시에 저장
        productCache.saveAllProducts(products);
        log.info("[ProductService] {}개의 상품을 캐시에 저장 완료", products.size());

        // 4. 반환
        return products;
    }
}
