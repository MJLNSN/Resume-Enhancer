package com.resumeenhancer.service;

import com.resumeenhancer.dto.AnalyzeRequest;
import com.resumeenhancer.dto.EnhancedResumeResponse;
import com.resumeenhancer.dto.EnhanceRequest;
import com.resumeenhancer.dto.TranslateRequest;
import com.resumeenhancer.entity.EnhancedResume;
import com.resumeenhancer.entity.Resume;
import com.resumeenhancer.repository.EnhancedResumeRepository;
import com.resumeenhancer.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnhancedResumeService {

    @Autowired
    private EnhancedResumeRepository enhancedResumeRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private GptService gptService;

    @Autowired
    private NlpService nlpService;

    @Autowired(required = false)
    private UsageTrackingService usageTrackingService;

    @Autowired(required = false)
    private CacheService cacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EnhancedResumeResponse enhanceResume(EnhanceRequest request, Long userId) {
        // Convert EnhanceRequest to AnalyzeRequest
        AnalyzeRequest analyzeRequest = new AnalyzeRequest();
        analyzeRequest.setResumeId(request.getResumeId());
        analyzeRequest.setJobDescription(request.getJobDescription());
        analyzeRequest.setMode(request.getMode());
        analyzeRequest.setForceRefresh(true); // Always force refresh for enhance requests
        
        return analyzeResume(analyzeRequest, userId);
    }

    public EnhancedResumeResponse analyzeResume(AnalyzeRequest request, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(request.getResumeId(), userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (resume.getRawText() == null || resume.getRawText().trim().isEmpty()) {
            throw new RuntimeException("Resume text is not available yet");
        }

        // Check for existing enhancement if not forcing refresh
        if (!Boolean.TRUE.equals(request.getForceRefresh())) {
            List<EnhancedResume> existing = enhancedResumeRepository.findByResumeIdOrderByCreatedAtDesc(request.getResumeId());
            if (!existing.isEmpty()) {
                EnhancedResume latest = existing.get(0);
                if ("rewrite".equals(latest.getEnhancementType())) {
                    return convertToDto(latest);
                }
            }
        }

        try {
            String enhancedText;
            List<String> suggestions = null;

            if ("gpt".equals(request.getMode()) && gptService.isServiceAvailable()) {
                // Check usage limits
                if (usageTrackingService != null && !usageTrackingService.canUseGptService(userId)) {
                    throw new RuntimeException("Daily GPT usage limit exceeded. Please try again tomorrow or use local mode.");
                }
                
                // Use GPT for enhancement
                enhancedText = gptService.enhanceResume(resume.getRawText(), request.getJobDescription());
                
                // Generate suggestions if parsed JSON is available
                if (resume.getParsedJson() != null) {
                    suggestions = gptService.generateSuggestions(resume.getParsedJson(), request.getJobDescription());
                }
                
                // Track usage
                if (usageTrackingService != null) {
                    usageTrackingService.trackGptUsage(userId);
                }
            } else {
                // Use local template enhancement
                enhancedText = enhanceWithLocalTemplate(resume.getRawText(), request.getJobDescription());
            }
            
            // Track enhancement usage regardless of mode
            if (usageTrackingService != null) {
                usageTrackingService.trackEnhancementUsage(userId);
            }

            // Save enhanced resume
            EnhancedResume enhanced = new EnhancedResume();
            enhanced.setResume(resume);
            enhanced.setEnhancedText(enhancedText);
            enhanced.setLanguage(EnhancedResume.Language.ORIGINAL);
            enhanced.setEnhancementType("rewrite");
            
            if (suggestions != null) {
                enhanced.setSuggestions(objectMapper.writeValueAsString(Map.of("suggestions", suggestions)));
            }
            
            EnhancedResume saved = enhancedResumeRepository.save(enhanced);
            return convertToDto(saved);

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze resume: " + e.getMessage(), e);
        }
    }

    public EnhancedResumeResponse translateResume(TranslateRequest request, Long userId) {
        String sourceText;
        Long resumeId = null;
        
        if (request.getResumeId() != null) {
            Resume resume = resumeRepository.findByIdAndUserId(request.getResumeId(), userId)
                    .orElseThrow(() -> new RuntimeException("Resume not found"));
            sourceText = resume.getRawText();
            resumeId = resume.getId();
        } else if (request.getText() != null && !request.getText().trim().isEmpty()) {
            sourceText = request.getText();
        } else {
            throw new RuntimeException("Either resumeId or text must be provided");
        }

        if (sourceText == null || sourceText.trim().isEmpty()) {
            throw new RuntimeException("Source text is empty");
        }

        try {
            String translatedText;
            
            System.out.println("=== TRANSLATION DEBUG ===");
            System.out.println("Request mode: " + request.getMode());
            System.out.println("GPT service available: " + gptService.isServiceAvailable());
            
            if ("gpt".equals(request.getMode()) && gptService.isServiceAvailable()) {
                System.out.println("Using GPT translation");
                
                // Check usage limits
                if (usageTrackingService != null && !usageTrackingService.canUseGptService(userId)) {
                    System.out.println("GPT usage limit exceeded, falling back to local");
                    throw new RuntimeException("Daily GPT usage limit exceeded. Please try again tomorrow or use local mode.");
                }
                
                translatedText = gptService.translateResume(sourceText, request.getTargetLang());
                
                // Track usage
                if (usageTrackingService != null) {
                    usageTrackingService.trackGptUsage(userId);
                }
            } else {
                System.out.println("Using local translation method");
                System.out.println("Reason: mode=" + request.getMode() + ", gptAvailable=" + gptService.isServiceAvailable());
                translatedText = translateWithLocalMethod(sourceText, request.getTargetLang());
            }

            // Save translation if resumeId is provided
            if (resumeId != null) {
                Resume resume = resumeRepository.findById(resumeId).orElseThrow();
                
                EnhancedResume enhanced = new EnhancedResume();
                enhanced.setResume(resume);
                enhanced.setEnhancedText(translatedText);
                enhanced.setLanguage("en".equals(request.getTargetLang()) ? 
                    EnhancedResume.Language.EN : EnhancedResume.Language.ZH);
                enhanced.setEnhancementType("translate");
                
                EnhancedResume saved = enhancedResumeRepository.save(enhanced);
                return convertToDto(saved);
            } else {
                // Return temporary response for text-only translation
                EnhancedResumeResponse response = new EnhancedResumeResponse();
                response.setEnhancedText(translatedText);
                response.setLanguage("en".equals(request.getTargetLang()) ? 
                    EnhancedResume.Language.EN : EnhancedResume.Language.ZH);
                response.setEnhancementType("translate");
                return response;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to translate resume: " + e.getMessage(), e);
        }
    }

    public EnhancedResumeResponse getEnhancedResume(Long id, Long userId) {
        EnhancedResume enhanced = enhancedResumeRepository.findByIdAndResumeUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Enhanced resume not found"));
        return convertToDto(enhanced);
    }

    public List<EnhancedResumeResponse> getEnhancedVersions(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        List<EnhancedResume> enhanced = enhancedResumeRepository.findByResumeIdOrderByCreatedAtDesc(resumeId);
        return enhanced.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private String enhanceWithLocalTemplate(String resumeText, String jobDescription) {
        // Simple local enhancement template
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("# Enhanced Resume\n\n");
        enhanced.append("*This resume has been optimized for professional presentation*\n\n");
        
        String[] sections = resumeText.split("\n\n");
        for (String section : sections) {
            if (section.trim().isEmpty()) continue;
            
            // Apply basic formatting and improvements
            String improvedSection = section.trim()
                    .replaceAll("(?i)responsible for", "Led")
                    .replaceAll("(?i)worked on", "Developed")
                    .replaceAll("(?i)helped with", "Collaborated on");
            
            enhanced.append(improvedSection).append("\n\n");
        }
        
        if (jobDescription != null && !jobDescription.trim().isEmpty()) {
            enhanced.append("---\n*Enhanced for role: ").append(jobDescription.substring(0, Math.min(100, jobDescription.length()))).append("...*\n");
        }
        
        return enhanced.toString();
    }

    private String translateWithLocalMethod(String text, String targetLang) {
        // Basic fallback translation (placeholder implementation)
        if ("en".equals(targetLang)) {
            return "# English Resume\n\n*Translated from original language*\n\n" + text;
        } else {
            return "# 中文简历\n\n*从原语言翻译*\n\n" + text;
        }
    }

    private EnhancedResumeResponse convertToDto(EnhancedResume enhanced) {
        List<String> suggestions = null;
        
        if (enhanced.getSuggestions() != null) {
            try {
                Map<String, Object> suggestionsMap = objectMapper.readValue(
                    enhanced.getSuggestions().toString(), Map.class);
                Object suggestionsObj = suggestionsMap.get("suggestions");
                if (suggestionsObj instanceof List) {
                    suggestions = (List<String>) suggestionsObj;
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        return new EnhancedResumeResponse(
                enhanced.getId(),
                enhanced.getResume().getId(),
                enhanced.getEnhancedText(),
                enhanced.getLanguage(),
                suggestions,
                enhanced.getEnhancementType(),
                enhanced.getCreatedAt()
        );
    }
}
