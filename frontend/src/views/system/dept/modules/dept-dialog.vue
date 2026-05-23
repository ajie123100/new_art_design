<template>
  <ElDialog
    :model-value="visible"
    :title="dialogTitle"
    width="640px"
    align-center
    @update:model-value="handleClose"
    @closed="resetForm"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="92px">
      <ElRow :gutter="16">
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="上级部门" prop="parentId">
            <ElTreeSelect
              v-model="form.parentId"
              :data="parentOptions"
              check-strictly
              default-expand-all
              node-key="value"
              :props="{ label: 'label', value: 'value', children: 'children' }"
              placeholder="请选择上级部门"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="部门名称" prop="deptName">
            <ElInput v-model.trim="form.deptName" placeholder="请输入部门名称" />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="显示排序" prop="orderNum">
            <ElInputNumber
              v-model="form.orderNum"
              :min="1"
              controls-position="right"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="负责人">
            <ElInput v-model.trim="form.leader" placeholder="请输入负责人" />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="联系电话">
            <ElInput v-model.trim="form.phone" placeholder="请输入联系电话" />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="邮箱">
            <ElInput v-model.trim="form.email" placeholder="请输入邮箱" />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="部门状态">
            <ElSwitch v-model="form.enabled" active-text="启用" inactive-text="停用" />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchCreateDept, fetchUpdateDept } from '@/api/system-manage'

  type DeptTreeItem = Api.SystemManage.DeptTreeItem
  type DeptSaveParams = Api.SystemManage.DeptSaveParams

  interface TreeOption {
    value: number
    label: string
    children?: TreeOption[]
  }

  interface Props {
    visible: boolean
    dialogType: 'add' | 'edit'
    deptData?: DeptTreeItem
    parentId?: number
    deptTree: DeptTreeItem[]
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    dialogType: 'add',
    deptData: undefined,
    parentId: 0,
    deptTree: () => []
  })

  const emit = defineEmits<Emits>()
  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  const form = reactive<DeptSaveParams>({
    deptId: undefined,
    parentId: 0,
    deptName: '',
    orderNum: 1,
    leader: '',
    phone: '',
    email: '',
    enabled: true
  })

  const rules = reactive<FormRules>({
    deptName: [
      { required: true, message: '请输入部门名称', trigger: 'blur' },
      { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
    ],
    orderNum: [{ required: true, message: '请输入显示排序', trigger: 'change' }]
  })

  const dialogTitle = computed(() => (props.dialogType === 'add' ? '新增部门' : '编辑部门'))

  const parentOptions = computed<TreeOption[]>(() => [
    {
      value: 0,
      label: '顶级部门',
      children: buildParentOptions(props.deptTree)
    }
  ])

  watch(
    () => props.visible,
    (visible) => {
      if (visible) initForm()
    }
  )

  watch(
    () => props.deptData,
    () => {
      if (props.visible) initForm()
    },
    { deep: true }
  )

  const buildParentOptions = (departments: DeptTreeItem[]): TreeOption[] => {
    return departments
      .filter((item) => item.deptId !== form.deptId)
      .map((item) => ({
        value: item.deptId,
        label: item.deptName,
        children: item.children?.length ? buildParentOptions(item.children) : undefined
      }))
  }

  const initForm = () => {
    if (props.dialogType === 'edit' && props.deptData) {
      Object.assign(form, {
        deptId: props.deptData.deptId,
        parentId: props.deptData.parentId ?? 0,
        deptName: props.deptData.deptName,
        orderNum: props.deptData.orderNum || 1,
        leader: props.deptData.leader || '',
        phone: props.deptData.phone || '',
        email: props.deptData.email || '',
        enabled: props.deptData.enabled
      })
    } else {
      Object.assign(form, {
        deptId: undefined,
        parentId: props.parentId ?? 0,
        deptName: '',
        orderNum: 1,
        leader: '',
        phone: '',
        email: '',
        enabled: true
      })
    }
    nextTick(() => formRef.value?.clearValidate())
  }

  const resetForm = () => {
    formRef.value?.resetFields()
  }

  const handleClose = () => {
    emit('update:visible', false)
  }

  const handleSubmit = async () => {
    if (!formRef.value) return

    submitting.value = true
    try {
      await formRef.value.validate()
      if (props.dialogType === 'add') {
        await fetchCreateDept({ ...form })
      } else {
        await fetchUpdateDept({ ...form })
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      emit('success')
      handleClose()
    } finally {
      submitting.value = false
    }
  }
</script>
