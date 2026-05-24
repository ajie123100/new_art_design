/**
 * 表格全局配置模块
 *
 * 提供表格与后端接口的字段映射配置
 *
 * ## 主要功能
 *
 * - 固定使用后端分页响应字段
 * - 固定使用后端分页请求参数
 * - 请求参数字段映射配置
 *
 * ## 使用场景
 *
 * - 统一对接后端分页接口格式
 * - 统一前端表格组件的数据处理
 * - 减少重复的数据转换代码
 *
 * ## 配置说明
 *
 * - recordFields: 列表数据字段名
 * - totalFields: 总条数字段名
 * - currentFields: 当前页码字段名
 * - sizeFields: 每页大小字段名
 * - paginationKey: 前端发送请求时使用的分页参数名
 *
 * @module utils/table/tableConfig
 * @author Art Design Pro Team
 */
export const tableConfig = {
  // 响应数据字段映射配置，系统会从接口返回数据中按顺序查找这些字段
  // 列表数据
  recordFields: ['records'],
  // 总条数
  totalFields: ['total'],
  // 当前页码
  currentFields: ['pageNum'],
  // 每页大小
  sizeFields: ['pageSize'],

  // 请求参数映射配置，前端发送请求时使用的分页参数名
  // useTable 组合式函数请求后端时固定使用 pageNum 跟 pageSize
  paginationKey: {
    // 当前页码
    current: 'pageNum',
    // 每页大小
    size: 'pageSize'
  }
}
