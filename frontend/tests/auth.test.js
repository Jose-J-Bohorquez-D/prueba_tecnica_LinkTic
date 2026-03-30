import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../src/stores/auth'
import { api } from '../src/api/api'

vi.mock('../src/api/api', () => ({
  api: {
    post: vi.fn()
  }
}))

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('login success', async () => {
    const authStore = useAuthStore()
    const mockToken = 'fake-jwt-token'
    api.post.mockResolvedValueOnce({ data: { token: mockToken } })

    const result = await authStore.login('admin', 'admin')

    expect(result).toBe(true)
    expect(authStore.token).toBe(mockToken)
    expect(localStorage.getItem('token')).toBe(mockToken)
  })

  it('login failure', async () => {
    const authStore = useAuthStore()
    api.post.mockRejectedValueOnce({ response: { data: { message: 'Invalid credentials' } } })

    const result = await authStore.login('wrong', 'wrong')

    expect(result).toBe(false)
    expect(authStore.token).toBeNull()
    expect(authStore.error).toBe('Invalid credentials')
  })
})
