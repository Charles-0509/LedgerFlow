<template>
  <div>
    <h1 class="page-title">收支分类</h1>
    <div class="toolbar">
      <el-radio-group v-model="activeType" @change="loadCategories">
        <el-radio-button label="EXPENSE">支出分类</el-radio-button>
        <el-radio-button label="INCOME">收入分类</el-radio-button>
      </el-radio-group>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
    </div>

    <div class="panel">
      <el-table :data="categories" stripe>
        <el-table-column label="颜色" width="80">
          <template #default="{ row }">
            <span :style="{ display: 'inline-block', width: '18px', height: '18px', borderRadius: '4px', background: row.color || '#409eff' }" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSystem ? 'info' : 'success'">{{ row.isSystem ? '默认' : '自定义' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" :icon="Tickets" @click="showRecords(row)">账单</el-button>
            <el-button size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="remove(row)">禁用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '修改分类' : '新增分类'" width="460px">
      <el-form :model="form" label-width="86px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button label="EXPENSE">支出</el-radio-button>
            <el-radio-button label="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，可为空" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="`${selected?.name || ''} 账单`" size="620px">
      <el-table :data="categoryRecords" stripe>
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="accountName" label="账户" width="120" />
        <el-table-column prop="note" label="备注" />
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'INCOME' ? 'income' : 'expense'">¥ {{ money(row.amount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Tickets } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categoryApi } from '../api'
import { money, typeText } from '../utils/format'

const activeType = ref('EXPENSE')
const categories = ref([])
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const categoryRecords = ref([])
const selected = ref(null)
const form = reactive(emptyForm())

function emptyForm() {
  return { id: null, name: '', type: activeType.value, icon: '', color: '#409eff' }
}

async function loadCategories() {
  categories.value = await categoryApi.list({ type: activeType.value })
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, name: row.name, type: row.type, icon: row.icon, color: row.color || '#409eff' })
  dialogVisible.value = true
}

async function save() {
  if (form.id) {
    await categoryApi.update(form.id, form)
  } else {
    await categoryApi.create(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadCategories()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认禁用分类「${row.name}」？历史账单仍会保留。`, '禁用确认')
  await categoryApi.remove(row.id)
  ElMessage.success('已禁用')
  await loadCategories()
}

async function showRecords(row) {
  selected.value = row
  categoryRecords.value = await categoryApi.records(row.id, { page: 1, size: 50 })
  drawerVisible.value = true
}

onMounted(loadCategories)
</script>
