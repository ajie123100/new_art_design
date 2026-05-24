<template>
  <ElDialog
    :model-value="visible"
    :title="dialogType === 'add' ? '新增字典类型' : '编辑字典类型'"
    width="560px"
    align-center
    @update:model-value="handleClose"
    @closed="resetForm"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElFormItem label="字典名称" prop="dictName">
        <ElInput v-model.trim="form.dictName" placeholder="请输入字典名称" />
      </ElFormItem>
      <ElFormItem label="字典类型" prop="dictType">
        <ElInput v-model.trim="form.dictType" placeholder="请输入字典类型" />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElSwitch v-model="form.enabled" active-text="启用" inactive-text="停用" />
      </ElFormItem>
      <ElFormItem label="备注">
        <ElInput v-model.trim="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchCreateDictType, fetchUpdateDictType } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    dialogType: 'add' | 'edit'
    row?: Api.SystemManage.DictTypeListItem
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    dialogType: 'add',
    row: undefined
  })

  const emit = defineEmits<Emits>()
  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  const form = reactive<Api.SystemManage.DictTypeSaveParams>({
    dictId: undefined,
    dictName: '',
    dictType: '',
    enabled: true,
    remark: ''
  })

  const rules: FormRules = {
    dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
    dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) initForm()
    }
  )

  const initForm = () => {
    if (props.dialogType === 'edit' && props.row) {
      Object.assign(form, { ...props.row })
    } else {
      Object.assign(form, {
        dictId: undefined,
        dictName: '',
        dictType: '',
        enabled: true,
        remark: ''
      })
    }
    nextTick(() => formRef.value?.clearValidate())
  }

  const resetForm = () => formRef.value?.resetFields()
  const handleClose = () => emit('update:visible', false)

  const handleSubmit = async () => {
    if (!formRef.value) return

    submitting.value = true
    try {
      await formRef.value.validate()
      if (props.dialogType === 'add') {
        await fetchCreateDictType({ ...form })
      } else {
        await fetchUpdateDictType({ ...form })
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      emit('success')
      handleClose()
    } finally {
      submitting.value = false
    }
  }
</script>
