import { defineStore } from 'pinia';
import { api } from '@/api/api';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [],
    meta: {},
    isLoading: false,
    error: null,
    // Para la caché
    lastFetched: null,
    cacheExpiration: 5 * 60 * 1000, // 5 minutos
  }),
  getters: {
    totalPages: (state) => state.meta.totalPages || 0,
    currentPage: (state) => state.meta.page || 0,
  },
  actions: {
    async fetchProducts(page = 0, status = null, search = null, sortBy = 'createdAt,desc', force = false) {
      const now = Date.now();
      if (!force && this.products.length > 0 && this.lastFetched && (now - this.lastFetched < this.cacheExpiration)) {
        console.log("Usando caché de productos.");
        return;
      }

      this.isLoading = true;
      this.error = null;

      try {
        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', 6); // 6 productos por página para un grid de 3x2
        if (status) params.append('status', status);
        if (search) params.append('search', search);
        if (sortBy) params.append('sortBy', sortBy);

        const response = await api.get(`/products?${params.toString()}`);
        
        this.products = response.data.data;
        this.meta = response.data.meta;
        this.lastFetched = now; // Actualizar timestamp de caché

      } catch (e) {
        this.error = e.response?.data?.errors?.[0]?.detail || e.message || 'Error al cargar los productos.';
        this.products = []; // Limpiar en caso de error
      } finally {
        this.isLoading = false;
      }
    },

    async createProduct(productData) {
      // No usamos isLoading aquí para no mostrar el esqueleto de toda la página
      try {
        const payload = {
          sku: productData.sku,
          name: productData.name,
          price: productData.price,
          status: productData.status,
        };
        await api.post('/products', payload);
        this.lastFetched = null; // Invalidar caché para forzar recarga
      } catch (e) {
        console.error("Error creating product:", e);
        throw new Error(e.response?.data?.errors?.[0]?.detail || 'No se pudo crear el producto.');
      }
    },

    async updateProduct(productId, productData) {
      try {
        const payload = {
          sku: productData.sku,
          name: productData.name,
          price: productData.price,
          status: productData.status,
        };
        await api.put(`/products/${productId}`, payload);
        this.lastFetched = null; // Invalidar caché
      } catch (e) {
        console.error("Error updating product:", e);
        throw new Error(e.response?.data?.errors?.[0]?.detail || 'No se pudo actualizar el producto.');
      }
    },

    async deleteProduct(productId) {
      try {
        await api.delete(`/products/${productId}`);
        this.lastFetched = null; // Invalidar caché
      } catch (e) {
        console.error("Error deleting product:", e);
        throw new Error(e.response?.data?.errors?.[0]?.detail || 'No se pudo eliminar el producto.');
      }
    },
  },
});
