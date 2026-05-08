import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createProject } from '../services/api'

const TECH_OPTIONS = ['React', 'Spring Boot', 'Node.js', 'Python', 'Flutter',
                      'MySQL', 'MongoDB', 'Docker', 'AWS', 'TypeScript']
const ROLE_OPTIONS = ['Frontend Dev', 'Backend Dev', 'UI Designer',
                      'DevOps', 'Mobile Dev', 'Data Scientist']

export default function CreateProject() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    title: '', description: '', techStack: [],
    rolesNeeded: [], commitmentLevel: 'MEDIUM',
    githubRepo: ''
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const toggleItem = (field, item) => {
    setForm(prev => ({
      ...prev,
      [field]: prev[field].includes(item)
        ? prev[field].filter(i => i !== item)
        : [...prev[field], item]
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const res = await createProject(form)
      navigate(`/projects/${res.data.id}`)
    } catch {
      setError('Failed to create project')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Post a Project</h1>
      {error && <p className="text-red-500 bg-red-50 p-3 rounded-lg text-sm mb-4">{error}</p>}
      <form onSubmit={handleSubmit} className="bg-white border border-gray-200 rounded-2xl p-6 space-y-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Project Title</label>
          <input
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="e.g. AI Resume Builder"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={4}
            placeholder="Describe your project idea, goals, and what you're building..."
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Tech Stack</label>
          <div className="flex flex-wrap gap-2">
            {TECH_OPTIONS.map(tech => (
              <button
                key={tech}
                type="button"
                onClick={() => toggleItem('techStack', tech)}
                className={`text-xs px-3 py-1.5 rounded-full border transition-all ${
                  form.techStack.includes(tech)
                    ? 'bg-blue-600 text-white border-blue-600'
                    : 'bg-white text-gray-600 border-gray-300 hover:border-blue-400'
                }`}
              >
                {tech}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Roles Needed</label>
          <div className="flex flex-wrap gap-2">
            {ROLE_OPTIONS.map(role => (
              <button
                key={role}
                type="button"
                onClick={() => toggleItem('rolesNeeded', role)}
                className={`text-xs px-3 py-1.5 rounded-full border transition-all ${
                  form.rolesNeeded.includes(role)
                    ? 'bg-purple-600 text-white border-purple-600'
                    : 'bg-white text-gray-600 border-gray-300 hover:border-purple-400'
                }`}
              >
                {role}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Commitment Level</label>
          <select
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={form.commitmentLevel}
            onChange={(e) => setForm({ ...form, commitmentLevel: e.target.value })}
          >
            <option value="LOW">Low — a few hours/week</option>
            <option value="MEDIUM">Medium — part time</option>
            <option value="HIGH">High — near full time</option>
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">GitHub Repository (Optional)</label>
          <input
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="e.g. facebook/react"
            value={form.githubRepo}
            onChange={(e) => setForm({ ...form, githubRepo: e.target.value })}
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? 'Posting...' : 'Post Project'}
        </button>
      </form>
    </div>
  )
}