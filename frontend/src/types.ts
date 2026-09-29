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
