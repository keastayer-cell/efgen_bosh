const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'

export async function requestJson(path, options = {}) {
  let response

  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      ...options,
      credentials: options.credentials || 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
    })
  } catch {
    throw new Error('Сервер недоступен. Попробуйте позже.')
  }

  const body = await response.json().catch(() => ({}))
  if (!response.ok) {
    const error = new Error(body.error || 'Не удалось выполнить запрос.')
    error.status = response.status
    error.body = body
    throw error
  }

  return body
}
