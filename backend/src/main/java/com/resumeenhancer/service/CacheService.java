package com.resumeenhancer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Value("${app.cache.enable-redis:false}")
    private boolean enableRedisCache;

    @Value("${app.cache.gpt-cache-hours:24}")
    private int gptCacheHours;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getCachedGptResponse(String input, String mode) {
        if (!enableRedisCache) {
            return null;
        }

        try {
            String cacheKey = generateCacheKey("gpt", input, mode);
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            return null; // Fail silently for cache misses
        }
    }

    public void cacheGptResponse(String input, String mode, String response) {
        if (!enableRedisCache || response == null) {
            return;
        }

        try {
            String cacheKey = generateCacheKey("gpt", input, mode);
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(gptCacheHours));
        } catch (Exception e) {
            // Fail silently for cache errors
        }
    }

    public String getCachedEnhancement(Long resumeId, String jobDescription, String mode) {
        if (!enableRedisCache) {
            return null;
        }

        try {
            String input = resumeId + "|" + (jobDescription != null ? jobDescription : "");
            String cacheKey = generateCacheKey("enhancement", input, mode);
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            return null;
        }
    }

    public void cacheEnhancement(Long resumeId, String jobDescription, String mode, String response) {
        if (!enableRedisCache || response == null) {
            return;
        }

        try {
            String input = resumeId + "|" + (jobDescription != null ? jobDescription : "");
            String cacheKey = generateCacheKey("enhancement", input, mode);
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(gptCacheHours));
        } catch (Exception e) {
            // Fail silently for cache errors
        }
    }

    public String getCachedTranslation(String text, String targetLang, String mode) {
        if (!enableRedisCache) {
            return null;
        }

        try {
            String input = text + "|" + targetLang;
            String cacheKey = generateCacheKey("translation", input, mode);
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            return null;
        }
    }

    public void cacheTranslation(String text, String targetLang, String mode, String response) {
        if (!enableRedisCache || response == null) {
            return;
        }

        try {
            String input = text + "|" + targetLang;
            String cacheKey = generateCacheKey("translation", input, mode);
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(gptCacheHours));
        } catch (Exception e) {
            // Fail silently for cache errors
        }
    }

    public void clearCache(String pattern) {
        if (!enableRedisCache) {
            return;
        }

        try {
            redisTemplate.delete(redisTemplate.keys(pattern + "*"));
        } catch (Exception e) {
            // Fail silently for cache errors
        }
    }

    private String generateCacheKey(String prefix, String input, String mode) {
        try {
            String combined = prefix + ":" + input + ":" + mode;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(combined.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "resume_enhancer:" + prefix + ":" + hexString.toString();
        } catch (Exception e) {
            // Fallback to simple key if hashing fails
            return "resume_enhancer:" + prefix + ":" + Math.abs((input + mode).hashCode());
        }
    }
}
