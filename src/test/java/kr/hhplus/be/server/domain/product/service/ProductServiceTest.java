package kr.hhplus.be.server.domain.product.service;

import domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class ProductServiceTest {

    private ProductService productService;
    private ProductRepository productRepository;
    private ProductCacheRepository productCacheRepository;

    private final List<Product> mockProductList = List.of(
            Product.builder().id(1L).name("상품A").price(1000L).stock(10).build(),
            Product.builder().id(2L).name("상품B").price(2000L).stock(5).build()
    );

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productCacheRepository = mock(ProductCacheRepository.class);
        productService = new ProductService(productRepository, productCacheRepository);
    }

    @Test
    @DisplayName("캐시에 상품 목록이 존재하면 DB 조회 없이 캐시 반환")
    void getProducts_from_cache() {
        //given
        when(productCacheRepository.getAllProducts()).thenReturn(mockProductList);

        // when
        List<Product> result = productService.getProducts();

        // then
        verify(productCacheRepository, times(1)).getAllProducts();
        verify(productRepository, never()).findAll();
        assertThat(result).isEqualTo(mockProductList);
    }

    @Test
    @DisplayName("캐시에 없으면 DB 조회 후 캐시에 저장하고 반환")
    void getProducts_from_db_then_cache() {
        // given
        when(productCacheRepository.getAllProducts()).thenReturn(null);
        when(productRepository.findAll()).thenReturn(mockProductList);

        // when
        List<Product> result = productService.getProducts();

        // then
        verify(productRepository, times(1)).findALl();
        verify(productCacheRepository, times(1)).saveAllProducts(mockProductList);
        assertThat(result).isEqualTo(mockProductList);
    }
}