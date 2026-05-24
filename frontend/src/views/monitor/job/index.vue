<template>
  <div class="art-full-height">
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton @click="fetchExportJobLog()">导出任务日志</ElButton>
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
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchChangeJobStatus,
    fetchDeleteJob,
    fetchExportJobLog,
    fetchJobList,
    fetchRunJob
  } from '@/api/monitor'
  import { ElMessageBox, ElSwitch, ElTag } from 'element-plus'

  defineOptions({ name: 'Job' })

  const { columns, columnChecks, data, loading, pagination, refreshData, handleSizeChange, handleCurrentChange } =
    useTable({
      core: {
        apiFn: fetchJobList,
        apiParams: { pageNum: 1, pageSize: 20 },
        columnsFactory: () => [
          { prop: 'jobName', label: '任务名称', minWidth: 140 },
          { prop: 'jobGroup', label: '任务组', minWidth: 100 },
          { prop: 'invokeTarget', label: '调用目标', minWidth: 220, showOverflowTooltip: true },
          { prop: 'cronExpression', label: 'Cron', minWidth: 140 },
          {
            prop: 'status',
            label: '状态',
            width: 90,
            formatter: (row) =>
              h(ElSwitch, {
                modelValue: row.status === '1',
                onChange: (enabled: string | number | boolean) => changeStatus(row.jobId, Boolean(enabled))
              })
          },
          { prop: 'createTime', label: '创建时间', width: 180 },
          {
            prop: 'operation',
            label: '操作',
            width: 150,
            fixed: 'right',
            formatter: (row) =>
              h('div', [
                h(ElTag, { class: 'mr-1', onClick: () => runJob(row.jobId) }, () => '执行'),
                h(ArtButtonTable, { type: 'delete', onClick: () => deleteRow(row.jobId) })
              ])
          }
        ]
      }
    })

  const changeStatus = async (jobId: number, enabled: boolean) => {
    await fetchChangeJobStatus(jobId, enabled ? '1' : '2')
    await refreshData()
  }

  const runJob = async (jobId: number) => {
    await fetchRunJob(jobId)
  }

  const deleteRow = (id: number) => {
    ElMessageBox.confirm('确定删除该定时任务吗？', '删除确认', { type: 'warning' }).then(async () => {
      await fetchDeleteJob(id)
      await refreshData()
    })
  }
</script>
