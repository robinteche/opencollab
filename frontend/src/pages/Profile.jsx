import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { getUserProfile, getMyProfile, updateProfile } from '../services/api'
import { useAuth } from '../context/useAuth'

export default function Profile() {
  const { id } = useParams()
  const { user: currentUser } = useAuth()
  const [profile, setProfile] = useState(null)
  const [isEditing, setIsEditing] = useState(false)
  const [editForm, setEditForm] = useState({
    bio: '',
    githubUrl: '',
    avatarUrl: '',
    skills: []
  })
  const [skillInput, setSkillInput] = useState('')
  const [loading, setLoading] = useState(true)
  const [githubRepos, setGithubRepos] = useState([])

  const isMyProfile = !id || Number(id) === Number(currentUser?.userId)

  useEffect(() => {
    setLoading(true)
    const fetchProfile = id ? getUserProfile(id) : getMyProfile()
    fetchProfile
      .then(res => {
        setProfile(res.data)
        setEditForm({
          bio: res.data.bio || '',
          githubUrl: res.data.githubUrl || '',
          avatarUrl: res.data.avatarUrl || '',
          skills: res.data.skills || []
        })
        if (res.data.githubUrl) {
          const username = res.data.githubUrl.replace(/\/$/, '').split('/').pop()
          if (username) {
             fetch(`https://api.github.com/users/${username}/repos?sort=updated&per_page=4`)
               .then(r => r.json())
               .then(repos => {
                  if (Array.isArray(repos)) setGithubRepos(repos)
               })
               .catch(console.error)
          }
        }
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [id])

  const handleUpdate = async () => {
    try {
      const res = await updateProfile(editForm)
      setProfile(res.data)
      setIsEditing(false)
    } catch (err) {
      console.error('Update failed:', err)
    }
  }

  const addSkill = () => {
    if (skillInput.trim() && !editForm.skills.includes(skillInput.trim())) {
      setEditForm({ ...editForm, skills: [...editForm.skills, skillInput.trim()] })
      setSkillInput('')
    }
  }

  const removeSkill = (skill) => {
    setEditForm({ ...editForm, skills: editForm.skills.filter(s => s !== skill) })
  }

  if (loading) return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <div className="animate-pulse text-gray-400">Loading profile...</div>
    </div>
  )

  if (!profile) return <div>Profile not found</div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-12">
      <div className="bg-white border border-gray-200 rounded-3xl overflow-hidden shadow-sm">
        {/* Header/Cover */}
        <div className="h-32 bg-gradient-to-r from-blue-600 to-indigo-600"></div>
        
        <div className="px-8 pb-8">
          <div className="relative flex justify-between items-end -mt-12 mb-6">
            <div className="w-24 h-24 rounded-3xl bg-white p-1 shadow-lg">
              <div className="w-full h-full rounded-2xl bg-gray-100 flex items-center justify-center text-3xl font-bold text-blue-600 uppercase">
                {profile.avatarUrl ? (
                  <img src={profile.avatarUrl} alt={profile.username} className="w-full h-full rounded-2xl object-cover" />
                ) : profile.username.charAt(0)}
              </div>
            </div>
            
            {isMyProfile && (
              <button
                onClick={() => setIsEditing(!isEditing)}
                className="bg-white border border-gray-200 text-gray-700 px-5 py-2 rounded-xl text-sm font-bold hover:bg-gray-50 transition-all shadow-sm"
              >
                {isEditing ? 'Cancel' : 'Edit Profile'}
              </button>
            )}
          </div>

          <div className="flex flex-col md:flex-row gap-8">
            <div className="flex-1">
              <h1 className="text-2xl font-bold text-gray-900 mb-1">{profile.username}</h1>
              <p className="text-sm text-gray-400 mb-4">{profile.email}</p>

              {isEditing ? (
                <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
                  <div>
                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5">Bio</label>
                    <textarea
                      className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 min-h-[100px]"
                      value={editForm.bio}
                      onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
                    />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5">GitHub URL</label>
                      <input
                        className="w-full border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        value={editForm.githubUrl}
                        onChange={(e) => setEditForm({ ...editForm, githubUrl: e.target.value })}
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5">Avatar URL</label>
                      <input
                        className="w-full border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        value={editForm.avatarUrl}
                        onChange={(e) => setEditForm({ ...editForm, avatarUrl: e.target.value })}
                      />
                    </div>
                  </div>
                  <button
                    onClick={handleUpdate}
                    className="bg-blue-600 text-white px-8 py-2.5 rounded-xl text-sm font-bold hover:bg-blue-700 shadow-lg shadow-blue-500/30 transition-all"
                  >
                    Save Changes
                  </button>
                </div>
              ) : (
                <div className="animate-in fade-in duration-500">
                  <p className="text-gray-600 leading-relaxed max-w-2xl mb-6">
                    {profile.bio || "No bio added yet."}
                  </p>
                  {profile.githubUrl && (
                    <a href={profile.githubUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-blue-600 transition-colors">
                       <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.43.372.823 1.102.823 2.222 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/></svg>
                       GitHub Profile
                    </a>
                  )}
                </div>
              )}

              {/* GitHub Repositories Section */}
              {!isEditing && githubRepos.length > 0 && (
                <div className="mt-12 animate-in fade-in duration-500">
                  <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                    <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.43.372.823 1.102.823 2.222 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/></svg>
                    Recent Repositories
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {githubRepos.map(repo => (
                      <a key={repo.id} href={repo.html_url} target="_blank" rel="noreferrer" className="block p-4 border border-gray-100 rounded-2xl bg-gray-50/50 hover:bg-white hover:border-blue-200 hover:shadow-md transition-all group">
                        <div className="flex justify-between items-start mb-2">
                          <h4 className="font-bold text-sm text-blue-600 group-hover:underline truncate pr-2">{repo.name}</h4>
                          <span className="text-[10px] font-bold text-gray-500 bg-white border border-gray-200 px-2 py-0.5 rounded-md flex items-center gap-1">
                            ⭐ {repo.stargazers_count}
                          </span>
                        </div>
                        <p className="text-xs text-gray-500 line-clamp-2 mb-3 h-8">
                          {repo.description || 'No description provided.'}
                        </p>
                        <div className="flex items-center gap-2">
                          {repo.language && (
                             <span className="text-[10px] font-bold text-gray-400">
                               <span className="w-2 h-2 rounded-full bg-blue-400 inline-block mr-1"></span>
                               {repo.language}
                             </span>
                          )}
                        </div>
                      </a>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="w-full md:w-64 border-t md:border-t-0 md:border-l border-gray-100 pt-8 md:pt-0 md:pl-8">
              <div className="mb-8">
                <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4">Skills</h3>
                {isEditing && (
                  <div className="flex gap-1.5 mb-4">
                    <input
                      className="flex-1 border border-gray-200 rounded-lg px-3 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Add a skill"
                      value={skillInput}
                      onChange={(e) => setSkillInput(e.target.value)}
                      onKeyDown={(e) => e.key === 'Enter' && addSkill()}
                    />
                    <button onClick={addSkill} className="bg-blue-600 text-white px-3 py-1.5 rounded-lg text-xs font-bold">+</button>
                  </div>
                )}
                <div className="flex flex-wrap gap-2">
                  {(isEditing ? editForm.skills : profile.skills)?.length === 0 ? (
                    <p className="text-xs text-gray-400 italic">No skills listed.</p>
                  ) : (isEditing ? editForm.skills : profile.skills).map(skill => (
                    <span key={skill} className="bg-gray-100 text-gray-700 text-[11px] font-bold px-2.5 py-1 rounded-lg flex items-center gap-1.5 group">
                      {skill}
                      {isEditing && (
                        <button onClick={() => removeSkill(skill)} className="text-gray-400 hover:text-red-500 transition-colors">×</button>
                      )}
                    </span>
                  ))}
                </div>
              </div>

              <div>
                <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">Collaboration Stats</h3>
                <div className="bg-gray-50 rounded-2xl p-4">
                   <div className="flex items-center justify-between">
                     <span className="text-xs text-gray-500">Member Since</span>
                     <span className="text-xs font-bold text-gray-900">{new Date(profile.createdAt).getFullYear()}</span>
                   </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
