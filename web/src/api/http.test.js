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
      message: 'Сессия истекла или отсутствует. Войдите в систему снова.',
      status: 401,
    })
  })

  it('shows the backend business error instead of a generic status message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 409,
      json: async () => ({
        timestamp: '2026-08-01T11:21:10.861833+03:00',
        status: 409,
        error: 'Данные конфликтуют с уже существующей записью.',
        path: '/api/v1/contractors',
        fieldErrors: {},
      }),
    }))

    await expect(requestJson('/api/v1/contractors', { method: 'POST' })).rejects.toMatchObject({
      message: 'Данные конфликтуют с уже существующей записью.',
      status: 409,
    })
  })

  it('formats validation field errors for the user', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      json: async () => ({ fieldErrors: { code: 'Код уже занят', shortName: ['Укажите имя'] } }),
    }))

    await expect(requestJson('/api/v1/contractors')).rejects.toMatchObject({
      message: 'code: Код уже занят; shortName: Укажите имя',
      status: 400,
    })
  })
})
