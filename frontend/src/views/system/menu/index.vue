<template>
  <div class="menu-page art-full-height">
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
        @refresh="loadMenus"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton @click="openAddDialog()" v-ripple>新增菜单</ElButton>
            <ElButton @click="toggleExpand" v-ripple>{{ isExpanded ? '收起' : '展开' }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        rowKey="menuId"
        :loading="loading"
        :columns="columns"
        :data="tableData"
        :stripe="false"
        :tree-props="{ children: 'children' }"
        :default-expand-all="false"
      />

      <MenuDialog
        v-model:visible="dialogVisible"
        :dialog-type="dialogType"
        :menu-data="currentMenu"
        :parent-id="currentParentId"
        :default-menu-type="defaultMenuType"
        :menu-tree="allMenus"
        @success="loadMenus"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import {
    fetchDeleteSystemMenu,
    fetchGetSystemMenuList,
    fetchUpdateSystemMenuStatus
  } from '@/api/system-manage'
  import MenuDialog from './modules/menu-dialog.vue'
  import { ElMessage, ElMessageBox, ElSwitch, ElTag } from 'element-plus'

  defineOptions({ name: 'Menus' })

  type MenuTreeItem = Api.SystemManage.MenuTreeItem
  type MenuSearchParams = Api.SystemManage.MenuSearchParams

  const loading = ref(false)
  const tableRef = ref()
  const isExpanded = ref(false)
  const tableData = ref<MenuTreeItem[]>([])
  const allMenus = ref<MenuTreeItem[]>([])

  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentMenu = ref<MenuTreeItem | undefined>(undefined)
  const currentParentId = ref(0)
  const defaultMenuType = ref<'M' | 'C' | 'F' | undefined>(undefined)

  const initialSearchState: MenuSearchParams = {
    menuName: undefined,
    path: undefined,
    perms: undefined,
    menuType: undefined,
    enabled: undefined
  }

  const formFilters = reactive<MenuSearchParams>({ ...initialSearchState })
  const appliedFilters = reactive<MenuSearchParams>({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '菜单名称',
      key: 'menuName',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: '路由地址',
      key: 'path',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: '权限标识',
      key: 'perms',
      type: 'input',
      props: { clearable: true }
    }
  ])

  const menuTypeMap = {
    M: { text: '目录', type: 'info' },
    C: { text: '菜单', type: 'primary' },
    F: { text: '按钮', type: 'warning' }
  } as const

  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'menuName',
      label: '菜单名称',
      minWidth: 180
    },
    {
      prop: 'menuType',
      label: '类型',
      width: 90,
      formatter: (row: MenuTreeItem) => {
        const config = menuTypeMap[row.menuType] || menuTypeMap.C
        return h(ElTag, { type: config.type }, () => config.text)
      }
    },
    {
      prop: 'icon',
      label: '图标',
      minWidth: 120
    },
    {
      prop: 'orderNum',
      label: '排序',
      width: 80
    },
    {
      prop: 'path',
      label: '路由地址',
      minWidth: 150,
      showOverflowTooltip: true
    },
    {
      prop: 'component',
      label: '组件路径',
      minWidth: 160,
      showOverflowTooltip: true
    },
    {
      prop: 'perms',
      label: '权限标识',
      minWidth: 160,
      showOverflowTooltip: true
    },
    {
      prop: 'enabled',
      label: '状态',
      width: 100,
      formatter: (row: MenuTreeItem) =>
        h(ElSwitch, {
          modelValue: row.enabled,
          onChange: (enabled: string | number | boolean) => updateMenuStatus(row, Boolean(enabled))
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
      width: 190,
      fixed: 'right',
      align: 'right',
      formatter: (row: MenuTreeItem) =>
        h('div', { style: 'text-align: right' }, [
          ...(row.menuType === 'F'
            ? [
                h(ArtButtonTable, {
                  type: 'add',
                  title: '新增同级按钮',
                  onClick: () => openAddDialog(row, 'F')
                })
              ]
            : [
                h(ArtButtonTable, {
                  type: 'add',
                  title: '新增子菜单',
                  onClick: () => openAddDialog(row, 'C')
                }),
                h(ArtButtonTable, {
                  type: 'add',
                  title: '新增按钮',
                  onClick: () => openAddDialog(row, 'F')
                })
              ]),
          h(ArtButtonTable, {
            type: 'edit',
            onClick: () => openEditDialog(row)
          }),
          h(ArtButtonTable, {
            type: 'delete',
            onClick: () => deleteMenu(row)
          })
        ])
    }
  ])

  onMounted(() => {
    loadMenus()
  })

  const loadMenus = async () => {
    loading.value = true
    try {
      tableData.value = await fetchGetSystemMenuList(appliedFilters)
      allMenus.value = await fetchGetSystemMenuList()
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    Object.assign(appliedFilters, { ...formFilters })
    loadMenus()
  }

  const handleReset = () => {
    Object.assign(formFilters, { ...initialSearchState })
    Object.assign(appliedFilters, { ...initialSearchState })
    loadMenus()
  }

  const openAddDialog = (row?: MenuTreeItem, menuType?: 'M' | 'C' | 'F') => {
    dialogType.value = 'add'
    currentMenu.value = undefined
    currentParentId.value = row?.menuType === 'F' ? row.parentId : row?.menuId || 0
    defaultMenuType.value = menuType
    dialogVisible.value = true
  }

  const openEditDialog = (row: MenuTreeItem) => {
    dialogType.value = 'edit'
    currentMenu.value = row
    currentParentId.value = row.parentId || 0
    defaultMenuType.value = undefined
    dialogVisible.value = true
  }

  const updateMenuStatus = async (row: MenuTreeItem, enabled: boolean) => {
    const previous = row.enabled
    row.enabled = enabled
    try {
      await fetchUpdateSystemMenuStatus({ menuId: row.menuId, enabled })
      ElMessage.success(enabled ? '已启用' : '已停用')
    } catch (error) {
      row.enabled = previous
      throw error
    }
  }

  const deleteMenu = (row: MenuTreeItem) => {
    ElMessageBox.confirm(`确定删除菜单 "${row.menuName}" 吗？存在子菜单时需要先删除子菜单。`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await fetchDeleteSystemMenu(row.menuId)
        ElMessage.success('删除成功')
        loadMenus()
      })
      .catch(() => {
        ElMessage.info('已取消删除')
      })
  }

  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      const rows = tableData.value || []
      const processRows = (items: MenuTreeItem[]) => {
        items.forEach((item) => {
          if (item.children?.length) {
            tableRef.value?.elTableRef?.toggleRowExpansion(item, isExpanded.value)
            processRows(item.children)
          }
        })
      }
      processRows(rows)
    })
  }
</script>
