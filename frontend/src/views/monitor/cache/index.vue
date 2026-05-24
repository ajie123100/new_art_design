<template>
  <div class="cache-page">
    <ElRow :gutter="12">
      <ElCol :span="8">
        <ElCard>
          <template #header>缓存概览</template>
          <ElDescriptions :column="1" border>
            <ElDescriptionsItem label="键数量">{{ cacheInfo?.keys || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="库大小">{{ cacheInfo?.dbSize || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="命令统计">{{ cacheInfo?.commandStats || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>
      </ElCol>
      <ElCol :span="16">
        <ElCard>
          <template #header>
            <div class="flex-cb">
              <span>缓存名称</span>
              <ElButton :loading="loading" @click="loadData">刷新</ElButton>
            </div>
          </template>
          <ElTable :data="names" v-loading="loading">
            <ElTableColumn prop="cacheName" label="缓存名" min-width="180" />
            <ElTableColumn prop="keyCount" label="键数量" width="120" />
            <ElTableColumn label="操作" width="120" align="right">
              <template #default="{ row }">
                <ElButton link type="danger" @click="clearName(row.cacheName)">清理</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import { fetchCacheInfo, fetchCacheNames, fetchClearCacheName } from '@/api/monitor'

  defineOptions({ name: 'Cache' })

  const loading = ref(false)
  const cacheInfo = ref<Api.Monitor.CacheInfo>()
  const names = ref<Api.Monitor.CacheNameInfo[]>([])

  const loadData = async () => {
    loading.value = true
    try {
      const [info, cacheNames] = await Promise.all([fetchCacheInfo(), fetchCacheNames()])
      cacheInfo.value = info
      names.value = cacheNames
    } finally {
      loading.value = false
    }
  }

  const clearName = async (cacheName: string) => {
    await fetchClearCacheName(cacheName)
    await loadData()
  }

  onMounted(loadData)
</script>

<style scoped>
  .cache-page {
    padding: 0;
  }
</style>
