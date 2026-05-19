package uk.co.next.loyalty.infrastructure.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisBalanceCache {

    private static final Duration TTL = Duration.ofSeconds(2);
    private static final String KEY_PREFIX = "customer:%s:balance";

    private final StringRedisTemplate redisTemplate;

    public RedisBalanceCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<Integer> getBalance(UUID customerId) {
        String val = redisTemplate.opsForValue().get(KEY_PREFIX.formatted(customerId));
        return val != null ? Optional.of(Integer.parseInt(val)) : Optional.empty();
    }

    public void setBalance(UUID customerId, int balance) {
        redisTemplate.opsForValue().set(KEY_PREFIX.formatted(customerId), String.valueOf(balance), TTL);
    }

    public void invalidate(UUID customerId) {
        redisTemplate.delete(KEY_PREFIX.formatted(customerId));
    }
}
