import { useState } from 'react'
import type { FormEvent } from 'react'
import { LoaderCircle, LockKeyhole, Toilet } from 'lucide-react'
import { api } from './api'
import type { UsuarioSesion } from './types'

function Login({ onLogin }: { onLogin: (usuario: UsuarioSesion) => void }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [cargando, setCargando] = useState(false)

  const enviar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setCargando(true)
    try {
      onLogin(await api.iniciarSesion(username, password))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo iniciar sesión.')
    } finally {
      setCargando(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-card">
        <span className="login-mark"><Toilet size={26} /></span>
        <div className="eyebrow"><span className="eyebrow-line" />IES REYES</div>
        <h1>Iniciar sesión</h1>
        <p>Accede a la gestión de permisos de baño del centro.</p>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <form className="login-form" onSubmit={enviar}>
          <label className="field">
            <span>Usuario <b>*</b></span>
            <input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" required />
          </label>
          <label className="field">
            <span>Contraseña <b>*</b></span>
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" required />
          </label>
          <button className="submit-button" type="submit" disabled={cargando}>
            {cargando ? <LoaderCircle size={17} className="spin" /> : <LockKeyhole size={17} />}
            {cargando ? 'Accediendo…' : 'Acceder'}
          </button>
        </form>
      </section>
    </main>
  )
}

export default Login
