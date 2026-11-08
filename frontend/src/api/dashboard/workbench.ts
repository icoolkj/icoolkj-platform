import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { WorkbenchVO } from '@/types/dashboard/workbench'

export function fetchWorkbench() {
  return request.get<ApiResponse<WorkbenchVO>>('/dashboard/workbench').then((res) => res.data.data)
}
