const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8081'

function accessToken() {
  return typeof localStorage === 'undefined' ? null : localStorage.getItem('efgen_access_token')
}

export async function requestJson(path, options = {}) {
  let response

  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      ...options,
      credentials: options.credentials || 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(accessToken()
          ? { Authorization: `Bearer ${accessToken()}` }
          : {}),
        ...(options.headers || {}),
      },
    })
  } catch {
    throw new Error('Сервер недоступен. Попробуйте позже.')
  }

  const body = await response.json().catch(() => ({}))
  if (!response.ok) {
    const error = new Error(body.message || body.error || 'Не удалось выполнить запрос.')
    error.status = response.status
    error.body = body
    throw error
  }

  return body
}
