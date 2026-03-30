<template>
  <div class="py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
      <router-link to="/" class="text-sm font-bold text-indigo-600 hover:text-indigo-500 flex items-center transition-colors">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
        Volver al Catálogo
      </router-link>
    </div>

    <main class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="bg-white p-8 rounded-xl shadow-sm border border-slate-200">
        <h1 class="text-3xl font-bold text-slate-900 mb-6">{{ isEditing ? 'Editar Producto' : 'Crear Nuevo Producto' }}</h1>

        <div v-if="prefillLoading" class="mb-6 text-sm text-slate-500 font-semibold">
          Cargando producto...
        </div>

        <div v-if="error" class="mb-6 bg-red-50 border border-red-200 text-red-700 p-4 rounded-xl text-sm">
          {{ error }}
        </div>
        
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label for="name" class="text-xs font-bold text-slate-500 uppercase">Nombre del Producto</label>
              <input type="text" v-model="form.name" id="name" class="mt-2 block w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-lg" required :disabled="prefillLoading">
            </div>
            <div>
              <label for="sku" class="text-xs font-bold text-slate-500 uppercase">SKU</label>
              <input type="text" v-model="form.sku" id="sku" class="mt-2 block w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-lg" required :disabled="prefillLoading">
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label for="price" class="text-xs font-bold text-slate-500 uppercase">Precio</label>
              <input type="number" v-model.number="form.price" id="price" min="0" step="0.01" class="mt-2 block w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-lg" required :disabled="prefillLoading">
            </div>
            <div>
              <label for="status" class="text-xs font-bold text-slate-500 uppercase">Estado</label>
              <select v-model="form.status" id="status" class="mt-2 block w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-lg" :disabled="prefillLoading">
                <option value="ACTIVE">Activo</option>
                <option value="INACTIVE">Inactivo</option>
              </select>
            </div>
          </div>

          <div class="border-t border-slate-200 pt-6 flex justify-end space-x-4">
            <router-link to="/" class="px-6 py-3 border border-slate-300 rounded-lg text-sm font-bold text-slate-700 hover:bg-slate-50">Cancelar</router-link>
            <button type="submit" :disabled="isLoading" class="px-6 py-3 bg-indigo-600 text-white font-bold rounded-lg hover:bg-indigo-700 disabled:bg-indigo-400">
              {{ isEditing ? 'Guardar Cambios' : 'Crear Producto' }}
            </button>
          </div>


        </form>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useProductStore } from '@/stores/product';
import { useToastStore } from '@/stores/toast';
import { api } from '@/api/api';

const route = useRoute();
const router = useRouter();
const productStore = useProductStore();
const toast = useToastStore();

const form = ref({
  name: '',
  sku: '',
  price: 0,
  status: 'ACTIVE',
});

const isLoading = ref(false);
const error = ref(null);
const prefillLoading = ref(false);

const isEditing = computed(() => !!route.params.id);

const loadProductForEdit = async () => {
  if (!isEditing.value) return;

  prefillLoading.value = true;
  error.value = null;

  try {
    const response = await api.get(`/products/${route.params.id}`);
    const product = response.data.data;
    form.value = {
      name: product.name ?? '',
      sku: product.sku ?? '',
      price: product.price ?? 0,
      status: product.status ?? 'ACTIVE',
    };
  } catch (e) {
    error.value = e.response?.data?.errors?.[0]?.detail || 'No se pudo cargar la información del producto para editar.';
  } finally {
    prefillLoading.value = false;
  }
};

onMounted(loadProductForEdit);
watch(() => route.params.id, loadProductForEdit);

const handleSubmit = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    if (isEditing.value) {
      await productStore.updateProduct(route.params.id, form.value);
      toast.success("Producto actualizado con éxito");
    } else {
      await productStore.createProduct(form.value);
      toast.success("Producto creado con éxito");
    }
    router.push('/');
  } catch (e) {
    error.value = e.message;
    toast.error(e.message);
  } finally {
    isLoading.value = false;
  }
};
</script>
