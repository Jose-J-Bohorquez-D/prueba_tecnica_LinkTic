import { createRouter, createWebHistory } from 'vue-router';
import ProductListView from '../views/ProductListView.vue';
import ProductDetailView from '../views/ProductDetailView.vue';
import ProductFormView from '../views/ProductFormView.vue';
import LoginView from '../views/LoginView.vue';
import { useAuthStore } from '@/stores/auth';

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/',
    name: 'products',
    component: ProductListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/product/new',
    name: 'product-new',
    component: ProductFormView,
    meta: { requiresAuth: true },
  },
  {
    path: '/product/edit/:id',
    name: 'product-edit',
    component: ProductFormView,
    meta: { requiresAuth: true },
  },
  {
    path: '/product/:id',
    name: 'product-detail',
    component: ProductDetailView,
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'login' });
  } else {
    next();
  }
});

export default router;
