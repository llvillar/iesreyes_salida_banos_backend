import { useEffect, useMemo, useState } from 'react'
import type { FormEvent, KeyboardEvent } from 'react'
import {
  CalendarDays,
  Check,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  CircleHelp,
  Clock3,
  FileClock,
  KeyRound,
  LayoutDashboard,
  ListFilter,
  LoaderCircle,
  LogOut,
  Plus,
  Search,
  ShieldCheck,
  Toilet,
  Trophy,
  UsersRound,
  X,
} from 'lucide-react'
import { api } from './api'
import GestionCatalogos from './GestionCatalogos'
import GestionUsuarios from './GestionUsuarios'
import CambioContrasena from './CambioContrasena'
import Login from './Login'
import type { Alumno, FiltrosPermisos, FranjaHoraria, Grupo, Permiso, Profesor, UsuarioSesion } from './types'

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
const permisosPorPagina = 5

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

function desplazarFecha(fecha: string, dias: number) {
  const [anio, mes, dia] = fecha.split('-').map(Number)
  const fechaNueva = new Date(Date.UTC(anio, mes - 1, dia + dias))
  return fechaNueva.toISOString().slice(0, 10)
}

function fechaEnCastellano(fecha: string) {
  return fechaLarga.format(new Date(`${fecha}T12:00:00`))
}

