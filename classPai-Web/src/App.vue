<template>
  <div id="app-root">
    <Register v-if="currentPage === 'register'" @goLogin="go('login')" />
    <Login v-else-if="currentPage === 'login'" @goRegister="go('register')" @loginSuccess="go('main')" />
    <Main v-else-if="currentPage === 'main'" @logout="handleLogout" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import Main from './views/Main.vue'

// 解析 URL hash → 页面名
// #/login → login, #/register → register, #/ → main, #/course/... → main
function hashToPage(hash) {
  if (hash === '#/login') return 'login'
  if (hash === '#/register') return 'register'
  if (hash === '#/' || /^#\/course\//.test(hash)) return 'main'
  return null
}

const currentPage = ref('login')

function syncFromHash() {
  const page = hashToPage(location.hash)
  const hasToken = !!sessionStorage.getItem('token')

  // 需要登录的页面但无 token → 跳到登录
  if (page === 'main' && !hasToken) {
    go('login', true)
    return
  }
  // 登录/注册页已有 token → 跳到主页
  if ((page === 'login' || page === 'register') && hasToken) {
    go('main', true)
    return
  }
  if (page) {
    currentPage.value = page
    return
  }
  // 未知 hash 或无 hash → 根据 token 决定
  go(hasToken ? 'main' : 'login', true)
}

// 导航到指定页面（修改 URL hash）
function go(page, replace) {
  const map = { login: '#/login', register: '#/register', main: '#/' }
  const hash = map[page]
  if (!hash) return
  if (replace) {
    location.replace(hash)
  } else {
    location.hash = hash
  }
}

function handleLogout() {
  go('login')
}

onMounted(() => {
  syncFromHash()
  window.addEventListener('hashchange', syncFromHash)
})

onUnmounted(() => {
  window.removeEventListener('hashchange', syncFromHash)
})
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
