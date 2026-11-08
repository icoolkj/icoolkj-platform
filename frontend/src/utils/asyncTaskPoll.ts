import { getAsyncTaskMine } from '@/api/infra/asyncTask'
import type { AsyncTaskVO } from '@/types/infra/asyncTask'

export const ASYNC_TASK_STATUS = {
  PENDING: '0',
  RUNNING: '1',
  SUCCESS: '2',
  FAILED: '3',
  CANCELLED: '4',
} as const

export function isAsyncTaskTerminal(status?: string) {
  return (
    status === ASYNC_TASK_STATUS.SUCCESS ||
    status === ASYNC_TASK_STATUS.FAILED ||
    status === ASYNC_TASK_STATUS.CANCELLED
  )
}

export type PollAsyncTaskOptions = {
  intervalMs?: number
  timeoutMs?: number
  pendingTimeoutMs?: number
  signal?: AbortSignal
}

export class AsyncTaskPollError extends Error {
  readonly code: 'aborted' | 'timeout' | 'pending_timeout'

  constructor(code: 'aborted' | 'timeout' | 'pending_timeout') {
    super(code)
    this.name = 'AsyncTaskPollError'
    this.code = code
  }
}

export async function pollAsyncTask(
  taskId: string,
  options: PollAsyncTaskOptions = {},
): Promise<AsyncTaskVO> {
  const intervalMs = options.intervalMs ?? 1500
  const timeoutMs = options.timeoutMs ?? 5 * 60 * 1000
  const pendingTimeoutMs = options.pendingTimeoutMs ?? 90_000
  const started = Date.now()

  while (true) {
    if (options.signal?.aborted) {
      throw new AsyncTaskPollError('aborted')
    }
    const res = await getAsyncTaskMine(taskId)
    if (options.signal?.aborted) {
      throw new AsyncTaskPollError('aborted')
    }
    const task = res.data.data
    if (isAsyncTaskTerminal(task.taskStatus)) {
      return task
    }
    const elapsed = Date.now() - started
    if (
      pendingTimeoutMs > 0 &&
      task.taskStatus === ASYNC_TASK_STATUS.PENDING &&
      elapsed > pendingTimeoutMs
    ) {
      throw new AsyncTaskPollError('pending_timeout')
    }
    if (elapsed > timeoutMs) {
      throw new AsyncTaskPollError('timeout')
    }
    await sleep(intervalMs, options.signal)
  }
}

function sleep(ms: number, signal?: AbortSignal) {
  return new Promise<void>((resolve, reject) => {
    if (signal?.aborted) {
      reject(new AsyncTaskPollError('aborted'))
      return
    }
    const timer = setTimeout(() => {
      signal?.removeEventListener('abort', onAbort)
      resolve()
    }, ms)
    const onAbort = () => {
      clearTimeout(timer)
      reject(new AsyncTaskPollError('aborted'))
    }
    signal?.addEventListener('abort', onAbort, { once: true })
  })
}
