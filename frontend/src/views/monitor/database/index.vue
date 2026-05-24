<template>
  <ElCard class="art-table-card" v-loading="loading">
    <template #header>
      <div class="flex-cb">
        <span>数据库监控</span>
        <ElButton @click="loadData">刷新</ElButton>
      </div>
    </template>
    <ElDescriptions v-if="info" :column="2" border>
      <ElDescriptionsItem label="数据库">{{ info.databaseProductName }}</ElDescriptionsItem>
      <ElDescriptionsItem label="版本">{{ info.databaseProductVersion }}</ElDescriptionsItem>
      <ElDescriptionsItem label="驱动">{{ info.driverName }}</ElDescriptionsItem>
      <ElDescriptionsItem label="驱动版本">{{ info.driverVersion }}</ElDescriptionsItem>
      <ElDescriptionsItem label="用户名">{{ info.userName }}</ElDescriptionsItem>
      <ElDescriptionsItem label="最大连接">{{ info.maxConnections }}</ElDescriptionsItem>
      <ElDescriptionsItem label="活动连接">{{ info.activeConnections }}</ElDescriptionsItem>
      <ElDescriptionsItem label="空闲连接">{{ info.idleConnections }}</ElDescriptionsItem>
      <ElDescriptionsItem label="连接地址" :span="2">{{ info.url }}</ElDescriptionsItem>
    </ElDescriptions>
  </ElCard>
</template>

<script setup lang="ts">
  import { fetchDatabaseInfo } from '@/api/monitor'

  defineOptions({ name: 'Database' })

  const loading = ref(false)
  const info = ref<Api.Monitor.DatabaseInfo>()

  const loadData = async () => {
    loading.value = true
    try {
      info.value = await fetchDatabaseInfo()
    } finally {
      loading.value = false
    }
  }

  onMounted(loadData)
</script>
