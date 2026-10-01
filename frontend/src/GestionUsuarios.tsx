import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Check, LoaderCircle, Plus, Trash2, X } from 'lucide-react'
import { api } from './api'
import type { Profesor, UsuarioApp, UsuarioSesion } from './types'

function GestionUsuarios() {
  const [usuarios, setUsuarios] = useState<UsuarioApp[]>([])
  const [profesores, setProfesores] = useState<Profesor[]>([])
  const [password, setPassword] = useState('')
  const [profesorId, setProfesorId] = useState('')
  const [perfil, setPerfil] = useState<UsuarioSesion['role']>('GESTION_PERMISOS')
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')

  const cargar = async () => {
    const [listaUsuarios, listaProfesores] = await Promise.all([
      api.listarUsuarios(),
      api.listarProfesores(),
    ])
    setUsuarios(listaUsuarios)
    setProfesores(listaProfesores)
  }

  useEffect(() => {
    let activo = true
    Promise.all([api.listarUsuarios(), api.listarProfesores()])
      .then(([listaUsuarios, listaProfesores]) => {
        if (!activo) return
        setUsuarios(listaUsuarios)
        setProfesores(listaProfesores)
      })
      .catch((cause: unknown) => {
        if (activo) setError(cause instanceof Error ? cause.message : 'No se pudieron cargar las cuentas.')
      })
      .finally(() => { if (activo) setCargando(false) })
    return () => { activo = false }
  }, [])

  const crear = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setAviso('')
    setGuardando(true)
    try {
      await api.crearUsuario({ password, profesorId: Number(profesorId), perfil })
      await cargar()
      setPassword('')
      setProfesorId('')
      setPerfil('GESTION_PERMISOS')
      setAviso('La cuenta se ha creado correctamente.')
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo crear la cuenta.')
    } finally {
      setGuardando(false)
    }
  }

  const cambiarPerfil = async (usuario: UsuarioApp, nuevoPerfil: UsuarioSesion['role']) => {
    setError('')
    setAviso('')
    try {
      await api.actualizarPerfilUsuario(usuario.id, nuevoPerfil)
      await cargar()
      setAviso(`Se ha actualizado el permiso de ${usuario.email}.`)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cambiar el permiso.')
    }
  }

  const eliminar = async (usuario: UsuarioApp) => {
    if (!window.confirm(`¿Seguro que quieres eliminar la cuenta de ${usuario.email}?`)) return
    setError('')
    setAviso('')
    try {
      await api.eliminarUsuario(usuario.id)
      await cargar()
      setAviso('La cuenta se ha eliminado.')
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo eliminar la cuenta.')
    }
  }

  const profesoresDisponibles = profesores.filter(
    (profesor) => Boolean(profesor.email?.trim()) &&
      !usuarios.some((usuario) => usuario.profesorId === profesor.id),
  )
  const profesoresSinCuenta = profesores.filter(
    (profesor) => !usuarios.some((usuario) => usuario.profesorId === profesor.id),
  )

  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow"><span className="eyebrow-line" />ADMINISTRACIÓN</div>
          <h1>Cuentas de usuario</h1>
          <p>Crea accesos para profesores y asigna permisos de gestión del catálogo.</p>
        </div>
      </section>
      {error && <div className="alert alert-error" role="alert"><span>{error}</span><button className="alert-close" onClick={() => setError('')} aria-label="Cerrar error"><X size={16} /></button></div>}
      {aviso && <div className="alert alert-success" role="status"><Check size={17} /><span>{aviso}</span><button className="alert-close" onClick={() => setAviso('')} aria-label="Cerrar aviso"><X size={16} /></button></div>}
      <section className="catalog-workspace">
        <article className="panel create-panel">
          <div className="panel-heading">
            <div className="panel-title-icon"><Plus size={18} /></div>
            <div><h2>Nueva cuenta</h2><p>La contraseña inicial debe tener al menos 12 caracteres.</p></div>
          </div>
          <form className="permission-form catalog-form" onSubmit={crear}>
            <label className="field"><span>Profesor <b>*</b></span>
              <div className="select-wrap"><select value={profesorId} onChange={(event) => setProfesorId(event.target.value)} required>
                <option value="">Selecciona un profesor</option>
                {profesoresDisponibles.map((profesor) => <option key={profesor.id} value={profesor.id}>
                  {profesor.apellidos}, {profesor.nombre} · {profesor.email}
                </option>)}
              </select></div>
              {!profesoresDisponibles.length && <small className="field-help">
                {profesoresSinCuenta.length
                  ? 'Añade un correo electrónico a los profesores que quieras habilitar.'
                  : 'Todos los profesores ya tienen una cuenta.'}
              </small>}
            </label>
            <label className="field"><span>Contraseña inicial <b>*</b></span>
              <input type="password" autoComplete="new-password" minLength={12} maxLength={72} value={password}
                onChange={(event) => setPassword(event.target.value)} required /></label>
            <label className="field"><span>Permiso <b>*</b></span>
              <div className="select-wrap"><select value={perfil} onChange={(event) => setPerfil(event.target.value as UsuarioSesion['role'])}>
                <option value="GESTION_PERMISOS">Gestión de permisos</option>
                <option value="GESTION_CATALOGOS">Gestión del catálogo</option>
              </select></div>
            </label>
            <button className="submit-button" type="submit" disabled={guardando || cargando || !profesoresDisponibles.length}>
              {guardando ? <LoaderCircle size={17} className="spin" /> : <Plus size={17} />}
              {guardando ? 'Creando…' : 'Crear cuenta'}
            </button>
          </form>
        </article>
        <article className="panel catalog-list-panel">
          <div className="history-heading">
            <div className="panel-heading"><div className="panel-title-icon panel-title-icon-light"><Plus size={18} /></div>
              <div><h2>Cuentas activas</h2><p>Una cuenta por profesor del centro.</p></div></div>
            <span className="catalog-count">{usuarios.length}</span>
          </div>
          {cargando ? <div className="table-state"><LoaderCircle className="spin" size={22} /><span>Cargando cuentas…</span></div>
            : !usuarios.length ? <div className="table-state empty-state"><strong>No hay cuentas registradas</strong></div>
              : <div className="catalog-list">{usuarios.map((usuario) => (
                <div className="catalog-row" key={usuario.id}>
                  <div><strong>{usuario.profesorApellidos}, {usuario.profesorNombre}</strong>
                    <small>{usuario.email} · {usuario.profesorDni}</small></div>
                  <div className="catalog-actions">
                    <label className="sr-only" htmlFor={`perfil-${usuario.id}`}>Permiso de {usuario.email}</label>
                    <select id={`perfil-${usuario.id}`} value={usuario.perfil}
                      onChange={(event) => { void cambiarPerfil(usuario, event.target.value as UsuarioSesion['role']) }}>
                      <option value="GESTION_PERMISOS">Permisos</option>
                      <option value="GESTION_CATALOGOS">Catálogo</option>
                    </select>
                    <button className="icon-button delete-button" type="button"
                      onClick={() => { void eliminar(usuario) }} aria-label={`Eliminar cuenta ${usuario.email}`} title="Eliminar cuenta">
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>
              ))}</div>}
        </article>
      </section>
    </>
  )
}

export default GestionUsuarios
