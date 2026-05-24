<!-- 用户管理页面 -->
<!-- art-full-height 自动计算出页面剩余高度 -->
<!-- art-table-card 一个符合系统样式的 class，同时自动撑满剩余高度 -->
<!-- 更多 useTable 使用示例请移步至 功能示例 下面的高级表格示例或者查看官方文档 -->
<!-- useTable 文档：https://www.artd.pro/docs/zh/guide/hooks/use-table.html -->
<template>
  <div class="user-page art-full-height">
    <!-- 搜索栏 -->
    <UserSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams"></UserSearch>

    <ElCard class="art-table-card">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="showDialog('add')" v-ripple>新增用户</ElButton>
            <ElButton @click="fetchExportUser(searchParams)">导出</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>

      <!-- 用户弹窗 -->
      <UserDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :user-data="currentUserData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchCreateUser,
    fetchDeleteUser,
    fetchExportUser,
    fetchGetUserList,
    fetchResetUserPassword,
    fetchUpdateUser,
    fetchUpdateUserStatus
  } from '@/api/system-manage'
  import UserSearch from './modules/user-search.vue'
  import UserDialog from './modules/user-dialog.vue'
  import { ElSwitch, ElMessage, ElMessageBox, ElImage } from 'element-plus'
  import { DialogType } from '@/types'

  defineOptions({ name: 'User' })

  type UserListItem = Api.SystemManage.UserListItem

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentUserData = ref<Partial<UserListItem>>({})

  // 选中行
  const selectedRows = ref<UserListItem[]>([])

  // 搜索表单
  const searchForm = ref({
    userName: undefined,
    userGender: undefined,
    userPhone: undefined,
    userEmail: undefined,
    status: '1'
  })

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    // 核心配置
    core: {
      apiFn: fetchGetUserList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        { type: 'index', width: 60, label: '序号' }, // 序号
        {
          prop: 'userInfo',
          label: '用户名',
          width: 280,
          // visible: false, // 默认是否显示列
          formatter: (row) => {
            return h('div', { class: 'user flex-c' }, [
              h(ElImage, {
                class: 'size-9.5 rounded-md',
                src: row.avatar,
                previewSrcList: [row.avatar],
                // 图片预览是否插入至 body 元素上，用于解决表格内部图片预览样式异常
                previewTeleported: true
              }),
              h('div', { class: 'ml-2' }, [
                h('p', { class: 'user-name' }, row.userName),
                h('p', { class: 'email' }, row.userEmail)
              ])
            ])
          }
        },
        {
          prop: 'userGender',
          label: '性别',
          sortable: true,
          formatter: (row) => row.userGender
        },
        { prop: 'userPhone', label: '手机号' },
        {
          prop: 'status',
          label: '状态',
          formatter: (row) => {
            return h(ElSwitch, {
              modelValue: row.status === '1',
              onChange: (enabled: string | number | boolean) => updateUserStatus(row, Boolean(enabled))
            })
          }
        },
        {
          prop: 'createTime',
          label: '创建日期',
          sortable: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right', // 固定列
          formatter: (row) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'view',
                icon: 'ri:key-2-line',
                iconClass: 'bg-warning/12 text-warning',
                title: '重置密码',
                onClick: () => resetPassword(row)
              }),
              h(ArtButtonTable, {
                type: 'edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => deleteUser(row)
              })
            ])
        }
      ]
    }
  })

  /**
   * 搜索处理
   * @param params 参数
   */
  const handleSearch = (params: Api.SystemManage.UserSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  /**
   * 显示用户弹窗
   */
  const showDialog = (type: DialogType, row?: UserListItem): void => {
    dialogType.value = type
    currentUserData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 删除用户
   */
  const deleteUser = (row: UserListItem): void => {
    ElMessageBox.confirm(`确定要删除该用户吗？`, '删除用户', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }).then(async () => {
      await fetchDeleteUser(row.id)
      ElMessage.success('删除成功')
      getData()
    })
  }

  const updateUserStatus = async (row: UserListItem, enabled: boolean) => {
    const previous = row.status
    row.status = enabled ? '1' : '2'
    try {
      await fetchUpdateUserStatus({ id: row.id, status: row.status })
      ElMessage.success(enabled ? '已启用' : '已停用')
    } catch (error) {
      row.status = previous
      throw error
    }
  }

  const resetPassword = (row: UserListItem) => {
    ElMessageBox.prompt(`请输入 "${row.userName}" 的新密码`, '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: '123456',
      inputValidator: (value) => Boolean(value && value.length >= 6),
      inputErrorMessage: '密码至少 6 位'
    }).then(async ({ value }) => {
      await fetchResetUserPassword({ id: row.id, password: value })
      ElMessage.success('密码已重置')
    })
  }

  /**
   * 处理弹窗提交事件
   */
  const handleDialogSubmit = async (params: Api.SystemManage.UserSaveParams) => {
    try {
      if (dialogType.value === 'add') {
        await fetchCreateUser(params)
      } else {
        await fetchUpdateUser(params)
      }
      ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功')
      dialogVisible.value = false
      currentUserData.value = {}
      getData()
    } catch {
      ElMessage.error(dialogType.value === 'add' ? '新增失败' : '更新失败')
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: UserListItem[]): void => {
    selectedRows.value = selection
  }
</script>
