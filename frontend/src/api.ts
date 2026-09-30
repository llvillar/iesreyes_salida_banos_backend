import type {
  Alumno,
  AlumnoNuevo,
  FranjaHoraria,
  FranjaHorariaNueva,
  Grupo,
  GrupoNuevo,
  FiltrosPermisos,
  Permiso,
  PermisoNuevo,
  Profesor,
  ProfesorNuevo,
} from './types'

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  })

  if (!response.ok) {
    let message = `Error ${response.status}: no se pudo completar la solicitud`
    try {
      const problem = await response.json() as { detail?: string; title?: string }
      message = problem.detail || problem.title || message
    } catch {
      // La respuesta puede no contener JSON.
    }
    throw new Error(message)
  }

  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  listarAlumnos: () => request<Alumno[]>('/api/alumnos'),
  crearAlumno: (alumno: AlumnoNuevo) =>
    request<Alumno>('/api/alumnos', { method: 'POST', body: JSON.stringify(alumno) }),
  actualizarAlumno: (id: number, alumno: AlumnoNuevo) =>
    request<Alumno>(`/api/alumnos/${id}`, { method: 'PUT', body: JSON.stringify(alumno) }),
  eliminarAlumno: (id: number) =>
    request<void>(`/api/alumnos/${id}`, { method: 'DELETE' }),
  listarProfesores: () => request<Profesor[]>('/api/profesores'),
  crearProfesor: (profesor: ProfesorNuevo) =>
    request<Profesor>('/api/profesores', { method: 'POST', body: JSON.stringify(profesor) }),
  actualizarProfesor: (id: number, profesor: ProfesorNuevo) =>
    request<Profesor>(`/api/profesores/${id}`, { method: 'PUT', body: JSON.stringify(profesor) }),
  eliminarProfesor: (id: number) =>
    request<void>(`/api/profesores/${id}`, { method: 'DELETE' }),
  listarGrupos: () => request<Grupo[]>('/api/grupos'),
  crearGrupo: (grupo: GrupoNuevo) =>
    request<Grupo>('/api/grupos', { method: 'POST', body: JSON.stringify(grupo) }),
  actualizarGrupo: (id: number, grupo: GrupoNuevo) =>
    request<Grupo>(`/api/grupos/${id}`, { method: 'PUT', body: JSON.stringify(grupo) }),
  eliminarGrupo: (id: number) =>
    request<void>(`/api/grupos/${id}`, { method: 'DELETE' }),
  listarFranjas: () => request<FranjaHoraria[]>('/api/franjas-horarias'),
  crearFranja: (franja: FranjaHorariaNueva) =>
    request<FranjaHoraria>('/api/franjas-horarias', { method: 'POST', body: JSON.stringify(franja) }),
  actualizarFranja: (id: number, franja: FranjaHorariaNueva) =>
    request<FranjaHoraria>(`/api/franjas-horarias/${id}`, { method: 'PUT', body: JSON.stringify(franja) }),
  eliminarFranja: (id: number) =>
    request<void>(`/api/franjas-horarias/${id}`, { method: 'DELETE' }),
  listarPermisos: (fecha?: string, filtros: FiltrosPermisos = {}) => {
    const params = new URLSearchParams()
    if (fecha) params.set('fecha', fecha)
    if (filtros.grupoId) params.set('grupoId', String(filtros.grupoId))
    if (filtros.alumnoId) params.set('alumnoId', String(filtros.alumnoId))
    if (filtros.profesorId) params.set('profesorId', String(filtros.profesorId))
    if (filtros.franjaHorariaId) params.set('franjaHorariaId', String(filtros.franjaHorariaId))
    const query = params.size ? `?${params.toString()}` : ''
    return request<Permiso[]>(`/api/permisos${query}`)
  },
  crearPermiso: (permiso: PermisoNuevo) =>
    request<Permiso>('/api/permisos', {
      method: 'POST',
      body: JSON.stringify(permiso),
    }),
}
