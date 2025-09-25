import React, { useState } from 'react';
import { Wand2, Brain, Globe, Loader, AlertCircle } from 'lucide-react';
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
  const [mode, setMode] = useState<'local' | 'gpt'>('local');

  const handleEnhance = async () => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiService.analyzeResume({
        resumeId,
        jobDescription: jobDescription.trim() || undefined,
        mode,
        forceRefresh: true,
      });
      
      onEnhancementComplete(result);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to enhance resume');
    } finally {
      setLoading(false);
    }
  };

  const handleTranslate = async (targetLang: 'en' | 'zh') => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiService.translateResume({
        resumeId,
        targetLang,
        mode: 'gpt',
      });
      
      onEnhancementComplete(result);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to translate resume');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 className="text-lg font-medium text-gray-900 mb-4">
        AI Enhancement
      </h3>

      {error && (
        <div className="mb-4 flex items-center space-x-2 text-red-600 text-sm">
          <AlertCircle className="h-4 w-4" />
          <span>{error}</span>
        </div>
      )}

      <div className="space-y-4">
        {/* Job Description Input */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Target Job Description (Optional)
          </label>
          <textarea
            value={jobDescription}
            onChange={(e) => setJobDescription(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            rows={3}
            placeholder="Paste the job description here to tailor the enhancement..."
            disabled={loading}
          />
        </div>

        {/* Mode Selection */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Enhancement Mode
          </label>
          <div className="flex space-x-3">
            <label className="flex items-center">
              <input
                type="radio"
                value="local"
                checked={mode === 'local'}
                onChange={(e) => setMode(e.target.value as 'local' | 'gpt')}
                className="mr-2"
                disabled={loading}
              />
              <span className="text-sm text-gray-700">Local Template</span>
            </label>
            <label className="flex items-center">
              <input
                type="radio"
                value="gpt"
                checked={mode === 'gpt'}
                onChange={(e) => setMode(e.target.value as 'local' | 'gpt')}
                className="mr-2"
                disabled={loading}
              />
              <span className="text-sm text-gray-700">GPT Powered</span>
            </label>
          </div>
          <p className="text-xs text-gray-500 mt-1">
            GPT mode requires OpenAI API key configuration
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap gap-3">
          <button
            onClick={handleEnhance}
            disabled={loading}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <Loader className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <Wand2 className="h-4 w-4 mr-2" />
            )}
            Enhance Resume
          </button>

          <button
            onClick={() => handleTranslate('en')}
            disabled={loading}
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Globe className="h-4 w-4 mr-2" />
            Translate to English
          </button>

          <button
            onClick={() => handleTranslate('zh')}
            disabled={loading}
            className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Globe className="h-4 w-4 mr-2" />
            翻译成中文
          </button>
        </div>

        {mode === 'gpt' && (
          <div className="text-xs text-amber-600 bg-amber-50 p-3 rounded-md">
            <strong>Note:</strong> GPT features require an OpenAI API key. 
            If not configured, local template will be used as fallback.
          </div>
        )}
      </div>
    </div>
  );
}
