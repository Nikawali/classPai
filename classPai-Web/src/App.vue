<template>
  <div id="app-root">
    <Register v-if="page === 'register'" @goLogin="page = 'login'" />
    <Login v-else-if="page === 'login'" @goRegister="page = 'register'" @loginSuccess="page = 'main'" />
    <Main v-else-if="page === 'main'" @logout="handleLogout" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import Main from './views/Main.vue'

const page = ref(sessionStorage.getItem('token') ? 'main' : 'login')

function handleLogout() {
  page.value = 'login'
}
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
