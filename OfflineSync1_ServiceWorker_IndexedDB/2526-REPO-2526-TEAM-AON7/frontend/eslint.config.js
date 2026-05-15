import { defineConfig, globalIgnores } from 'eslint/config'
import globals from 'globals'
import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'

export default defineConfig([
  {
    name: 'app/files-to-lint',
    files: ['**/*.{vue,js,mjs,jsx}'],
  },

  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),

  {
    languageOptions: {
      globals: {
        ...globals.browser,
      },
    },

    rules: {
      "complexity": ["error", 10],
      "max-depth": ["warn", 4],
      "max-lines-per-function": ["warn", 50],
      "max-params": ["warn", 4],

      "camelcase": "error",
      "no-unused-vars": "error",
      "no-console": "warn",
      "eqeqeq": "error",
      "no-return-assign": "error",
      "no-else-return": "warn",
      "consistent-return": "error"
    }
  },

  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  
])
