import request from '@/utils/http'
import { AppRouteRecord } from '@/types/router'

// 获取用户列表
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get<Api.SystemManage.UserList>({
    url: '/api/user/list',
    params
  })
}

// 获取用户详情
export function fetchGetUserDetail(id: number) {
  return request.get<Api.SystemManage.UserListItem>({
    url: `/api/user/${id}`
  })
}

// 新增用户
export function fetchCreateUser(params: Api.SystemManage.UserSaveParams) {
  return request.post<number>({
    url: '/api/user',
    params,
    showSuccessMessage: true
  })
}

// 更新用户
export function fetchUpdateUser(params: Api.SystemManage.UserSaveParams) {
  return request.put<void>({
    url: '/api/user',
    params,
    showSuccessMessage: true
  })
}

// 删除用户
export function fetchDeleteUser(ids: number | number[]) {
  const pathIds = Array.isArray(ids) ? ids.join(',') : ids
  return request.del<void>({
    url: `/api/user/${pathIds}`,
    showSuccessMessage: true
  })
}

// 修改用户状态
export function fetchUpdateUserStatus(
  params: Pick<Api.SystemManage.UserSaveParams, 'id' | 'status'>
) {
  return request.put<void>({
    url: '/api/user/status',
    params,
    showSuccessMessage: true
  })
}

// 重置用户密码
export function fetchResetUserPassword(params: { id: number; password: string }) {
  return request.put<void>({
    url: '/api/user/reset-password',
    params,
    showSuccessMessage: true
  })
}

// 获取角色列表
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.get<Api.SystemManage.RoleList>({
    url: '/api/role/list',
    params
  })
}

// 获取角色详情
export function fetchGetRoleDetail(id: number) {
  return request.get<Api.SystemManage.RoleListItem>({
    url: `/api/role/${id}`
  })
}

// 新增角色
export function fetchCreateRole(params: Api.SystemManage.RoleSaveParams) {
  return request.post<number>({
    url: '/api/role',
    params,
    showSuccessMessage: true
  })
}

// 更新角色
export function fetchUpdateRole(params: Api.SystemManage.RoleSaveParams) {
  return request.put<void>({
    url: '/api/role',
    params,
    showSuccessMessage: true
  })
}

// 删除角色
export function fetchDeleteRole(ids: number | number[]) {
  const pathIds = Array.isArray(ids) ? ids.join(',') : ids
  return request.del<void>({
    url: `/api/role/${pathIds}`,
    showSuccessMessage: true
  })
}

// 修改角色状态
export function fetchUpdateRoleStatus(
  params: Pick<Api.SystemManage.RoleSaveParams, 'roleId' | 'enabled'>
) {
  return request.put<void>({
    url: '/api/role/status',
    params,
    showSuccessMessage: true
  })
}

// 获取角色授权菜单树
export function fetchGetRoleMenuTree() {
  return request.get<AppRouteRecord[]>({
    url: '/api/role/menu-tree'
  })
}

// 获取角色已授权菜单 ID
export function fetchGetRoleMenuIds(roleId: number) {
  return request.get<number[]>({
    url: `/api/role/${roleId}/menu-ids`
  })
}

// 保存角色菜单授权
export function fetchSaveRoleMenus(params: Api.SystemManage.RoleMenuParams) {
  return request.put<void>({
    url: '/api/role/menu',
    params,
    showSuccessMessage: true
  })
}

// 获取菜单列表
export function fetchGetMenuList() {
  return request.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus'
  })
}

// 获取系统菜单管理树
export function fetchGetSystemMenuList(params?: Api.SystemManage.MenuSearchParams) {
  return request.get<Api.SystemManage.MenuTreeItem[]>({
    url: '/api/menu/list',
    params
  })
}

// 获取系统菜单详情
export function fetchGetSystemMenuDetail(id: number) {
  return request.get<Api.SystemManage.MenuTreeItem>({
    url: `/api/menu/${id}`
  })
}

// 新增系统菜单
export function fetchCreateSystemMenu(params: Api.SystemManage.MenuSaveParams) {
  return request.post<number>({
    url: '/api/menu',
    params,
    showSuccessMessage: true
  })
}

// 更新系统菜单
export function fetchUpdateSystemMenu(params: Api.SystemManage.MenuSaveParams) {
  return request.put<void>({
    url: '/api/menu',
    params,
    showSuccessMessage: true
  })
}

// 删除系统菜单
export function fetchDeleteSystemMenu(ids: number | number[]) {
  const pathIds = Array.isArray(ids) ? ids.join(',') : ids
  return request.del<void>({
    url: `/api/menu/${pathIds}`,
    showSuccessMessage: true
  })
}

// 修改系统菜单状态
export function fetchUpdateSystemMenuStatus(params: Pick<Api.SystemManage.MenuSaveParams, 'menuId'> & {
  enabled: boolean
}) {
  return request.put<void>({
    url: '/api/menu/status',
    params,
    showSuccessMessage: true
  })
}

// 获取部门列表
export function fetchGetDeptList(params?: Api.SystemManage.DeptSearchParams) {
  return request.get<Api.SystemManage.DeptTreeItem[]>({
    url: '/api/dept/list',
    params
  })
}

// 获取部门详情
export function fetchGetDeptDetail(id: number) {
  return request.get<Api.SystemManage.DeptTreeItem>({
    url: `/api/dept/${id}`
  })
}

// 新增部门
export function fetchCreateDept(params: Api.SystemManage.DeptSaveParams) {
  return request.post<number>({
    url: '/api/dept',
    params,
    showSuccessMessage: true
  })
}

// 更新部门
export function fetchUpdateDept(params: Api.SystemManage.DeptSaveParams) {
  return request.put<void>({
    url: '/api/dept',
    params,
    showSuccessMessage: true
  })
}

// 删除部门
export function fetchDeleteDept(ids: number | number[]) {
  const pathIds = Array.isArray(ids) ? ids.join(',') : ids
  return request.del<void>({
    url: `/api/dept/${pathIds}`,
    showSuccessMessage: true
  })
}

// 修改部门状态
export function fetchUpdateDeptStatus(params: Pick<Api.SystemManage.DeptSaveParams, 'deptId'> & {
  enabled: boolean
}) {
  return request.put<void>({
    url: '/api/dept/status',
    params,
    showSuccessMessage: true
  })
}
