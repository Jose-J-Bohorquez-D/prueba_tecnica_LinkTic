<template>
  <div class="py-8">
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Filtros y Búsqueda -->
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
        <h1 class="text-3xl font-black text-slate-900">Catálogo de Productos</h1>
        <div class="flex flex-col sm:flex-row gap-2 w-full md:w-auto">
          <input 
            type="text" 
            v-model="search" 
            placeholder="Buscar por SKU o nombre..." 
            class="px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white shadow-sm transition-all"
          >
          <select 
            v-model="status" 
            class="px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white shadow-sm"
          >
            <option :value="null">Todos los estados</option>
            <option value="ACTIVE">Activo</option>
            <option value="INACTIVE">Inactivo</option>
          </select>
          <select 
            v-model="sortBy" 
            class="px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white shadow-sm"
          >
            <option value="createdAt,desc">Más nuevos primero</option>
            <option value="price,asc">Precio: Menor a Mayor</option>
            <option value="price,desc">Precio: Mayor a Menor</option>
          </select>
        </div>
      </div>

      <!-- Estado de Carga (Skeleton) -->
      <div v-if="productStore.isLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="n in 6" :key="n" class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 animate-pulse">
          <div class="h-4 bg-slate-200 rounded w-3/4 mb-4"></div>
          <div class="h-3 bg-slate-200 rounded w-1/2 mb-2"></div>
          <div class="h-8 bg-slate-200 rounded w-1/4 mt-4"></div>
        </div>
      </div>

      <!-- Estado de Error -->
      <div v-else-if="productStore.error" class="bg-red-50 border border-red-200 text-red-600 p-8 rounded-xl text-center">
        <p class="font-bold text-lg mb-2">Ups! Algo salió mal</p>
        <p class="mb-4">{{ productStore.error }}</p>
        <button @click="productStore.fetchProducts(0, status, search, sortBy, true)" class="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-all font-semibold shadow-md shadow-red-100">
          Reintentar manualmente
        </button>
      </div>

      <!-- Estado Vacío -->
      <div v-else-if="!productStore.products.length" class="text-center py-16">
        <p class="text-xl font-semibold text-slate-500">No se encontraron productos</p>
        <p class="text-slate-400 mt-2">Intenta ajustar los filtros o la búsqueda.</p>
      </div>

      <!-- Lista de Productos -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="product in productStore.products" :key="product.id" class="bg-white p-6 rounded-xl shadow-sm border border-slate-200 flex flex-col">
          <div class="flex-grow">
            <div class="flex justify-between items-start">
              <h3 class="font-bold text-lg text-slate-900">{{ product.name }}</h3>
              <span :class="product.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'" class="text-xs font-bold uppercase px-2 py-1 rounded-full">{{ product.status }}</span>
            </div>
            <p class="text-sm text-slate-500 mt-2">SKU: {{ product.sku }}</p>
            <p class="text-2xl font-extrabold text-slate-900 mt-4">${{ product.price.toFixed(2) }}</p>
          </div>
          <div class="border-t border-slate-200 mt-4 pt-4 flex justify-between items-center">
            <router-link :to="`/product/${product.id}`" class="text-sm font-bold text-indigo-600 hover:underline">Ver Detalles</router-link>
            <div class="flex space-x-2">
              <router-link :to="`/product/edit/${product.id}`" class="text-slate-400 hover:text-indigo-600 p-2 rounded-full transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.5L14.732 3.732z"></path></svg>
              </router-link>
              <button @click.prevent="openDeleteModal(product)" class="text-slate-400 hover:text-red-600 p-2 rounded-full transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Paginación -->
      <div v-if="productStore.totalPages > 1" class="flex justify-center items-center mt-8 space-x-2">
        <button @click="changePage(productStore.currentPage - 1)" :disabled="productStore.currentPage === 0" class="px-4 py-2 border border-slate-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed">Anterior</button>
        <span class="text-sm text-slate-600">Página {{ productStore.currentPage + 1 }} de {{ productStore.totalPages }}</span>
        <button @click="changePage(productStore.currentPage + 1)" :disabled="productStore.currentPage >= productStore.totalPages - 1" class="px-4 py-2 border border-slate-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed">Siguiente</button>
      </div>
    </main>

    <ConfirmModal 
      :is-open="isModalOpen"
      title="Confirmar Eliminación"
      :message="`¿Estás seguro de que quieres eliminar el producto '${productToDelete?.name}'? Esta acción no se puede deshacer.`"
      @close="closeModal"
      @confirm="handleDelete"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useProductStore } from '@/stores/product';
import { useAuthStore } from '@/stores/auth';
import { useToastStore } from '@/stores/toast';
import ConfirmModal from '@/components/ConfirmModal.vue';

const productStore = useProductStore();
const authStore = useAuthStore();
const toast = useToastStore();

const isModalOpen = ref(false);
const productToDelete = ref(null);

const search = ref('');
const status = ref(null);
const sortBy = ref('createdAt,desc');

onMounted(() => {
  productStore.fetchProducts(0, status.value, search.value, sortBy.value);
});

watch([search, status, sortBy], () => {
  productStore.fetchProducts(0, status.value, search.value, sortBy.value, true);
});

const changePage = (page) => {
  productStore.fetchProducts(page, status.value, search.value, sortBy.value);
};

const openDeleteModal = (product) => {
  productToDelete.value = product;
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
  productToDelete.value = null;
};

const handleDelete = async () => {
  if (!productToDelete.value) return;
  try {
    await productStore.deleteProduct(productToDelete.value.id);
    toast.success(`Producto '${productToDelete.value.name}' eliminado con éxito.`);
    closeModal();
    // Forzar recarga de la lista
    productStore.fetchProducts(productStore.currentPage, status.value, search.value, sortBy.value, true);
  } catch (error) {
    toast.error(error.message);
  }
};
</script>
