import { useState, useEffect } from 'react';
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
      <div className="space-y-8">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent">
              Your Resumes
            </h1>
            <p className="mt-2 text-gray-600">
              AI-powered resume enhancement for your next career move
            </p>
          </div>
          
          <button
            onClick={() => setShowUpload(true)}
            className="inline-flex items-center px-6 py-3 border border-transparent text-sm font-semibold rounded-xl shadow-sm text-white bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all duration-200"
          >
            <Plus className="h-5 w-5 mr-2" />
            Upload Resume
          </button>
        </div>

        {showUpload && (
          <div className="bg-white/70 backdrop-blur-sm rounded-2xl border border-gray-200/50 p-8 shadow-lg">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-semibold text-gray-900">
                Upload New Resume
              </h3>
              <button
                onClick={() => setShowUpload(false)}
                className="text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full w-8 h-8 flex items-center justify-center transition-colors"
              >
                ×
              </button>
            </div>
            <FileUpload onUploadComplete={handleUploadComplete} />
          </div>
        )}

        {resumes.length === 0 ? (
          <div className="text-center py-20">
            <div className="mx-auto w-16 h-16 bg-gradient-to-r from-indigo-100 to-purple-100 rounded-2xl flex items-center justify-center mb-6">
              <Plus className="h-8 w-8 text-indigo-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900">
              No resumes yet
            </h3>
            <p className="mt-2 text-gray-600 max-w-sm mx-auto">
              Get started by uploading your first resume and let AI enhance it for you
            </p>
            <div className="mt-8">
              <button
                onClick={() => setShowUpload(true)}
                className="inline-flex items-center px-6 py-3 border border-transparent shadow-sm font-semibold rounded-xl text-white bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 transition-all duration-200"
              >
                <Plus className="h-5 w-5 mr-2" />
                Upload Resume
              </button>
            </div>
          </div>
        ) : (
          <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
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
