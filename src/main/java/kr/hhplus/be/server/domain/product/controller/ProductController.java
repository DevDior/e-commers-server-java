package kr.hhplus.be.server.domain.product.controller;

import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/product")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        log.info("[ProductController] GET /api/product 요청 수신");

        List<Product> products = productService.getAllProducts();

        log.info("[ProductController] 총 {}개의 상품 반환", products.size());
        return products;
    }
}
