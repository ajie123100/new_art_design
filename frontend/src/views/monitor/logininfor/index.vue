<template>
  <div class="art-full-height">
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="fetchExportLoginLog(searchParams)">导出</ElButton>
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
  import { fetchCleanLoginLog, fetchExportLoginLog, fetchLoginLogList } from '@/api/monitor'
  import { ElMessageBox, ElTag } from 'element-plus'

  defineOptions({ name: 'Logininfor' })

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
      apiFn: fetchLoginLogList,
      apiParams: { pageNum: 1, pageSize: 20 },
      columnsFactory: () => [
        { prop: 'userName', label: '用户账号', minWidth: 120 },
        { prop: 'ipaddr', label: '登录IP', minWidth: 140 },
        { prop: 'browser', label: '浏览器', minWidth: 100 },
        { prop: 'os', label: '操作系统', minWidth: 100 },
        {
          prop: 'status',
          label: '状态',
          width: 90,
          formatter: (row) => h(ElTag, { type: row.status === '1' ? 'success' : 'danger' }, () => row.status === '1' ? '成功' : '失败')
        },
        { prop: 'msg', label: '消息', minWidth: 180, showOverflowTooltip: true },
        { prop: 'loginTime', label: '登录时间', width: 180 }
      ]
    }
  })

  const cleanLogs = () => {
    ElMessageBox.confirm('确定清空登录日志吗？', '清空确认', { type: 'warning' }).then(async () => {
      await fetchCleanLoginLog()
      await refreshData()
    })
  }
</script>
