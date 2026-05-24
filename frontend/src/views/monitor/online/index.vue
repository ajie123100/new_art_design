<template>
  <div class="art-full-height">
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="loadData" />
      <ArtTable :loading="loading" :data="data" :columns="columns" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { fetchForceLogout, fetchOnlineUsers } from '@/api/monitor'
  import { ElMessageBox } from 'element-plus'

  defineOptions({ name: 'Online' })

  const loading = ref(false)
  const data = ref<Api.Monitor.OnlineUser[]>([])

  const { columns, columnChecks } = useTableColumns<Api.Monitor.OnlineUser>(() => [
    { prop: 'loginName', label: '登录账号', minWidth: 120 },
    { prop: 'deptName', label: '部门', minWidth: 120 },
    { prop: 'ipaddr', label: '登录IP', minWidth: 140 },
    { prop: 'browser', label: '浏览器', minWidth: 100 },
    { prop: 'os', label: '操作系统', minWidth: 100 },
    { prop: 'device', label: '设备', minWidth: 100 },
    { prop: 'loginTime', label: '登录时间', width: 180 },
    {
      prop: 'operation',
      label: '操作',
      width: 90,
      formatter: (row) => h(ArtButtonTable, { type: 'delete', title: '强退', onClick: () => forceLogout(row.tokenId) })
    }
  ])

  const loadData = async () => {
    loading.value = true
    try {
      data.value = await fetchOnlineUsers()
    } finally {
      loading.value = false
    }
  }

  const forceLogout = (tokenId: string) => {
    ElMessageBox.confirm('确定强制该用户下线吗？', '强退确认', { type: 'warning' }).then(async () => {
      await fetchForceLogout(tokenId)
      await loadData()
    })
  }

  onMounted(loadData)
</script>
