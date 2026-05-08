<template>
  <div>
    <h1 class="page-title">月度财务分析</h1>
    <div class="toolbar">
      <el-date-picker v-model="month" type="month" value-format="YYYY-MM" placeholder="选择月份" />
      <el-button type="primary" :icon="TrendCharts" @click="loadData">分析</el-button>
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
        <h3 style="margin-top: 0">每日收支趋势</h3>
        <div ref="trendChart" class="chart"></div>
      </div>
      <div class="panel">
        <h3 style="margin-top: 0">分类占比</h3>
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

async function loadData() {
  data.value = await statisticsApi.monthly({ month: month.value })
  await nextTick()
  renderTrend()
  renderCategory()
}

function renderTrend() {
  const chart = echarts.init(trendChart.value)
  const rows = data.value.trendStats || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'] },
    xAxis: { type: 'category', data: rows.map((item) => item.day) },
    yAxis: { type: 'value' },
    series: [
      { name: '收入', type: 'bar', data: rows.map((item) => item.income), itemStyle: { color: '#147d64' } },
      { name: '支出', type: 'bar', data: rows.map((item) => item.expense), itemStyle: { color: '#c2410c' } }
    ]
  })
}

function renderCategory() {
  const chart = echarts.init(categoryChart.value)
  const rows = (data.value.categoryStats || []).filter((item) => item.type === 'EXPENSE')
  chart.setOption({
    tooltip: { trigger: 'item' },
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
