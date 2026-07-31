import { afterEach, describe, expect, it, vi } from 'vitest'
import { requestJson } from './http'

describe('requestJson', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('returns a parsed successful response', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'UP' }),
    }))

    await expect(requestJson('/api/health')).resolves.toEqual({ status: 'UP' })
  })

  it('turns an API error into an exception with status', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({ error: 'Требуется авторизация.' }),
    }))

    await expect(requestJson('/api/private')).rejects.toMatchObject({
      message: 'Требуется авторизация.',
      status: 401,
    })
  })
})
