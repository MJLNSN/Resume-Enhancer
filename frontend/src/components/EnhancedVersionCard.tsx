import React from 'react';
import { Download, Calendar, Globe, Wand2, FileText, ExternalLink } from 'lucide-react';
import { EnhancedResumeResponse } from '../types';
import { apiService } from '../services/api';

interface EnhancedVersionCardProps {
  version: EnhancedResumeResponse;
  onClick: () => void;
}

export function EnhancedVersionCard({ version, onClick }: EnhancedVersionCardProps) {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getTypeIcon = () => {
    switch (version.enhancementType) {
      case 'rewrite':
        return <Wand2 className="h-4 w-4" />;
      case 'translate':
        return <Globe className="h-4 w-4" />;
      default:
        return <FileText className="h-4 w-4" />;
    }
  };

  const getTypeLabel = () => {
    switch (version.enhancementType) {
      case 'rewrite':
        return 'Enhanced';
      case 'translate':
        return `Translated (${version.language})`;
      default:
        return version.enhancementType;
    }
  };

  const getLanguageLabel = () => {
    switch (version.language) {
      case 'EN':
        return 'English';
      case 'ZH':
        return '中文';
      default:
        return 'Original';
    }
  };

  const handleExportMarkdown = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      const blob = await apiService.exportMarkdownDirect(version.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = `resume_${version.id}.md`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Failed to export markdown:', error);
    }
  };

  const handleExportPdf = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      const response = await apiService.exportResume({
        enhancedResumeId: version.id,
        format: 'pdf',
      });
      
      if (response.downloadUrl) {
        window.open(response.downloadUrl, '_blank');
      }
    } catch (error) {
      console.error('Failed to export PDF:', error);
    }
  };

  const handleExportComparison = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      const blob = await apiService.exportComparison(version.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = `comparison_${version.id}.html`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Failed to export comparison:', error);
    }
  };

  return (
    <div
      onClick={onClick}
      className="bg-white rounded-lg border border-gray-200 p-4 hover:shadow-md transition-shadow cursor-pointer"
    >
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center space-x-2">
          {getTypeIcon()}
          <span className="font-medium text-gray-900">{getTypeLabel()}</span>
          <span className="text-sm text-gray-500">({getLanguageLabel()})</span>
        </div>
        
        <div className="flex items-center space-x-1">
          <button
            onClick={handleExportMarkdown}
            className="p-1.5 text-gray-400 hover:text-gray-600 rounded"
            title="Export Markdown"
          >
            <FileText className="h-4 w-4" />
          </button>
          <button
            onClick={handleExportPdf}
            className="p-1.5 text-gray-400 hover:text-gray-600 rounded"
            title="Export PDF"
          >
            <Download className="h-4 w-4" />
          </button>
          {version.enhancementType === 'rewrite' && (
            <button
              onClick={handleExportComparison}
              className="p-1.5 text-gray-400 hover:text-gray-600 rounded"
              title="Compare Versions"
            >
              <ExternalLink className="h-4 w-4" />
            </button>
          )}
        </div>
      </div>

      <div className="text-sm text-gray-600 mb-3">
        <div className="flex items-center space-x-1 mb-1">
          <Calendar className="h-3 w-3" />
          <span>{formatDate(version.createdAt)}</span>
        </div>
      </div>

      <div className="text-sm text-gray-700 line-clamp-3 mb-3">
        {version.enhancedText.substring(0, 200)}...
      </div>

      {version.suggestions && version.suggestions.length > 0 && (
        <div className="border-t pt-3">
          <p className="text-xs font-medium text-gray-700 mb-2">
            AI Suggestions ({version.suggestions.length})
          </p>
          <ul className="text-xs text-gray-600 space-y-1">
            {version.suggestions.slice(0, 2).map((suggestion, index) => (
              <li key={index} className="truncate">
                • {suggestion}
              </li>
            ))}
            {version.suggestions.length > 2 && (
              <li className="text-gray-400">
                +{version.suggestions.length - 2} more...
              </li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
}
