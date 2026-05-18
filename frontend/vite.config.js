import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    sourcemap: false,
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/echarts')) return 'charts'
          if (id.includes('node_modules/element-plus') || id.includes('node_modules/@element-plus')) return 'element'
          if (id.includes('node_modules/vue') || id.includes('node_modules/vue-router')) return 'vue'
          return undefined
        }
      }
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
