import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext.jsx'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import Feed from './pages/Feed'
import CreateProject from './pages/CreateProject'
import ProjectDetail from './pages/ProjectDetail'
import Profile from './pages/Profile'
import Dashboard from './pages/Dashboard'
import Chat from './pages/Chat'
import GithubCallback from './pages/GithubCallback'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <div className="min-h-screen bg-gray-50">
          <Routes>
            <Route path="/" element={<Navigate to="/feed" />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/auth/github/callback" element={<GithubCallback />} />
            <Route path="/feed" element={
              <ProtectedRoute><Feed /></ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute><Profile /></ProtectedRoute>
            } />
            <Route path="/profile/:id" element={
              <ProtectedRoute><Profile /></ProtectedRoute>
            } />
            <Route path="/projects/create" element={
              <ProtectedRoute><CreateProject /></ProtectedRoute>
            } />
            <Route path="/projects/:id" element={
              <ProtectedRoute><ProjectDetail /></ProtectedRoute>
            } />
            <Route path="/dashboard" element={
              <ProtectedRoute><Dashboard /></ProtectedRoute>
            } />
            <Route path="/chat/:userId" element={
              <ProtectedRoute><Chat /></ProtectedRoute>
            } />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  )
}