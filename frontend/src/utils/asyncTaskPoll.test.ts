import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ASYNC_TASK_STATUS, AsyncTaskPollError, pollAsyncTask } from '@/utils/asyncTaskPoll'

vi.mock('@/api/infra/asyncTask', () => ({
  getAsyncTaskMine: vi.fn(),
}))

import { getAsyncTaskMine } from '@/api/infra/asyncTask'

describe('pollAsyncTask', () => {
  beforeEach(() => {
    vi.mocked(getAsyncTaskMine).mockReset()
  })

  it('returns terminal SUCCESS', async () => {
    vi.mocked(getAsyncTaskMine).mockResolvedValue({
      data: { data: { taskStatus: ASYNC_TASK_STATUS.SUCCESS, resultData: '{}' } },
    } as never)
    const task = await pollAsyncTask('t1', { intervalMs: 1, timeoutMs: 1000 })
    expect(task.taskStatus).toBe(ASYNC_TASK_STATUS.SUCCESS)
  })

  it('throws aborted when signal already aborted', async () => {
    const ac = new AbortController()
    ac.abort()
    await expect(pollAsyncTask('t1', { signal: ac.signal })).rejects.toMatchObject({
      name: 'AsyncTaskPollError',
      code: 'aborted',
    } satisfies Partial<AsyncTaskPollError>)
    expect(getAsyncTaskMine).not.toHaveBeenCalled()
  })

  it('throws aborted after fetch when signal aborted during request', async () => {
    const ac = new AbortController()
    vi.mocked(getAsyncTaskMine).mockImplementation(async () => {
      ac.abort()
      return {
        data: { data: { taskStatus: ASYNC_TASK_STATUS.SUCCESS } },
      } as never
    })
    await expect(pollAsyncTask('t1', { signal: ac.signal, intervalMs: 1 })).rejects.toMatchObject({
      code: 'aborted',
    })
  })
})
