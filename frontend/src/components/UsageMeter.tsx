import { useEffect, useState } from 'react';
import { Zap, Brain, AlertTriangle } from 'lucide-react';
import { apiService } from '../services/api';

interface UsageLimits {
  gptRemaining: number;
  enhancementRemaining: number;
  unlimited: boolean;
  canUseGpt: boolean;
  canUseEnhancement: boolean;
  resetTimeFormatted?: string;
  hoursUntilReset?: number;
}

export function UsageMeter() {
  const [usage, setUsage] = useState<UsageLimits | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUsage();
  }, []);

  const loadUsage = async () => {
    try {
      const response = await apiService.getUsageLimits();
      setUsage(response);
    } catch (error) {
      console.error('Failed to load usage limits:', error);
    } finally {
      setLoading(false);
    }
  };

  const getUsageColor = (remaining: number, total: number = 100) => {
    const percentage = (remaining / total) * 100;
    if (percentage > 50) return 'text-green-500';
    if (percentage > 20) return 'text-yellow-500';
    return 'text-red-500';
  };

  const getProgressColor = (remaining: number, total: number = 100) => {
    const percentage = (remaining / total) * 100;
    if (percentage > 50) return 'bg-green-500';
    if (percentage > 20) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-4 animate-pulse">
        <div className="h-4 bg-gray-200 rounded w-24 mb-2"></div>
        <div className="h-2 bg-gray-200 rounded"></div>
      </div>
    );
  }

  if (!usage || usage.unlimited) {
    return (
      <div className="bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl border border-green-200/50 p-6 shadow-sm">
        <div className="flex items-center space-x-3 text-green-700">
          <div className="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
            <Zap className="h-4 w-4" />
          </div>
          <span className="font-semibold">Unlimited Usage</span>
        </div>
        <p className="text-sm text-green-600 mt-2">
          No daily limits configured
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white/70 backdrop-blur-sm rounded-xl border border-gray-200/50 p-6 space-y-6 shadow-sm">
      <div className="flex items-center space-x-3">
        <div className="w-8 h-8 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center">
          <Brain className="h-4 w-4 text-white" />
        </div>
        <h3 className="font-semibold text-gray-900">Daily Usage</h3>
      </div>

      {/* GPT Usage */}
      <div>
        <div className="flex justify-between items-center mb-2">
          <span className="text-xs font-medium text-gray-700">GPT Features</span>
          <span className={`text-xs font-medium ${getUsageColor(usage.gptRemaining, 50)}`}>
            {usage.gptRemaining} remaining
          </span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-3">
          <div
            className={`h-3 rounded-full transition-all duration-500 ${getProgressColor(usage.gptRemaining, 50)} shadow-sm`}
            style={{
              width: `${Math.max(0, Math.min(100, (usage.gptRemaining / 50) * 100))}%`
            }}
          ></div>
        </div>
        {!usage.canUseGpt && (
          <div className="flex items-center space-x-1 mt-1">
            <AlertTriangle className="h-3 w-3 text-red-500" />
            <span className="text-xs text-red-600">Daily limit reached</span>
          </div>
        )}
      </div>

      {/* Enhancement Usage */}
      <div>
        <div className="flex justify-between items-center mb-2">
          <span className="text-xs font-medium text-gray-700">Enhancements</span>
          <span className={`text-xs font-medium ${getUsageColor(usage.enhancementRemaining, 100)}`}>
            {usage.enhancementRemaining} remaining
          </span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-3">
          <div
            className={`h-3 rounded-full transition-all duration-500 ${getProgressColor(usage.enhancementRemaining, 100)} shadow-sm`}
            style={{
              width: `${Math.max(0, Math.min(100, (usage.enhancementRemaining / 100) * 100))}%`
            }}
          ></div>
        </div>
        {!usage.canUseEnhancement && (
          <div className="flex items-center space-x-1 mt-1">
            <AlertTriangle className="h-3 w-3 text-red-500" />
            <span className="text-xs text-red-600">Daily limit reached</span>
          </div>
        )}
      </div>

      <div className="text-xs text-gray-500 border-t border-gray-200/50 pt-4">
        {usage.resetTimeFormatted ? (
          <div className="bg-gray-50 rounded-lg p-3">
            <div className="font-medium text-gray-700">Limits reset in: <span className="text-indigo-600">{usage.resetTimeFormatted}</span></div>
            <div className="text-gray-500 mt-1">Daily at midnight UTC</div>
          </div>
        ) : (
          <div className="text-center">Limits reset daily at midnight UTC</div>
        )}
      </div>
    </div>
  );
}
