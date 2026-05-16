<template>
  <div>
    <div class="page-hero">
      <div>
        <p class="page-kicker">今日概览</p>
        <h1 class="page-title">让每一笔钱都有去向。</h1>
        <p class="page-desc">快速查看当日收支、本月结余和预算余量，近期账单与分类占比都在同一个视图里。</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="loadHome">刷新数据</el-button>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索备注、分类或账户"
        style="width: 320px"
        :prefix-icon="Search"
        @keyup.enter="loadSearch"
      />
      <el-button type="primary" :icon="Search" @click="loadSearch">搜索</el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-label">今日收入</div>
        <div class="stat-value income">¥ {{ money(home.today?.income) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日支出</div>
        <div class="stat-value expense">¥ {{ money(home.today?.expense) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">本月结余</div>
        <div class="stat-value">¥ {{ money(home.month?.balance) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">预算剩余</div>
        <div class="stat-value">¥ {{ money(home.budgetRemaining) }}</div>
      </div>
    </div>

    <div class="two-column">
      <div class="panel">
        <h2 class="panel-title">近期账单</h2>
        <el-table :data="displayRecords" stripe>
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="accountName" label="账户" width="120" />
          <el-table-column prop="note" label="备注" min-width="160" show-overflow-tooltip />
          <el-table-column label="金额" width="150" align="right">
            <template #default="{ row }">
              <span class="amount-cell" :class="row.type === 'INCOME' ? 'income' : 'expense'">
                {{ row.type === 'INCOME' ? '+' : '-' }} ¥ {{ money(row.amount) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="panel">
        <h2 class="panel-title">本月分类</h2>
        <el-empty v-if="!home.categoryOverview?.length" description="暂无数据" />
        <div v-for="item in home.categoryOverview" :key="`${item.type}-${item.categoryName}`" style="margin-bottom: 16px">
          <div style="display: flex; justify-content: space-between; margin-bottom: 8px">
            <span>{{ item.categoryName || '未分类' }}</span>
            <strong :class="item.type === 'INCOME' ? 'income' : 'expense'">¥ {{ money(item.total) }}</strong>
          </div>
          <el-progress :percentage="progressOf(item.total)" :show-text="false" color="#9fe870" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { recordApi, statisticsApi } from '../api'
import { money } from '../utils/format'

const home = ref({})
const keyword = ref('')
const searched = ref(null)

const displayRecords = computed(() => searched.value || home.value.recentRecords || [])

async function loadHome() {
  searched.value = null
  home.value = await statisticsApi.home()
}

async function loadSearch() {
  if (!keyword.value.trim()) {
    searched.value = null
    return
  }
  const result = await recordApi.list({ keyword: keyword.value, page: 1, size: 20 })
  searched.value = result.records
}

function progressOf(value) {
  const totals = home.value.categoryOverview || []
  const max = Math.max(...totals.map((item) => Number(item.total || 0)), 1)
  return Math.round((Number(value || 0) / max) * 100)
}

onMounted(loadHome)
</script>
