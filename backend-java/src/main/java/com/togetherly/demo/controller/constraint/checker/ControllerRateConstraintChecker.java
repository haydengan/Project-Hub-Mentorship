package com.togetherly.demo.controller.constraint.checker;

import com.togetherly.demo.controller.constraint.rate.LimitTarget;
import com.togetherly.demo.controller.constraint.rate.RateLimit;
import com.togetherly.demo.exception.ControllerConstraintViolation;
import com.togetherly.demo.utils.AuthUtil;
import com.togetherly.demo.utils.IPUtils;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

/**
 * Enforces rate limiting on controller methods using a Redis-backed token bucket.
 *
 * TOKEN BUCKET ALGORITHM:
 * - Each user/IP gets a bucket stored in Redis as a hash: {token: N, access_time: timestamp}
 * - On each request: calculate token refill since last access, consume 1 token
 * - If bucket is empty → 429 Too Many Requests
 *
 * OPTIMISTIC LOCKING (Redis WATCH/MULTI/EXEC):
 * Two concurrent requests might both read the same bucket and try to update it.
 * Redis WATCH detects this: if the key was modified between WATCH and EXEC,
 * EXEC returns empty → we retry (up to MAX_RETRY times).
 * This is the same pattern as database optimistic locking but in Redis.
 *
 * HAND-WRITTEN.
 */
@Component
public class ControllerRateConstraintChecker {
    private static final Logger logger =
            LoggerFactory.getLogger(ControllerRateConstraintChecker.class);
    private static final int MAX_RETRY = 3;

    @Autowired private RedisTemplate<String, Map<String, Object>> redisTemplate;

    public void checkWithMethod(Method method) throws ControllerConstraintViolation {
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) return;

        // USER-targeted rate limit only applies to authenticated users
        if (rateLimit.target() == LimitTarget.USER && !AuthUtil.isAuthenticated()) return;

        String targetIdentifier = switch (rateLimit.target()) {
            case USER -> AuthUtil.currentUserDetail().getId();
            default -> IPUtils.getRequestIP();
        };

        checkRateLimitByTokenBucket(
                rateLimit.key(), targetIdentifier, rateLimit.limit(), rateLimit.period());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void checkRateLimitByTokenBucket(
            String key, String targetIdentifier, long limit, long window)
            throws ControllerConstraintViolation {
        final boolean[] isLockFailed = {false};
        final boolean[] isExceed = {false};

        // Retry loop for optimistic lock failures
        for (int retry = MAX_RETRY; retry > 0; retry--) {
            redisTemplate.execute(new SessionCallback<>() {
                @Override
                public List execute(RedisOperations operations) throws DataAccessException {
                    String bucketKey = key + "_bucket_for_" + targetIdentifier;

                    // WATCH: monitor this key for changes by other requests
                    operations.watch(bucketKey);
                    Map bucket = operations.opsForHash().entries(bucketKey);

                    long currentTime = System.currentTimeMillis();

                    if (bucket == null || bucket.isEmpty()) {
                        // First request: create bucket with limit-1 tokens
                        bucket = new HashMap<>();
                        bucket.put("token", limit - 1);
                        bucket.put("access_time", currentTime);
                    } else {
                        // Existing bucket: refill tokens based on elapsed time
                        long tokens = ((Number) bucket.get("token")).longValue();
                        long refill = (long) (
                                ((currentTime - (long) bucket.get("access_time")) / 1000)
                                        / (window * 1.0 / limit));
                        tokens = Math.min(tokens + refill, limit);

                        if (tokens > 0) {
                            bucket.put("token", tokens - 1);
                            bucket.put("access_time", currentTime);
                        } else {
                            isExceed[0] = true;
                        }
                    }

                    // MULTI/EXEC: atomic update (fails if key was modified since WATCH)
                    operations.multi();
                    operations.opsForHash().putAll(bucketKey, bucket);
                    operations.expire(bucketKey, Duration.ofSeconds(window));

                    isLockFailed[0] = operations.exec().isEmpty();
                    return null;
                }
            });

            // If EXEC succeeded, stop retrying
            if (!isLockFailed[0]) break;

            // Last retry exhausted
            if (retry - 1 <= 0)
                throw new RuntimeException(
                        "cannot obtain rate limit from redis after retry for "
                                + MAX_RETRY + " times !");
        }

        if (isExceed[0]) {
            throw new ControllerConstraintViolation(
                    429, "You have sent too many request, please try again later !");
        }
    }
}
