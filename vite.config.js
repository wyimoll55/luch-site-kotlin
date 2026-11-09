import { resolve } from 'path';
import { defineConfig } from 'vite';

// base нужен для GitHub Pages: сайт живёт по адресу wyimoll55.github.io/luch-site/
// Локально (npm run dev) всё работает как раньше, на localhost:5173
export default defineConfig(({ command }) => ({
  base: command === 'build' ? '/luch-site/' : '/',
  build: {
    cssMinify: false,   // не сжимаем CSS, чтобы сборка не выкидывала свойства вроде backdrop-filter
    rollupOptions: {
      input: {
        main: resolve(import.meta.dirname, 'index.html'),
        catalog: resolve(import.meta.dirname, 'catalog.html'),
        collections: resolve(import.meta.dirname, 'collections.html'),
        cart: resolve(import.meta.dirname, 'cart.html'),
        product: resolve(import.meta.dirname, 'product.html'),
        about: resolve(import.meta.dirname, 'about.html'),
        contacts: resolve(import.meta.dirname, 'contacts.html'),
      },
    },
  },
}));
