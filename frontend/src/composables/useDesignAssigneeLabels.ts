import { computed, ref, watch, type Ref } from 'vue'
import { getDept } from '@/api/system/dept'
import { getPost, pagePost } from '@/api/system/post'
import { pageRole } from '@/api/system/role'
import { getUser } from '@/api/system/user'
import type { SilentRequestConfig } from '@/utils/request'
import {
  collectDesignAssigneeRefs,
  type AssigneeNameResolvers,
  type WorkflowDesignNode,
} from '@/types/workflow/design'

/** 审批详情只读流程图也会解析展示名；普通员工无用户/角色管理权限，失败须静默 */
const SILENT: SilentRequestConfig = { silentError: true }

/**
 * 按设计节点批量解析办理人展示名：用户昵称 / 角色名 / 部门名 / 岗位名。
 */
export function useDesignAssigneeLabels(nodes: Ref<WorkflowDesignNode[]>) {
  const userNames = ref<Record<string, string>>({})
  const roleNames = ref<Record<string, string>>({})
  const deptNames = ref<Record<string, string>>({})
  const postNames = ref<Record<string, string>>({})

  const resolvers = computed<AssigneeNameResolvers>(() => ({
    userNameOf: (id) => userNames.value[id] || id,
    roleNameOf: (code) => roleNames.value[code] || code,
    deptNameOf: (id) => deptNames.value[id] || id,
    postNameOf: (id) => postNames.value[id] || id,
  }))

  async function resolve() {
    const refs = collectDesignAssigneeRefs(nodes.value)
    const tasks: Promise<void>[] = []

    if (refs.roleCodes.length) {
      tasks.push(
        pageRole({ pageNo: 1, pageSize: 200 }, SILENT)
          .then((res) => {
            const map: Record<string, string> = { ...roleNames.value }
            for (const r of res.data.data?.list ?? []) {
              if (r.roleCode) map[r.roleCode] = r.roleName || r.roleCode
            }
            roleNames.value = map
          })
          .catch(() => undefined),
      )
    }

    for (const userId of refs.userIds) {
      if (userNames.value[userId]) continue
      tasks.push(
        getUser(userId, SILENT)
          .then((res) => {
            const u = res.data.data
            const name = u?.nickname || u?.realName || u?.username || userId
            userNames.value = { ...userNames.value, [userId]: name }
          })
          .catch(() => {
            userNames.value = { ...userNames.value, [userId]: userId }
          }),
      )
    }

    for (const deptId of refs.deptIds) {
      if (deptNames.value[deptId]) continue
      tasks.push(
        getDept(deptId, SILENT)
          .then((res) => {
            const d = res.data.data
            deptNames.value = {
              ...deptNames.value,
              [deptId]: d?.deptName || d?.deptCode || deptId,
            }
          })
          .catch(() => {
            deptNames.value = { ...deptNames.value, [deptId]: deptId }
          }),
      )
    }

    if (refs.postIds.length) {
      const needLookup = refs.postIds.filter((id) => !postNames.value[id])
      if (needLookup.length) {
        tasks.push(
          pagePost({ pageNo: 1, pageSize: 200 }, SILENT)
            .then(async (res) => {
              const map: Record<string, string> = { ...postNames.value }
              for (const p of res.data.data?.list ?? []) {
                if (p.id) map[p.id] = p.postName || p.postCode || p.id
                if (p.postCode) map[p.postCode] = p.postName || p.postCode
              }
              // 列表未命中时按 id 补查
              await Promise.all(
                needLookup
                  .filter((id) => !map[id])
                  .map((id) =>
                    getPost(id, SILENT)
                      .then((r) => {
                        const p = r.data.data
                        map[id] = p?.postName || p?.postCode || id
                        if (p?.postCode) map[p.postCode] = p.postName || p.postCode
                      })
                      .catch(() => {
                        map[id] = id
                      }),
                  ),
              )
              postNames.value = map
            })
            .catch(() => undefined),
        )
      }
    }

    if (tasks.length) await Promise.all(tasks)
  }

  watch(
    nodes,
    () => {
      void resolve()
    },
    { immediate: true, deep: true },
  )

  return { resolvers, resolve }
}
