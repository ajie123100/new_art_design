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
  import { fetchDeletePost, fetchGetPostList } from '@/api/system-manage'
  import { ElMessageBox, ElTag } from 'element-plus'

  defineOptions({ name: 'Post' })

  const { columns, columnChecks, data, loading, pagination, refreshData, handleSizeChange, handleCurrentChange } =
    useTable({
      core: {
        apiFn: fetchGetPostList,
        apiParams: { pageNum: 1, pageSize: 20 },
        columnsFactory: () => [
          { prop: 'postCode', label: '岗位编码', minWidth: 140 },
          { prop: 'postName', label: '岗位名称', minWidth: 140 },
          { prop: 'postSort', label: '排序', width: 80 },
          {
            prop: 'enabled',
            label: '状态',
            width: 90,
            formatter: (row) => h(ElTag, { type: row.enabled ? 'success' : 'info' }, () => row.enabled ? '启用' : '停用')
          },
          { prop: 'createTime', label: '创建时间', width: 180 },
          {
            prop: 'operation',
            label: '操作',
            width: 80,
            formatter: (row) => h(ArtButtonTable, { type: 'delete', onClick: () => deleteRow(row.postId) })
          }
        ]
      }
    })

  const deleteRow = (id: number) => {
    ElMessageBox.confirm('确定删除该岗位吗？', '删除确认', { type: 'warning' }).then(async () => {
      await fetchDeletePost(id)
      await refreshData()
    })
  }
</script>
