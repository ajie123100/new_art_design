<template>
  <div class="dept-page art-full-height">
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card">
      <ArtTableHeader
        v-model:columns="columnChecks"
        :showZebra="false"
        :loading="loading"
        @refresh="loadDepartments"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton @click="openAddDialog()" v-ripple>新增部门</ElButton>
            <ElButton @click="toggleExpand" v-ripple>{{ isExpanded ? '收起' : '展开' }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        rowKey="deptId"
        :loading="loading"
        :columns="columns"
        :data="tableData"
        :stripe="false"
        :tree-props="{ children: 'children' }"
        :default-expand-all="false"
      />

      <DeptDialog
        v-model:visible="dialogVisible"
        :dialog-type="dialogType"
        :dept-data="currentDept"
        :parent-id="currentParentId"
        :dept-tree="allDepartments"
        @success="loadDepartments"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import {
    fetchDeleteDept,
    fetchGetDeptList,
    fetchUpdateDeptStatus
  } from '@/api/system-manage'
  import DeptDialog from './modules/dept-dialog.vue'
  import { ElMessage, ElMessageBox, ElSwitch, ElTag } from 'element-plus'

  defineOptions({ name: 'Dept' })

  type DeptTreeItem = Api.SystemManage.DeptTreeItem
  type DeptSearchParams = Api.SystemManage.DeptSearchParams

  const loading = ref(false)
  const tableRef = ref()
  const isExpanded = ref(false)
  const tableData = ref<DeptTreeItem[]>([])
  const allDepartments = ref<DeptTreeItem[]>([])

  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentDept = ref<DeptTreeItem | undefined>(undefined)
  const currentParentId = ref(0)

  const initialSearchState: DeptSearchParams = {
    deptName: undefined,
    leader: undefined,
    enabled: undefined
  }

  const formFilters = reactive<DeptSearchParams>({ ...initialSearchState })
  const appliedFilters = reactive<DeptSearchParams>({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '部门名称',
      key: 'deptName',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: '负责人',
      key: 'leader',
      type: 'input',
      props: { clearable: true }
    }
  ])

  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'deptName',
      label: '部门名称',
      minWidth: 180
    },
    {
      prop: 'orderNum',
      label: '排序',
      width: 80
    },
    {
      prop: 'leader',
      label: '负责人',
      minWidth: 110
    },
    {
      prop: 'phone',
      label: '联系电话',
      minWidth: 130
    },
    {
      prop: 'email',
      label: '邮箱',
      minWidth: 180,
      showOverflowTooltip: true
    },
    {
      prop: 'enabled',
      label: '状态',
      width: 100,
      formatter: (row: DeptTreeItem) =>
        h(ElSwitch, {
          modelValue: row.enabled,
          onChange: (enabled: string | number | boolean) => updateDeptStatus(row, Boolean(enabled))
        })
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180
    },
    {
      prop: 'operation',
      label: '操作',
      width: 150,
      fixed: 'right',
      align: 'right',
      formatter: (row: DeptTreeItem) =>
        h('div', { style: 'text-align: right' }, [
          h(ArtButtonTable, {
            type: 'add',
            title: '新增子部门',
            onClick: () => openAddDialog(row)
          }),
          h(ArtButtonTable, {
            type: 'edit',
            onClick: () => openEditDialog(row)
          }),
          h(ArtButtonTable, {
            type: 'delete',
            onClick: () => deleteDept(row)
          })
        ])
    }
  ])

  onMounted(() => {
    loadDepartments()
  })

  const loadDepartments = async () => {
    loading.value = true
    try {
      tableData.value = await fetchGetDeptList(appliedFilters)
      allDepartments.value = await fetchGetDeptList()
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    Object.assign(appliedFilters, { ...formFilters })
    loadDepartments()
  }

  const handleReset = () => {
    Object.assign(formFilters, { ...initialSearchState })
    Object.assign(appliedFilters, { ...initialSearchState })
    loadDepartments()
  }

  const openAddDialog = (row?: DeptTreeItem) => {
    dialogType.value = 'add'
    currentDept.value = undefined
    currentParentId.value = row?.deptId || 0
    dialogVisible.value = true
  }

  const openEditDialog = (row: DeptTreeItem) => {
    dialogType.value = 'edit'
    currentDept.value = row
    currentParentId.value = row.parentId || 0
    dialogVisible.value = true
  }

  const updateDeptStatus = async (row: DeptTreeItem, enabled: boolean) => {
    const previous = row.enabled
    row.enabled = enabled
    try {
      await fetchUpdateDeptStatus({ deptId: row.deptId, enabled })
      ElMessage.success(enabled ? '已启用' : '已停用')
    } catch (error) {
      row.enabled = previous
      throw error
    }
  }

  const deleteDept = (row: DeptTreeItem) => {
    ElMessageBox.confirm(`确定删除部门 "${row.deptName}" 吗？存在子部门或用户时不能删除。`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await fetchDeleteDept(row.deptId)
        ElMessage.success('删除成功')
        loadDepartments()
      })
      .catch(() => {
        ElMessage.info('已取消删除')
      })
  }

  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      const processRows = (items: DeptTreeItem[]) => {
        items.forEach((item) => {
          if (item.children?.length) {
            tableRef.value?.elTableRef?.toggleRowExpansion(item, isExpanded.value)
            processRows(item.children)
          }
        })
      }
      processRows(tableData.value || [])
    })
  }
</script>
