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
        return enhanceResume(resumeText, jobDescription, null);
    }

    public String enhanceResume(String resumeText, String jobDescription, String outputLanguage) {
        System.out.println("=== GPT ENHANCE DEBUG ===");
        System.out.println("Input text: " + resumeText.substring(0, Math.min(100, resumeText.length())) + "...");
        System.out.println("Job description: " + (jobDescription != null ? jobDescription : "null"));
        System.out.println("Output language: " + (outputLanguage != null ? outputLanguage : "original"));
        
        String prompt = buildEnhancePrompt(resumeText, jobDescription, outputLanguage);
        System.out.println("Generated prompt length: " + prompt.length());
        System.out.println("Language instruction included: " + (prompt.contains("CRITICAL LANGUAGE REQUIREMENTS")));
        System.out.println("Prompt preview: " + prompt.substring(0, Math.min(500, prompt.length())) + "...");
        
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
        return buildEnhancePrompt(resumeText, jobDescription, null);
    }

    private String buildEnhancePrompt(String resumeText, String jobDescription, String outputLanguage) {
        String template = getResumeTemplate();
        String jobFocus = jobDescription != null && !jobDescription.trim().isEmpty() ? jobDescription : "General professional improvement";
        String languageInstruction = "";
        
        if (outputLanguage != null && !outputLanguage.trim().isEmpty() && !"en".equals(outputLanguage.toLowerCase())) {
            String targetLangFull = getLanguageName(outputLanguage);
            languageInstruction = String.format(
                "\n\nCRITICAL LANGUAGE REQUIREMENTS - THIS IS MANDATORY:\n" +
                "- MUST write the ENTIRE resume in %s language ONLY\n" +
                "- ALL section headers, job titles, descriptions must be in %s\n" +
                "- Use professional %s terminology and natural expressions\n" +
                "- Translate ALL text content to %s while maintaining template structure\n" +
                "- Keep dates, phone numbers, and email addresses unchanged\n" +
                "- Company names can remain in original language or be translated as appropriate\n" +
                "- The output must be 100%% in %s language - no English text allowed",
                targetLangFull, targetLangFull, targetLangFull, targetLangFull, targetLangFull);
        }
        
        return String.format(
            "You are an expert career coach and resume writer.%s\n\nTransform the following resume to match the EXACT template format while tailoring it for the target position:\n\n" +
            "TEMPLATE FORMAT:\n%s\n\n" +
            "CURRENT RESUME:\n%s\n\n" +
            "TARGET JOB DESCRIPTION:\n%s\n\n" +
            "ENHANCEMENT INSTRUCTIONS:\n" +
            "1. Restructure the resume to EXACTLY match the template format\n" +
            "2. Replace template placeholders with actual information from the current resume\n" +
            "3. Preserve all factual information (names, dates, companies, exact metrics)\n" +
            "4. TAILOR the content to align with the target job requirements:\n" +
            "   - Highlight relevant skills mentioned in the job description\n" +
            "   - Emphasize experiences that match the job requirements\n" +
            "   - Use keywords and terminology from the job posting\n" +
            "   - Reorder skills to prioritize those most relevant to the position\n" +
            "5. Enhance bullet points to be results-oriented and quantified when possible\n" +
            "6. Adjust the professional summary/objective to align with the target role\n" +
            "7. If missing relevant skills/experience, suggest how existing experience translates\n" +
            "8. Output ONLY the enhanced resume in the exact template format\n" +
            "9. Do not include any explanations, notes, or additional text\n\n" +
            "Focus on making this resume compelling for the specific target position while maintaining complete accuracy.",
            languageInstruction, template, resumeText, jobFocus);
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
            String jobFocus = jobDescription != null && !jobDescription.trim().isEmpty() ? jobDescription : "General career advancement";
            
            return String.format(
                "You are an expert career coach with deep knowledge of hiring practices. Analyze this resume and provide personalized improvement suggestions.\n\n" +
                "CANDIDATE'S RESUME DATA:\n%s\n\n" +
                "TARGET POSITION/INDUSTRY:\n%s\n\n" +
                "ANALYSIS INSTRUCTIONS:\n" +
                "1. Consider the candidate's current experience level and career stage\n" +
                "2. Identify specific gaps between their profile and the target position\n" +
                "3. Suggest concrete, actionable improvements they can implement\n" +
                "4. Focus on both content and presentation enhancements\n" +
                "5. Consider industry-specific requirements and trends\n\n" +
                "SUGGESTION CATEGORIES TO COVER:\n" +
                "- Skills & Technical Competencies: Missing skills, certifications, or tools\n" +
                "- Experience Presentation: How to better highlight relevant experience\n" +
                "- Achievement Quantification: Specific metrics or results to add\n" +
                "- Industry Alignment: Terminology, keywords, or focus areas to emphasize\n" +
                "- Professional Development: Learning opportunities or next steps\n\n" +
                "Provide 4-6 specific, personalized suggestions that this candidate can realistically implement. " +
                "Each suggestion should be practical and directly tied to improving their competitiveness for this type of role.\n\n" +
                "Return ONLY a JSON object in this exact format: {\"suggestions\": [\"suggestion1\", \"suggestion2\", \"suggestion3\", \"suggestion4\"]} " +
                "No other text or formatting.",
                resumeJsonStr, jobFocus);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize resume JSON", e);
        }
    }

    private String buildTranslatePrompt(String resumeText, String targetLanguage) {
        String targetLangFull = getLanguageName(targetLanguage);
        return String.format(
            "Translate the following resume into %s. IMPORTANT RULES:\n" +
            "1. ONLY translate the text content - DO NOT change the format, structure, or layout\n" +
            "2. Preserve ALL Markdown formatting exactly as it is (headers, bullets, bold, italic, etc.)\n" +
            "3. Keep dates, numbers, company names, and technology names unchanged\n" +
            "4. Maintain the exact same line breaks and spacing\n" +
            "5. Do not add any additional content or explanations\n" +
            "6. Output ONLY the translated resume with original formatting\n" +
            "7. Use native speakers' natural expressions and professional terminology\n\n" +
            "Resume to translate:\n%s",
            targetLangFull, resumeText);
    }

    private String getLanguageName(String languageCode) {
        switch (languageCode.toLowerCase()) {
            case "en": return "English";
            case "zh": return "Chinese (Simplified)";
            case "es": return "Spanish";
            case "fr": return "French";
            case "de": return "German";
            case "ja": return "Japanese";
            case "ko": return "Korean";
            default: return "English"; // fallback
        }
    }

    public boolean isServiceAvailable() {
        return apiKey != null && !apiKey.isEmpty() && !"your-openai-api-key-here".equals(apiKey);
    }
}
