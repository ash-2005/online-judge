import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  // needed for https://ash-2005.github.io/online-judge/
  base: '/online-judge/',
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
})
