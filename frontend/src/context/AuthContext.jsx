import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/api';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));
  const [user, setUser] = useState(null);
  const [userId, setUserId] = useState(localStorage.getItem('userId') || null);

  const login = (token) => {
    localStorage.setItem('token', token);
    setIsAuthenticated(true);
    const decoded = jwtDecode(token);
    const id = decoded.userId;
    if (id) {
      localStorage.setItem('userId', id);
      setUserId(id);
    }
    fetchUserData();
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    setIsAuthenticated(false);
    setUser(null);
    setUserId(null);
  };

  const register = (userData) => {
    setUser(userData);
    if (userData.id) {
      localStorage.setItem('userId', userData.id);
      setUserId(userData.id);
    }
  };

  const fetchUserData = async () => {
    if (isAuthenticated) {
      try {
        const token = localStorage.getItem('token');
        if (!token) throw new Error('Токен не найден');
        const decoded = jwtDecode(token);
        const id = decoded.userId;
        if (!id) throw new Error('User ID not found in token');
        localStorage.setItem('userId', id);
        setUserId(id);
        const response = await api.get(`/users/${id}/profile`);
        if (response.status === 200) {
          setUser(response.data);
        } else {
          console.error('Profile fetch error:', response.data);
          logout();
        }
      } catch (err) {
        console.error('Fetch user data error:', err);
        if (err.response?.status === 403 || err.response?.status === 500) {
          logout();
        }
      }
    }
  };

  useEffect(() => {
    fetchUserData();
  }, [isAuthenticated]);

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, userId, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);