import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

const proxyTarget = process.env.VITE_PROXY_TARGET || 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: proxyTarget,
        changeOrigin: true,
      },
      '/batch': {
        target: proxyTarget,
        changeOrigin: true,
      },
      '/swagger-ui': {
        target: proxyTarget,
        changeOrigin: true,
      },
      '/v3/api-docs': {
        target: proxyTarget,
        changeOrigin: true,
      },
    },
  },
})
