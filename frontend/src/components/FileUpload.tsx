import { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, FileText, AlertCircle, Type } from 'lucide-react';
import { apiService } from '../services/api';

interface FileUploadProps {
  onUploadComplete: (resumeId: number) => void;
}

export function FileUpload({ onUploadComplete }: FileUploadProps) {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [inputMode, setInputMode] = useState<'file' | 'text'>('file');
  const [textInput, setTextInput] = useState('');

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    const file = acceptedFiles[0];
    if (!file) return;

    setUploading(true);
    setError(null);

    try {
      console.log('Uploading file:', file.name, file.type, file.size);
      const resume = await apiService.uploadResume(file);
      console.log('Upload successful:', resume);
      onUploadComplete(resume.id);
    } catch (err: any) {
      console.error('Upload error:', err);
      const errorMessage = err.response?.data?.error || 
                          err.response?.data?.message || 
                          err.message || 
                          'Failed to upload file';
      setError(errorMessage);
    } finally {
      setUploading(false);
    }
  }, [onUploadComplete]);

  const handleTextSubmit = async () => {
    if (!textInput.trim()) return;

    setUploading(true);
    setError(null);

    try {
      console.log('Submitting text:', textInput.length, 'characters');
      const resume = await apiService.createTextResume({ text: textInput.trim() });
      console.log('Text submission successful:', resume);
      onUploadComplete(resume.id);
      setTextInput('');
    } catch (err: any) {
      console.error('Text submission error:', err);
      const errorMessage = err.response?.data?.error || 
                          err.response?.data?.message || 
                          err.message || 
                          'Failed to create resume from text';
      setError(errorMessage);
    } finally {
      setUploading(false);
    }
  };

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/pdf': ['.pdf'],
      'text/plain': ['.txt'],
      'text/markdown': ['.md'],
    },
    maxSize: 5 * 1024 * 1024, // 5MB
    multiple: false,
  });

  return (
    <div className="w-full">
      {/* Input Mode Toggle */}
      <div className="flex space-x-1 mb-4 bg-gray-100 p-1 rounded-lg">
        <button
          onClick={() => setInputMode('file')}
          className={`flex-1 flex items-center justify-center px-3 py-2 text-sm font-medium rounded-md transition-colors ${
            inputMode === 'file'
              ? 'bg-white text-gray-900 shadow-sm'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          <Upload className="h-4 w-4 mr-2" />
          Upload File
        </button>
        <button
          onClick={() => setInputMode('text')}
          className={`flex-1 flex items-center justify-center px-3 py-2 text-sm font-medium rounded-md transition-colors ${
            inputMode === 'text'
              ? 'bg-white text-gray-900 shadow-sm'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          <Type className="h-4 w-4 mr-2" />
          Paste Text
        </button>
      </div>

      {inputMode === 'file' ? (
        <div
          {...getRootProps()}
          className={`
            border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors
            ${isDragActive 
              ? 'border-blue-400 bg-blue-50' 
              : 'border-gray-300 hover:border-gray-400'
            }
            ${uploading ? 'pointer-events-none opacity-50' : ''}
          `}
        >
          <input {...getInputProps()} />
          
          <div className="flex flex-col items-center space-y-3">
            {uploading ? (
              <>
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                <p className="text-sm text-gray-600">Uploading...</p>
              </>
            ) : (
              <>
                <Upload className="h-8 w-8 text-gray-400" />
                <div>
                  <p className="text-sm font-medium text-gray-900">
                    {isDragActive 
                      ? 'Drop your resume here' 
                      : 'Click to upload or drag and drop'
                    }
                  </p>
                  <p className="text-xs text-gray-500 mt-1">
                    PDF, TXT, or MD files only, max 5MB
                  </p>
                </div>
              </>
            )}
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          <textarea
            value={textInput}
            onChange={(e) => setTextInput(e.target.value)}
            placeholder="Paste your resume text here..."
            className="w-full h-64 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
            disabled={uploading}
          />
          <button
            onClick={handleTextSubmit}
            disabled={uploading || !textInput.trim()}
            className="w-full inline-flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {uploading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                Creating Resume...
              </>
            ) : (
              <>
                <FileText className="h-4 w-4 mr-2" />
                Create Resume
              </>
            )}
          </button>
        </div>
      )}

      {error && (
        <div className="mt-3 flex items-center space-x-2 text-red-600 text-sm">
          <AlertCircle className="h-4 w-4" />
          <span>{error}</span>
        </div>
      )}
    </div>
  );
}
