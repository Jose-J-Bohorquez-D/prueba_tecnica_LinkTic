<template>
  <div id="app" class="min-h-screen bg-slate-50 flex flex-col">
    <!-- Navbar Global (Solo visible si está autenticado) -->
    <Navbar />

    <!-- Toaster Global para Notificaciones -->
    <Toaster />

    <!-- Contenido Principal -->
    <div class="flex-grow">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- Footer Simple -->
    <footer v-if="authStore.isAuthenticated" class="bg-white border-t border-slate-200 py-6 mt-12">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <p class="text-slate-400 text-sm">
          &copy; 2026 TiendaPro - Sistema de Gestión de Inventario
        </p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import Navbar from '@/components/Navbar.vue';
import Toaster from '@/components/Toaster.vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
</script>

<style>
/* Reset de estilos globales */
body {
  margin: 0;
  padding: 0;
  @apply antialiased text-slate-900;
}

/* Transición Fade para rutas */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
