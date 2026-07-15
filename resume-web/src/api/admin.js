import request from './request'

export const getDashboard = () => request.get('/admin/dashboard')
