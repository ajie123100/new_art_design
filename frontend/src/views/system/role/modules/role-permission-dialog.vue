<template>
  <ElDialog
    v-model="visible"
    title="菜单权限"
    width="520px"
    align-center
    class="el-dialog-border"
    @close="handleClose"
  >
    <ElScrollbar height="70vh" v-loading="loading">
      <ElTree
        ref="treeRef"
        :data="processedMenuList"
        show-checkbox
        node-key="id"
        :default-expand-all="isExpandAll"
        :props="defaultProps"
        @check="handleTreeCheck"
      >
        <template #default="{ data }">
          <span>{{ defaultProps.label(data) }}</span>
        </template>
      </ElTree>
    </ElScrollbar>
    <template #footer>
      <ElButton @click="toggleExpandAll">{{ isExpandAll ? '全部收起' : '全部展开' }}</ElButton>
      <ElButton @click="toggleSelectAll">{{ isSelectAll ? '取消全选' : '全部选择' }}</ElButton>
      <ElButton type="primary" :loading="saving" @click="savePermission">保存</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchGetRoleMenuIds,
    fetchGetRoleMenuTree,
    fetchSaveRoleMenus
  } from '@/api/system-manage'
  import { formatMenuTitle } from '@/utils/router'

  type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  interface MenuNode {
    id?: number
    name?: string
    label?: string
    meta?: {
      title?: string
    }
    children?: MenuNode[]
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    roleData: undefined
  })

  const emit = defineEmits<Emits>()

  const treeRef = ref()
  const menuList = ref<MenuNode[]>([])
  const loading = ref(false)
  const saving = ref(false)
  const isExpandAll = ref(true)
  const isSelectAll = ref(false)

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const processedMenuList = computed(() => menuList.value)

  const defaultProps = {
    children: 'children',
    label: (data: MenuNode) => formatMenuTitle(data.meta?.title || '') || data.label || data.name || ''
  }

  watch(
    () => props.modelValue,
    async (newVal) => {
      if (newVal && props.roleData) {
        await loadPermission()
      }
    }
  )

  const loadPermission = async () => {
    if (!props.roleData) return

    loading.value = true
    try {
      const [tree, checkedIds] = await Promise.all([
        fetchGetRoleMenuTree(),
        fetchGetRoleMenuIds(props.roleData.roleId)
      ])
      menuList.value = tree as MenuNode[]
      nextTick(() => {
        treeRef.value?.setCheckedKeys(checkedIds)
        handleTreeCheck()
      })
    } finally {
      loading.value = false
    }
  }

  const handleClose = () => {
    visible.value = false
    treeRef.value?.setCheckedKeys([])
    isSelectAll.value = false
  }

  const savePermission = async () => {
    if (!props.roleData || !treeRef.value) return

    saving.value = true
    try {
      const checkedKeys = treeRef.value.getCheckedKeys() as number[]
      const halfCheckedKeys = treeRef.value.getHalfCheckedKeys() as number[]
      await fetchSaveRoleMenus({
        roleId: props.roleData.roleId,
        menuIds: [...new Set([...checkedKeys, ...halfCheckedKeys])]
      })
      ElMessage.success('权限保存成功')
      emit('success')
      handleClose()
    } finally {
      saving.value = false
    }
  }

  const toggleExpandAll = () => {
    const tree = treeRef.value
    if (!tree) return

    Object.values(tree.store.nodesMap).forEach((node: any) => {
      node.expanded = !isExpandAll.value
    })
    isExpandAll.value = !isExpandAll.value
  }

  const toggleSelectAll = () => {
    const tree = treeRef.value
    if (!tree) return

    if (!isSelectAll.value) {
      tree.setCheckedKeys(getAllNodeKeys(processedMenuList.value))
    } else {
      tree.setCheckedKeys([])
    }
    isSelectAll.value = !isSelectAll.value
  }

  const getAllNodeKeys = (nodes: MenuNode[]): number[] => {
    const keys: number[] = []
    const traverse = (nodeList: MenuNode[]) => {
      nodeList.forEach((node) => {
        if (node.id !== undefined) keys.push(node.id)
        if (node.children?.length) traverse(node.children)
      })
    }
    traverse(nodes)
    return keys
  }

  const handleTreeCheck = () => {
    const tree = treeRef.value
    if (!tree) return

    const checkedKeys = tree.getCheckedKeys()
    const allKeys = getAllNodeKeys(processedMenuList.value)
    isSelectAll.value = checkedKeys.length === allKeys.length && allKeys.length > 0
  }
</script>
