const BASE_URL = '/api'

async function request(url, options = {}) {
  const config = {
    headers: { 'Content-Type': 'application/json' },
    ...options
  }
  const res = await fetch(BASE_URL + url, config)
  const data = await res.json()
  if (data.code !== 200) {
    throw new Error(data.message || '请求失败')
  }
  return data
}

export const api = {
  sendCode(phone) {
    return request(`/user/send-code?phone=${phone}`)
  },
  register(body) {
    return request('/user/register', {
      method: 'POST',
      body: JSON.stringify(body)
    })
  },
  getSchools() {
    return request('/school/list')
  }
}
