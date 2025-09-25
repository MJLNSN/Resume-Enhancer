export interface User {
  id: number;
  email: string;
}

export interface AuthRequest {
  email: string;
  password: string;
  fullName?: string;
}

export interface AuthResponse {
  userId: number;
  token: string;
  email: string;
}

export interface Resume {
  id: number;
  fileUrl: string;
  rawText: string | null;
  parsedJson: ParsedResume | null;
  parseError: boolean;
  createdAt: string;
}

export interface ParsedResume {
  skills: string[];
  experiences: Experience[];
  education: Education[];
}

export interface Experience {
  company: string;
  title: string;
  years: string;
  bullets: string[];
}

export interface Education {
  institution: string;
  degree: string;
  field: string;
  years: string;
}

export interface EnhancedResume {
  id: number;
  resumeId: number;
  enhancedText: string;
  language: 'ORIGINAL' | 'EN' | 'ZH';
  suggestions: string[] | null;
  enhancementType: 'rewrite' | 'translate' | 'suggest';
  createdAt: string;
}

export interface AnalyzeRequest {
  resumeId: number;
  jobDescription?: string;
  outputLanguage?: string;
  mode: 'local' | 'gpt';
  forceRefresh?: boolean;
}

export interface TranslateRequest {
  resumeId?: number;
  text?: string;
  targetLang: string; // Support all language codes
  mode: 'gpt' | 'local';
}

export interface EnhancedResumeResponse {
  id: number;
  resumeId: number;
  enhancedText: string;
  language: 'ORIGINAL' | 'EN' | 'ZH';
  suggestions: string[] | null;
  enhancementType: 'rewrite' | 'translate' | 'suggest';
  createdAt: string;
}

export interface ExportRequest {
  enhancedResumeId: number;
  format: 'markdown' | 'pdf';
  filename?: string;
}

export interface ExportResponse {
  downloadUrl: string | null;
  format: string;
  filename: string;
  fileSize: number | null;
}
