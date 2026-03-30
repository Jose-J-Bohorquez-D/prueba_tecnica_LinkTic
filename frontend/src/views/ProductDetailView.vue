<template>
  <div class="py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
      <router-link to="/" class="text-sm font-bold text-indigo-600 hover:text-indigo-500 flex items-center transition-colors">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
        Volver al Catálogo
      </router-link>
    </div>

    <main class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Skeleton Loader -->
      <div v-if="productLoading">
        <div class="grid md:grid-cols-2 gap-8">
          <div class="bg-white p-8 rounded-xl shadow-sm border border-slate-200 animate-pulse">
            <div class="h-8 bg-slate-200 rounded w-3/4 mb-4"></div>
            <div class="h-4 bg-slate-200 rounded w-1/2 mb-6"></div>
            <div class="h-12 bg-slate-200 rounded w-1/3"></div>
          </div>
          <div class="bg-white p-8 rounded-xl shadow-sm border border-slate-200 animate-pulse">
            <div class="h-6 bg-slate-200 rounded w-1/4 mb-6"></div>
            <div class="h-16 bg-slate-200 rounded w-full"></div>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-else-if="productError">
         <div class="bg-red-50 border border-red-200 text-red-600 p-8 rounded-xl text-center">
          <p class="font-bold text-lg mb-2">Oh no! No pudimos cargar el producto.</p>
          <p class="mb-4">{{ productError }}</p>
          <button @click="fetchProduct" class="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-all font-semibold shadow-md shadow-red-100">
            Intentar de Nuevo
          </button>
        </div>
      </div>

      <!-- Content -->
      <div v-else-if="product" class="grid md:grid-cols-2 gap-8">
        <!-- Product Info -->
        <div class="bg-white p-8 rounded-xl shadow-sm border border-slate-200">
          <div class="flex justify-between items-start mb-4">
            <h1 class="text-4xl font-extrabold text-slate-900 tracking-tight">{{ product.name }}</h1>
            <span :class="product.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'" class="text-xs font-bold uppercase px-3 py-1 rounded-full">{{ product.status }}</span>
          </div>
          <p class="text-slate-500">SKU: {{ product.sku }}</p>
          <p class="text-5xl font-black text-slate-900 mt-6">${{ product.price.toFixed(2) }}</p>
        </div>

        <!-- Inventory & Purchase -->
        <div class="bg-white p-8 rounded-xl shadow-sm border border-slate-200">
          <h3 class="text-xl font-bold text-slate-900 mb-6">Inventario y Compra</h3>
          
          <div v-if="inventoryLoading" class="flex flex-col items-center py-4 space-y-3">
            <div class="animate-spin text-3xl text-indigo-600">🌀</div>
            <p class="text-xs text-slate-400 font-bold uppercase">Consultando Stock...</p>
          </div>

          <div v-else-if="inventoryError" class="bg-amber-50 border border-amber-100 p-4 rounded-xl text-amber-700 text-sm">
            <p class="font-bold mb-1">Servicio no disponible</p>
            <p class="opacity-80 mb-3">{{ inventoryError }}</p>
            <button @click="fetchInventory" class="text-indigo-600 font-bold underline">Reintentar</button>
          </div>

          <div v-else-if="inventory" class="space-y-6">
            <div class="flex items-center justify-between">
              <p class="text-slate-500 font-bold uppercase text-xs">Disponible</p>
              <span class="text-3xl font-black" :class="inventory.available > 0 ? 'text-slate-900' : 'text-red-600'">{{ inventory.available }}</span>
            </div>

            <form v-if="inventory.available > 0" @submit.prevent="handlePurchase" class="space-y-4">
              <div>
                <label for="quantity" class="text-xs font-bold text-slate-400 uppercase">Cantidad a comprar</label>
                <input type="number" v-model.number="quantity" id="quantity" min="1" :max="inventory.available" class="mt-2 block w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500" required>
              </div>
              <button type="submit" :disabled="purchaseLoading" class="w-full py-3 px-4 bg-indigo-600 text-white font-bold rounded-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-indigo-400 flex items-center justify-center">
                <span v-if="!purchaseLoading">Comprar Ahora</span>
                <span v-else>Procesando...</span>
              </button>
            </form>

            <div v-if="purchaseError" class="text-sm text-red-600 p-3 bg-red-50 rounded-lg">{{ purchaseError }}</div>
            <div v-if="purchaseSuccess" class="text-sm text-green-600 p-3 bg-green-50 rounded-lg">¡Compra realizada con éxito! El inventario ha sido actualizado.</div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { api, inventoryApi } from '@/api/api';
import { useAuthStore } from '@/stores/auth';
import { useToastStore } from '@/stores/toast';

const route = useRoute();
const authStore = useAuthStore();
const toast = useToastStore();

const product = ref(null);
const productLoading = ref(true);
const productError = ref(null);

const inventory = ref(null);
const inventoryLoading = ref(true);
const inventoryError = ref(null);

const quantity = ref(1);
const purchaseLoading = ref(false);
const purchaseError = ref(null);
const purchaseSuccess = ref(false);

const fetchProduct = async () => {
  productLoading.value = true;
  productError.value = null;
  try {
    const res = await api.get(`/products/${route.params.id}`);
    product.value = res.data.data; // Ajustado para el wrapper JSON:API
    fetchInventory();
  } catch (e) {
    productError.value = e.response?.data?.errors?.[0]?.detail || 'No se pudo cargar el producto.';
  } finally {
    productLoading.value = false;
  }
};

const fetchInventory = async () => {
  inventoryLoading.value = true;
  inventoryError.value = null;
  try {
    const res = await inventoryApi.get(`/inventory/${route.params.id}`);
    inventory.value = res.data.data; // Ajustado para el wrapper JSON:API
  } catch (e) {
    inventoryError.value = e.response?.data?.errors?.[0]?.detail || 'El servicio de inventario no está disponible.';
  } finally {
    inventoryLoading.value = false;
  }
};

const handlePurchase = async () => {
  purchaseLoading.value = true;
  purchaseError.value = null;
  purchaseSuccess.value = false;

  try {
    await inventoryApi.post('/inventory/purchase', {
      productId: route.params.id,
      quantity: quantity.value,
    }, {
      headers: { 'Idempotency-Key': `purchase-${route.params.id}-${Date.now()}` }
    });
    purchaseSuccess.value = true;
    toast.success('¡Compra realizada con éxito!');
    // Refrescar inventario para mostrar el nuevo stock
    await fetchInventory();
  } catch (e) {
    const errorMsg = e.response?.data?.errors?.[0]?.detail || 'La compra no pudo ser procesada.';
    purchaseError.value = errorMsg;
    toast.error(errorMsg);
  } finally {
    purchaseLoading.value = false;
  }
};

onMounted(() => {
  fetchProduct();
});
</script>
