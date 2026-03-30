import { test, expect } from '@playwright/test';

test('listar → detalle → compra exitosa', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.fill('input[type=\"text\"]', 'admin');
  await page.fill('input[type=\"password\"]', 'admin');
  await page.click('button[type=\"submit\"]');
  await expect(page).toHaveURL('http://localhost:3000/');
  await expect(page.locator('h1')).toContainText('Catálogo de Productos');

  // Ir al primer producto de la lista
  const firstProductLink = page.locator('a:text(\"Ver Detalles\")').first();
  await firstProductLink.click();

  // En detalle, intentar comprar 1 unidad
  await expect(page.locator('h1')).toBeVisible();
  const qtyInput = page.locator('input#quantity');
  if (await qtyInput.count()) {
    await qtyInput.fill('1');
    await page.click('button:has-text(\"Comprar Ahora\")');
    // Feedback
    await expect(page.locator('div:text(\"¡Compra realizada con éxito!\")')).toBeVisible();
  }
});

test('error de inventario no disponible', async ({ page }) => {
  // Asume sesión activa de prueba anterior
  await page.goto('http://localhost:3000/');
  const firstProductLink = page.locator('a:text(\"Ver Detalles\")').first();
  await firstProductLink.click();

  // Simular error: cantidad grande esperando mensaje de error
  const qtyInput = page.locator('input#quantity');
  if (await qtyInput.count()) {
    await qtyInput.fill('9999');
    await page.click('button:has-text(\"Comprar Ahora\")');
    await expect(page.locator('div:text(\"La compra no pudo ser procesada.\")')).toBeVisible();
  }
});
