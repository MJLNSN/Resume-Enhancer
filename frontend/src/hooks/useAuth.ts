import { useState, useEffect } from 'react';
import { User, AuthRequest, AuthResponse } from '../types';
import { apiService } from '../services/api';

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (token && savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (error) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (credentials: AuthRequest): Promise<void> => {
    const response: AuthResponse = await apiService.login(credentials);
    
    const userData: User = {
      id: response.userId,
      email: response.email,
    };
    
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(userData));
    
    // Use a Promise to ensure state update completes
    return new Promise((resolve) => {
      setUser(userData);
      // Force a small delay to ensure React state update
      setTimeout(resolve, 50);
    });
  };

  const register = async (credentials: AuthRequest): Promise<void> => {
    const response: AuthResponse = await apiService.register(credentials);
    
    const userData: User = {
      id: response.userId,
      email: response.email,
    };
    
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(userData));
    
    // Use a Promise to ensure state update completes
    return new Promise((resolve) => {
      setUser(userData);
      // Force a small delay to ensure React state update
      setTimeout(resolve, 50);
    });
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return {
    user,
    loading,
    login,
    register,
    logout,
    isAuthenticated: !!user,
  };
}
