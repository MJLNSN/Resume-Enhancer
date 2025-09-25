package com.resumeenhancer.service;

import com.resumeenhancer.dto.ResumeResponse;
import com.resumeenhancer.dto.TextResumeRequest;
import com.resumeenhancer.entity.Resume;
import com.resumeenhancer.entity.User;
import com.resumeenhancer.repository.ResumeRepository;
import com.resumeenhancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PdfTextExtractor pdfTextExtractor;

    @Autowired
    private NlpService nlpService;

    public ResumeResponse uploadResume(MultipartFile file, Long userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        if (!isValidFileType(file)) {
            throw new IllegalArgumentException("Only PDF and TXT files are allowed");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Upload to S3
        String fileUrl = storageService.uploadFile(file, "resumes");

        // Create resume record
        Resume resume = new Resume(user, fileUrl);
        Resume savedResume = resumeRepository.save(resume);

        // Extract text asynchronously
        extractTextAsync(savedResume.getId(), file);

        return convertToDto(savedResume);
    }

    public ResumeResponse createTextResume(TextResumeRequest request, Long userId) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create resume record with text content
        Resume resume = new Resume(user, "text-input");
        resume.setRawText(request.getText());
        Resume savedResume = resumeRepository.save(resume);

        // Parse resume with NLP service asynchronously
        parseResumeAsync(savedResume.getId(), request.getText());

        return convertToDto(savedResume);
    }

    @Async
    public void extractTextAsync(Long resumeId, MultipartFile file) {
        try {
            String rawText;
            
            if (pdfTextExtractor.isPdfFile(file)) {
                rawText = pdfTextExtractor.extractTextFromPdf(file);
            } else if (pdfTextExtractor.isTextFile(file)) {
                rawText = pdfTextExtractor.extractTextFromText(file);
            } else {
                markParseError(resumeId, "Unsupported file type");
                return;
            }

            if (rawText == null || rawText.trim().isEmpty()) {
                markParseError(resumeId, "No text content extracted");
                return;
            }

            updateResumeText(resumeId, rawText);
            
            // Parse resume with NLP service
            parseResumeAsync(resumeId, rawText);

        } catch (Exception e) {
            markParseError(resumeId, "Failed to extract text: " + e.getMessage());
        }
    }

    private void updateResumeText(Long resumeId, String rawText) {
        resumeRepository.findById(resumeId).ifPresent(resume -> {
            resume.setRawText(rawText);
            resume.setParseError(false);
            resumeRepository.save(resume);
        });
    }

    private void markParseError(Long resumeId, String error) {
        resumeRepository.findById(resumeId).ifPresent(resume -> {
            resume.setParseError(true);
            resumeRepository.save(resume);
        });
    }

    @Async
    public void parseResumeAsync(Long resumeId, String rawText) {
        try {
            Object parsedJson = nlpService.parseResume(rawText);
            updateResumeParsedJson(resumeId, parsedJson);
        } catch (Exception e) {
            markParseError(resumeId, "Failed to parse resume: " + e.getMessage());
        }
    }

    private void updateResumeParsedJson(Long resumeId, Object parsedJson) {
        resumeRepository.findById(resumeId).ifPresent(resume -> {
            resume.setParsedJson(parsedJson);
            resumeRepository.save(resume);
        });
    }

    public ResumeResponse getResume(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return convertToDto(resume);
    }

    public List<ResumeResponse> getUserResumes(Long userId) {
        List<Resume> resumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return resumes.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private boolean isValidFileType(MultipartFile file) {
        return pdfTextExtractor.isPdfFile(file) || pdfTextExtractor.isTextFile(file);
    }

    private ResumeResponse convertToDto(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getFileUrl(),
                resume.getRawText(),
                resume.getParsedJson(),
                resume.getParseError(),
                resume.getCreatedAt()
        );
    }
}
