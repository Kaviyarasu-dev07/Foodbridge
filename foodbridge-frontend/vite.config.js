import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

// https://vite.dev/config/
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
      }
    }
  },
  plugins: [
    react(),
    VitePWA({
      strategies: 'injectManifest',
      srcDir: 'src',
      filename: 'sw.js',
      registerType: 'autoUpdate',
      injectRegister: null,
      manifest: {
        name: "FoodBridge",
        short_name: "FoodBridge",
        description: "Real-time food rescue platform",
        theme_color: "#085041",
        background_color: "#ffffff",
        display: "standalone",
        start_url: "/",
        scope: "/",
        icons: [
          {
            src: "icons/icon-192.png",
            sizes: "192x192",
            type: "image/png"
          },
          {
            src: "icons/icon-512.png",
            sizes: "512x512",
            type: "image/png"
          }
        ],
        shortcuts: [
          {
            name: "Post food",
            url: "/donor/dashboard",
            icons: [{ src: "icons/icon-192.png", sizes: "192x192" }]
          },
          {
            name: "View alerts",
            url: "/ngo/dashboard",
            icons: [{ src: "icons/icon-192.png", sizes: "192x192" }]
          }
        ]
      },
      devOptions: {
        enabled: false,
        type: 'module',
      }
    })
  ]
});
