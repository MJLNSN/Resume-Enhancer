import React, { useState, useEffect } from 'react';
import { Layout } from '../components/Layout';
import { FileUpload } from '../components/FileUpload';
import { ResumeCard } from '../components/ResumeCard';
import { Resume } from '../types';
import { apiService } from '../services/api';
import { Plus, Loader } from 'lucide-react';

interface DashboardPageProps {
  onResumeSelect: (resumeId: number) => void;
}

export function DashboardPage({ onResumeSelect }: DashboardPageProps) {
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [loading, setLoading] = useState(true);
  const [showUpload, setShowUpload] = useState(false);

  useEffect(() => {
    loadResumes();
  }, []);

  const loadResumes = async () => {
    try {
      const data = await apiService.getUserResumes();
      setResumes(data);
    } catch (error) {
      console.error('Failed to load resumes:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUploadComplete = async (resumeId: number) => {
    setShowUpload(false);
    await loadResumes();
    onResumeSelect(resumeId);
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

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Your Resumes</h1>
            <p className="mt-1 text-sm text-gray-500">
              Upload and enhance your resumes with AI-powered suggestions
            </p>
          </div>
          
          <button
            onClick={() => setShowUpload(true)}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            <Plus className="h-4 w-4 mr-2" />
            Upload Resume
          </button>
        </div>

        {showUpload && (
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-medium text-gray-900">
                Upload New Resume
              </h3>
              <button
                onClick={() => setShowUpload(false)}
                className="text-gray-400 hover:text-gray-500"
              >
                ×
              </button>
            </div>
            <FileUpload onUploadComplete={handleUploadComplete} />
          </div>
        )}

        {resumes.length === 0 ? (
          <div className="text-center py-12">
            <div className="mx-auto h-12 w-12 text-gray-400">
              <Plus className="h-12 w-12" />
            </div>
            <h3 className="mt-2 text-sm font-medium text-gray-900">
              No resumes yet
            </h3>
            <p className="mt-1 text-sm text-gray-500">
              Get started by uploading your first resume
            </p>
            <div className="mt-6">
              <button
                onClick={() => setShowUpload(true)}
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700"
              >
                <Plus className="h-4 w-4 mr-2" />
                Upload Resume
              </button>
            </div>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {resumes.map((resume) => (
              <ResumeCard
                key={resume.id}
                resume={resume}
                onClick={() => onResumeSelect(resume.id)}
              />
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
