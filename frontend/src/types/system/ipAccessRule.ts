/** AUTO-GENERATED — IP访问规则 类型 */

export interface IpAccessRuleVO {
  id?: string
  ruleName?: string
  ipAddress?: string
  matchType?: string
  ruleType?: string
  applyScope?: string
  priorityNo?: number
  status?: string
  startAt?: string
  endAt?: string
  remark?: string
}

export interface IpAccessRulePageQuery {
  pageNo?: number
  pageSize?: number
  ruleName?: string
  ipAddress?: string
  matchType?: string
  ruleType?: string
  applyScope?: string
  priorityNo?: number
  status?: string
  startAt?: string
  endAt?: string
  remark?: string
}

export interface IpAccessRuleForm {
  id?: string
  ruleName?: string
  ipAddress?: string
  matchType?: string
  ruleType?: string
  applyScope?: string
  priorityNo?: number
  status?: string
  startAt?: string
  endAt?: string
  remark?: string
}
