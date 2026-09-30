import { useEffect, useMemo, useState } from 'react'
import type { FormEvent, KeyboardEvent } from 'react'
import {
  ArrowDownUp,
  Check,
  ChevronDown,
  CircleHelp,
  Clock3,
  FileClock,
  LayoutDashboard,
  ListFilter,
  LoaderCircle,
  Plus,
  Search,
  ShieldCheck,
  Toilet,
  UsersRound,
  X,
} from 'lucide-react'
import { api } from './api'
import GestionCatalogos from './GestionCatalogos'
import type { Alumno, FiltrosPermisos, FranjaHoraria, Grupo, Permiso, Profesor } from './types'

const fechaHoy = () => {
  const ahora = new Date()
  const zona = ahora.getTimezoneOffset() * 60_000
  return new Date(ahora.getTime() - zona).toISOString().slice(0, 10)
}

const horaActual = () => {
  const ahora = new Date()
  return `${String(ahora.getHours()).padStart(2, '0')}:${String(ahora.getMinutes()).padStart(2, '0')}`
}

const fechaLarga = new Intl.DateTimeFormat('es-ES', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
})

function nombreCompleto(persona: { nombre: string; apellidos: string }) {
  return `${persona.apellidos}, ${persona.nombre}`
}

function compararApellidos(
  a: { nombre: string; apellidos: string },
  b: { nombre: string; apellidos: string },
) {
  return a.apellidos.localeCompare(b.apellidos, 'es', { sensitivity: 'base' }) ||
    a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' })
}

function horaVisible(hora: string) {
  return hora.slice(0, 5)
}

function errorComoTexto(error: unknown) {
  return error instanceof Error ? error.message : 'Ha ocurrido un error inesperado.'
}

interface OpcionDesplegable {
  id: number
  etiqueta: string
}

function BuscadorDesplegable({
  etiqueta,
  placeholder,
  valor,
  opciones,
  deshabilitado = false,
  alCambiar,
}: {
  etiqueta: string
  placeholder: string
  valor: string
  opciones: OpcionDesplegable[]
  deshabilitado?: boolean
  alCambiar: (valor: string) => void
}) {
  const [abierto, setAbierto] = useState(false)
  const [busqueda, setBusqueda] = useState('')
  const [indiceActivo, setIndiceActivo] = useState(0)
  const idLista = `opciones-${etiqueta.toLocaleLowerCase('es').replace(/\s+/g, '-')}`
  const seleccionado = opciones.find((opcion) => String(opcion.id) === valor)
  const normalizar = (texto: string) => texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLocaleLowerCase('es')
  const opcionesFiltradas = opciones.filter((opcion) => normalizar(opcion.etiqueta).includes(normalizar(busqueda.trim())))

  const seleccionar = (opcion: OpcionDesplegable) => {
    alCambiar(String(opcion.id))
    setBusqueda('')
    setAbierto(false)
  }

  const manejarTeclado = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Escape') {
      setBusqueda('')
      setAbierto(false)
    } else if (event.key === 'ArrowDown') {
      event.preventDefault()
      setAbierto(true)
      setIndiceActivo((indice) => Math.min(indice + 1, opcionesFiltradas.length - 1))
    } else if (event.key === 'ArrowUp') {
      event.preventDefault()
      setIndiceActivo((indice) => Math.max(indice - 1, 0))
    } else if (event.key === 'Enter' && abierto && opcionesFiltradas[indiceActivo]) {
      event.preventDefault()
      seleccionar(opcionesFiltradas[indiceActivo])
    }
  }

  return (
    <label className="field">
      <span>{etiqueta} <b>*</b></span>
      <div className="search-select">
        <input
          role="combobox"
          aria-label={etiqueta}
          aria-autocomplete="list"
          aria-expanded={abierto}
          aria-controls={idLista}
          aria-activedescendant={abierto && opcionesFiltradas[indiceActivo] ? `${idLista}-${opcionesFiltradas[indiceActivo].id}` : undefined}
          autoComplete="off"
          value={abierto ? busqueda : seleccionado?.etiqueta ?? ''}
          placeholder={deshabilitado ? placeholder : seleccionado?.etiqueta ?? placeholder}
          disabled={deshabilitado}
          onFocus={() => { setBusqueda(''); setIndiceActivo(0); setAbierto(true) }}
          onChange={(event) => { setBusqueda(event.target.value); setIndiceActivo(0); setAbierto(true); alCambiar('') }}
          onKeyDown={manejarTeclado}
          onBlur={() => { setAbierto(false); setBusqueda('') }}
        />
        <ChevronDown size={16} aria-hidden="true" />
        {abierto && <div className="search-select-options" id={idLista} role="listbox" aria-label={etiqueta}>
          {opcionesFiltradas.length ? opcionesFiltradas.map((opcion, indice) => (
            <button
              type="button"
              role="option"
              aria-selected={String(opcion.id) === valor}
              id={`${idLista}-${opcion.id}`}
              className={indice === indiceActivo ? 'search-select-option highlighted' : 'search-select-option'}
              key={opcion.id}
              onMouseDown={(event) => event.preventDefault()}
              onMouseEnter={() => setIndiceActivo(indice)}
              onClick={() => seleccionar(opcion)}
            >
              {opcion.etiqueta}
            </button>
          )) : <span className="search-select-empty">No hay resultados</span>}
        </div>}
      </div>
    </label>
  )
}

