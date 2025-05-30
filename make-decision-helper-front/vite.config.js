import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [
      vue(),
      // 개발환경에서만 devtools 활성화
      ...(mode === 'development' ? [vueDevTools()] : []),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    define: {
      global: 'window'
    },
    
    // 개발 서버 설정
    server: {
      proxy: {
        '/api': {
          target: env.VITE_APP_API_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: true
        }
      }
    },

    // 빌드 설정
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