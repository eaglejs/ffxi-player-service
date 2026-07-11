import { fileURLToPath, URL } from 'node:url'
import { viteStaticCopy } from 'vite-plugin-static-copy'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { readFileSync } from 'node:fs'

const { version } = JSON.parse(readFileSync(new URL('./package.json', import.meta.url), 'utf-8'))

const backendPort = process.env.VITE_PORT || '8080'

// https://vitejs.dev/config/
export default defineConfig({
  define: {
    __APP_VERSION__: JSON.stringify(version)
  },
  plugins: [
    vue(),
    viteStaticCopy({
      targets: [
        {
          src: 'src/assets/icons/*',
          dest: 'assets'
        },
        {
          src: 'src/assets/images/*',
          dest: 'assets'
        }
      ]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: true, // listen on 0.0.0.0 so Docker containers can reach via host.docker.internal
    allowedHosts: ['host.docker.internal'],
    proxy: {
      '/api': {
        target: `http://localhost:${backendPort}`,
        changeOrigin: true
      },
      '/ws': {
        target: `ws://localhost:${backendPort}`,
        ws: true
      }
    }
  }
})