function etiquetaPeriodo(desde: string, hasta: string) {
  if (desde && hasta && desde === hasta) return fechaEnCastellano(desde)
  if (desde && hasta) return `${fechaEnCastellano(desde)} — ${fechaEnCastellano(hasta)}`
  if (desde) return `Desde ${fechaEnCastellano(desde)}`
  if (hasta) return `Hasta ${fechaEnCastellano(hasta)}`
  return 'Todas las fechas'
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

function CriteriosPermisos({
  desde,
  hasta,
  grupoId,
  alumnoId,
  profesorId,
  franjaId,
  grupos,
  alumnos,
  profesores,
  franjas,
  hayFiltros,
  onDesde,
  onHasta,
  onGrupo,
  onAlumno,
  onProfesor,
  onFranja,
  onLimpiar,
  onIrAHoy,
  onDesplazar,
}: {
  desde: string
  hasta: string
  grupoId: string
  alumnoId: string
  profesorId: string
  franjaId: string
  grupos: Grupo[]
  alumnos: Alumno[]
  profesores: Profesor[]
  franjas: FranjaHoraria[]
  hayFiltros: boolean
  onDesde: (value: string) => void
  onHasta: (value: string) => void
  onGrupo: (value: string) => void
  onAlumno: (value: string) => void
  onProfesor: (value: string) => void
  onFranja: (value: string) => void
  onLimpiar: () => void
  onIrAHoy: () => void
  onDesplazar: (dias: number) => void
}) {
  return (
    <section className="panel criteria-panel" aria-label="Criterios del resumen y del historial">
      <div className="history-heading">
        <div className="panel-heading">
          <div className="panel-title-icon"><ListFilter size={18} /></div>
          <div><h2>Filtrar actividad</h2><p>Los criterios se aplican al resumen y al historial de permisos.</p></div>
        </div>
        <button className="icon-button" type="button" onClick={onIrAHoy} title="Mostrar los datos de hoy" aria-label="Mostrar datos de hoy">
          <CalendarDays size={17} />
        </button>
      </div>
      <div className="history-tools">
        <div className="day-navigation" aria-label="Navegar por fechas">
          <button type="button" onClick={() => onDesplazar(-1)} title="Desplazar el rango un día atrás" aria-label="Día anterior"><ChevronLeft size={16} /><span>Anterior</span></button>
          <button type="button" onClick={() => onDesplazar(1)} title="Desplazar el rango un día adelante" aria-label="Día siguiente"><span>Siguiente</span><ChevronRight size={16} /></button>
        </div>
      </div>
      <div className="history-date-range">
        <label className="history-filter-field"><span>Desde</span><input type="date" value={desde} onChange={(event) => onDesde(event.target.value)} aria-label="Fecha inicial" /></label>
        <span className="date-range-separator">—</span>
        <label className="history-filter-field"><span>Hasta</span><input type="date" value={hasta} onChange={(event) => onHasta(event.target.value)} aria-label="Fecha final" /></label>
        <span className="range-hint"><ListFilter size={14} />Rango inclusivo</span>
      </div>
      <div className="history-filters">
        <label className="history-filter-field"><span>Grupo</span><select value={grupoId} onChange={(event) => onGrupo(event.target.value)} aria-label="Filtrar por grupo">
          <option value="">Todos</option>{grupos.map((grupo) => <option key={grupo.id} value={grupo.id}>{grupo.codigo}</option>)}
        </select></label>
        <label className="history-filter-field"><span>Alumno</span><select value={alumnoId} onChange={(event) => onAlumno(event.target.value)} aria-label="Filtrar por alumno">
          <option value="">Todos</option>{alumnos.map((alumno) => <option key={alumno.id} value={alumno.id}>{nombreCompleto(alumno)}</option>)}
        </select></label>
        <label className="history-filter-field"><span>Profesor</span><select value={profesorId} onChange={(event) => onProfesor(event.target.value)} aria-label="Filtrar por profesor">
          <option value="">Todos</option>{profesores.map((profesor) => <option key={profesor.id} value={profesor.id}>{nombreCompleto(profesor)}</option>)}
        </select></label>
        <label className="history-filter-field"><span>Franja</span><select value={franjaId} onChange={(event) => onFranja(event.target.value)} aria-label="Filtrar por franja horaria">
          <option value="">Todas</option>{franjas.map((franja) => <option key={franja.id} value={franja.id}>{franja.nombre}</option>)}
        </select></label>
        <button className="clear-history-filters" type="button" onClick={onLimpiar} disabled={!hayFiltros}>Limpiar filtros</button>
      </div>
    </section>
  )
}

function App() {
  const [usuario, setUsuario] = useState<UsuarioSesion | null>(null)
  const [comprobandoSesion, setComprobandoSesion] = useState(true)
  const [errorSesion, setErrorSesion] = useState('')

  useEffect(() => {
    let activo = true
    api.usuarioActual()
      .then((sesion) => { if (activo) setUsuario(sesion) })
      .catch((cause: unknown) => {
        if (!activo) return
        const estado = (cause as Error & { status?: number }).status
        if (estado !== 401) setErrorSesion(errorComoTexto(cause))
      })
      .finally(() => { if (activo) setComprobandoSesion(false) })
    return () => { activo = false }
  }, [])

  if (comprobandoSesion) {
    return <main className="login-page"><div className="table-state"><LoaderCircle className="spin" size={22} /><span>Comprobando sesión…</span></div></main>
  }
  if (errorSesion) {
    return <main className="login-page"><div className="alert alert-error" role="alert">{errorSesion}</div></main>
  }
  if (!usuario) return <Login onLogin={setUsuario} />

  const cerrarSesion = async () => {
    await api.cerrarSesion()
    setUsuario(null)
  }
  return <AuthenticatedApp usuario={usuario} onLogout={cerrarSesion} />
}

function AuthenticatedApp({
  usuario,
  onLogout,
}: {
  usuario: UsuarioSesion
  onLogout: () => Promise<void>
}) {
  const [vista, setVista] = useState<'resumen' | 'permisos' | 'gestion' | 'usuarios' | 'cuenta'>('resumen')
  const [alumnos, setAlumnos] = useState<Alumno[]>([])
  const [grupos, setGrupos] = useState<Grupo[]>([])
  const [profesores, setProfesores] = useState<Profesor[]>([])
  const [franjas, setFranjas] = useState<FranjaHoraria[]>([])
  const [permisos, setPermisos] = useState<Permiso[]>([])
  const [paginaHistorial, setPaginaHistorial] = useState(0)
  const [fecha, setFecha] = useState(fechaHoy)
  const [historialDesde, setHistorialDesde] = useState(fechaHoy)
  const [historialHasta, setHistorialHasta] = useState(fechaHoy)
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
  const puedeGestionarCatalogos = usuario.role === 'GESTION_CATALOGOS'
  const rolVisible = puedeGestionarCatalogos ? 'Gestión de catálogos' : 'Gestión de salidas'

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
    if (historialDesde && historialHasta && historialDesde > historialHasta) {
      setError('La fecha inicial no puede ser posterior a la fecha final.')
      setPermisos([])
      setCargandoPermisos(false)
      return () => { activo = false }
    }
    const filtros: FiltrosPermisos = {
      desde: historialDesde || undefined,
      hasta: historialHasta || undefined,
      grupoId: filtroGrupoHistorial ? Number(filtroGrupoHistorial) : undefined,
      alumnoId: filtroAlumnoHistorial ? Number(filtroAlumnoHistorial) : undefined,
      profesorId: filtroProfesorHistorial ? Number(filtroProfesorHistorial) : undefined,
      franjaHorariaId: filtroFranjaHistorial ? Number(filtroFranjaHistorial) : undefined,
    }
    setCargandoPermisos(true)
    setError('')
    api.listarPermisos(undefined, filtros)
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
  }, [historialDesde, historialHasta, filtroGrupoHistorial, filtroAlumnoHistorial, filtroProfesorHistorial, filtroFranjaHistorial])

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

  useEffect(() => {
    setPaginaHistorial(0)
  }, [
    busqueda,
    historialDesde,
    historialHasta,
    filtroGrupoHistorial,
    filtroAlumnoHistorial,
    filtroProfesorHistorial,
    filtroFranjaHistorial,
  ])

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
      if (historialDesde && historialHasta && historialDesde > historialHasta) {
        setAviso('Permiso registrado correctamente.')
        setHora(horaActual())
        return
      }
      const actualizados = await api.listarPermisos(undefined, {
        desde: historialDesde || undefined,
        hasta: historialHasta || undefined,
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

  const alumnosAtendidos = new Set(permisos.map((permiso) => permiso.alumno.id)).size
  const totalPaginasHistorial = Math.ceil(permisosFiltrados.length / permisosPorPagina)
  const permisosPagina = permisosFiltrados.slice(
    paginaHistorial * permisosPorPagina,
    (paginaHistorial + 1) * permisosPorPagina,
  )
  const primerPermisoPagina = permisosFiltrados.length ? paginaHistorial * permisosPorPagina + 1 : 0
  const ultimoPermisoPagina = Math.min((paginaHistorial + 1) * permisosPorPagina, permisosFiltrados.length)
  const topAlumnos = [...permisos.reduce((ranking, permiso) => {
    const alumno = ranking.get(permiso.alumno.id)
    if (alumno) {
      alumno.permisos += 1
    } else {
      ranking.set(permiso.alumno.id, { alumno: permiso.alumno, permisos: 1 })
    }
    return ranking
  }, new Map<number, { alumno: Alumno; permisos: number }>()).values()]
    .sort((a, b) => b.permisos - a.permisos ||
      compararApellidos(a.alumno, b.alumno))
    .slice(0, 10)
  const maxPermisosTop = topAlumnos[0]?.permisos ?? 0
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
  const hayFiltrosResumen = Boolean(
    historialDesde !== fechaHoy() || historialHasta !== fechaHoy() ||
      filtroGrupoHistorial || filtroAlumnoHistorial || filtroProfesorHistorial || filtroFranjaHistorial,
  )
  const periodoVisible = etiquetaPeriodo(historialDesde, historialHasta)
  const hayFiltrosHistorial = hayFiltrosResumen || Boolean(busqueda)
  const desplazarRangoHistorial = (dias: number) => {
    const desde = historialDesde || historialHasta || fechaHoy()
    const hasta = historialHasta || historialDesde || fechaHoy()
    setHistorialDesde(desplazarFecha(desde, dias))
    setHistorialHasta(desplazarFecha(hasta, dias))
  }
  const irAHoy = () => {
    const hoy = fechaHoy()
    setHistorialDesde(hoy)
    setHistorialHasta(hoy)
  }
  const limpiarFiltrosResumen = () => {
    irAHoy()
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
          <button className={`nav-link${vista === 'resumen' ? ' active' : ''}`} type="button" onClick={() => setVista('resumen')}><LayoutDashboard size={18} />Resumen</button>
          <button className={`nav-link${vista === 'permisos' ? ' active' : ''}`} type="button" onClick={() => setVista('permisos')}><FileClock size={18} />Permisos<span className="nav-count">{permisos.length}</span></button>
        </nav>
        {puedeGestionarCatalogos && <>
          <div className="nav-label nav-label-spaced">GESTIÓN</div>
          <button className={`nav-link nav-static${vista === 'gestion' ? ' active' : ''}`} type="button" onClick={() => setVista('gestion')}><UsersRound size={18} />Comunidad escolar</button>
          <button className={`nav-link nav-static${vista === 'usuarios' ? ' active' : ''}`} type="button" onClick={() => setVista('usuarios')}><UsersRound size={18} />Cuentas de usuario</button>
        </>}
        <div className="nav-label nav-label-spaced">CUENTA</div>
        <button className={`nav-link nav-static${vista === 'cuenta' ? ' active' : ''}`} type="button" onClick={() => setVista('cuenta')}><KeyRound size={18} />Cambiar contraseña</button>
        <div className="sidebar-bottom">
          <div className="help-card">
            <span className="help-icon"><CircleHelp size={18} /></span>
            <strong>¿Necesitas ayuda?</strong>
            <p>Contacta con el equipo de administración del centro.</p>
          </div>
          <div className="sidebar-user">
            <span className="avatar avatar-purple">{usuario.email.slice(0, 2).toLocaleUpperCase('es')}</span>
            <span><strong>{usuario.email}</strong><small>{rolVisible}</small></span>
            <span className="online-dot" />
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">Centro <span>/</span> <strong>{
            vista === 'resumen' ? 'Resumen'
              : vista === 'permisos' ? 'Permisos de baño'
                : vista === 'gestion' ? 'Comunidad escolar'
                  : vista === 'usuarios' ? 'Cuentas de usuario' : 'Mi cuenta'
          }</strong></div>
          <div className="topbar-right">
            <div className="today-pill"><span /> Sistema operativo</div>
            <span className="avatar avatar-purple top-avatar">{usuario.email.slice(0, 2).toLocaleUpperCase('es')}</span>
            <button className="logout-button" type="button" onClick={() => { void onLogout().catch((cause: unknown) => setError(errorComoTexto(cause))) }} aria-label="Cerrar sesión" title="Cerrar sesión"><LogOut size={17} /></button>
          </div>
        </header>

        <div className="page-content">
          {vista === 'gestion' && puedeGestionarCatalogos ? <GestionCatalogos />
            : vista === 'usuarios' && puedeGestionarCatalogos ? <GestionUsuarios />
              : vista === 'cuenta' ? <CambioContrasena /> : vista === 'resumen' ? <>
          <section className="page-heading" id="resumen">
            <div>
              <div className="eyebrow"><span className="eyebrow-line" />RESUMEN</div>
              <h1>Resumen de actividad</h1>
              <p>Consulta las estadísticas y el top 10 según los criterios seleccionados.</p>
            </div>
            <div className="current-date"><Clock3 size={16} />{periodoVisible}</div>
          </section>

          {error && (
            <div className="alert alert-error" role="alert">
              <span>{error}</span>
              <button className="alert-close" onClick={() => setError('')} aria-label="Cerrar error"><X size={16} /></button>
            </div>
          )}
          <section className="stats-grid" aria-label="Resumen del periodo seleccionado">
            <article className="stat-card">
              <div className="stat-top"><span>Permisos según los filtros</span><span className="stat-icon icon-blue"><FileClock size={18} /></span></div>
              <div className="stat-value">{permisos.length}</div>
              <div className="stat-foot">En el periodo y criterios elegidos</div>
            </article>
            <article className="stat-card">
              <div className="stat-top"><span>Alumnos atendidos</span><span className="stat-icon icon-green"><UsersRound size={18} /></span></div>
              <div className="stat-value">{alumnosAtendidos}</div>
              <div className="stat-foot">Alumnos diferentes en la selección</div>
            </article>
            <article className="stat-card stat-highlight">
              <div className="stat-top"><span>Estado del sistema</span><span className="stat-icon icon-white"><ShieldCheck size={18} /></span></div>
              <div className="status-value"><span /> En funcionamiento</div>
              <div className="stat-foot">Conectado con el centro</div>
            </article>
          </section>

          <CriteriosPermisos
            desde={historialDesde}
            hasta={historialHasta}
            grupoId={filtroGrupoHistorial}
            alumnoId={filtroAlumnoHistorial}
            profesorId={filtroProfesorHistorial}
            franjaId={filtroFranjaHistorial}
            grupos={gruposHistorialOrdenados}
            alumnos={alumnosHistorialOrdenados}
            profesores={profesoresOrdenados}
            franjas={franjasOrdenadas}
            hayFiltros={hayFiltrosResumen}
            onDesde={setHistorialDesde}
            onHasta={setHistorialHasta}
            onGrupo={setFiltroGrupoHistorial}
            onAlumno={setFiltroAlumnoHistorial}
            onProfesor={setFiltroProfesorHistorial}
            onFranja={setFiltroFranjaHistorial}
            onLimpiar={limpiarFiltrosResumen}
            onIrAHoy={irAHoy}
            onDesplazar={desplazarRangoHistorial}
          />

          <section className="panel ranking-panel" aria-labelledby="ranking-title">
            <div className="ranking-heading">
              <div className="panel-heading">
                <div className="panel-title-icon ranking-icon"><Trophy size={18} /></div>
                <div><h2 id="ranking-title">Alumnos con más permisos</h2><p>Top 10 según el periodo y los criterios seleccionados.</p></div>
              </div>
            </div>
            {cargandoPermisos ? (
              <div className="ranking-empty"><LoaderCircle className="spin" size={20} />Cargando ranking…</div>
            ) : topAlumnos.length ? (
              <ol className="ranking-list">
                {topAlumnos.map(({ alumno, permisos: cantidad }, index) => (
                  <li className="ranking-row" key={alumno.id}>
                    <span className={`ranking-position${index < 3 ? ` ranking-position-${index + 1}` : ''}`}>{index + 1}</span>
                    <span className="ranking-student">
                      <strong>{nombreCompleto(alumno)}</strong>
                      <small>{alumno.grupo.codigo}</small>
                    </span>
                    <span className="ranking-bar-track" aria-hidden="true"><span style={{ width: `${(cantidad / maxPermisosTop) * 100}%` }} /></span>
                    <strong className="ranking-count">{cantidad}<small>{cantidad === 1 ? ' permiso' : ' permisos'}</small></strong>
                  </li>
                ))}
              </ol>
            ) : (
              <div className="ranking-empty">
                {historialDesde && historialHasta && historialDesde > historialHasta
                  ? 'Corrige el rango de fechas para ver el ranking.'
                  : 'No hay permisos registrados con estos criterios.'}
              </div>
            )}
          </section>
          <footer className="page-footer">© {new Date().getFullYear()} IES Reyes <span>·</span> Gestión de permisos de baño</footer>
          </> : <>
          <section className="page-heading">
            <div>
              <div className="eyebrow"><span className="eyebrow-line" />GESTIÓN DIARIA</div>
              <h1>Permisos de baño</h1>
              <p>Controla las salidas al baño de forma sencilla y organizada.</p>
            </div>
            <div className="current-date"><Clock3 size={16} />{periodoVisible}</div>
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

          <CriteriosPermisos
            desde={historialDesde}
            hasta={historialHasta}
            grupoId={filtroGrupoHistorial}
            alumnoId={filtroAlumnoHistorial}
            profesorId={filtroProfesorHistorial}
            franjaId={filtroFranjaHistorial}
            grupos={gruposHistorialOrdenados}
            alumnos={alumnosHistorialOrdenados}
            profesores={profesoresOrdenados}
            franjas={franjasOrdenadas}
            hayFiltros={hayFiltrosResumen}
            onDesde={setHistorialDesde}
            onHasta={setHistorialHasta}
            onGrupo={setFiltroGrupoHistorial}
            onAlumno={setFiltroAlumnoHistorial}
            onProfesor={setFiltroProfesorHistorial}
            onFranja={setFiltroFranjaHistorial}
            onLimpiar={limpiarFiltrosResumen}
            onIrAHoy={irAHoy}
            onDesplazar={desplazarRangoHistorial}
          />

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
                  <div><h2>Historial de permisos</h2><p>Salidas del periodo y criterios elegidos en el resumen.</p></div>
                </div>
              </div>
              <div className="history-tools">
                <div className="search-box"><Search size={16} /><input value={busqueda} onChange={(event) => setBusqueda(event.target.value)} placeholder="Buscar alumno, grupo…" aria-label="Buscar en permisos" /></div>
              </div>
              <div className="table-head">
                <span>ALUMNO</span><span>GRUPO</span><span>HORA / FRANJA</span><span>PROFESOR</span>
              </div>
              {cargandoPermisos ? (
                <div className="table-state"><LoaderCircle className="spin" size={22} /><span>Cargando permisos…</span></div>
              ) : permisosFiltrados.length ? (
                <div className="permission-list">
                  {permisosPagina.map((permiso) => (
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
                  <span>{hayFiltrosHistorial ? 'Prueba con otras fechas o filtros.' : 'Los permisos registrados para hoy aparecerán aquí.'}</span>
                </div>
              )}
              <div className="list-footer">
                <span>{permisosFiltrados.length
                  ? `Mostrando ${primerPermisoPagina}–${ultimoPermisoPagina} de ${permisosFiltrados.length} permisos`
                  : `0 permisos${hayFiltrosHistorial ? ' encontrados' : ''}`}</span>
                {totalPaginasHistorial > 1 && (
                  <nav className="history-pagination" aria-label="Paginación del historial">
                    <button
                      type="button"
                      onClick={() => setPaginaHistorial((pagina) => Math.max(0, pagina - 1))}
                      disabled={paginaHistorial === 0}
                      aria-label="Página anterior"
                      title="Página anterior"
                    ><ChevronLeft size={15} /></button>
                    <span aria-live="polite">{paginaHistorial + 1} / {totalPaginasHistorial}</span>
                    <button
                      type="button"
                      onClick={() => setPaginaHistorial((pagina) => Math.min(totalPaginasHistorial - 1, pagina + 1))}
                      disabled={paginaHistorial >= totalPaginasHistorial - 1}
                      aria-label="Página siguiente"
                      title="Página siguiente"
                    ><ChevronRight size={15} /></button>
                  </nav>
                )}
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
