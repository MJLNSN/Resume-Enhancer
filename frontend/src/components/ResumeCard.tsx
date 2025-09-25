import React from 'react';
import { FileText, Calendar, AlertTriangle, CheckCircle, Clock } from 'lucide-react';
import { Resume } from '../types';

interface ResumeCardProps {
  resume: Resume;
  onClick: () => void;
}

export function ResumeCard({ resume, onClick }: ResumeCardProps) {
  const getStatusIcon = () => {
    if (resume.parseError) {
      return <AlertTriangle className="h-4 w-4 text-red-500" />;
    }
    if (resume.parsedJson) {
      return <CheckCircle className="h-4 w-4 text-green-500" />;
    }
    return <Clock className="h-4 w-4 text-yellow-500" />;
  };

  const getStatusText = () => {
    if (resume.parseError) return 'Parse Error';
    if (resume.parsedJson) return 'Ready';
    return 'Processing';
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div
      onClick={onClick}
      className="bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow cursor-pointer"
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center space-x-2 mb-2">
            <FileText className="h-5 w-5 text-gray-400" />
            <h3 className="text-lg font-medium text-gray-900">
              Resume #{resume.id}
            </h3>
          </div>
          
          <div className="flex items-center space-x-4 text-sm text-gray-500 mb-3">
            <div className="flex items-center space-x-1">
              <Calendar className="h-4 w-4" />
              <span>{formatDate(resume.createdAt)}</span>
            </div>
          </div>

          {resume.rawText && (
            <p className="text-sm text-gray-600 line-clamp-2 mb-3">
              {resume.rawText.substring(0, 150)}...
            </p>
          )}

          {resume.parsedJson && (
            <div className="flex items-center space-x-4 text-xs text-gray-500">
              <span>{resume.parsedJson.skills?.length || 0} skills</span>
              <span>{resume.parsedJson.experiences?.length || 0} experiences</span>
              <span>{resume.parsedJson.education?.length || 0} education</span>
            </div>
          )}
        </div>

        <div className="flex items-center space-x-1 text-sm">
          {getStatusIcon()}
          <span className={`
            ${resume.parseError ? 'text-red-500' : ''}
            ${resume.parsedJson ? 'text-green-500' : ''}
            ${!resume.parseError && !resume.parsedJson ? 'text-yellow-500' : ''}
          `}>
            {getStatusText()}
          </span>
        </div>
      </div>
    </div>
  );
}
