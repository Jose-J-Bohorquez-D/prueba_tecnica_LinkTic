import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([]);

  const add = (message, type = 'success') => {
    const id = Date.now();
    toasts.value.push({ id, message, type });
    setTimeout(() => {
      remove(id);
    }, 3000);
  };

  const remove = (id) => {
    toasts.value = toasts.value.filter(t => t.id !== id);
  };

  return {
    toasts,
    success: (msg) => add(msg, 'success'),
    error: (msg) => add(msg, 'error'),
    remove
  };
});
