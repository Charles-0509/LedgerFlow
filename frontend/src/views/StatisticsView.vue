<template>
  <div>
    <div class="page-hero">
      <div>
        <p class="page-kicker">月度分析</p>
        <h1 class="page-title">看见本月财务走势。</h1>
        <p class="page-desc">收入、支出、结余、预算使用率和分类占比集中展示，适合课程答辩演示数据库统计能力。</p>
      </div>
      <el-button type="primary" :icon="TrendCharts" @click="loadData">重新分析</el-button>
    </div>

    <div class="toolbar">
      <el-date-picker v-model="month" type="month" value-format="YYYY-MM" placeholder="选择月份" />
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-label">月收入</div>
        <div class="stat-value income">¥ {{ money(data.summary?.income) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">月支出</div>
        <div class="stat-value expense">¥ {{ money(data.summary?.expense) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">月结余</div>
        <div class="stat-value">¥ {{ money(data.summary?.balance) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">预算使用率</div>
        <div class="stat-value">{{ money(data.budgetUsageRate) }}%</div>
      </div>
    </div>

    <el-alert v-if="data.overBudget" type="warning" show-icon title="本月支出已超过预算" style="margin-bottom: 16px" />

    <div class="two-column">
      <div class="panel">
        <h2 class="panel-title">每日收支趋势</h2>
        <div ref="trendChart" class="chart"></div>
      </div>
      <div class="panel">
        <h2 class="panel-title">支出分类占比</h2>
        <div ref="categoryChart" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onMounted, ref } from 'vue'
import { TrendCharts } from '@element-plus/icons-vue'
import { statisticsApi } from '../api'
import { currentMonth, money } from '../utils/format'

const month = ref(currentMonth())
const data = ref({})
const trendChart = ref(null)
const categoryChart = ref(null)
let trendInstance
let categoryInstance

async function loadData() {
  data.value = await statisticsApi.monthly({ month: month.value })
  await nextTick()
  renderTrend()
  renderCategory()
}

function renderTrend() {
  trendInstance?.dispose()
  trendInstance = echarts.init(trendChart.value)
  const rows = data.value.trendStats || []
  trendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'] },
    xAxis: { type: 'category', data: rows.map((item) => item.day) },
    yAxis: { type: 'value' },
    grid: { left: 42, right: 18, top: 42, bottom: 38 },
    series: [
      { name: '收入', type: 'bar', data: rows.map((item) => item.income), itemStyle: { color: '#054d28', borderRadius: [8, 8, 0, 0] } },
      { name: '支出', type: 'bar', data: rows.map((item) => item.expense), itemStyle: { color: '#d03238', borderRadius: [8, 8, 0, 0] } }
    ]
  })
}

function renderCategory() {
  categoryInstance?.dispose()
  categoryInstance = echarts.init(categoryChart.value)
  const rows = (data.value.categoryStats || []).filter((item) => item.type === 'EXPENSE')
  categoryInstance.setOption({
    tooltip: { trigger: 'item' },
    color: ['#9fe870', '#ffc091', '#e2f6d5', '#ffd11a', '#38c8ff', '#d03238'],
    series: [
      {
        type: 'pie',
        radius: ['42%', '70%'],
        data: rows.map((item) => ({ name: item.categoryName || '未分类', value: item.total })),
        label: { formatter: '{b}: {d}%' }
      }
    ]
  })
}

onMounted(loadData)
</script>
