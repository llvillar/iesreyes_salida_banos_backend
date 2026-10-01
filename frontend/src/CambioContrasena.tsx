import { useState } from 'react'
import type { FormEvent } from 'react'
import { Check, KeyRound, LoaderCircle } from 'lucide-react'
import { api } from './api'

function CambioContrasena() {
  const [contrasenaActual, setContrasenaActual] = useState('')
  const [contrasenaNueva, setContrasenaNueva] = useState('')
  const [confirmacion, setConfirmacion] = useState('')
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [guardando, setGuardando] = useState(false)

  const enviar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setAviso('')
    if (contrasenaNueva !== confirmacion) {
      setError('La confirmación no coincide con la nueva contraseña.')
      return
    }
    setGuardando(true)
    try {
      await api.cambiarContrasena(contrasenaActual, contrasenaNueva)
      setContrasenaActual('')
      setContrasenaNueva('')
      setConfirmacion('')
      setAviso('La contraseña se ha cambiado correctamente.')
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cambiar la contraseña.')
    } finally {
      setGuardando(false)
    }
  }

  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow"><span className="eyebrow-line" />MI CUENTA</div>
          <h1>Cambiar contraseña</h1>
          <p>Actualiza la contraseña de acceso a tu cuenta de profesor.</p>
        </div>
      </section>
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      {aviso && <div className="alert alert-success" role="status"><Check size={17} />{aviso}</div>}
      <article className="panel create-panel">
        <div className="panel-heading">
          <div className="panel-title-icon"><KeyRound size={18} /></div>
          <div><h2>Seguridad de la cuenta</h2><p>La nueva contraseña debe tener al menos 12 caracteres.</p></div>
        </div>
        <form className="permission-form catalog-form" onSubmit={enviar}>
          <label className="field"><span>Contraseña actual <b>*</b></span>
            <input type="password" autoComplete="current-password" value={contrasenaActual}
              onChange={(event) => setContrasenaActual(event.target.value)} required /></label>
          <label className="field"><span>Nueva contraseña <b>*</b></span>
            <input type="password" autoComplete="new-password" minLength={12} maxLength={72}
              value={contrasenaNueva} onChange={(event) => setContrasenaNueva(event.target.value)} required /></label>
          <label className="field"><span>Repite la nueva contraseña <b>*</b></span>
            <input type="password" autoComplete="new-password" minLength={12} maxLength={72}
              value={confirmacion} onChange={(event) => setConfirmacion(event.target.value)} required /></label>
          <button className="submit-button" type="submit" disabled={guardando}>
            {guardando ? <LoaderCircle size={17} className="spin" /> : <KeyRound size={17} />}
            {guardando ? 'Guardando…' : 'Cambiar contraseña'}
          </button>
        </form>
      </article>
    </>
  )
}

export default CambioContrasena
