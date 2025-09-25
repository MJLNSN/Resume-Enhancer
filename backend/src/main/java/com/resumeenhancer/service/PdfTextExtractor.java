package com.resumeenhancer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfTextExtractor {

    public String extractTextFromPdf(MultipartFile file) throws IOException {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        }
    }

    public String extractTextFromText(MultipartFile file) throws IOException {
        return new String(file.getBytes()).trim();
    }

    public boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }

    public boolean isTextFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("text/plain") || 
               contentType.equals("application/octet-stream"));
    }
}