function App() {
  const [vista, setVista] = useState<'permisos' | 'gestion'>('permisos')
  const [alumnos, setAlumnos] = useState<Alumno[]>([])
  const [grupos, setGrupos] = useState<Grupo[]>([])
  const [profesores, setProfesores] = useState<Profesor[]>([])
  const [franjas, setFranjas] = useState<FranjaHoraria[]>([])
  const [permisos, setPermisos] = useState<Permiso[]>([])
  const [fecha, setFecha] = useState(fechaHoy)
  const [grupoId, setGrupoId] = useState('')
  const [alumnoId, setAlumnoId] = useState('')
  const [profesorId, setProfesorId] = useState('')
  const [franjaId, setFranjaId] = useState('')
  const [hora, setHora] = useState(horaActual)
  const [busqueda, setBusqueda] = useState('')
  const [filtroGrupoHistorial, setFiltroGrupoHistorial] = useState('')
  const [filtroAlumnoHistorial, setFiltroAlumnoHistorial] = useState('')
  const [filtroProfesorHistorial, setFiltroProfesorHistorial] = useState('')
  const [filtroFranjaHistorial, setFiltroFranjaHistorial] = useState('')
  const [cargandoCatalogos, setCargandoCatalogos] = useState(true)
  const [cargandoPermisos, setCargandoPermisos] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')

  useEffect(() => {
    let activo = true
    Promise.all([api.listarAlumnos(), api.listarGrupos(), api.listarProfesores(), api.listarFranjas()])
      .then(([listaAlumnos, listaGrupos, listaProfesores, listaFranjas]) => {
        if (!activo) return
        setAlumnos(listaAlumnos)
        setGrupos(listaGrupos)
        setProfesores(listaProfesores)
        setFranjas(listaFranjas)
        if (listaProfesores.length) setProfesorId(String(listaProfesores[0].id))
        if (listaFranjas.length) setFranjaId(String(listaFranjas[0].id))
      })
      .catch((cause: unknown) => {
        if (activo) setError(errorComoTexto(cause))
      })
      .finally(() => {
        if (activo) setCargandoCatalogos(false)
      })
    return () => { activo = false }
  }, [])

  useEffect(() => {
    let activo = true
    setCargandoPermisos(true)
    setError('')
    const filtros: FiltrosPermisos = {
      grupoId: filtroGrupoHistorial ? Number(filtroGrupoHistorial) : undefined,
      alumnoId: filtroAlumnoHistorial ? Number(filtroAlumnoHistorial) : undefined,
      profesorId: filtroProfesorHistorial ? Number(filtroProfesorHistorial) : undefined,
      franjaHorariaId: filtroFranjaHistorial ? Number(filtroFranjaHistorial) : undefined,
    }
    api.listarPermisos(fecha, filtros)
      .then((lista) => {
        if (activo) setPermisos(lista)
      })
      .catch((cause: unknown) => {
        if (activo) setError(errorComoTexto(cause))
      })
      .finally(() => {
        if (activo) setCargandoPermisos(false)
      })
    return () => { activo = false }
  }, [fecha, filtroGrupoHistorial, filtroAlumnoHistorial, filtroProfesorHistorial, filtroFranjaHistorial])

  const permisosFiltrados = useMemo(() => {
    const termino = busqueda.trim().toLocaleLowerCase('es')
    if (!termino) return permisos
    return permisos.filter((permiso) =>
      [
        nombreCompleto(permiso.alumno),
        permiso.alumno.dni,
        permiso.alumno.grupo.codigo,
        nombreCompleto(permiso.profesor),
        permiso.franjaHoraria.nombre,
      ].some((dato) => dato.toLocaleLowerCase('es').includes(termino)),
    )
  }, [busqueda, permisos])

  const crearPermiso = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setAviso('')
    setGuardando(true)
    try {
      await api.crearPermiso({
        alumnoId: Number(alumnoId),
        profesorId: Number(profesorId),
        franjaHorariaId: Number(franjaId),
        fecha,
        hora: `${hora}:00`,
      })
      const actualizados = await api.listarPermisos(fecha, {
        grupoId: filtroGrupoHistorial ? Number(filtroGrupoHistorial) : undefined,
        alumnoId: filtroAlumnoHistorial ? Number(filtroAlumnoHistorial) : undefined,
        profesorId: filtroProfesorHistorial ? Number(filtroProfesorHistorial) : undefined,
        franjaHorariaId: filtroFranjaHistorial ? Number(filtroFranjaHistorial) : undefined,
      })
      setPermisos(actualizados)
      setAviso('Permiso registrado correctamente.')
      setHora(horaActual())
    } catch (cause) {
      setError(errorComoTexto(cause))
    } finally {
      setGuardando(false)
    }
  }

  const alumnosHoy = new Set(permisos.map((permiso) => permiso.alumno.id)).size
  const franjasOrdenadas = [...franjas].sort((a, b) => a.numero - b.numero)
  const gruposOrdenados = [...grupos].sort((a, b) =>
    a.codigo.localeCompare(b.codigo, 'es', { sensitivity: 'base' }))
  const gruposHistorialOrdenados = gruposOrdenados
  const alumnosHistorialOrdenados = [...alumnos].sort(compararApellidos)
  const alumnosDelGrupo = alumnos
    .filter((alumno) => String(alumno.grupo.id) === grupoId)
    .sort(compararApellidos)
  const profesoresOrdenados = [...profesores].sort(compararApellidos)
  const opcionesGrupo = gruposOrdenados.map((grupo) => ({ id: grupo.id, etiqueta: grupo.codigo }))
  const opcionesAlumno = alumnosDelGrupo.map((alumno) => ({ id: alumno.id, etiqueta: nombreCompleto(alumno) }))
  const opcionesProfesor = profesoresOrdenados.map((profesor) => ({ id: profesor.id, etiqueta: nombreCompleto(profesor) }))
  const hayFiltrosHistorial = Boolean(
    busqueda || filtroGrupoHistorial || filtroAlumnoHistorial || filtroProfesorHistorial || filtroFranjaHistorial,
  )
  const limpiarFiltrosHistorial = () => {
    setBusqueda('')
    setFiltroGrupoHistorial('')
    setFiltroAlumnoHistorial('')
    setFiltroProfesorHistorial('')
    setFiltroFranjaHistorial('')
  }
  const noHayCatalogos = !cargandoCatalogos &&
    (!alumnos.length || !grupos.length || !profesores.length || !franjas.length)

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#" aria-label="BañoControl, inicio">
          <span className="brand-mark"><Toilet size={22} strokeWidth={2.3} /></span>
          <span className="brand-name">baño<span>control</span></span>
        </a>
        <div className="school-chip">
          <span className="school-monogram">IR</span>
          <span><strong>IES Reyes</strong><small>Panel del centro</small></span>
          <ChevronDown size={15} className="school-chevron" />
        </div>

        <div className="nav-label">MENÚ PRINCIPAL</div>
        <nav className="main-nav" aria-label="Navegación principal">
          <button className="nav-link" type="button" onClick={() => setVista('permisos')}><LayoutDashboard size={18} />Resumen</button>
          <button className={`nav-link${vista === 'permisos' ? ' active' : ''}`} type="button" onClick={() => setVista('permisos')}><FileClock size={18} />Permisos<span className="nav-count">{permisos.length}</span></button>
        </nav>
        <div className="nav-label nav-label-spaced">GESTIÓN</div>
        <button className={`nav-link nav-static${vista === 'gestion' ? ' active' : ''}`} type="button" onClick={() => setVista('gestion')}><UsersRound size={18} />Comunidad escolar</button>
        <div className="sidebar-bottom">
          <div className="help-card">
            <span className="help-icon"><CircleHelp size={18} /></span>
            <strong>¿Necesitas ayuda?</strong>
            <p>Contacta con el equipo de administración del centro.</p>
          </div>
          <div className="sidebar-user">
            <span className="avatar avatar-purple">AD</span>
            <span><strong>Administración</strong><small>IES Reyes</small></span>
            <span className="online-dot" />
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">Centro <span>/</span> <strong>{vista === 'permisos' ? 'Permisos de baño' : 'Comunidad escolar'}</strong></div>
          <div className="topbar-right">
            <div className="today-pill"><span /> Sistema operativo</div>
            <span className="avatar avatar-purple top-avatar">AD</span>
          </div>
        </header>

        <div className="page-content">
          {vista === 'gestion' ? <GestionCatalogos /> : <>
          <section className="page-heading" id="resumen">
            <div>
              <div className="eyebrow"><span className="eyebrow-line" />GESTIÓN DIARIA</div>
              <h1>Permisos de baño</h1>
              <p>Controla las salidas al baño de forma sencilla y organizada.</p>
            </div>
            <div className="current-date"><Clock3 size={16} />{fechaLarga.format(new Date(`${fechaHoy()}T12:00:00`))}</div>
          </section>

          <section className="stats-grid" aria-label="Resumen del día">
            <article className="stat-card">
              <div className="stat-top"><span>Permisos del día</span><span className="stat-icon icon-blue"><FileClock size={18} /></span></div>
              <div className="stat-value">{fecha === fechaHoy() ? permisos.length : '—'}</div>
              <div className="stat-foot">Registrados para hoy</div>
            </article>
            <article className="stat-card">
              <div className="stat-top"><span>Alumnos atendidos</span><span className="stat-icon icon-green"><UsersRound size={18} /></span></div>
              <div className="stat-value">{fecha === fechaHoy() ? alumnosHoy : '—'}</div>
              <div className="stat-foot">Alumnos diferentes</div>
            </article>
            <article className="stat-card stat-highlight">
              <div className="stat-top"><span>Estado del sistema</span><span className="stat-icon icon-white"><ShieldCheck size={18} /></span></div>
              <div className="status-value"><span /> En funcionamiento</div>
              <div className="stat-foot">Conectado con el centro</div>
            </article>
          </section>

          {error && (
            <div className="alert alert-error" role="alert">
              <span>{error}</span>
              <button className="alert-close" onClick={() => setError('')} aria-label="Cerrar error"><X size={16} /></button>
            </div>
          )}
          {aviso && (
            <div className="alert alert-success" role="status">
              <Check size={17} /><span>{aviso}</span>
              <button className="alert-close" onClick={() => setAviso('')} aria-label="Cerrar aviso"><X size={16} /></button>
            </div>
          )}

          <section className="workspace">
            <article className="panel create-panel">
              <div className="panel-heading">
                <div className="panel-title-icon"><Plus size={18} /></div>
                <div><h2>Nuevo permiso</h2><p>Completa los datos para registrar una salida.</p></div>
              </div>
              {noHayCatalogos && (
                <div className="inline-warning">Faltan alumnos, grupos, profesores o franjas horarias. Carga los catálogos en la API antes de registrar permisos.</div>
              )}
              <form className="permission-form" onSubmit={crearPermiso}>
                <BuscadorDesplegable
                  etiqueta="Grupo"
                  placeholder="Busca o selecciona un grupo"
                  valor={grupoId}
                  opciones={opcionesGrupo}
                  deshabilitado={cargandoCatalogos || !grupos.length}
                  alCambiar={(valor) => { setGrupoId(valor); setAlumnoId('') }}
                />
                <BuscadorDesplegable
                  etiqueta="Alumno"
                  placeholder={grupoId ? 'Busca o selecciona un alumno' : 'Selecciona primero un grupo'}
                  valor={alumnoId}
                  opciones={opcionesAlumno}
                  deshabilitado={cargandoCatalogos || !grupoId || !alumnosDelGrupo.length}
                  alCambiar={setAlumnoId}
                />
                {grupoId && !alumnosDelGrupo.length && <small className="field-help">No hay alumnos en este grupo.</small>}
                <BuscadorDesplegable
                  etiqueta="Profesor responsable"
                  placeholder="Busca o selecciona un profesor"
                  valor={profesorId}
                  opciones={opcionesProfesor}
                  deshabilitado={cargandoCatalogos || !profesores.length}
                  alCambiar={setProfesorId}
                />
                <label className="field">
                  <span>Franja horaria <b>*</b></span>
                  <div className="select-wrap">
                    <select value={franjaId} onChange={(event) => setFranjaId(event.target.value)} required disabled={cargandoCatalogos || !franjas.length}>
                      <option value="" disabled>Selecciona una franja</option>
                      {franjasOrdenadas.map((franja) => (
                        <option key={franja.id} value={franja.id}>{franja.nombre}</option>
                      ))}
                    </select><ChevronDown size={16} />
                  </div>
                </label>
                <div className="field-row">
                  <label className="field">
                    <span>Fecha <b>*</b></span>
                    <input type="date" value={fecha} onChange={(event) => setFecha(event.target.value)} required />
                  </label>
                  <label className="field">
                    <span>Hora <b>*</b></span>
                    <input type="time" value={hora} onChange={(event) => setHora(event.target.value)} required />
                  </label>
                </div>
                <div className="form-note"><ShieldCheck size={15} />El permiso quedará registrado en el historial del centro.</div>
                <button className="submit-button" type="submit" disabled={guardando || cargandoCatalogos || noHayCatalogos || !grupoId || !alumnoId}>
                  {guardando ? <LoaderCircle size={17} className="spin" /> : <Plus size={17} />}
                  {guardando ? 'Registrando permiso…' : 'Registrar permiso'}
                </button>
              </form>
            </article>

            <article className="panel history-panel" id="permisos">
              <div className="history-heading">
                <div className="panel-heading">
                  <div className="panel-title-icon panel-title-icon-light"><FileClock size={18} /></div>
                  <div><h2>Historial de permisos</h2><p>Consulta y filtra las salidas registradas.</p></div>
                </div>
                <button className="icon-button" type="button" onClick={() => setFecha(fechaHoy())} title="Mostrar los permisos de hoy" aria-label="Mostrar permisos de hoy">
                  <ArrowDownUp size={17} />
                </button>
              </div>
              <div className="history-tools">
                <div className="search-box"><Search size={16} /><input value={busqueda} onChange={(event) => setBusqueda(event.target.value)} placeholder="Buscar alumno, grupo…" aria-label="Buscar en permisos" /></div>
                <label className="date-filter"><ListFilter size={15} /><span className="sr-only">Filtrar por fecha</span><input type="date" value={fecha} onChange={(event) => setFecha(event.target.value)} /></label>
              </div>
              <div className="history-filters">
                <label className="history-filter-field"><span>Grupo</span><select value={filtroGrupoHistorial} onChange={(event) => setFiltroGrupoHistorial(event.target.value)} aria-label="Filtrar historial por grupo">
                  <option value="">Todos</option>{gruposHistorialOrdenados.map((grupo) => <option key={grupo.id} value={grupo.id}>{grupo.codigo}</option>)}
                </select></label>
                <label className="history-filter-field"><span>Alumno</span><select value={filtroAlumnoHistorial} onChange={(event) => setFiltroAlumnoHistorial(event.target.value)} aria-label="Filtrar historial por alumno">
                  <option value="">Todos</option>{alumnosHistorialOrdenados.map((alumno) => <option key={alumno.id} value={alumno.id}>{nombreCompleto(alumno)}</option>)}
                </select></label>
                <label className="history-filter-field"><span>Profesor</span><select value={filtroProfesorHistorial} onChange={(event) => setFiltroProfesorHistorial(event.target.value)} aria-label="Filtrar historial por profesor">
                  <option value="">Todos</option>{profesoresOrdenados.map((profesor) => <option key={profesor.id} value={profesor.id}>{nombreCompleto(profesor)}</option>)}
                </select></label>
                <label className="history-filter-field"><span>Franja</span><select value={filtroFranjaHistorial} onChange={(event) => setFiltroFranjaHistorial(event.target.value)} aria-label="Filtrar historial por franja horaria">
                  <option value="">Todas</option>{franjasOrdenadas.map((franja) => <option key={franja.id} value={franja.id}>{franja.nombre}</option>)}
                </select></label>
                <button className="clear-history-filters" type="button" onClick={limpiarFiltrosHistorial} disabled={!hayFiltrosHistorial}>Limpiar filtros</button>
              </div>
              <div className="table-head">
                <span>ALUMNO</span><span>GRUPO</span><span>HORA / FRANJA</span><span>PROFESOR</span>
              </div>
              {cargandoPermisos ? (
                <div className="table-state"><LoaderCircle className="spin" size={22} /><span>Cargando permisos…</span></div>
              ) : permisosFiltrados.length ? (
                <div className="permission-list">
                  {permisosFiltrados.map((permiso) => (
                    <div className="permission-row" key={permiso.id}>
                      <div className="student-cell">
                        <span className="avatar student-avatar">{permiso.alumno.nombre.slice(0, 1)}{permiso.alumno.apellidos.slice(0, 1)}</span>
                        <span><strong>{nombreCompleto(permiso.alumno)}</strong><small>{permiso.alumno.dni}</small></span>
                      </div>
                      <div className="group-cell"><span className="group-badge">{permiso.alumno.grupo.codigo}</span></div>
                      <div className="time-cell"><strong>{horaVisible(permiso.hora)}</strong><small>{permiso.franjaHoraria.nombre}</small></div>
                      <div className="teacher-cell"><span className="teacher-dot">{permiso.profesor.nombre.slice(0, 1)}</span><span>{nombreCompleto(permiso.profesor)}</span></div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="table-state empty-state">
                  <span className="empty-icon"><FileClock size={22} /></span>
                  <strong>{hayFiltrosHistorial ? 'No hay resultados' : 'Todavía no hay permisos'}</strong>
                  <span>{hayFiltrosHistorial ? 'Prueba con otros filtros.' : 'Los permisos registrados para esta fecha aparecerán aquí.'}</span>
                </div>
              )}
              <div className="list-footer">
                <span>{permisosFiltrados.length} {permisosFiltrados.length === 1 ? 'permiso' : 'permisos'}{hayFiltrosHistorial ? ' encontrados' : ''}</span>
                <span className="live-indicator"><span /> Datos actualizados</span>
              </div>
            </article>
          </section>
          <footer className="page-footer">© {new Date().getFullYear()} IES Reyes <span>·</span> Gestión de permisos de baño</footer>
          </>}
        </div>
      </main>
    </div>
  )
}

export default App
