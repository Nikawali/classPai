<template>
  <div id="app-root">
    <Register />
  </div>
</template>

<script setup>
import Register from './views/Register.vue'
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
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}
</style>
