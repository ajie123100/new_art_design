<template>
  <div class="art-full-height">
    <ArtSearchBar
      v-model="filters"
      :items="searchItems"
      :show-expand="false"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="openDialog('add')">新增</ElButton>
            <ElUpload :auto-upload="false" :show-file-list="false" @change="handleImport">
              <ElButton>导入</ElButton>
            </ElUpload>
            <ElButton @click="fetchExportConfig(searchParams)">导出</ElButton>
            <ElButton @click="refreshCache">刷新缓存</ElButton>
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

    <ConfigDialog
      v-model:visible="dialogVisible"
      :dialog-type="dialogType"
      :row="currentRow"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import ConfigDialog from './modules/config-dialog.vue'
  import {
    fetchDeleteConfig,
    fetchExportConfig,
    fetchGetConfigList,
    fetchImportConfig,
    fetchRefreshConfigCache
  } from '@/api/system-manage'
  import type { UploadFile } from 'element-plus'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'

  defineOptions({ name: 'Config' })

  const filters = reactive<Api.SystemManage.ConfigSearchParams>({
    configName: undefined,
    configKey: undefined,
    configType: undefined
  })
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentRow = ref<Api.SystemManage.ConfigListItem>()

  const searchItems = computed(() => [
    { label: '参数名称', key: 'configName', type: 'input', props: { clearable: true } },
    { label: '参数键名', key: 'configKey', type: 'input', props: { clearable: true } },
    {
      label: '系统内置',
      key: 'configType',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: '是', value: 'Y' },
          { label: '否', value: 'N' }
        ]
      }
    }
  ])

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    replaceSearchParams,
    refreshData,
    handleSizeChange,
    handleCurrentChange
  } = useTable({
    core: {
      apiFn: fetchGetConfigList,
      apiParams: { pageNum: 1, pageSize: 20 },
      columnsFactory: () => [
        { prop: 'configName', label: '参数名称', minWidth: 140 },
        { prop: 'configKey', label: '参数键名', minWidth: 180, showOverflowTooltip: true },
        { prop: 'configValue', label: '参数键值', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'configType',
          label: '系统内置',
          width: 100,
          formatter: (row) => h(ElTag, { type: row.configType === 'Y' ? 'success' : 'info' }, () => row.configType)
        },
        { prop: 'createTime', label: '创建时间', width: 180 },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right',
          formatter: (row) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                onClick: () => openDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => deleteRow(row.configId)
              })
            ])
        }
      ]
    }
  })

  const handleSearch = (params: Api.SystemManage.ConfigSearchParams) => {
    replaceSearchParams(params)
    refreshData()
  }

  const handleReset = () => {
    Object.assign(filters, { configName: undefined, configKey: undefined, configType: undefined })
    replaceSearchParams({})
    refreshData()
  }

  const openDialog = (type: 'add' | 'edit', row?: Api.SystemManage.ConfigListItem) => {
    dialogType.value = type
    currentRow.value = row
    dialogVisible.value = true
  }

  const handleImport = async (uploadFile: UploadFile) => {
    if (!uploadFile.raw) return
    const result = await fetchImportConfig(uploadFile.raw)
    ElMessage.success(`导入成功 ${result.imported} 条，跳过 ${result.skipped} 条`)
    await refreshData()
  }

  const deleteRow = (id: number) => {
    ElMessageBox.confirm('确定删除该参数吗？', '删除确认', { type: 'warning' }).then(async () => {
      await fetchDeleteConfig(id)
      await refreshData()
    })
  }

  const refreshCache = async () => {
    await fetchRefreshConfigCache()
    ElMessage.success('缓存已刷新')
  }
</script>
