/**
 * 联调测试账号（可整目录删除：frontend/src/dev/test-accounts）
 * 与 docs/deployment-linux-host.md §14、icoolkj 种子一致。
 */
export const TEST_PASSWORD = 'StudyOnly@2026'

export type TestAccountDef = {
  tenantCode: string
  username: string
  labelZh: string
  labelEn: string
}

export type TestAccountGroupDef = {
  id: string
  titleZh: string
  titleEn: string
  noteZh?: string
  noteEn?: string
  accounts: TestAccountDef[]
}

export const TEST_ACCOUNT_GROUPS: TestAccountGroupDef[] = [
  {
    id: 'baseline',
    titleZh: '基线租户',
    titleEn: 'Baseline tenants',
    accounts: [
      {
        tenantCode: 'platform',
        username: 'admin',
        labelZh: '平台超级管理员',
        labelEn: 'Platform super admin',
      },
      {
        tenantCode: 'default',
        username: 'tenant_admin',
        labelZh: '默认企业租户管理员',
        labelEn: 'Default enterprise tenant admin',
      },
      {
        tenantCode: 'demo_org',
        username: 'org_admin',
        labelZh: '演示单位租户管理员',
        labelEn: 'Demo organization tenant admin',
      },
    ],
  },
  {
    id: 'icoolkj',
    titleZh: 'icoolkj有限公司 · 工作流联调',
    titleEn: 'icoolkj Co. workflow lab',
    noteZh: '通用发起 icoolkj_fe01，报价 icoolkj_sales01，抄送 icoolkj_cc01',
    noteEn: 'Start: fe01 / sales01; CC: cc01',
    accounts: [
      { tenantCode: 'icoolkj', username: 'icoolkj_admin', labelZh: '租户管理员 · 林志远', labelEn: 'Tenant admin · Lin' },
      { tenantCode: 'icoolkj', username: 'icoolkj_ceo', labelZh: '总经理 · 张伟', labelEn: 'CEO · Zhang Wei' },
      { tenantCode: 'icoolkj', username: 'icoolkj_cc01', labelZh: '抄送 · 孙雨欣', labelEn: 'CC · Sun' },
      { tenantCode: 'icoolkj', username: 'icoolkj_fe_lead', labelZh: '前端组长 · 刘晓彤', labelEn: 'FE lead · Liu' },
      { tenantCode: 'icoolkj', username: 'icoolkj_fe01', labelZh: '前端 · 李明轩（常发起）', labelEn: 'FE · Li (starter)' },
      { tenantCode: 'icoolkj', username: 'icoolkj_hr_lead', labelZh: 'HR经理 · 周雅琴', labelEn: 'HR · Zhou' },
      { tenantCode: 'icoolkj', username: 'icoolkj_fin_lead', labelZh: '财务经理 · 吴嘉慧', labelEn: 'Finance · Wu' },
      { tenantCode: 'icoolkj', username: 'icoolkj_adm_lead', labelZh: '行政经理 · 郑立群', labelEn: 'Admin · Zheng' },
      { tenantCode: 'icoolkj', username: 'icoolkj_legal_lead', labelZh: '法务经理 · 马文博', labelEn: 'Legal · Ma' },
      { tenantCode: 'icoolkj', username: 'icoolkj_it_lead', labelZh: 'IT经理 · 何志强', labelEn: 'IT · He' },
      { tenantCode: 'icoolkj', username: 'icoolkj_tech_dir', labelZh: '技术总监 · 杨启明', labelEn: 'Tech Dir · Yang' },
      { tenantCode: 'icoolkj', username: 'icoolkj_pur_lead', labelZh: '采购经理 · 冯晓薇', labelEn: 'Purchase · Feng' },
      { tenantCode: 'icoolkj', username: 'icoolkj_wh_lead', labelZh: '仓储主管 · 曹国栋', labelEn: 'Warehouse · Cao' },
      { tenantCode: 'icoolkj', username: 'icoolkj_sales_lead', labelZh: '销售主管 · 沈悦', labelEn: 'Sales lead · Shen' },
      { tenantCode: 'icoolkj', username: 'icoolkj_sales_mgr', labelZh: '销售经理 · 韩立成', labelEn: 'Sales mgr · Han' },
      { tenantCode: 'icoolkj', username: 'icoolkj_sales01', labelZh: '销售 · 朱明辉（报价发起）', labelEn: 'Sales · Zhu (quote)' },
      { tenantCode: 'icoolkj', username: 'icoolkj_pd01', labelZh: '产品 · 钱雨桐（合同发起）', labelEn: 'Product · Qian (contract)' },
      { tenantCode: 'icoolkj', username: 'icoolkj_rd_lead', labelZh: '研发负责人 · 陈浩然', labelEn: 'R&D · Chen' },
      { tenantCode: 'icoolkj', username: 'icoolkj_be_lead', labelZh: '后端组长 · 王俊杰', labelEn: 'BE lead · Wang' },
    ],
  },
]

/** 后期下线：删除本目录并去掉 router 中 registerDevTestAccountRoutes 调用即可 */
export const DEV_TEST_ACCOUNTS_PATH = '/dev/test-accounts'
