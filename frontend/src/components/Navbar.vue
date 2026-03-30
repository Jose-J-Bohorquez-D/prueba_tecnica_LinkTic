<template>
  <nav v-if="authStore.isAuthenticated" class="bg-indigo-600 text-white shadow-lg sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <!-- Logo y Navegación Principal -->
        <div class="flex items-center space-x-8">
          <router-link to="/" class="flex items-center space-x-2">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 118 0v4M5 9h14l1 12H4L5 9z"></path>
            </svg>
            <span class="text-xl font-black tracking-tight">TiendaPro</span>
          </router-link>

          <div class="hidden md:flex space-x-4">
            <router-link 
              to="/" 
              class="px-3 py-2 rounded-md text-sm font-medium hover:bg-indigo-500 transition-colors"
              active-class="bg-indigo-700"
            >
              Catálogo
            </router-link>
            <router-link 
              to="/product/new" 
              class="px-3 py-2 rounded-md text-sm font-medium hover:bg-indigo-500 transition-colors"
              active-class="bg-indigo-700"
            >
              Nuevo Producto
            </router-link>
          </div>
        </div>

        <!-- Usuario y Acciones -->
        <div class="flex items-center space-x-4">
          <div class="hidden sm:flex flex-col items-end mr-2">
            <span class="text-xs font-bold uppercase opacity-75">Administrador</span>
            <span class="text-sm font-medium">Jose Pérez</span>
          </div>
          <button 
            @click="handleLogout" 
            class="bg-indigo-700 hover:bg-indigo-800 p-2 rounded-full transition-all group"
            title="Cerrar Sesión"
          >
            <svg class="w-5 h-5 group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.router-link-active {
  @apply bg-indigo-700;
}
</style>
