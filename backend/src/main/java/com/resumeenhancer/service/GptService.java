package com.resumeenhancer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class GptService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    @Autowired(required = false)
    private CacheService cacheService;

    public GptService(@Value("${app.openai.api-base:https://api.chatanywhere.tech}") String apiBase,
                      @Value("${app.openai.api-key}") String apiKey,
                      @Value("${OPENAI_MODEL:gpt-3.5-turbo}") String model,
                      @Value("${OPENAI_MAX_TOKENS:2000}") int maxTokens,
                      @Value("${OPENAI_TEMPERATURE:0.3}") double temperature) {
        
        System.out.println("=== GPT SERVICE INITIALIZATION ===");
        System.out.println("API Base: " + apiBase);
        System.out.println("API Key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
        System.out.println("Model: " + model);
        
        this.webClient = WebClient.builder()
                .baseUrl(apiBase)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    public String enhanceResume(String resumeText, String jobDescription) {
        System.out.println("=== GPT ENHANCE DEBUG ===");
        System.out.println("Input text: " + resumeText.substring(0, Math.min(100, resumeText.length())) + "...");
        System.out.println("Job description: " + (jobDescription != null ? jobDescription : "null"));
        
        String prompt = buildEnhancePrompt(resumeText, jobDescription);
        System.out.println("Generated prompt length: " + prompt.length());
        System.out.println("Prompt preview: " + prompt.substring(0, Math.min(300, prompt.length())) + "...");
        
        // Check cache first
        if (cacheService != null) {
            String cached = cacheService.getCachedGptResponse(prompt, "enhance");
            if (cached != null) {
                System.out.println("Using cached enhancement");
                return cached;
            }
        }
        
        try {
            System.out.println("Calling GPT API for enhancement...");
            String response = callGptApi(prompt);
            System.out.println("GPT API response received for enhancement");
            
            String content = extractContentFromResponse(response);
            System.out.println("Enhanced content preview: " + content.substring(0, Math.min(200, content.length())) + "...");
            
            // Cache the result
            if (cacheService != null) {
                cacheService.cacheGptResponse(prompt, "enhance", content);
            }
            
            return content;
        } catch (Exception e) {
            System.err.println("Enhancement error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to enhance resume: " + e.getMessage(), e);
        }
    }

    public List<String> generateSuggestions(Object parsedJson, String jobDescription) {
        String prompt = buildSuggestionsPrompt(parsedJson, jobDescription);
        
        // Check cache first
        if (cacheService != null) {
            String cached = cacheService.getCachedGptResponse(prompt, "suggestions");
            if (cached != null) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(cached);
                    JsonNode suggestions = jsonNode.get("suggestions");
                    if (suggestions != null && suggestions.isArray()) {
                        return objectMapper.convertValue(suggestions, 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                    }
                } catch (Exception e) {
                    // Continue with API call if cache parsing fails
                }
            }
        }
        
        try {
            String response = callGptApi(prompt);
            String content = extractContentFromResponse(response);
            
            // Parse JSON response - fix single quotes to double quotes
            String normalizedContent = content.replaceAll("'", "\"");
            System.out.println("Normalized JSON content: " + normalizedContent.substring(0, Math.min(200, normalizedContent.length())) + "...");
            
            JsonNode jsonNode = objectMapper.readTree(normalizedContent);
            JsonNode suggestions = jsonNode.get("suggestions");
            
            if (suggestions != null && suggestions.isArray()) {
                // Cache the result
                if (cacheService != null) {
                    cacheService.cacheGptResponse(prompt, "suggestions", normalizedContent);
                }
                
                return objectMapper.convertValue(suggestions, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
            
            throw new RuntimeException("Invalid suggestions format from GPT");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate suggestions: " + e.getMessage(), e);
        }
    }

    public String translateResume(String resumeText, String targetLanguage) {
        System.out.println("=== GPT TRANSLATE DEBUG ===");
        System.out.println("Input text: " + resumeText.substring(0, Math.min(100, resumeText.length())) + "...");
        System.out.println("Target language: " + targetLanguage);
        
        String prompt = buildTranslatePrompt(resumeText, targetLanguage);
        System.out.println("Generated prompt: " + prompt.substring(0, Math.min(200, prompt.length())) + "...");
        
        // Check cache first
        if (cacheService != null) {
            String cached = cacheService.getCachedTranslation(resumeText, targetLanguage, "gpt");
            if (cached != null) {
                System.out.println("Using cached translation");
                return cached;
            }
        }
        
        try {
            System.out.println("Calling GPT API...");
            String response = callGptApi(prompt);
            System.out.println("GPT API response received: " + response.substring(0, Math.min(200, response.length())) + "...");
            
            String content = extractContentFromResponse(response);
            System.out.println("Extracted content: " + content.substring(0, Math.min(200, content.length())) + "...");
            
            // Cache the result
            if (cacheService != null) {
                cacheService.cacheTranslation(resumeText, targetLanguage, "gpt", content);
            }
            
            return content;
        } catch (Exception e) {
            System.err.println("Translation error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to translate resume: " + e.getMessage(), e);
        }
    }

    private String callGptApi(String prompt) throws Exception {
        Map<String, Object> request = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            ),
            "max_tokens", maxTokens,
            "temperature", temperature
        );

        String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (response == null) {
            throw new RuntimeException("No response from GPT API");
        }

        return response;
    }

    private String extractContentFromResponse(String response) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode choices = jsonNode.get("choices");
        
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).get("message");
            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null) {
                    return content.asText().trim();
                }
            }
        }
        
        throw new RuntimeException("Invalid response format from GPT API");
    }

    private String buildEnhancePrompt(String resumeText, String jobDescription) {
        String template = getResumeTemplate();
        return String.format(
            "You are an expert career coach. Transform the following resume to match this EXACT template format:\n\n" +
            "TEMPLATE FORMAT:\n%s\n\n" +
            "CURRENT RESUME:\n%s\n\n" +
            "JOB FOCUS: %s\n\n" +
            "INSTRUCTIONS:\n" +
            "1. Restructure the resume to EXACTLY match the template format\n" +
            "2. Replace template placeholders with actual information from the current resume\n" +
            "3. Preserve all factual information (names, dates, companies, metrics)\n" +
            "4. Enhance descriptions to be more professional and results-oriented\n" +
            "5. If sections are missing, create appropriate content based on available information\n" +
            "6. Output ONLY the enhanced resume in the exact template format\n" +
            "7. Do not include any explanations or additional text",
            template, resumeText, jobDescription != null ? jobDescription : "General improvement");
    }
    
    private String getResumeTemplate() {
        return "# [FULL NAME]\n\n" +
               "📞 [PHONE NUMBER] | 📧 [EMAIL] | 📍 [LOCATION]\n\n" +
               "---\n\n" +
               "## EDUCATION\n\n" +
               "**[UNIVERSITY/INSTITUTION]** – *[DEGREE], [FIELD OF STUDY]*\n" +
               "*[START DATE] – [END DATE]*\n\n" +
               "**[UNIVERSITY/INSTITUTION]** – *[DEGREE], [FIELD OF STUDY]*\n" +
               "*[START DATE] – [END DATE]*\n\n" +
               "## SKILLS\n\n" +
               "**Programming Languages:** [List programming languages, e.g., Python, Java, JavaScript]\n" +
               "**Frameworks & Tools:** [List frameworks and tools, e.g., React, Node.js, Docker, AWS]\n" +
               "**Core Strengths:** [List key strengths, e.g., problem-solving, teamwork, leadership]\n" +
               "**Languages:** [List languages and proficiency levels, e.g., English (Native), Spanish (Fluent)]\n\n" +
               "## EXPERIENCE\n\n" +
               "**[JOB TITLE]** | [COMPANY NAME] ([company-website.com])  *[START DATE] – [END DATE]* · [LOCATION/REMOTE]\n\n" +
               "- [Bullet point describing key responsibility or achievement]\n" +
               "- [Bullet point describing key responsibility or achievement]\n" +
               "- [Bullet point describing key responsibility or achievement]\n" +
               "- [Bullet point describing key responsibility or achievement]\n\n" +
               "**[JOB TITLE]** | [COMPANY NAME] ([company-website.com])  *[START DATE] – [END DATE]* · [LOCATION/REMOTE]\n\n" +
               "- [Bullet point describing key responsibility or achievement]\n" +
               "- [Bullet point describing key responsibility or achievement]\n" +
               "- [Bullet point describing key responsibility or achievement]\n" +
               "- [Bullet point describing key responsibility or achievement]\n\n" +
               "## PROJECTS\n\n" +
               "**[PROJECT NAME]** | *[PROJECT DURATION]*\n\n" +
               "- [Bullet point describing project scope and technologies used]\n\n" +
               "**[PROJECT NAME]** | *[PROJECT DURATION]*\n\n" +
               "- [Bullet point describing project scope and technologies used]";
    }

    private String buildSuggestionsPrompt(Object parsedJson, String jobDescription) {
        try {
            String resumeJsonStr = objectMapper.writeValueAsString(parsedJson);
            return String.format(
                "Given resume: %s and job: %s, suggest 3-5 practical ways the candidate can improve competitiveness. " +
                "Return ONLY JSON: {'suggestions': ['...','...']} " +
                "Focus on actionable improvements like: " +
                "- Specific skills to learn or certify " +
                "- Project ideas to strengthen portfolio " +
                "- Ways to quantify achievements " +
                "- Industry-relevant experience to gain " +
                "- Networking or learning opportunities",
                resumeJsonStr, jobDescription != null ? jobDescription : "general career improvement");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize resume JSON", e);
        }
    }

    private String buildTranslatePrompt(String resumeText, String targetLanguage) {
        String targetLangFull = "en".equals(targetLanguage) ? "English" : "Chinese";
        return String.format(
            "Translate the following resume into %s, preserving dates, numbers, and technology names. " +
            "Output Markdown format. Keep the structure and formatting intact.\n\n" +
            "Resume text:\n%s",
            targetLangFull, resumeText);
    }

    public boolean isServiceAvailable() {
        return apiKey != null && !apiKey.isEmpty() && !"your-openai-api-key-here".equals(apiKey);
    }
}
