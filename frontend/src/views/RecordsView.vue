<template>
  <div>
    <h1 class="page-title">记账记录</h1>
    <div class="toolbar">
      <el-input v-model="filters.keyword" clearable placeholder="关键词" style="width: 200px" />
      <el-select v-model="filters.type" clearable placeholder="类型" style="width: 130px">
        <el-option label="收入" value="INCOME" />
        <el-option label="支出" value="EXPENSE" />
      </el-select>
      <el-select v-model="filters.categoryId" clearable placeholder="分类" style="width: 150px">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-button type="primary" :icon="Search" @click="loadRecords">查询</el-button>
      <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
      <el-button type="success" :icon="Plus" @click="openCreate">新增</el-button>
    </div>

    <div class="panel">
      <el-table :data="page.records" stripe>
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'warning'">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="accountName" label="账户" width="120" />
        <el-table-column prop="note" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-cell" :class="row.type === 'INCOME' ? 'income' : 'expense'">¥ {{ money(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="View" @click="showDetail(row)">详情</el-button>
            <el-button size="small" :icon="Edit" @click="openEdit(row)" />
            <el-button size="small" type="danger" :icon="Delete" @click="remove(row)" />
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 16px"
        layout="prev, pager, next, total"
        :total="page.total"
        :page-size="page.size"
        v-model:current-page="page.page"
        @current-change="loadRecords"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '修改账单' : '新增账单'" width="520px">
      <el-form :model="form" label-width="86px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button label="INCOME">收入</el-radio-button>
            <el-radio-button label="EXPENSE">支出</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="item in formCategories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户">
          <el-select v-model="form.accountId" style="width: 100%">
            <el-option v-for="item in accounts" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.recordDate" value-format="YYYY-MM-DD" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="账单详情" size="360px">
      <el-descriptions :column="1" border v-if="detail">
        <el-descriptions-item label="日期">{{ detail.recordDate }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeText(detail.type) }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥ {{ money(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detail.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="账户">{{ detail.accountName }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.note || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { accountApi, categoryApi, recordApi } from '../api'
import { money, today, typeText } from '../utils/format'

const filters = reactive({ keyword: '', type: '', categoryId: null })
const dateRange = ref([])
const categories = ref([])
const accounts = ref([])
const page = reactive({ total: 0, page: 1, size: 10, records: [] })
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const form = reactive(emptyForm())

const formCategories = computed(() => categories.value.filter((item) => item.type === form.type))

function emptyForm() {
  return { id: null, type: 'EXPENSE', amount: 0.01, categoryId: null, accountId: null, recordDate: today(), note: '' }
}

async function loadOptions() {
  categories.value = await categoryApi.list()
  accounts.value = await accountApi.list()
}

async function loadRecords() {
  const [startDate, endDate] = dateRange.value || []
  const result = await recordApi.list({ ...filters, startDate, endDate, page: page.page, size: page.size })
  Object.assign(page, result)
}

function resetFilters() {
  Object.assign(filters, { keyword: '', type: '', categoryId: null })
  dateRange.value = []
  page.page = 1
  loadRecords()
}

function openCreate() {
  Object.assign(form, emptyForm())
  form.accountId = accounts.value[0]?.id || null
  form.categoryId = formCategories.value[0]?.id || null
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    type: row.type,
    amount: Number(row.amount),
    categoryId: row.categoryId,
    accountId: row.accountId,
    recordDate: row.recordDate,
    note: row.note
  })
  dialogVisible.value = true
}

async function save() {
  if (form.id) {
    await recordApi.update(form.id, form)
  } else {
    await recordApi.create(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadRecords()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除 ${row.categoryName} 账单？`, '删除确认')
  await recordApi.remove(row.id)
  ElMessage.success('删除成功')
  await loadRecords()
}

async function showDetail(row) {
  detail.value = await recordApi.detail(row.id)
  detailVisible.value = true
}

onMounted(async () => {
  await loadOptions()
  await loadRecords()
})

watch(
  () => form.type,
  () => {
    if (!formCategories.value.some((item) => item.id === form.categoryId)) {
      form.categoryId = formCategories.value[0]?.id || null
    }
  }
)
</script>
