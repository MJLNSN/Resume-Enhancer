package com.resumeenhancer.service;

import com.resumeenhancer.entity.User;
import com.resumeenhancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
public class UsageTrackingService {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Value("${app.usage.daily-gpt-limit:10}")
    private int dailyGptLimit;

    @Value("${app.usage.daily-enhancement-limit:20}")
    private int dailyEnhancementLimit;

    @Value("${app.usage.enable-redis-cache:false}")
    private boolean enableRedisCache;

    public boolean canUseGptService(Long userId) {
        if (!enableRedisCache) {
            return true; // No limits if Redis is not configured
        }

        String key = "gpt_usage:" + userId + ":" + LocalDate.now();
        String usage = redisTemplate.opsForValue().get(key);
        
        int currentUsage = usage != null ? Integer.parseInt(usage) : 0;
        return currentUsage < dailyGptLimit;
    }

    public boolean canUseEnhancement(Long userId) {
        if (!enableRedisCache) {
            return true; // No limits if Redis is not configured
        }

        String key = "enhancement_usage:" + userId + ":" + LocalDate.now();
        String usage = redisTemplate.opsForValue().get(key);
        
        int currentUsage = usage != null ? Integer.parseInt(usage) : 0;
        return currentUsage < dailyEnhancementLimit;
    }

    public void trackGptUsage(Long userId) {
        if (!enableRedisCache) {
            return;
        }

        String key = "gpt_usage:" + userId + ":" + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofDays(1));
    }

    public void trackEnhancementUsage(Long userId) {
        if (!enableRedisCache) {
            return;
        }

        String key = "enhancement_usage:" + userId + ":" + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofDays(1));
    }

    public int getRemainingGptUsage(Long userId) {
        if (!enableRedisCache) {
            return dailyGptLimit; // Return full limit if no tracking
        }

        String key = "gpt_usage:" + userId + ":" + LocalDate.now();
        String usage = redisTemplate.opsForValue().get(key);
        
        int currentUsage = usage != null ? Integer.parseInt(usage) : 0;
        return Math.max(0, dailyGptLimit - currentUsage);
    }

    public int getRemainingEnhancementUsage(Long userId) {
        if (!enableRedisCache) {
            return dailyEnhancementLimit; // Return full limit if no tracking
        }

        String key = "enhancement_usage:" + userId + ":" + LocalDate.now();
        String usage = redisTemplate.opsForValue().get(key);
        
        int currentUsage = usage != null ? Integer.parseInt(usage) : 0;
        return Math.max(0, dailyEnhancementLimit - currentUsage);
    }

    public void resetDailyUsage(Long userId) {
        if (!enableRedisCache) {
            return;
        }

        String gptKey = "gpt_usage:" + userId + ":" + LocalDate.now();
        String enhancementKey = "enhancement_usage:" + userId + ":" + LocalDate.now();
        
        redisTemplate.delete(gptKey);
        redisTemplate.delete(enhancementKey);
    }
}
