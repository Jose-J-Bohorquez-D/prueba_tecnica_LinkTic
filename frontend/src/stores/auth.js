import { defineStore } from 'pinia';
import { api } from '@/api/api';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: null, // Podrías decodificar el token para obtener info del usuario aquí
    isLoading: false,
    error: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
  },
  actions: {
    async login(username, password) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await api.post('/auth/login', { username, password });
        const token = response.data.token; // Asumiendo que la respuesta es { token: '...' }
        if (!token) {
          throw new Error('No se recibió un token');
        }
        this.token = token;
        localStorage.setItem('token', token);
        return true;
      } catch (e) {
        this.error = e.response?.data?.errors?.[0]?.detail || e.message || 'Error desconocido al iniciar sesión.';
        // Limpiamos el token por si acaso
        localStorage.removeItem('token');
        this.token = null;
        return false;
      } finally {
        this.isLoading = false;
      }
    },
    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('token');
      // Opcional: redirigir al login
      // router.push('/login');
    },
  },
});
