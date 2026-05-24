<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
    width="560px"
    align-center
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElFormItem label="角色名称" prop="roleName">
        <ElInput v-model.trim="form.roleName" placeholder="请输入角色名称" />
      </ElFormItem>
      <ElFormItem label="角色编码" prop="roleCode">
        <ElInput v-model.trim="form.roleCode" placeholder="请输入角色编码，如 R_ADMIN" />
      </ElFormItem>
      <ElFormItem label="描述" prop="description">
        <ElInput v-model.trim="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
      </ElFormItem>
      <ElFormItem label="启用">
        <ElSwitch v-model="form.enabled" />
      </ElFormItem>
      <ElFormItem label="数据范围" prop="dataScope">
        <ElSelect v-model="form.dataScope">
          <ElOption label="全部数据权限" value="1" />
          <ElOption label="自定数据权限" value="2" />
          <ElOption label="本部门数据权限" value="3" />
          <ElOption label="本部门及以下" value="4" />
          <ElOption label="仅本人数据权限" value="5" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem v-if="form.dataScope === '2'" label="部门权限" prop="deptIds">
        <ElScrollbar class="dept-tree-wrap" height="220px" v-loading="deptLoading">
          <ElTree
            ref="deptTreeRef"
            :data="deptTree"
            show-checkbox
            node-key="deptId"
            default-expand-all
            :props="deptTreeProps"
          />
        </ElScrollbar>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchCreateRole,
    fetchGetDeptList,
    fetchGetRoleDeptIds,
    fetchUpdateRole
  } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'

  type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    roleData: undefined
  })

  const emit = defineEmits<Emits>()
  const formRef = ref<FormInstance>()
  const deptTreeRef = ref()
  const submitting = ref(false)
  const deptLoading = ref(false)
  const deptTree = ref<Api.SystemManage.DeptTreeItem[]>([])

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const rules = reactive<FormRules>({
    roleName: [
      { required: true, message: '请输入角色名称', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    roleCode: [
      { required: true, message: '请输入角色编码', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    description: [{ required: true, message: '请输入角色描述', trigger: 'blur' }]
  })

  const form = reactive<Api.SystemManage.RoleSaveParams>({
    roleId: undefined,
    roleName: '',
    roleCode: '',
    description: '',
    dataScope: '1',
    deptIds: [],
    enabled: true
  })

  const deptTreeProps = {
    children: 'children',
    label: 'deptName'
  }

  watch(
    () => props.modelValue,
    async (newVal) => {
      if (newVal) {
        await initForm()
      }
    }
  )

  watch(
    () => props.roleData,
    async () => {
      if (props.modelValue) {
        await initForm()
      }
    },
    { deep: true }
  )

  watch(
    () => form.dataScope,
    async (value) => {
      if (visible.value && value === '2') {
        await loadDeptTree()
      }
    }
  )

  const initForm = async () => {
    if (props.dialogType === 'edit' && props.roleData) {
      Object.assign(form, {
        roleId: props.roleData.roleId,
        roleName: props.roleData.roleName,
        roleCode: props.roleData.roleCode,
        description: props.roleData.description,
        dataScope: props.roleData.dataScope || '1',
        deptIds: [],
        enabled: props.roleData.enabled
      })
      if (form.dataScope === '2') {
        await loadDeptTree()
        await loadRoleDeptIds(props.roleData.roleId)
      }
    } else {
      Object.assign(form, {
        roleId: undefined,
        roleName: '',
        roleCode: '',
        description: '',
        dataScope: '1',
        deptIds: [],
        enabled: true
      })
    }
    nextTick(() => formRef.value?.clearValidate())
  }

  const loadDeptTree = async () => {
    if (deptTree.value.length) return

    deptLoading.value = true
    try {
      deptTree.value = await fetchGetDeptList()
    } finally {
      deptLoading.value = false
    }
  }

  const loadRoleDeptIds = async (roleId: number) => {
    const deptIds = await fetchGetRoleDeptIds(roleId)
    form.deptIds = deptIds
    await nextTick()
    deptTreeRef.value?.setCheckedKeys(deptIds)
  }

  const handleClose = () => {
    visible.value = false
    formRef.value?.resetFields()
    deptTreeRef.value?.setCheckedKeys([])
  }

  const handleSubmit = async () => {
    if (!formRef.value) return

    submitting.value = true
    try {
      await formRef.value.validate()
      if (form.dataScope === '2') {
        const checkedKeys = (deptTreeRef.value?.getCheckedKeys() || []) as number[]
        const halfCheckedKeys = (deptTreeRef.value?.getHalfCheckedKeys() || []) as number[]
        form.deptIds = [...new Set([...checkedKeys, ...halfCheckedKeys])]
        if (!form.deptIds.length) {
          ElMessage.warning('请选择部门权限')
          return
        }
      } else {
        form.deptIds = []
      }
      if (props.dialogType === 'add') {
        await fetchCreateRole(form)
      } else {
        await fetchUpdateRole(form)
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      emit('success')
      handleClose()
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
  .dept-tree-wrap {
    width: 100%;
    padding: 8px;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
  }
</style>
