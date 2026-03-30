<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-100 p-4">
    <div class="max-w-md w-full bg-white rounded-xl shadow-lg p-8 space-y-6 border border-slate-200">
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-slate-900">Bienvenido</h1>
        <p class="text-slate-500">Inicia sesión para gestionar tu tienda</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label for="username" class="text-xs font-bold text-slate-500 uppercase">Usuario</label>
          <input 
            type="text" 
            v-model="username" 
            id="username" 
            class="mt-2 block w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
            required
          >
        </div>
        <div>
          <label for="password" class="text-xs font-bold text-slate-500 uppercase">Contraseña</label>
          <input 
            type="password" 
            v-model="password" 
            id="password" 
            class="mt-2 block w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
            required
          >
        </div>
        
        <div class="pt-2">
          <button 
            type="submit" 
            :disabled="authStore.isLoading" 
            class="w-full py-3 px-4 bg-indigo-600 text-white font-bold rounded-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-indigo-400 disabled:cursor-not-allowed transition-all flex items-center justify-center"
          >
            <span v-if="!authStore.isLoading">Ingresar</span>
            <span v-else class="flex items-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Procesando...
            </span>
          </button>
        </div>

        <div v-if="authStore.error" class="bg-red-50 border-l-4 border-red-400 p-4 mt-4 rounded-r-lg">
          <div class="flex">
            <div class="py-1"><svg class="h-6 w-6 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg></div>
            <div class="ml-3">
              <h3 class="text-sm font-bold text-red-800">Error de Autenticación</h3>
              <p class="text-sm text-red-700 mt-1">{{ authStore.error }}</p>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const username = ref('user');
const password = ref('password');

const authStore = useAuthStore();
const router = useRouter();

const handleLogin = async () => {
  await authStore.login(username.value, password.value);
  if (authStore.isAuthenticated) {
    router.push('/');
  }
};
</script>
