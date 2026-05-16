<template>
  <div class="login-page">
    <div class="login-box">
      <h1 class="login-title">Ledger Flow</h1>
      <p class="login-subtitle">把日常收支整理成清晰、可分析的个人财务流。</p>
      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top" @keyup.enter="submitLogin">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="demo" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" placeholder="123456" type="password" show-password />
            </el-form-item>
            <el-button type="primary" style="width: 100%" :loading="loading" @click="submitLogin">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-position="top" @keyup.enter="submitRegister">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.nickname" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" show-password />
            </el-form-item>
            <el-button type="primary" style="width: 100%" :loading="loading" @click="submitRegister">注册并登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

const router = useRouter()
const mode = ref('login')
const loading = ref(false)
const loginForm = reactive({ username: 'demo', password: '123456' })
const registerForm = reactive({ username: '', nickname: '', password: '' })

async function saveSession(response) {
  localStorage.setItem('token', response.token)
  localStorage.setItem('user', JSON.stringify(response.user))
  router.push('/dashboard')
}

async function submitLogin() {
  loading.value = true
  try {
    await saveSession(await authApi.login(loginForm))
    ElMessage.success('登录成功')
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  loading.value = true
  try {
    await saveSession(await authApi.register(registerForm))
    ElMessage.success('注册成功')
  } finally {
    loading.value = false
  }
}
</script>
