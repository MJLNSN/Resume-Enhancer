package com.resumeenhancer.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TextResumeRequest {
    @NotBlank(message = "Resume text cannot be empty")
    @Size(min = 50, message = "Resume text must be at least 50 characters")
    private String text;
    
    private String title;

    public TextResumeRequest() {}

    public TextResumeRequest(String text, String title) {
        this.text = text;
        this.title = title;
    }

    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
