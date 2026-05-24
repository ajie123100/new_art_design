<template>
  <ElDialog
    :model-value="visible"
    :title="dialogType === 'add' ? '新增参数' : '编辑参数'"
    width="560px"
    align-center
    @update:model-value="handleClose"
    @closed="resetForm"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElFormItem label="参数名称" prop="configName">
        <ElInput v-model.trim="form.configName" placeholder="请输入参数名称" />
      </ElFormItem>
      <ElFormItem label="参数键名" prop="configKey">
        <ElInput v-model.trim="form.configKey" placeholder="请输入参数键名" />
      </ElFormItem>
      <ElFormItem label="参数键值" prop="configValue">
        <ElInput v-model.trim="form.configValue" placeholder="请输入参数键值" />
      </ElFormItem>
      <ElFormItem label="系统内置">
        <ElRadioGroup v-model="form.configType">
          <ElRadioButton value="Y">是</ElRadioButton>
          <ElRadioButton value="N">否</ElRadioButton>
        </ElRadioGroup>
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
  import { fetchCreateConfig, fetchUpdateConfig } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    dialogType: 'add' | 'edit'
    row?: Api.SystemManage.ConfigListItem
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

  const form = reactive<Api.SystemManage.ConfigSaveParams>({
    configId: undefined,
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'N',
    remark: ''
  })

  const rules: FormRules = {
    configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
    configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
    configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) initForm()
    }
  )

  const initForm = () => {
    if (props.dialogType === 'edit' && props.row) {
      Object.assign(form, {
        configId: props.row.configId,
        configName: props.row.configName,
        configKey: props.row.configKey,
        configValue: props.row.configValue,
        configType: props.row.configType || 'N',
        remark: ''
      })
    } else {
      Object.assign(form, {
        configId: undefined,
        configName: '',
        configKey: '',
        configValue: '',
        configType: 'N',
        remark: ''
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
        await fetchCreateConfig({ ...form })
      } else {
        await fetchUpdateConfig({ ...form })
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      emit('success')
      handleClose()
    } finally {
      submitting.value = false
    }
  }
</script>
