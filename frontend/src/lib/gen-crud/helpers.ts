/**
 * Gen CRUD 资源定义 helpers — 纯数据层，不依赖 components/gen。
 * api/*.ts 应从此处 import，避免 api → UI 框架依赖倒置。
 */
import type { GenCrudApi, GenCrudPerm, GenMasterDetailApi } from '@/types/gen/crudApi'
import type { GenCrudResource } from '@/types/gen/crudResource'

type TaskIdResponse = { data: { data: { taskId: string } } }

type StandardCrudFns = Pick<GenCrudApi, 'page'> &
  Partial<
    Pick<
      GenCrudApi,
      | 'get'
      | 'create'
      | 'update'
      | 'delete'
      | 'batchDelete'
      | 'exportFileAsync'
      | 'importFileAsync'
      | 'importFile'
      | 'exportPath'
      | 'importPath'
    >
  >

export type { StandardCrudFns }

/** 只读页占位 mutator（详情/表单不发起真实写操作） */
export const READONLY_CRUD_STUBS = {
  get: async () => ({}),
  create: async () => ({}),
  update: async () => ({}),
  delete: async () => ({}),
} as const satisfies Pick<GenCrudApi, 'get' | 'create' | 'update' | 'delete'>

/** L0+ — 构建 GenCrudApi（不发 HTTP，只做形状适配） */
export function standardCrudApi(fns: StandardCrudFns): GenCrudApi {
  return fns
}

export function wrapExportFileAsync<Q>(
  fn: (query: Q) => Promise<TaskIdResponse>,
): NonNullable<GenCrudApi['exportFileAsync']> {
  return async (query) => {
    const res = await fn(query as Q)
    return res.data.data
  }
}

export function wrapImportFileAsync(
  fn: (file: File) => Promise<TaskIdResponse>,
): NonNullable<GenCrudApi['importFileAsync']> {
  return async (file) => {
    const res = await fn(file)
    return res.data.data
  }
}

/** 定义标准单表 Gen 资源（供 api/*.ts 尾部 export 使用） */
export function defineGenCrudResource(perm: GenCrudPerm, api: StandardCrudFns): GenCrudResource {
  return { perm, api: standardCrudApi(api) }
}

/** 定义只读 Gen 资源（page + 可选 get/export 等，mutator 为 stub） */
export function defineReadOnlyGenCrudResource(
  perm: GenCrudPerm,
  page: StandardCrudFns['page'],
  extras?: Partial<StandardCrudFns>,
): GenCrudResource {
  return defineGenCrudResource(perm, { page, ...READONLY_CRUD_STUBS, ...extras })
}

/** 定义主从 Gen 资源 */
export function defineMasterDetailGenCrudResource(
  perm: GenCrudPerm,
  master: StandardCrudFns,
  detail: StandardCrudFns,
): GenCrudResource {
  return {
    perm,
    api: {
      master: standardCrudApi(master),
      detail: standardCrudApi(detail),
    } satisfies GenMasterDetailApi,
  }
}
