<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Tabbar, TabbarItem } from 'vant'

const route = useRoute()

const active = computed(() => {
  const path = route.path
  if (path.startsWith('/teacher')) return 0
  if (path.startsWith('/student')) return 1
  return 0
})

const tabbarShow = computed(() => {
  const path = route.path
  return path !== '/login' && path !== '/'
})
</script>

<template>
  <div class="app-container">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
    
    <Tabbar v-if="tabbarShow" v-model:active="active" route placeholder>
      <TabbarItem to="/teacher/home" icon="home-o" text="首页" />
      <TabbarItem to="/teacher/scan" icon="scan" text="扫码" />
      <TabbarItem to="/teacher/wrong" icon="warning-o" text="错题" />
      <TabbarItem to="/teacher/profile" icon="user-o" text="我的" />
    </Tabbar>
  </div>
</template>

<style lang="scss">
.app-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 50px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
