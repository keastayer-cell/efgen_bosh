// API paths below already include the `/api` prefix. Keep the base URL host-only
// even if a deployment environment accidentally provides a trailing `/api`.
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8081').replace(/\/api\/?$/, '')

function accessToken() {
  return typeof localStorage === 'undefined' ? null : localStorage.getItem('efgen_access_token')
}

function apiErrorMessage(body, status) {
  if (typeof body === 'string' && body.trim()) return body.trim()
  if (body && typeof body.error === 'string' && body.error.trim()) return body.error.trim()
  if (body && typeof body.message === 'string' && body.message.trim()) return body.message.trim()
  if (body && typeof body.detail === 'string' && body.detail.trim()) return body.detail.trim()

  if (body?.fieldErrors && typeof body.fieldErrors === 'object') {
    const fields = Object.entries(body.fieldErrors)
      .flatMap(([field, value]) => Array.isArray(value) ? value.map((message) => `${field}: ${message}`) : [`${field}: ${value}`])
      .filter(Boolean)
    if (fields.length) return fields.join('; ')
  }

  if (Array.isArray(body?.errors) && body.errors.length) {
    return body.errors.map((item) => typeof item === 'string' ? item : item?.message || item?.defaultMessage).filter(Boolean).join('; ')
  }

  return status >= 500 ? 'Внутренняя ошибка сервера.' : 'Не удалось выполнить запрос.'
}

function notifyApiError(message, status) {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('efgen-api-error', { detail: { message, status } }))
  }
}

export async function requestJson(path, options = {}) {
  let response
  const isPublicAuthRequest = /^\/api\/auth\/(register|login)$/.test(path)

  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      ...options,
      credentials: options.credentials || 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(!isPublicAuthRequest && accessToken()
          ? { Authorization: `Bearer ${accessToken()}` }
          : {}),
        ...(options.headers || {}),
      },
    })
  } catch {
    const message = 'Сервер недоступен. Попробуйте позже.'
    notifyApiError(message, 0)
    throw new Error(message)
  }

  const body = await response.json().catch(() => ({}))
  if (!response.ok) {
    const error = new Error(apiErrorMessage(body, response.status))
    error.status = response.status
    error.body = body
    notifyApiError(error.message, response.status)
    throw error
  }

  return body
}
