<template>
  <ElDialog
    :model-value="visible"
    :title="dialogTitle"
    width="720px"
    align-center
    @update:model-value="handleClose"
    @closed="resetForm"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElRow :gutter="16">
        <ElCol :xs="24" :sm="12">
          <ElFormItem label="上级菜单" prop="parentId">
            <ElTreeSelect
              v-model="form.parentId"
              :data="parentOptions"
              check-strictly
              default-expand-all
              node-key="value"
              :props="{ label: 'label', value: 'value', children: 'children' }"
              placeholder="请选择上级菜单"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="菜单类型" prop="menuType">
            <ElRadioGroup v-model="form.menuType">
              <ElRadioButton value="M">目录</ElRadioButton>
              <ElRadioButton value="C">菜单</ElRadioButton>
              <ElRadioButton value="F">按钮</ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="菜单名称" prop="menuName">
            <ElInput v-model.trim="form.menuName" placeholder="请输入菜单名称" />
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

        <ElCol v-if="form.menuType !== 'F'" :xs="24" :sm="12">
          <ElFormItem label="路由地址" prop="path">
            <ElInput v-model.trim="form.path" placeholder="如 /system 或 user" />
          </ElFormItem>
        </ElCol>

        <ElCol v-if="form.menuType !== 'F'" :xs="24" :sm="12">
          <ElFormItem label="路由名称" prop="routeName">
            <ElInput v-model.trim="form.routeName" placeholder="如 SystemUser" />
          </ElFormItem>
        </ElCol>

        <ElCol v-if="form.menuType !== 'F'" :xs="24" :sm="12">
          <ElFormItem label="组件路径" prop="component">
            <ElInput v-model.trim="form.component" placeholder="如 /system/user 或 /index/index" />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="权限标识" prop="perms">
            <ElInput v-model.trim="form.perms" placeholder="如 system:menu:add" />
          </ElFormItem>
        </ElCol>

        <ElCol v-if="form.menuType !== 'F'" :xs="24" :sm="12">
          <ElFormItem label="菜单图标" prop="icon">
            <ElInput v-model.trim="form.icon" placeholder="如 ri:menu-line" />
          </ElFormItem>
        </ElCol>

        <ElCol :xs="24" :sm="12">
          <ElFormItem label="菜单状态">
            <ElSwitch v-model="form.enabled" active-text="启用" inactive-text="停用" />
          </ElFormItem>
        </ElCol>

        <ElCol v-if="form.menuType !== 'F'" :xs="24" :sm="12">
          <ElFormItem label="显示菜单">
            <ElSwitch v-model="form.visible" active-text="显示" inactive-text="隐藏" />
          </ElFormItem>
        </ElCol>

        <ElCol v-if="form.menuType !== 'F'" :xs="24" :sm="12">
          <ElFormItem label="页面缓存">
            <ElSwitch v-model="form.keepAlive" active-text="缓存" inactive-text="不缓存" />
          </ElFormItem>
        </ElCol>

        <ElCol v-if="form.menuType !== 'F'" :xs="24" :sm="12">
          <ElFormItem label="是否外链">
            <ElSwitch v-model="form.external" />
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
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchCreateSystemMenu, fetchUpdateSystemMenu } from '@/api/system-manage'

  type MenuTreeItem = Api.SystemManage.MenuTreeItem
  type MenuSaveParams = Api.SystemManage.MenuSaveParams

  interface TreeOption {
    value: number
    label: string
    children?: TreeOption[]
  }

  interface Props {
    visible: boolean
    dialogType: 'add' | 'edit'
    menuData?: MenuTreeItem
    parentId?: number
    defaultMenuType?: 'M' | 'C' | 'F'
    menuTree: MenuTreeItem[]
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    dialogType: 'add',
    menuData: undefined,
    parentId: 0,
    defaultMenuType: undefined,
    menuTree: () => []
  })

  const emit = defineEmits<Emits>()
  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  const form = reactive<MenuSaveParams>({
    menuId: undefined,
    parentId: 0,
    menuName: '',
    orderNum: 1,
    path: '',
    component: '',
    routeName: '',
    external: false,
    keepAlive: true,
    menuType: 'C',
    visible: true,
    enabled: true,
    perms: '',
    icon: '',
    remark: ''
  })

  const validatePath = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (form.menuType !== 'F' && !value) {
      callback(new Error('请输入路由地址'))
      return
    }
    callback()
  }

  const rules = reactive<FormRules>({
    menuName: [
      { required: true, message: '请输入菜单名称', trigger: 'blur' },
      { min: 2, max: 80, message: '长度在 2 到 80 个字符', trigger: 'blur' }
    ],
    orderNum: [{ required: true, message: '请输入显示排序', trigger: 'change' }],
    menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
    path: [{ validator: validatePath, trigger: 'blur' }]
  })

  const dialogTitle = computed(() => (props.dialogType === 'add' ? '新增菜单' : '编辑菜单'))

  const parentOptions = computed<TreeOption[]>(() => [
    {
      value: 0,
      label: '顶级菜单',
      children: buildParentOptions(props.menuTree)
    }
  ])

  watch(
    () => props.visible,
    (visible) => {
      if (visible) initForm()
    }
  )

  watch(
    () => props.menuData,
    () => {
      if (props.visible) initForm()
    },
    { deep: true }
  )

  const buildParentOptions = (menus: MenuTreeItem[]): TreeOption[] => {
    return menus
      .filter((item) => item.menuType !== 'F' && item.menuId !== form.menuId)
      .map((item) => ({
        value: item.menuId,
        label: item.menuName,
        children: item.children?.length ? buildParentOptions(item.children) : undefined
      }))
  }

  const initForm = () => {
    if (props.dialogType === 'edit' && props.menuData) {
      Object.assign(form, {
        menuId: props.menuData.menuId,
        parentId: props.menuData.parentId ?? 0,
        menuName: props.menuData.menuName,
        orderNum: props.menuData.orderNum || 1,
        path: props.menuData.path || '',
        component: props.menuData.component || '',
        routeName: props.menuData.routeName || '',
        external: props.menuData.external,
        keepAlive: props.menuData.keepAlive,
        menuType: props.menuData.menuType,
        visible: props.menuData.visible,
        enabled: props.menuData.enabled,
        perms: props.menuData.perms || '',
        icon: props.menuData.icon || '',
        remark: props.menuData.remark || ''
      })
    } else {
      Object.assign(form, {
        menuId: undefined,
        parentId: props.parentId ?? 0,
        menuName: '',
        orderNum: 1,
        path: '',
        component: '',
        routeName: '',
        external: false,
        keepAlive: true,
        menuType: props.defaultMenuType || (props.parentId ? 'C' : 'M'),
        visible: true,
        enabled: true,
        perms: '',
        icon: '',
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
      const payload = { ...form }
      if (payload.menuType === 'F') {
        payload.path = ''
        payload.component = ''
        payload.routeName = ''
        payload.icon = ''
        payload.external = false
        payload.keepAlive = false
        payload.visible = true
      }

      if (props.dialogType === 'add') {
        await fetchCreateSystemMenu(payload)
      } else {
        await fetchUpdateSystemMenu(payload)
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      emit('success')
      handleClose()
    } finally {
      submitting.value = false
    }
  }
</script>
