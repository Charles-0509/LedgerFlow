<template>
  <div>
    <h1 class="page-title">个人设置</h1>
    <div class="two-column">
      <div class="panel">
        <h3 style="margin-top: 0">财务信息</h3>
        <el-form :model="profileForm" label-width="110px">
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" />
          </el-form-item>
          <el-form-item label="月度预算">
            <el-input-number v-model="profileForm.defaultMonthlyBudget" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="货币">
            <el-select v-model="profileForm.currency" style="width: 100%">
              <el-option label="人民币 CNY" value="CNY" />
              <el-option label="美元 USD" value="USD" />
              <el-option label="欧元 EUR" value="EUR" />
            </el-select>
          </el-form-item>
          <el-form-item label="默认账户">
            <el-select v-model="profileForm.defaultAccountId" clearable style="width: 100%">
              <el-option v-for="item in accounts" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Check" @click="saveProfile">保存设置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="panel">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
          <h3 style="margin: 0">账户</h3>
          <el-button type="primary" :icon="Plus" @click="openAccount">新增账户</el-button>
        </div>
        <el-table :data="accounts" stripe>
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="type" label="类型" width="110" />
          <el-table-column label="余额" width="120" align="right">
            <template #default="{ row }">¥ {{ money(row.balance) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" :icon="Edit" @click="editAccount(row)" />
              <el-button size="small" type="danger" :icon="Delete" @click="removeAccount(row)" />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="accountDialog" :title="accountForm.id ? '修改账户' : '新增账户'" width="460px">
      <el-form :model="accountForm" label-width="86px">
        <el-form-item label="名称">
          <el-input v-model="accountForm.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="accountForm.type" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="银行卡" value="BANK_CARD" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="余额">
          <el-input-number v-model="accountForm.balance" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认账户">
          <el-switch v-model="accountForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAccount">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Check, Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { accountApi, userApi } from '../api'
import { money } from '../utils/format'

const accounts = ref([])
const accountDialog = ref(false)
const profileForm = reactive({ nickname: '', defaultMonthlyBudget: 0, currency: 'CNY', defaultAccountId: null })
const accountForm = reactive(emptyAccount())

function emptyAccount() {
  return { id: null, name: '', type: 'CASH', balance: 0, isDefault: false }
}

async function loadData() {
  const profile = await userApi.profile()
  accounts.value = profile.accounts || []
  Object.assign(profileForm, {
    nickname: profile.user?.nickname || '',
    defaultMonthlyBudget: Number(profile.profile?.defaultMonthlyBudget || 0),
    currency: profile.profile?.currency || 'CNY',
    defaultAccountId: profile.profile?.defaultAccountId || null
  })
}

async function saveProfile() {
  await userApi.updateProfile(profileForm)
  ElMessage.success('设置已保存')
  await loadData()
}

function openAccount() {
  Object.assign(accountForm, emptyAccount())
  accountDialog.value = true
}

function editAccount(row) {
  Object.assign(accountForm, {
    id: row.id,
    name: row.name,
    type: row.type,
    balance: Number(row.balance),
    isDefault: row.isDefault === 1
  })
  accountDialog.value = true
}

async function saveAccount() {
  if (accountForm.id) {
    await accountApi.update(accountForm.id, accountForm)
  } else {
    await accountApi.create(accountForm)
  }
  ElMessage.success('账户已保存')
  accountDialog.value = false
  await loadData()
}

async function removeAccount(row) {
  await ElMessageBox.confirm(`确认删除账户「${row.name}」？`, '删除确认')
  await accountApi.remove(row.id)
  ElMessage.success('账户已删除')
  await loadData()
}

onMounted(loadData)
</script>
