import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [
      vue(),
      ...(mode === 'development' ? [vueDevTools()] : []),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        'vue': 'vue/dist/vue.esm-bundler.js'
      },
    },
    define: {
      global: 'window'
    },
    
    // 개발 서버 설정 - proxy로 /api 요청을 백엔드로 전달
    server: {
      proxy: {
        '/api': {
          target: env.VITE_APP_API_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: true,
          // /api 경로를 그대로 유지 (rewrite 하지 않음)
        }
      }
    },

    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: false,
      
      rollupOptions: {
        output: {
          manualChunks: {
            'vue-vendor': ['vue', 'vue-router', 'pinia'],
            'ui-vendor': ['vuetify'],
            'utils': ['axios', '@stomp/stompjs', 'sockjs-client']
          }
        }
      }
    }
  }
})