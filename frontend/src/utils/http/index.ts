/**
 * HTTP 请求封装模块
 * 基于 Axios 封装的 HTTP 请求工具，提供统一的请求/响应处理
 *
 * ## 主要功能
 *
 * - 请求/响应拦截器（自动添加 Token、统一错误处理）
 * - 401 未授权自动登出（带防抖机制）
 * - 请求失败自动重试（可配置）
 * - 统一的成功/错误消息提示
 * - 支持 GET/POST/PUT/DELETE 等常用方法
 *
 * @module utils/http
 * @author Art Design Pro Team
 */

import axios, { AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/store/modules/user'
import { ApiStatus } from './status'
import { HttpError, handleError, showError, showSuccess } from './error'
import { $t } from '@/locales'
import { BaseResponse } from '@/types'

/** 请求配置常量 */
const REQUEST_TIMEOUT = 15000
const LOGOUT_DELAY = 500
const MAX_RETRIES = 0
const RETRY_DELAY = 1000
const UNAUTHORIZED_DEBOUNCE_TIME = 3000
const REFRESH_TOKEN_URL = '/api/auth/refresh'

/** 401防抖状态 */
let isUnauthorizedErrorShown = false
let unauthorizedTimer: NodeJS.Timeout | null = null
let refreshTokenPromise: Promise<string> | null = null

/** 扩展 AxiosRequestConfig */
interface ExtendedAxiosRequestConfig extends AxiosRequestConfig {
  showErrorMessage?: boolean
  showSuccessMessage?: boolean
  skipAuthRefresh?: boolean
  rawResponse?: boolean
  _retry?: boolean
}

const { VITE_API_URL, VITE_WITH_CREDENTIALS } = import.meta.env

/** Axios实例 */
const axiosInstance = axios.create({
  timeout: REQUEST_TIMEOUT,
  baseURL: VITE_API_URL,
  withCredentials: VITE_WITH_CREDENTIALS === 'true',
  validateStatus: (status) => status >= 200 && status < 300,
  transformResponse: [
    (data, headers) => {
      const contentType = headers['content-type']
      if (contentType?.includes('application/json')) {
        try {
          return JSON.parse(data)
        } catch {
          return data
        }
      }
      return data
    }
  ]
})

/** 请求拦截器 */
axiosInstance.interceptors.request.use(
  (request: InternalAxiosRequestConfig) => {
    const { accessToken } = useUserStore()
    if (accessToken) request.headers.set('Authorization', accessToken)

    if (
      request.data &&
      typeof request.data !== 'string' &&
      !(request.data instanceof FormData) &&
      !request.headers['Content-Type']
    ) {
      request.headers.set('Content-Type', 'application/json')
      request.data = JSON.stringify(request.data)
    }

    return request
  },
  (error) => {
    showError(createHttpError($t('httpMsg.requestConfigError'), ApiStatus.error))
    return Promise.reject(error)
  }
)

/** 响应拦截器 */
axiosInstance.interceptors.response.use(
  async (response: AxiosResponse<BaseResponse>) => {
    if ((response.config as ExtendedAxiosRequestConfig).rawResponse) {
      return response
    }
    const { code, msg } = response.data
    if (code === ApiStatus.success) return response
    if (code === ApiStatus.unauthorized) return handleUnauthorizedResponse(response, msg)
    throw createHttpError(msg || $t('httpMsg.requestFailed'), code)
  },
  async (error) => {
    if (error.response?.status === ApiStatus.unauthorized) {
      return handleUnauthorizedRequest(error.config, error.response?.data?.msg)
    }
    return Promise.reject(handleError(error))
  }
)

/** 统一创建HttpError */
function createHttpError(message: string, code: number) {
  return new HttpError(message, code)
}

/** 处理业务401响应 */
async function handleUnauthorizedResponse(
  response: AxiosResponse<BaseResponse>,
  message?: string
): Promise<AxiosResponse<BaseResponse>> {
  return handleUnauthorizedRequest(response.config, message)
}

/** 处理401并尝试刷新令牌 */
async function handleUnauthorizedRequest(
  config?: InternalAxiosRequestConfig | AxiosRequestConfig,
  message?: string
): Promise<AxiosResponse<BaseResponse>> {
  const requestConfig = config as ExtendedAxiosRequestConfig | undefined
  if (!requestConfig || requestConfig._retry || requestConfig.skipAuthRefresh) {
    throwUnauthorizedError(message)
  }

  const userStore = useUserStore()
  if (!userStore.refreshToken) {
    throwUnauthorizedError(message)
  }

  requestConfig._retry = true

  try {
    const newToken = await refreshAccessToken()
    setAuthorizationHeader(requestConfig, newToken)
    return axiosInstance.request<BaseResponse>(requestConfig)
  } catch {
    throwUnauthorizedError(message)
  }
}

/** 刷新访问令牌，多个401共用同一个刷新请求 */
async function refreshAccessToken(): Promise<string> {
  if (!refreshTokenPromise) {
    const userStore = useUserStore()
    refreshTokenPromise = axiosInstance
      .post<BaseResponse<Api.Auth.LoginResponse>>(
        REFRESH_TOKEN_URL,
        { refreshToken: userStore.refreshToken },
        {
          skipAuthRefresh: true,
          showErrorMessage: false
        } as ExtendedAxiosRequestConfig
      )
      .then((response) => {
        const tokenInfo = response.data.data
        if (!tokenInfo?.token || !tokenInfo.refreshToken) {
          throw createHttpError($t('httpMsg.unauthorized'), ApiStatus.unauthorized)
        }
        userStore.setToken(tokenInfo.token, tokenInfo.refreshToken)
        return tokenInfo.token
      })
      .finally(() => {
        refreshTokenPromise = null
      })
  }

  return refreshTokenPromise
}

/** 设置请求头中的访问令牌 */
function setAuthorizationHeader(config: ExtendedAxiosRequestConfig, token: string) {
  if (!config.headers) {
    config.headers = {}
  }
  if (typeof (config.headers as any).set === 'function') {
    ;(config.headers as any).set('Authorization', token)
  } else {
    ;(config.headers as Record<string, string>).Authorization = token
  }
}

/** 处理无法恢复的401错误（带防抖） */
function throwUnauthorizedError(message?: string): never {
  const error = createHttpError(message || $t('httpMsg.unauthorized'), ApiStatus.unauthorized)

  if (!isUnauthorizedErrorShown) {
    isUnauthorizedErrorShown = true
    logOut()

    unauthorizedTimer = setTimeout(resetUnauthorizedError, UNAUTHORIZED_DEBOUNCE_TIME)

    showError(error, true)
    throw error
  }

  throw error
}

/** 重置401防抖状态 */
function resetUnauthorizedError() {
  isUnauthorizedErrorShown = false
  if (unauthorizedTimer) clearTimeout(unauthorizedTimer)
  unauthorizedTimer = null
}

/** 退出登录函数 */
function logOut() {
  setTimeout(() => {
    useUserStore().logOut()
  }, LOGOUT_DELAY)
}

/** 是否需要重试 */
function shouldRetry(statusCode: number) {
  return [
    ApiStatus.requestTimeout,
    ApiStatus.internalServerError,
    ApiStatus.badGateway,
    ApiStatus.serviceUnavailable,
    ApiStatus.gatewayTimeout
  ].includes(statusCode)
}

/** 请求重试逻辑 */
async function retryRequest<T>(
  config: ExtendedAxiosRequestConfig,
  retries: number = MAX_RETRIES
): Promise<T> {
  try {
    return await request<T>(config)
  } catch (error) {
    if (retries > 0 && error instanceof HttpError && shouldRetry(error.code)) {
      await delay(RETRY_DELAY)
      return retryRequest<T>(config, retries - 1)
    }
    throw error
  }
}

/** 延迟函数 */
function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/** 请求函数 */
async function request<T = any>(config: ExtendedAxiosRequestConfig): Promise<T> {
  // POST | PUT 参数自动填充
  if (
    ['POST', 'PUT'].includes(config.method?.toUpperCase() || '') &&
    config.params &&
    !config.data
  ) {
    config.data = config.params
    config.params = undefined
  }

  try {
    const res = await axiosInstance.request<BaseResponse<T>>(config)

    // 显示成功消息
    if (config.showSuccessMessage && res.data.msg) {
      showSuccess(res.data.msg)
    }

    return res.data.data as T
  } catch (error) {
    if (error instanceof HttpError && error.code !== ApiStatus.unauthorized) {
      const showMsg = config.showErrorMessage !== false
      showError(error, showMsg)
    }
    return Promise.reject(error)
  }
}

function resolveDownloadFilename(response: AxiosResponse, fallback: string): string {
  const disposition = response.headers['content-disposition']
  if (!disposition) return fallback

  const encodedMatch = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (encodedMatch?.[1]) {
    return decodeURIComponent(encodedMatch[1])
  }

  const match = disposition.match(/filename="?([^";]+)"?/i)
  return match?.[1] ? decodeURIComponent(match[1]) : fallback
}

function saveBlob(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

async function download(config: ExtendedAxiosRequestConfig & { filename?: string }): Promise<void> {
  try {
    const response = await axiosInstance.request<Blob>({
      ...config,
      method: config.method || 'GET',
      responseType: 'blob',
      rawResponse: true
    } as ExtendedAxiosRequestConfig)

    const filename = resolveDownloadFilename(response, config.filename || 'download')
    saveBlob(response.data, filename)
  } catch (error) {
    if (error instanceof HttpError && error.code !== ApiStatus.unauthorized) {
      const showMsg = config.showErrorMessage !== false
      showError(error, showMsg)
    }
    return Promise.reject(error)
  }
}

/** API方法集合 */
const api = {
  get<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'GET' })
  },
  post<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'POST' })
  },
  put<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'PUT' })
  },
  del<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'DELETE' })
  },
  request<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>(config)
  },
  download
}

export default api
