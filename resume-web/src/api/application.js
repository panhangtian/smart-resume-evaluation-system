import request from './request'

export const applyJob = (resumeId, jobId) => request.post('/applications', null, { params: { resumeId, jobId } })
export const getMyApplications = () => request.get('/applications/my')
export const getJobApplications = (jobId) => request.get(`/applications/job/${jobId}`)
export const updateApplicationStatus = (id, status) => request.put(`/applications/${id}/status`, null, { params: { status } })
