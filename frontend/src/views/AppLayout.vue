<template>
  <el-container class="app-shell">
    <el-aside width="248px" class="app-aside">
      <div class="brand">
        <div class="brand-mark">
          <el-icon><Wallet /></el-icon>
        </div>
        <div>
          <span>LedgerFlow</span>
          <span class="brand-sub">日常记账理财</span>
        </div>
      </div>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><Tickets /></el-icon>
          <span>账单</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><CollectionTag /></el-icon>
          <span>分类</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><TrendCharts /></el-icon>
          <span>统计</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <div>
          <div class="header-title">{{ routeName }}</div>
          <div class="muted">清楚记录每一笔流动，让预算更有方向。</div>
        </div>
        <div class="header-actions">
          <span class="muted">{{ user?.nickname || user?.username }}</span>
          <el-button :icon="SwitchButton" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CollectionTag, DataBoard, Setting, SwitchButton, Tickets, TrendCharts, Wallet } from '@element-plus/icons-vue'
import { authApi } from '../api'

const route = useRoute()
const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || 'null')

const routeName = computed(() => {
  const names = {
    '/dashboard': '首页仪表盘',
    '/records': '记账记录',
    '/categories': '收支分类',
    '/statistics': '月度分析',
    '/settings': '个人设置'
  }
  return names[route.path] || 'LedgerFlow'
})

async function logout() {
  try {
    await authApi.logout()
  } finally {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }
}
</script>
