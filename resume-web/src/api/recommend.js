import request from './request'

export const recommendJobs = (topN = 20) => request.get('/recommendations/jobs', { params: { topN } })
export const recommendCandidates = (jobId, topN = 10) => request.get(`/recommendations/candidates/${jobId}`, { params: { topN } })
export const getHistory = () => request.get('/recommendations/history')
