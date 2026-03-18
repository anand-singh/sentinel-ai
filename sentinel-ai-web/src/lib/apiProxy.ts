/**
 * Proxy helper — forwards Next.js API route requests to the Java backend.
 *
 * When SENTINEL_API_URL is set (server-side env var), all requests are
 * forwarded to the Java API. When it is NOT set, falls back to the
 * in-process mock database so local dev without the Java API still works.
 */

export const SENTINEL_API_URL = process.env.SENTINEL_API_URL ?? ''

export function isApiConnected(): boolean {
  return SENTINEL_API_URL.length > 0
}

/**
 * Forward a GET request to the Java API, preserving query params.
 * Returns the Response from fetch (or throws on network error).
 */
export async function proxyGet(path: string, searchParams?: URLSearchParams): Promise<Response> {
  const qs = searchParams?.toString()
  const url = `${SENTINEL_API_URL}${path}${qs ? '?' + qs : ''}`
  return fetch(url, { headers: { Accept: 'application/json' } })
}

/**
 * Forward a POST request to the Java API with a JSON body.
 */
export async function proxyPost(path: string, body?: unknown): Promise<Response> {
  const url = `${SENTINEL_API_URL}${path}`
  return fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })
}

/**
 * Forward a raw POST (body already a string) — used for /api/ingest.
 */
export async function proxyPostRaw(path: string, rawBody: string): Promise<Response> {
  const url = `${SENTINEL_API_URL}${path}`
  return fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: rawBody,
  })
}
