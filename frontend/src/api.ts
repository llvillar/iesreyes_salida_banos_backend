import type { Alumno, FranjaHoraria, Permiso, PermisoNuevo, Profesor } from './types'

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
  listarProfesores: () => request<Profesor[]>('/api/profesores'),
  listarFranjas: () => request<FranjaHoraria[]>('/api/franjas-horarias'),
  listarPermisos: (fecha?: string) => {
    const query = fecha ? `?fecha=${encodeURIComponent(fecha)}` : ''
    return request<Permiso[]>(`/api/permisos${query}`)
  },
  crearPermiso: (permiso: PermisoNuevo) =>
    request<Permiso>('/api/permisos', {
      method: 'POST',
      body: JSON.stringify(permiso),
    }),
}
