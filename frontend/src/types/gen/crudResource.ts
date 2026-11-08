import type { GenCrudApi, GenCrudPerm, GenMasterDetailApi } from './crudApi'

/** Gen 运行时 CRUD 资源条目（api 模块 export genCrudResource 的形状） */
export type GenCrudResource = {
  api: GenCrudApi | GenMasterDetailApi
  perm: GenCrudPerm
}
