import { useState } from 'react';
import { Wand2, Brain, Loader, AlertCircle } from 'lucide-react';
import { apiService } from '../services/api';
import { EnhancedResumeResponse } from '../types';

interface EnhancementPanelProps {
  resumeId: number;
  onEnhancementComplete: (enhanced: EnhancedResumeResponse) => void;
}

export function EnhancementPanel({ resumeId, onEnhancementComplete }: EnhancementPanelProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [jobDescription, setJobDescription] = useState('');
  const [mode] = useState<'local' | 'gpt'>('gpt'); // Always use GPT mode
  const [outputLanguage, setOutputLanguage] = useState('en');

  const handleEnhance = async () => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiService.enhanceResume({
        resumeId,
        jobDescription: jobDescription.trim() || undefined,
        outputLanguage: outputLanguage,
        mode,
      });
      
      onEnhancementComplete(result);
    } catch (err: any) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Failed to enhance resume');
    } finally {
      setLoading(false);
    }
  };


  const languageOptions = [
    { code: 'en', name: 'English', flag: '🇺🇸' },
    { code: 'zh', name: '中文', flag: '🇨🇳' },
    { code: 'es', name: 'Español', flag: '🇪🇸' },
    { code: 'fr', name: 'Français', flag: '🇫🇷' },
    { code: 'de', name: 'Deutsch', flag: '🇩🇪' },
    { code: 'ja', name: '日本語', flag: '🇯🇵' },
    { code: 'ko', name: '한국어', flag: '🇰🇷' },
  ];

  const handleGenerateSuggestions = async () => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiService.generateSuggestions({
        resumeId,
        jobDescription: jobDescription.trim() || undefined,
        mode,
      });
      
      // Create a temporary response with suggestions
      const suggestionsResponse = {
        id: Date.now(), // temporary ID
        resumeId,
        enhancedText: `# Improvement Suggestions\n\n${result.suggestions.map((s, i) => `${i + 1}. ${s}`).join('\n\n')}`,
        language: 'ORIGINAL' as const,
        suggestions: result.suggestions,
        enhancementType: 'suggest' as const,
        createdAt: new Date().toISOString(),
      };
      
      onEnhancementComplete(suggestionsResponse);
    } catch (err: any) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Failed to generate suggestions');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-lg border border-gray-200/50 p-8">
      <div className="flex items-center space-x-3 mb-6">
        <div className="w-10 h-10 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center">
          <Wand2 className="h-5 w-5 text-white" />
        </div>
        <h3 className="text-xl font-semibold text-gray-900">
          AI Enhancement
        </h3>
      </div>

      {error && (
        <div className="mb-6 flex items-center space-x-3 p-4 bg-red-50 border border-red-200 rounded-xl text-red-700">
          <AlertCircle className="h-5 w-5 flex-shrink-0" />
          <span className="text-sm">{error}</span>
        </div>
      )}

      <div className="space-y-6">
        {/* Job Description Input */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Target Job Description (Recommended)
          </label>
          <textarea
            value={jobDescription}
            onChange={(e) => setJobDescription(e.target.value)}
            className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors resize-none"
            rows={4}
            placeholder="Paste the target job description here. AI will tailor your resume to highlight relevant skills, experiences, and keywords that match the position requirements..."
            disabled={loading}
          />
          <p className="text-xs text-gray-500 mt-1">
            💡 Adding a job description significantly improves resume targeting and keyword optimization
          </p>
        </div>


        {/* Output Language Selection */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Output Language
          </label>
          <select
            value={outputLanguage}
            onChange={(e) => setOutputLanguage(e.target.value)}
            className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors"
            disabled={loading}
          >
            {languageOptions.map((lang) => (
              <option key={lang.code} value={lang.code}>
                {lang.flag} {lang.name}
              </option>
            ))}
          </select>
          <p className="text-xs text-gray-500 mt-1">
            The enhanced resume will be formatted in the selected language
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap gap-4">
          <button
            onClick={handleEnhance}
            disabled={loading}
            className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-semibold rounded-xl text-white bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-lg hover:shadow-xl"
          >
            {loading ? (
              <Loader className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <Wand2 className="h-4 w-4 mr-2" />
            )}
            Format & Enhance
          </button>

          <button
            onClick={handleGenerateSuggestions}
            disabled={loading}
            className="inline-flex items-center px-6 py-3 border border-gray-300 text-sm font-semibold rounded-xl text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-sm hover:shadow-md"
          >
            {loading ? (
              <Loader className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <Brain className="h-4 w-4 mr-2" />
            )}
            Get Suggestions
          </button>

        </div>

        <div className="text-sm text-blue-700 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 p-4 rounded-xl">
          <div className="flex items-center space-x-2">
            <div className="w-5 h-5 bg-blue-100 rounded-full flex items-center justify-center">
              <span className="text-blue-600 text-xs font-bold">✨</span>
            </div>
            <span className="font-medium">All enhancements are powered by GPT for the best results.</span>
          </div>
        </div>
      </div>
    </div>
  );
}
