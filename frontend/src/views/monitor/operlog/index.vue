<template>
  <div class="art-full-height">
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="fetchExportOperLog(searchParams)">导出</ElButton>
            <ElButton @click="cleanLogs">清空</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { fetchCleanOperLog, fetchExportOperLog, fetchOperLogList } from '@/api/monitor'
  import { ElMessageBox, ElTag } from 'element-plus'

  defineOptions({ name: 'Operlog' })

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    refreshData,
    handleSizeChange,
    handleCurrentChange
  } = useTable({
    core: {
      apiFn: fetchOperLogList,
      apiParams: { pageNum: 1, pageSize: 20 },
      columnsFactory: () => [
        { prop: 'title', label: '模块', minWidth: 120 },
        { prop: 'operName', label: '操作人', minWidth: 100 },
        { prop: 'operIp', label: '操作IP', minWidth: 140 },
        { prop: 'operUrl', label: '请求地址', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'status',
          label: '状态',
          width: 90,
          formatter: (row) => h(ElTag, { type: row.status === '1' ? 'success' : 'danger' }, () => row.status === '1' ? '成功' : '失败')
        },
        { prop: 'costTime', label: '耗时(ms)', width: 100 },
        { prop: 'operTime', label: '操作时间', width: 180 }
      ]
    }
  })

  const cleanLogs = () => {
    ElMessageBox.confirm('确定清空操作日志吗？', '清空确认', { type: 'warning' }).then(async () => {
      await fetchCleanOperLog()
      await refreshData()
    })
  }
</script>
