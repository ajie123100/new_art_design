<template>
  <ElDialog
    :model-value="visible"
    :title="dialogType === 'add' ? '新增字典数据' : '编辑字典数据'"
    width="640px"
    align-center
    @update:model-value="handleClose"
    @closed="resetForm"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElRow :gutter="16">
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="字典类型" prop="dictType">
            <ElInput v-model.trim="form.dictType" placeholder="请输入字典类型" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="显示排序" prop="dictSort">
            <ElInputNumber v-model="form.dictSort" :min="0" controls-position="right" style="width: 100%" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="数据标签" prop="dictLabel">
            <ElInput v-model.trim="form.dictLabel" placeholder="请输入数据标签" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="数据键值" prop="dictValue">
            <ElInput v-model.trim="form.dictValue" placeholder="请输入数据键值" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="回显样式">
            <ElInput v-model.trim="form.listClass" placeholder="如 primary/success/warning" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="CSS类名">
            <ElInput v-model.trim="form.cssClass" placeholder="请输入 CSS 类名" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="默认值">
            <ElSwitch v-model="form.defaultValue" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="状态">
            <ElSwitch v-model="form.enabled" active-text="启用" inactive-text="停用" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="备注">
            <ElInput v-model.trim="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
  import { fetchCreateDictData, fetchUpdateDictData } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    dialogType: 'add' | 'edit'
    row?: Api.SystemManage.DictDataListItem
    dictType?: string
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    dialogType: 'add',
    row: undefined,
    dictType: undefined
  })

  const emit = defineEmits<Emits>()
  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  const form = reactive<Api.SystemManage.DictDataSaveParams>({
    dictCode: undefined,
    dictSort: 0,
    dictLabel: '',
    dictValue: '',
    dictType: '',
    cssClass: '',
    listClass: '',
    defaultValue: false,
    enabled: true,
    remark: ''
  })

  const rules: FormRules = {
    dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
    dictSort: [{ required: true, message: '请输入显示排序', trigger: 'change' }],
    dictLabel: [{ required: true, message: '请输入数据标签', trigger: 'blur' }],
    dictValue: [{ required: true, message: '请输入数据键值', trigger: 'blur' }]
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
        dictCode: undefined,
        dictSort: 0,
        dictLabel: '',
        dictValue: '',
        dictType: props.dictType || '',
        cssClass: '',
        listClass: '',
        defaultValue: false,
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
        await fetchCreateDictData({ ...form })
      } else {
        await fetchUpdateDictData({ ...form })
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      emit('success')
      handleClose()
    } finally {
      submitting.value = false
    }
  }
</script>
