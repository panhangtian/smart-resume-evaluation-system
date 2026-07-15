import request from './request'

export const updateProfile = (data) => request.put('/auth/profile', data)
export const updateResumeData = (id, data) => request.put(`/resumes/${id}`, data)
