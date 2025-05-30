
import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig(({ mode }) => {
  // 환경 변수 로드
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [
      vue(),
      vueDevTools(),
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
          secure: true,
          rewrite: (path) => path.replace(/^\/api/, '/api')
        },
        '/ws': {
          target: (env.VITE_WS_URL || 'ws://localhost:8080').replace('wss://', 'https://').replace('ws://', 'http://'),
          ws: true,
          changeOrigin: true,
          secure: true
        }
      }
    },
    // 빌드 설정
    build: {
      outDir: 'dist',
      rollupOptions: {
        output: {
          manualChunks: {
            vendor: ['vue', 'vue-router', 'pinia'],
            axios: ['axios']
          }
        }
      }
    }
  }
})