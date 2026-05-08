import axios from 'axios'

const API = axios.create({ baseURL: 'http://localhost:8080/api' })

// Attach JWT token to every request automatically
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Auth
export const register = (data) => API.post('/auth/register', data)
export const login = (data) => API.post('/auth/login', data)
export const githubLogin = (data) => API.post('/auth/github', data)

// Projects
export const getAllProjects = (params) => API.get('/projects', { params })
export const getProject = (id) => API.get(`/projects/${id}`)
export const createProject = (data) => API.post('/projects', data)
export const updateProject = (id, data) => API.put(`/projects/${id}`, data)
export const deleteProject = (id) => API.delete(`/projects/${id}`)
export const getMyProjects = () => API.get('/projects/my')

// Applications
export const applyToProject = (projectId, data) =>
  API.post(`/applications/project/${projectId}`, data)
export const getProjectApplications = (projectId) =>
  API.get(`/applications/project/${projectId}`)
export const getIncomingApplications = () => API.get('/applications/incoming')
export const getMyApplications = () => API.get('/applications/my')
export const updateApplicationStatus = (id, status) =>
  API.patch(`/applications/${id}/status`, null, { params: { status } })
export const withdrawApplication = (id) => API.delete(`/applications/${id}`)

// Messages
export const getConversation = (otherUserId) => API.get(`/messages/${otherUserId}`)
export const getUnreadMessages = () => API.get('/messages/unread')

// Notifications
export const getNotifications = () => API.get('/notifications')
export const getUnreadCount = () => API.get('/notifications/unread-count')
export const markNotificationsRead = () => API.post('/notifications/mark-read')

// Profiles
export const getUserProfile = (id) => API.get(`/users/${id}`)
export const getMyProfile = () => API.get('/users/me')
export const updateProfile = (data) => API.put('/users/profile', data)

// Milestones
export const getMilestones = (projectId) => API.get(`/projects/${projectId}/milestones`)
export const createMilestone = (projectId, data) => API.post(`/projects/${projectId}/milestones`, data)
export const updateMilestone = (milestoneId, data) => API.patch(`/milestones/${milestoneId}`, data)
export const deleteMilestone = (milestoneId) => API.delete(`/milestones/${milestoneId}`)