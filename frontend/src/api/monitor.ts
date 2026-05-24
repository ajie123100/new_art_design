import request from '@/utils/http'

export function fetchServerInfo() {
  return request.get<Api.Monitor.ServerInfo>({ url: '/api/monitor/server' })
}

export function fetchOnlineUsers() {
  return request.get<Api.Monitor.OnlineUser[]>({ url: '/api/monitor/online/list' })
}

export function fetchForceLogout(tokenValue: string) {
  return request.del<void>({ url: `/api/monitor/online/${tokenValue}`, showSuccessMessage: true })
}

export function fetchCacheInfo() {
  return request.get<Api.Monitor.CacheInfo>({ url: '/api/monitor/cache' })
}

export function fetchCacheNames() {
  return request.get<Api.Monitor.CacheNameInfo[]>({ url: '/api/monitor/cache/names' })
}

export function fetchCacheKeys(cacheName: string) {
  return request.get<string[]>({ url: `/api/monitor/cache/keys/${cacheName}` })
}

export function fetchCacheValue(cacheName: string, key: string) {
  return request.get<unknown>({ url: `/api/monitor/cache/value/${cacheName}`, params: { key } })
}

export function fetchClearCache(key: string) {
  return request.del<void>({ url: `/api/monitor/cache/${key}`, showSuccessMessage: true })
}

export function fetchClearCacheKey(cacheName: string, key: string) {
  return request.del<void>({
    url: `/api/monitor/cache/${cacheName}/keys`,
    params: { key },
    showSuccessMessage: true
  })
}

export function fetchClearCacheName(cacheName: string) {
  return request.del<void>({
    url: `/api/monitor/cache/name/${cacheName}`,
    showSuccessMessage: true
  })
}

export function fetchDatabaseInfo() {
  return request.get<Api.Monitor.DatabaseInfo>({ url: '/api/monitor/database' })
}

export function fetchJobList(params: Api.Monitor.JobSearchParams) {
  return request.get<Api.Monitor.JobList>({ url: '/api/job/list', params })
}

export function fetchCreateJob(params: Api.Monitor.JobSaveParams) {
  return request.post<number>({ url: '/api/job', params, showSuccessMessage: true })
}

export function fetchUpdateJob(params: Api.Monitor.JobSaveParams) {
  return request.put<void>({ url: '/api/job', params, showSuccessMessage: true })
}

export function fetchDeleteJob(ids: number | number[]) {
  const pathIds = Array.isArray(ids) ? ids.join(',') : ids
  return request.del<void>({ url: `/api/job/${pathIds}`, showSuccessMessage: true })
}

export function fetchChangeJobStatus(jobId: number, status: string) {
  return request.put<void>({
    url: `/api/job/changeStatus/${jobId}/${status}`,
    showSuccessMessage: true
  })
}

export function fetchRunJob(ids: number | number[]) {
  const pathIds = Array.isArray(ids) ? ids.join(',') : ids
  return request.post<void>({ url: `/api/job/run/${pathIds}`, showSuccessMessage: true })
}

export function fetchJobLogList(params: Api.Monitor.JobLogSearchParams) {
  return request.get<Api.Monitor.JobLogList>({ url: '/api/jobLog/list', params })
}

export function fetchDeleteJobLog(ids: number | number[]) {
  const pathIds = Array.isArray(ids) ? ids.join(',') : ids
  return request.del<void>({ url: `/api/jobLog/${pathIds}`, showSuccessMessage: true })
}

export function fetchCleanJobLog() {
  return request.del<void>({ url: '/api/jobLog/clean', showSuccessMessage: true })
}

export function fetchExportJobLog(params?: Api.Monitor.JobLogSearchParams) {
  return request.download({ url: '/api/jobLog/export', params, filename: 'job_log.xlsx' })
}

export function fetchLoginLogList(params: Api.Monitor.LoginLogSearchParams) {
  return request.get<Api.Monitor.LoginLogList>({ url: '/api/logininfor/list', params })
}

export function fetchCleanLoginLog() {
  return request.del<void>({ url: '/api/logininfor/clean', showSuccessMessage: true })
}

export function fetchExportLoginLog(params?: Api.Monitor.LoginLogSearchParams) {
  return request.download({ url: '/api/logininfor/export', params, filename: 'login_log.xlsx' })
}

export function fetchOperLogList(params: Api.Monitor.OperLogSearchParams) {
  return request.get<Api.Monitor.OperLogList>({ url: '/api/operlog/list', params })
}

export function fetchCleanOperLog() {
  return request.del<void>({ url: '/api/operlog/clean', showSuccessMessage: true })
}

export function fetchExportOperLog(params?: Api.Monitor.OperLogSearchParams) {
  return request.download({ url: '/api/operlog/export', params, filename: 'oper_log.xlsx' })
}
