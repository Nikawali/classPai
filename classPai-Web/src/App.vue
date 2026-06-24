<template>
  <div id="app-root">
    <Register v-if="page === 'register'" @goLogin="page = 'login'" />
    <Login v-else @goRegister="page = 'register'" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'

const page = ref('login')
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, 'Microsoft YaHei', sans-serif;
  background: #f0f2f5;
  min-height: 100vh;
}
#app-root {
  min-height: 100vh;
}
</style>
