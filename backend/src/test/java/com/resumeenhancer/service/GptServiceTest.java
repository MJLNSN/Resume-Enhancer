package com.resumeenhancer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class GptServiceTest {

    @Mock
    private CacheService cacheService;

    private GptService gptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Initialize with test values
        gptService = new GptService(
            "https://api.test.com/v1",
            "test-api-key", 
            "gpt-3.5-turbo", 
            2000, 
            0.7
        );
    }

    @Test
    void testIsServiceAvailable_WithValidKey() {
        assertTrue(gptService.isServiceAvailable());
    }

    @Test
    void testIsServiceAvailable_WithInvalidKey() {
        GptService invalidService = new GptService(
            "https://api.test.com/v1",
            "your-openai-api-key-here", 
            "gpt-3.5-turbo", 
            2000, 
            0.7
        );
        assertFalse(invalidService.isServiceAvailable());
    }

    @Test
    void testGetLanguageName() {
        // Use reflection to access private method
        try {
            java.lang.reflect.Method method = GptService.class.getDeclaredMethod("getLanguageName", String.class);
            method.setAccessible(true);
            
            assertEquals("English", method.invoke(gptService, "en"));
            assertEquals("Chinese (Simplified)", method.invoke(gptService, "zh"));
            assertEquals("Japanese", method.invoke(gptService, "ja"));
            assertEquals("Spanish", method.invoke(gptService, "es"));
            assertEquals("French", method.invoke(gptService, "fr"));
            assertEquals("German", method.invoke(gptService, "de"));
            assertEquals("Korean", method.invoke(gptService, "ko"));
            assertEquals("English", method.invoke(gptService, "unknown"));
        } catch (Exception e) {
            fail("Failed to test getLanguageName method: " + e.getMessage());
        }
    }

    @Test
    void testBuildEnhancePrompt_WithoutLanguage() {
        try {
            java.lang.reflect.Method method = GptService.class.getDeclaredMethod("buildEnhancePrompt", String.class, String.class, String.class);
            method.setAccessible(true);
            
            String result = (String) method.invoke(gptService, "Test resume", "Test job", null);
            assertNotNull(result);
            assertFalse(result.contains("CRITICAL LANGUAGE REQUIREMENTS"));
            assertTrue(result.contains("Test resume"));
            assertTrue(result.contains("Test job"));
        } catch (Exception e) {
            fail("Failed to test buildEnhancePrompt method: " + e.getMessage());
        }
    }

    @Test
    void testBuildEnhancePrompt_WithLanguage() {
        try {
            java.lang.reflect.Method method = GptService.class.getDeclaredMethod("buildEnhancePrompt", String.class, String.class, String.class);
            method.setAccessible(true);
            
            String result = (String) method.invoke(gptService, "Test resume", "Test job", "zh");
            assertNotNull(result);
            assertTrue(result.contains("CRITICAL LANGUAGE REQUIREMENTS"));
            assertTrue(result.contains("Chinese (Simplified)"));
            assertTrue(result.contains("Test resume"));
            assertTrue(result.contains("Test job"));
        } catch (Exception e) {
            fail("Failed to test buildEnhancePrompt with language: " + e.getMessage());
        }
    }

    @Test
    void testBuildEnhancePrompt_WithEnglishLanguage() {
        try {
            java.lang.reflect.Method method = GptService.class.getDeclaredMethod("buildEnhancePrompt", String.class, String.class, String.class);
            method.setAccessible(true);
            
            String result = (String) method.invoke(gptService, "Test resume", "Test job", "en");
            assertNotNull(result);
            assertFalse(result.contains("CRITICAL LANGUAGE REQUIREMENTS"));
        } catch (Exception e) {
            fail("Failed to test buildEnhancePrompt with English language: " + e.getMessage());
        }
    }
}
