<template>
  <div class="art-full-height">
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />
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
  import { fetchDeleteNotice, fetchGetNoticeList } from '@/api/system-manage'
  import { ElMessageBox, ElTag } from 'element-plus'

  defineOptions({ name: 'Notice' })

  const { columns, columnChecks, data, loading, pagination, refreshData, handleSizeChange, handleCurrentChange } =
    useTable({
      core: {
        apiFn: fetchGetNoticeList,
        apiParams: { pageNum: 1, pageSize: 20 },
        columnsFactory: () => [
          { prop: 'noticeTitle', label: '标题', minWidth: 180, showOverflowTooltip: true },
          {
            prop: 'noticeType',
            label: '类型',
            width: 100,
            formatter: (row) => h(ElTag, { type: row.noticeType === '2' ? 'warning' : 'primary' }, () => row.noticeType === '2' ? '公告' : '通知')
          },
          {
            prop: 'status',
            label: '状态',
            width: 90,
            formatter: (row) => h(ElTag, { type: row.status === '1' ? 'success' : 'info' }, () => row.status === '1' ? '正常' : '关闭')
          },
          { prop: 'createTime', label: '创建时间', width: 180 },
          {
            prop: 'operation',
            label: '操作',
            width: 80,
            formatter: (row) => h(ArtButtonTable, { type: 'delete', onClick: () => deleteRow(row.noticeId) })
          }
        ]
      }
    })

  const deleteRow = (id: number) => {
    ElMessageBox.confirm('确定删除该通知公告吗？', '删除确认', { type: 'warning' }).then(async () => {
      await fetchDeleteNotice(id)
      await refreshData()
    })
  }
</script>
