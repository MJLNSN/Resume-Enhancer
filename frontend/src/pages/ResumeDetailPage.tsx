import { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { EnhancementPanel } from '../components/EnhancementPanel';
import { EnhancedVersionCard } from '../components/EnhancedVersionCard';
import { Resume, EnhancedResumeResponse } from '../types';
import { apiService } from '../services/api';
import { ArrowLeft, Loader, AlertCircle, CheckCircle, Clock, Sparkles, Download } from 'lucide-react';

interface ResumeDetailPageProps {
  resumeId: number;
  onBack: () => void;
}

export function ResumeDetailPage({ resumeId, onBack }: ResumeDetailPageProps) {
  const [resume, setResume] = useState<Resume | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [enhancedVersions, setEnhancedVersions] = useState<EnhancedResumeResponse[]>([]);
  const [selectedVersion, setSelectedVersion] = useState<EnhancedResumeResponse | null>(null);
  const [showEnhancementPanel, setShowEnhancementPanel] = useState(false);

  useEffect(() => {
    loadResume();
    const interval = setInterval(() => {
      if (resume && (!resume.rawText || !resume.parsedJson)) {
        loadResume();
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [resumeId]);

  const loadResume = async () => {
    try {
      const data = await apiService.getResume(resumeId);
      setResume(data);
      
      // Load enhanced versions
      if (data.parsedJson) {
        await loadEnhancedVersions();
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load resume');
    } finally {
      setLoading(false);
    }
  };

  const loadEnhancedVersions = async () => {
    try {
      const versions = await apiService.getEnhancedVersions(resumeId);
      setEnhancedVersions(versions);
    } catch (err) {
      console.error('Failed to load enhanced versions:', err);
    }
  };

  const handleEnhancementComplete = async (enhanced: EnhancedResumeResponse) => {
    await loadEnhancedVersions();
    setSelectedVersion(enhanced);
    setShowEnhancementPanel(false);
  };

  const handleVersionSelect = (version: EnhancedResumeResponse) => {
    setSelectedVersion(version);
  };

  const handleDownloadMarkdown = async () => {
    if (!selectedVersion) return;
    
    try {
      const blob = await apiService.exportMarkdownDirect(selectedVersion.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = `resume_${selectedVersion.id}.md`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Failed to download markdown:', error);
    }
  };

  const handleDownloadHtml = async () => {
    if (!selectedVersion) return;
    
    try {
      const blob = await apiService.exportHtmlDirect(selectedVersion.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = `resume_${selectedVersion.id}.html`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Failed to download HTML:', error);
    }
  };


  const getProcessingStatus = () => {
    if (!resume) return { icon: <Clock className="h-5 w-5" />, text: 'Loading...', color: 'text-yellow-500' };
    
    if (resume.parseError) {
      return { 
        icon: <AlertCircle className="h-5 w-5" />, 
        text: 'Processing failed', 
        color: 'text-red-500' 
      };
    }
    
    if (resume.parsedJson) {
      return { 
        icon: <CheckCircle className="h-5 w-5" />, 
        text: 'Ready for enhancement', 
        color: 'text-green-500' 
      };
    }
    
    return { 
      icon: <Clock className="h-5 w-5" />, 
      text: 'Processing...', 
      color: 'text-yellow-500' 
    };
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <Loader className="h-8 w-8 animate-spin text-gray-400" />
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="text-center py-12">
          <AlertCircle className="mx-auto h-12 w-12 text-red-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">Error</h3>
          <p className="mt-1 text-sm text-gray-500">{error}</p>
          <button
            onClick={onBack}
            className="mt-6 inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Dashboard
          </button>
        </div>
      </Layout>
    );
  }

  const status = getProcessingStatus();

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <button
            onClick={onBack}
            className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-700"
          >
            <ArrowLeft className="h-4 w-4 mr-1" />
            Back to Dashboard
          </button>
          
          <div className="flex items-center space-x-4">
            {resume?.parsedJson && (
              <button
                onClick={() => setShowEnhancementPanel(!showEnhancementPanel)}
                className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-semibold rounded-xl text-white bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 transition-all duration-200 shadow-lg hover:shadow-xl"
              >
                <Sparkles className="h-4 w-4 mr-2" />
                {showEnhancementPanel ? 'Hide Enhancement' : 'Enhance Resume'}
              </button>
            )}
            
            <div className={`flex items-center space-x-2 ${status.color}`}>
              {status.icon}
              <span className="text-sm font-medium">{status.text}</span>
            </div>
          </div>
        </div>

        <div className="bg-white/70 backdrop-blur-sm shadow-xl rounded-2xl border border-gray-200/50">
          <div className="px-8 py-6 border-b border-gray-200/50">
            <h1 className="text-2xl font-bold text-gray-900">
              Resume #{resume?.id}
            </h1>
            <p className="mt-2 text-gray-600">
              Uploaded {resume && new Date(resume.createdAt).toLocaleDateString()}
            </p>
          </div>

          <div className="p-8 space-y-10">
            {/* Enhancement Panel */}
            {showEnhancementPanel && resume?.parsedJson && (
              <EnhancementPanel
                resumeId={resume.id}
                onEnhancementComplete={handleEnhancementComplete}
              />
            )}

            {/* Enhanced Versions */}
            {enhancedVersions.length > 0 && (
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                  Enhanced Versions ({enhancedVersions.length})
                </h3>
                <div className="grid gap-4 md:grid-cols-2">
                  {enhancedVersions.map((version) => (
                    <EnhancedVersionCard
                      key={version.id}
                      version={version}
                      onClick={() => handleVersionSelect(version)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Selected Enhanced Version */}
            {selectedVersion && (
              <div>
                <div className="flex justify-between items-center mb-4">
                  <h3 className="text-lg font-medium text-gray-900">
                    Enhanced Content - {selectedVersion.enhancementType} ({selectedVersion.language})
                  </h3>
                  <div className="flex space-x-3">
                    <button
                      onClick={handleDownloadMarkdown}
                      className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
                    >
                      <Download className="h-4 w-4 mr-2" />
                      Download MD
                    </button>
                    <button
                      onClick={handleDownloadHtml}
                      className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
                    >
                      <Download className="h-4 w-4 mr-2" />
                      Download HTML
                    </button>
                  </div>
                </div>
                <div className="bg-gray-50 rounded-lg p-6">
                  <div className="prose max-w-none">
                    <pre className="text-sm text-gray-700 whitespace-pre-wrap font-mono">
                      {selectedVersion.enhancedText}
                    </pre>
                  </div>
                  
                  {selectedVersion.suggestions && selectedVersion.suggestions.length > 0 && (
                    <div className="mt-6 border-t pt-6">
                      <h4 className="font-medium text-gray-900 mb-3">AI Suggestions for Improvement:</h4>
                      <ul className="space-y-2">
                        {selectedVersion.suggestions.map((suggestion, index) => (
                          <li key={index} className="flex items-start space-x-2 text-sm text-gray-700">
                            <span className="flex-shrink-0 w-5 h-5 bg-blue-100 text-blue-800 rounded-full flex items-center justify-center text-xs font-medium">
                              {index + 1}
                            </span>
                            <span>{suggestion}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              </div>
            )}

            {resume?.rawText ? (
              <>
                <div>
                  <h3 className="text-lg font-medium text-gray-900 mb-4">
                    Extracted Text
                  </h3>
                  <div className="bg-gray-50 rounded-lg p-4">
                    <pre className="text-sm text-gray-700 whitespace-pre-wrap font-mono">
                      {resume.rawText}
                    </pre>
                  </div>
                </div>

                {resume.parsedJson && (
                  <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-4">
                      Parsed Information
                    </h3>
                    <div className="grid gap-6 lg:grid-cols-3">
                      <div className="bg-blue-50 rounded-lg p-4">
                        <h4 className="font-medium text-blue-900 mb-3">Skills</h4>
                        <div className="flex flex-wrap gap-2">
                          {resume.parsedJson.skills?.map((skill, index) => (
                            <span
                              key={index}
                              className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                            >
                              {skill}
                            </span>
                          ))}
                        </div>
                      </div>

                      <div className="bg-green-50 rounded-lg p-4">
                        <h4 className="font-medium text-green-900 mb-3">Experience</h4>
                        <div className="space-y-3">
                          {resume.parsedJson.experiences?.map((exp, index) => (
                            <div key={index} className="text-sm">
                              <p className="font-medium text-green-800">{exp.title}</p>
                              <p className="text-green-600">{exp.company}</p>
                              <p className="text-green-500 text-xs">{exp.years}</p>
                            </div>
                          ))}
                        </div>
                      </div>

                      <div className="bg-purple-50 rounded-lg p-4">
                        <h4 className="font-medium text-purple-900 mb-3">Education</h4>
                        <div className="space-y-3">
                          {resume.parsedJson.education?.map((edu, index) => (
                            <div key={index} className="text-sm">
                              <p className="font-medium text-purple-800">{edu.degree}</p>
                              <p className="text-purple-600">{edu.institution}</p>
                              <p className="text-purple-500 text-xs">{edu.years}</p>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </>
            ) : (
              <div className="text-center py-8">
                <Loader className="mx-auto h-8 w-8 animate-spin text-gray-400" />
                <p className="mt-2 text-sm text-gray-500">
                  Processing your resume...
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}
