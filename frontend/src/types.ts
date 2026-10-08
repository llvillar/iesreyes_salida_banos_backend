export interface Grupo {
  id: number
  curso: string
  seccion: string
  codigo: string
}

export interface Alumno {
  id: number
  dni: string
  nombre: string
  apellidos: string
  email: string | null
  grupo: Grupo
}

export interface Profesor {
  id: number
  dni: string
  nombre: string
  apellidos: string
  email: string | null
}

export interface FranjaHoraria {
  id: number
  numero: number
  nombre: string
  horaInicio: string
  horaFin: string
}

export interface Permiso {
  id: number
  fecha: string
  hora: string
  alumno: Alumno
  profesor: Profesor
  franjaHoraria: FranjaHoraria
  creadoEn: string
}

export interface PermisoNuevo {
  alumnoId: number
  profesorId: number
  franjaHorariaId: number
  fecha: string
  hora: string
}

export interface FiltrosPermisos {
  desde?: string
  hasta?: string
  grupoId?: number
  alumnoId?: number
  profesorId?: number
  franjaHorariaId?: number
}

export interface UsuarioSesion {
  email: string
  role: 'GESTION_CATALOGOS' | 'GESTION_PERMISOS'
}

export interface UsuarioApp {
  id: number
  email: string
  perfil: UsuarioSesion['role']
  profesorId: number
  profesorDni: string
  profesorNombre: string
  profesorApellidos: string
}

export interface AlumnoNuevo {
  dni: string
  nombre: string
  apellidos: string
  email: string
  grupoId: number
}

export interface ProfesorNuevo {
  dni: string
  nombre: string
  apellidos: string
  email: string
}

export interface GrupoNuevo {
  curso: string
  seccion: string
}

export interface FranjaHorariaNueva {
  numero: number
  horaInicio: string
  horaFin: string
}
