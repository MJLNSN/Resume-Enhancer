import React, { useState } from 'react';
import { useAuth } from './hooks/useAuth';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { ResumeDetailPage } from './pages/ResumeDetailPage';

type View = 'dashboard' | 'resume-detail';

function App() {
  const { isAuthenticated, loading } = useAuth();
  const [currentView, setCurrentView] = useState<View>('dashboard');
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null);

  const handleResumeSelect = (resumeId: number) => {
    setSelectedResumeId(resumeId);
    setCurrentView('resume-detail');
  };

  const handleBackToDashboard = () => {
    setSelectedResumeId(null);
    setCurrentView('dashboard');
  };

  const handleAuthSuccess = () => {
    setCurrentView('dashboard');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <LoginPage onSuccess={handleAuthSuccess} />;
  }

  switch (currentView) {
    case 'resume-detail':
      return selectedResumeId ? (
        <ResumeDetailPage
          resumeId={selectedResumeId}
          onBack={handleBackToDashboard}
        />
      ) : (
        <DashboardPage onResumeSelect={handleResumeSelect} />
      );
    
    case 'dashboard':
    default:
      return <DashboardPage onResumeSelect={handleResumeSelect} />;
  }
}

export default App;
