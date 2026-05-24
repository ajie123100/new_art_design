import request from '@/utils/http'

/**
 * 获取验证码
 * @returns 验证码信息
 */
export function fetchGetCaptcha() {
  return request.get<Api.Auth.CaptchaInfo>({
    url: '/api/auth/captchaImage'
  })
}

/**
 * 登录
 * @param params 登录参数
 * @returns 登录响应
 */
export function fetchLogin(params: Api.Auth.LoginParams) {
  return request.post<Api.Auth.LoginResponse>({
    url: '/api/auth/login',
    params
    // showSuccessMessage: true // 显示成功消息
    // showErrorMessage: false // 不显示错误消息
  })
}

/**
 * 刷新访问令牌
 * @param params 刷新令牌参数
 * @returns 新令牌响应
 */
export function fetchRefreshToken(params: Api.Auth.RefreshTokenParams) {
  return request.post<Api.Auth.LoginResponse>({
    url: '/api/auth/refresh',
    params,
    showErrorMessage: false
  })
}

/**
 * 退出登录
 * @param params 刷新令牌参数
 */
export function fetchLogout(params?: Partial<Api.Auth.RefreshTokenParams>) {
  return request.post<void>({
    url: '/api/auth/logout',
    params,
    showErrorMessage: false
  })
}

/**
 * 获取用户信息
 * @returns 用户信息
 */
export function fetchGetUserInfo() {
  return request.get<Api.Auth.UserInfo>({
    url: '/api/user/info'
    // 自定义请求头
    // headers: {
    //   'X-Custom-Header': 'your-custom-value'
    // }
  })
}
