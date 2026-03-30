<template>
  <transition name="fade">
    <div v-if="isOpen" class="fixed inset-0 z-[1000] overflow-y-auto" role="dialog" aria-modal="true">
      <div class="fixed inset-0 bg-slate-900/50 backdrop-blur-sm transition-opacity" @click="closeModal"></div>
      
      <div class="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
        <transition name="pop">
          <div 
            class="relative transform overflow-hidden rounded-2xl bg-white text-left shadow-2xl transition-all sm:my-8 sm:w-full sm:max-w-md p-6"
            @click.stop
          >
            <div class="mb-4">
              <h3 class="text-xl font-bold text-slate-900 mb-2">{{ title }}</h3>
              <p class="text-slate-500">{{ message }}</p>
            </div>

            <div class="flex justify-end gap-3 pt-4 border-t border-slate-100">
              <button 
                @click="closeModal" 
                class="px-4 py-2 text-sm font-bold text-slate-600 hover:bg-slate-50 rounded-lg transition-colors"
              >
                Cancelar
              </button>
              <button 
                @click="confirm" 
                class="px-4 py-2 text-sm font-bold text-white bg-red-600 hover:bg-red-700 rounded-lg shadow-md shadow-red-100 transition-all"
              >
                Confirmar
              </button>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </transition>
</template>

<script setup>
defineProps({
  isOpen: Boolean,
  title: String,
  message: String,
})

const emit = defineEmits(['close', 'confirm'])

function closeModal() {
  emit('close')
}

function confirm() {
  emit('confirm')
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.pop-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.pop-leave-active { transition: all 0.2s ease-in; }
.pop-enter-from { opacity: 0; transform: scale(0.9); }
.pop-leave-to { opacity: 0; transform: scale(0.95); }
</style>
