import request from './request'

export const uploadResume = (formData) => request.post('/resumes/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
export const getResume = (id) => request.get(`/resumes/${id}`)
export const getMyResumes = () => request.get('/resumes/my')
export const reparseResume = (id) => request.post(`/resumes/${id}/reparse`)
