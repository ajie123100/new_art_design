<template>
  <div class="art-full-height dict-page">
    <ArtSearchBar
      v-model="typeFilters"
      :items="typeSearchItems"
      :show-expand="false"
      @search="handleTypeSearch"
      @reset="handleTypeReset"
    />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="typeColumnChecks" :loading="typeLoading" @refresh="refreshTypes">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="openTypeDialog('add')">新增类型</ElButton>
            <ElUpload :auto-upload="false" :show-file-list="false" @change="handleImportType">
              <ElButton>导入类型</ElButton>
            </ElUpload>
            <ElButton @click="fetchExportDictType(typeSearchParams)">导出类型</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        :loading="typeLoading"
        :data="typeData"
        :columns="typeColumns"
        :pagination="typePagination"
        @row-click="selectType"
        @pagination:size-change="handleTypeSizeChange"
        @pagination:current-change="handleTypeCurrentChange"
      />
    </ElCard>

    <ElCard class="art-table-card mt-3">
      <ArtTableHeader v-model:columns="dataColumnChecks" :loading="dataLoading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="openDataDialog('add')" :disabled="!activeDictType">新增数据</ElButton>
            <ElUpload :auto-upload="false" :show-file-list="false" @change="handleImportData">
              <ElButton>导入数据</ElButton>
            </ElUpload>
            <ElButton @click="fetchExportDictData(dataSearchParams)">导出数据</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        :loading="dataLoading"
        :data="dictData"
        :columns="dataColumns"
        :pagination="dataPagination"
        @pagination:size-change="handleDataSizeChange"
        @pagination:current-change="handleDataCurrentChange"
      />
    </ElCard>

    <DictTypeDialog
      v-model:visible="typeDialogVisible"
      :dialog-type="typeDialogType"
      :row="currentType"
      @success="refreshTypes"
    />
    <DictDataDialog
      v-model:visible="dataDialogVisible"
      :dialog-type="dataDialogType"
      :row="currentData"
      :dict-type="activeDictType"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import DictDataDialog from './modules/dict-data-dialog.vue'
  import DictTypeDialog from './modules/dict-type-dialog.vue'
  import {
    fetchDeleteDictData,
    fetchDeleteDictType,
    fetchExportDictData,
    fetchExportDictType,
    fetchGetDictDataList,
    fetchGetDictTypeList,
    fetchImportDictData,
    fetchImportDictType,
    fetchUpdateDictDataStatus,
    fetchUpdateDictTypeStatus
  } from '@/api/system-manage'
  import type { UploadFile } from 'element-plus'
  import { ElMessage, ElMessageBox, ElSwitch, ElTag } from 'element-plus'

  defineOptions({ name: 'Dict' })

  const activeDictType = ref<string>()
  const typeDialogVisible = ref(false)
  const dataDialogVisible = ref(false)
  const typeDialogType = ref<'add' | 'edit'>('add')
  const dataDialogType = ref<'add' | 'edit'>('add')
  const currentType = ref<Api.SystemManage.DictTypeListItem>()
  const currentData = ref<Api.SystemManage.DictDataListItem>()

  const typeFilters = reactive<Api.SystemManage.DictTypeSearchParams>({
    dictName: undefined,
    dictType: undefined,
    enabled: undefined
  })

  const typeSearchItems = computed(() => [
    { label: '字典名称', key: 'dictName', type: 'input', props: { clearable: true } },
    { label: '字典类型', key: 'dictType', type: 'input', props: { clearable: true } },
    {
      label: '状态',
      key: 'enabled',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: '启用', value: true },
          { label: '停用', value: false }
        ]
      }
    }
  ])

  const {
    columns: typeColumns,
    columnChecks: typeColumnChecks,
    data: typeData,
    loading: typeLoading,
    pagination: typePagination,
    searchParams: typeSearchParams,
    refreshData: refreshTypes,
    replaceSearchParams: replaceTypeSearchParams,
    handleSizeChange: handleTypeSizeChange,
    handleCurrentChange: handleTypeCurrentChange
  } = useTable({
    core: {
      apiFn: fetchGetDictTypeList,
      apiParams: { pageNum: 1, pageSize: 10 },
      columnsFactory: () => [
        { prop: 'dictName', label: '字典名称', minWidth: 140 },
        { prop: 'dictType', label: '字典类型', minWidth: 160 },
        {
          prop: 'enabled',
          label: '状态',
          width: 90,
          formatter: (row) =>
            h(ElSwitch, {
              modelValue: row.enabled,
              onChange: (enabled: string | number | boolean) =>
                fetchUpdateDictTypeStatus({ dictId: row.dictId, enabled: Boolean(enabled) })
            })
        },
        { prop: 'remark', label: '备注', minWidth: 140, showOverflowTooltip: true },
        { prop: 'createTime', label: '创建时间', width: 180 },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          formatter: (row) =>
            h('div', [
              h(ArtButtonTable, { type: 'edit', onClick: () => openTypeDialog('edit', row) }),
              h(ArtButtonTable, { type: 'delete', onClick: () => deleteType(row.dictId) })
            ])
        }
      ]
    }
  })

  const {
    columns: dataColumns,
    columnChecks: dataColumnChecks,
    data: dictData,
    loading: dataLoading,
    pagination: dataPagination,
    searchParams: dataSearchParams,
    refreshData,
    replaceSearchParams,
    handleSizeChange: handleDataSizeChange,
    handleCurrentChange: handleDataCurrentChange
  } = useTable({
    core: {
      apiFn: fetchGetDictDataList,
      apiParams: { pageNum: 1, pageSize: 10 },
      columnsFactory: () => [
        { prop: 'dictSort', label: '排序', width: 80 },
        { prop: 'dictLabel', label: '标签', minWidth: 120 },
        { prop: 'dictValue', label: '键值', minWidth: 120 },
        { prop: 'dictType', label: '类型', minWidth: 150 },
        {
          prop: 'defaultValue',
          label: '默认',
          width: 80,
          formatter: (row) => h(ElTag, { type: row.defaultValue ? 'success' : 'info' }, () => row.defaultValue ? '是' : '否')
        },
        {
          prop: 'enabled',
          label: '状态',
          width: 90,
          formatter: (row) =>
            h(ElSwitch, {
              modelValue: row.enabled,
              onChange: (enabled: string | number | boolean) =>
                fetchUpdateDictDataStatus({ dictCode: row.dictCode, enabled: Boolean(enabled) })
            })
        },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          formatter: (row) =>
            h('div', [
              h(ArtButtonTable, { type: 'edit', onClick: () => openDataDialog('edit', row) }),
              h(ArtButtonTable, { type: 'delete', onClick: () => deleteData(row.dictCode) })
            ])
        }
      ]
    }
  })

  const handleTypeSearch = (params: Api.SystemManage.DictTypeSearchParams) => {
    replaceTypeSearchParams(params)
    refreshTypes()
  }

  const handleTypeReset = () => {
    Object.assign(typeFilters, { dictName: undefined, dictType: undefined, enabled: undefined })
    replaceTypeSearchParams({})
    refreshTypes()
  }

  const selectType = (row: Api.SystemManage.DictTypeListItem) => {
    activeDictType.value = row.dictType
    replaceSearchParams({ dictType: row.dictType })
    refreshData()
  }

  const openTypeDialog = (type: 'add' | 'edit', row?: Api.SystemManage.DictTypeListItem) => {
    typeDialogType.value = type
    currentType.value = row
    typeDialogVisible.value = true
  }

  const openDataDialog = (type: 'add' | 'edit', row?: Api.SystemManage.DictDataListItem) => {
    dataDialogType.value = type
    currentData.value = row
    dataDialogVisible.value = true
  }

  const handleImportType = async (uploadFile: UploadFile) => {
    if (!uploadFile.raw) return
    const result = await fetchImportDictType(uploadFile.raw)
    ElMessage.success(`导入成功 ${result.imported} 条，跳过 ${result.skipped} 条`)
    await refreshTypes()
  }

  const handleImportData = async (uploadFile: UploadFile) => {
    if (!uploadFile.raw) return
    const result = await fetchImportDictData(uploadFile.raw)
    ElMessage.success(`导入成功 ${result.imported} 条，跳过 ${result.skipped} 条`)
    await refreshData()
  }

  const deleteType = (id: number) => {
    ElMessageBox.confirm('确定删除该字典类型吗？', '删除确认', { type: 'warning' }).then(async () => {
      await fetchDeleteDictType(id)
      await refreshTypes()
    })
  }

  const deleteData = (id: number) => {
    ElMessageBox.confirm('确定删除该字典数据吗？', '删除确认', { type: 'warning' }).then(async () => {
      await fetchDeleteDictData(id)
      await refreshData()
    })
  }
</script>
