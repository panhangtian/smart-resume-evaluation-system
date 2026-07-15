import request from './request'

export const createJob = (data) => request.post('/jobs', data)
export const updateJob = (id, data) => request.put(`/jobs/${id}`, data)
export const getJob = (id) => request.get(`/jobs/${id}`)
export const getMyJobs = () => request.get('/jobs/my')
export const getAvailableJobs = (keyword) => request.get('/jobs/available', { params: { keyword } })
export const toggleJob = (id, status) => request.post(`/jobs/${id}/toggle`, null, { params: { status } })
