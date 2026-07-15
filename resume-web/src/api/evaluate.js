import request from './request'

export const evaluate = (resumeId, jobId) => request.post('/evaluations', null, { params: { resumeId, jobId } })
export const getEvaluation = (id) => request.get(`/evaluations/${id}`)
export const getMyEvaluations = (resumeId) => request.get('/evaluations/my', { params: { resumeId } })
