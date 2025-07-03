package kr.hhplus.be.server.domain.product.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCache {

    private static final String KEY = "products:all";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public List<Product> getAllProducts() {
        try {
            String json = redisTemplate.opsForValue().get(KEY);
            if (json == null) {
                log.info("ProductCache MISS: no cache for key '{}'", KEY);
                return null;
            }
            List<Product> result = objectMapper.readValue(json, new TypeReference<>() {});
            log.info("ProductCache HIT: {} products returned from cache", result.size());
            return result;
        } catch (Exception e) {
            log.warn("ProductCache Error while getting cache: {}", e.getMessage());
            return null;
        }
    }

    public void saveAllProducts(List<Product> products) {
        try {
            String json = objectMapper.writeValueAsString(products);
            redisTemplate.opsForValue().set(KEY, json);
            log.info("ProductCache SAVED: {} products cached under key '{}'", products.size(), KEY);
        } catch (Exception e) {
            log.warn("ProductCache Error while saving cachee: {}", e.getMessage());
        }
    }
}
