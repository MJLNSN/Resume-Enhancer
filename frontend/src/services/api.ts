import axios, { AxiosInstance } from 'axios';
import { 
  AuthRequest, 
  AuthResponse, 
  Resume, 
  AnalyzeRequest, 
  TranslateRequest,
  EnhancedResumeResponse,
  ExportRequest,
  ExportResponse
} from '../types';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: 'http://localhost:8080/api/v1',
      timeout: 30000,
    });

    this.api.interceptors.request.use((config) => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth
  async register(data: AuthRequest): Promise<AuthResponse> {
    const response = await this.api.post('/auth/register', data);
    return response.data;
  }

  async login(data: AuthRequest): Promise<AuthResponse> {
    const response = await this.api.post('/auth/login', data);
    return response.data;
  }

  // Resumes
  async uploadResume(file: File): Promise<Resume> {
    console.log('API: Uploading file:', file.name);
    const formData = new FormData();
    formData.append('file', file);
    
    try {
      const response = await this.api.post('/resumes/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      console.log('API: Upload response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('API: Upload error:', error.response?.status, error.response?.data);
      throw error;
    }
  }

  async getResume(id: number): Promise<Resume> {
    const response = await this.api.get(`/resumes/${id}`);
    return response.data;
  }

  async getUserResumes(): Promise<Resume[]> {
    const response = await this.api.get('/resumes');
    return response.data;
  }

  async createTextResume(data: { text: string }): Promise<Resume> {
    const response = await this.api.post('/resumes/text', data);
    return response.data;
  }

  // Analysis
  async analyzeResume(data: AnalyzeRequest): Promise<EnhancedResumeResponse> {
    const response = await this.api.post('/analyze', data);
    return response.data;
  }

  // Enhancement
  async enhanceResume(data: { resumeId: number; jobDescription?: string; outputLanguage?: string; mode: 'local' | 'gpt' }): Promise<EnhancedResumeResponse> {
    const response = await this.api.post('/enhance', data);
    return response.data;
  }

  // Suggestions
  async generateSuggestions(data: { resumeId: number; jobDescription?: string; mode: 'local' | 'gpt' }): Promise<{ suggestions: string[] }> {
    const response = await this.api.post('/suggestions', data);
    return response.data;
  }

  // Translation
  async translateResume(data: TranslateRequest): Promise<EnhancedResumeResponse> {
    const response = await this.api.post('/translate', data);
    return response.data;
  }

  // Enhanced resumes
  async getEnhancedResume(id: number): Promise<EnhancedResumeResponse> {
    const response = await this.api.get(`/enhanced/${id}`);
    return response.data;
  }

  async getEnhancedVersions(resumeId: number): Promise<EnhancedResumeResponse[]> {
    const response = await this.api.get(`/resumes/${resumeId}/enhanced`);
    return response.data;
  }

  // Export
  async exportResume(data: ExportRequest): Promise<ExportResponse> {
    const response = await this.api.post('/export', data);
    return response.data;
  }

  async exportMarkdownDirect(enhancedResumeId: number): Promise<Blob> {
    const response = await this.api.get(`/export/markdown/${enhancedResumeId}`, {
      responseType: 'blob'
    });
    return response.data;
  }

  async exportHtmlDirect(enhancedResumeId: number): Promise<Blob> {
    const response = await this.api.get(`/export/html/${enhancedResumeId}`, {
      responseType: 'blob'
    });
    return response.data;
  }

  async exportComparison(enhancedResumeId: number): Promise<Blob> {
    const response = await this.api.get(`/export/compare/${enhancedResumeId}`, {
      responseType: 'blob'
    });
    return response.data;
  }


  // Usage tracking
  async getUsageLimits(): Promise<any> {
    const response = await this.api.get('/usage/limits');
    return response.data;
  }

  async resetUsage(): Promise<any> {
    const response = await this.api.post('/usage/reset');
    return response.data;
  }
}

export const apiService = new ApiService();
