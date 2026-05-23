<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
    width="420px"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="86px">
      <ElFormItem label="用户名" prop="userName">
        <ElInput v-model.trim="formData.userName" placeholder="请输入用户名" />
      </ElFormItem>
      <ElFormItem label="昵称" prop="nickName">
        <ElInput v-model.trim="formData.nickName" placeholder="请输入昵称" />
      </ElFormItem>
      <ElFormItem v-if="dialogType === 'add'" label="密码" prop="password">
        <ElInput v-model.trim="formData.password" placeholder="请输入密码" show-password />
      </ElFormItem>
      <ElFormItem label="手机号" prop="userPhone">
        <ElInput v-model.trim="formData.userPhone" placeholder="请输入手机号" />
      </ElFormItem>
      <ElFormItem label="邮箱" prop="userEmail">
        <ElInput v-model.trim="formData.userEmail" placeholder="请输入邮箱" />
      </ElFormItem>
      <ElFormItem label="性别" prop="userGender">
        <ElSelect v-model="formData.userGender">
          <ElOption label="男" value="男" />
          <ElOption label="女" value="女" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="状态" prop="status">
        <ElSelect v-model="formData.status">
          <ElOption label="正常" value="1" />
          <ElOption label="停用" value="2" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="角色" prop="userRoles">
        <ElSelect v-model="formData.userRoles" multiple placeholder="请选择角色">
          <ElOption
            v-for="role in roleList"
            :key="role.roleCode"
            :value="role.roleCode"
            :label="role.roleName"
          />
        </ElSelect>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchGetRoleList } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    type: string
    userData?: Partial<Api.SystemManage.UserListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', value: Api.SystemManage.UserSaveParams): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const roleList = ref<Api.SystemManage.RoleListItem[]>([])
  const submitting = ref(false)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)
  const formRef = ref<FormInstance>()

  const formData = reactive<Api.SystemManage.UserSaveParams>({
    id: undefined,
    userName: '',
    nickName: '',
    password: '123456',
    userPhone: '',
    userEmail: '',
    userGender: '男',
    status: '1',
    userRoles: []
  })

  const rules: FormRules = {
    userName: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    userPhone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
    userEmail: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
    userRoles: [{ required: true, message: '请选择角色', trigger: 'change' }]
  }

  const loadRoles = async () => {
    const result = await fetchGetRoleList({ current: 1, size: 100 })
    roleList.value = result.records
  }

  const initFormData = () => {
    const isEdit = props.type === 'edit' && props.userData
    const row = props.userData

    Object.assign(formData, {
      id: isEdit ? row?.id : undefined,
      userName: isEdit ? row?.userName || '' : '',
      nickName: isEdit ? row?.nickName || '' : '',
      password: isEdit ? undefined : '123456',
      userPhone: isEdit ? row?.userPhone || '' : '',
      userEmail: isEdit ? row?.userEmail || '' : '',
      userGender: isEdit ? row?.userGender || '男' : '男',
      status: isEdit ? row?.status || '1' : '1',
      userRoles: isEdit && Array.isArray(row?.userRoles) ? [...row.userRoles] : []
    })
  }

  watch(
    () => [props.visible, props.type, props.userData],
    async ([visible]) => {
      if (visible) {
        initFormData()
        if (!roleList.value.length) {
          await loadRoles()
        }
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return

    submitting.value = true
    try {
      await formRef.value.validate()
      emit('submit', { ...formData, userRoles: [...(formData.userRoles || [])] })
    } finally {
      submitting.value = false
    }
  }
</script>
