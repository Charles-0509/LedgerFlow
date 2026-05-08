import request from './request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  logout: () => request.post('/auth/logout')
}

export const userApi = {
  profile: () => request.get('/user/profile'),
  updateProfile: (data) => request.put('/user/profile', data)
}

export const accountApi = {
  list: () => request.get('/accounts'),
  create: (data) => request.post('/accounts', data),
  update: (id, data) => request.put(`/accounts/${id}`, data),
  remove: (id) => request.delete(`/accounts/${id}`)
}

export const categoryApi = {
  list: (params = {}) => request.get('/categories', { params }),
  create: (data) => request.post('/categories', data),
  update: (id, data) => request.put(`/categories/${id}`, data),
  remove: (id) => request.delete(`/categories/${id}`),
  records: (id, params = {}) => request.get(`/categories/${id}/records`, { params })
}

export const recordApi = {
  list: (params = {}) => request.get('/records', { params }),
  detail: (id) => request.get(`/records/${id}`),
  create: (data) => request.post('/records', data),
  update: (id, data) => request.put(`/records/${id}`, data),
  remove: (id) => request.delete(`/records/${id}`)
}

export const statisticsApi = {
  home: () => request.get('/statistics/home'),
  monthly: (params = {}) => request.get('/statistics/monthly', { params }),
  category: (params = {}) => request.get('/statistics/category', { params }),
  trend: (params = {}) => request.get('/statistics/trend', { params })
}
