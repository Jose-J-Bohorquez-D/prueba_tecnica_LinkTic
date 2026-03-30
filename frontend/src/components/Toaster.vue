<template>
  <div class="fixed bottom-4 right-4 z-[9999] flex flex-col gap-2">
    <transition-group name="list">
      <div 
        v-for="toast in toastStore.toasts" 
        :key="toast.id" 
        class="flex items-center p-4 min-w-[300px] rounded-lg shadow-xl text-white transform transition-all duration-300"
        :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'"
      >
        <span class="flex-grow">{{ toast.message }}</span>
        <button @click="toastStore.remove(toast.id)" class="ml-4 hover:scale-125 transition-transform">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { useToastStore } from '@/stores/toast';

const toastStore = useToastStore();
</script>

<style scoped>
.list-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
