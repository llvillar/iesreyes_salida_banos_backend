import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { ArrowDownUp, Check, LoaderCircle, Pencil, Plus, Search, Trash2, X } from 'lucide-react'
import { api } from './api'
import type { Alumno, FranjaHoraria, Grupo, Profesor } from './types'

type TipoCatalogo = 'alumnos' | 'profesores' | 'grupos' | 'franjas'
type Formulario = {
  dni: string
  nombre: string
  apellidos: string
  email: string
  grupoId: string
  curso: string
  seccion: string
  numero: string
  horaInicio: string
  horaFin: string
}

const formularioVacio = (): Formulario => ({
  dni: '', nombre: '', apellidos: '', email: '', grupoId: '', curso: '', seccion: 'A', numero: '',
  horaInicio: '', horaFin: '',
})

const cursos = ['1º ESO', '2º ESO', '3º ESO', '4º ESO', '1º Bachillerato', '2º Bachillerato']
const titulos: Record<TipoCatalogo, string> = {
  alumnos: 'Alumnos',
  profesores: 'Profesores',
  grupos: 'Grupos',
  franjas: 'Franjas horarias',
}

function mensajeError(error: unknown) {
  return error instanceof Error ? error.message : 'Ha ocurrido un error inesperado.'
}

function normalizarBusqueda(valor: string) {
  return valor.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLocaleLowerCase('es').trim()
}

function compararPersonasPorApellidos(
  a: { nombre: string; apellidos: string },
  b: { nombre: string; apellidos: string },
) {
  return a.apellidos.localeCompare(b.apellidos, 'es', { sensitivity: 'base' }) ||
    a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' })
}

function GestionCatalogos() {
  const [tipo, setTipo] = useState<TipoCatalogo>('alumnos')
  const [alumnos, setAlumnos] = useState<Alumno[]>([])
  const [profesores, setProfesores] = useState<Profesor[]>([])
  const [grupos, setGrupos] = useState<Grupo[]>([])
  const [franjas, setFranjas] = useState<FranjaHoraria[]>([])
  const [formulario, setFormulario] = useState<Formulario>(formularioVacio)
  const [editando, setEditando] = useState<number | null>(null)
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [filtroApellidos, setFiltroApellidos] = useState('')
  const [filtroProfesores, setFiltroProfesores] = useState('')
  const [filtroGrupos, setFiltroGrupos] = useState('')
  const [filtroGrupoId, setFiltroGrupoId] = useState('')
  const [apellidosAscendentes, setApellidosAscendentes] = useState(true)
  const [profesoresAscendentes, setProfesoresAscendentes] = useState(true)

  const cargarCatalogos = async () => {
    const [listaAlumnos, listaProfesores, listaGrupos, listaFranjas] = await Promise.all([
      api.listarAlumnos(), api.listarProfesores(), api.listarGrupos(), api.listarFranjas(),
    ])
    setAlumnos(listaAlumnos)
    setProfesores(listaProfesores)
    setGrupos(listaGrupos)
    setFranjas(listaFranjas)
  }

  useEffect(() => {
    let activo = true
    Promise.all([api.listarAlumnos(), api.listarProfesores(), api.listarGrupos(), api.listarFranjas()])
      .then(([listaAlumnos, listaProfesores, listaGrupos, listaFranjas]) => {
        if (!activo) return
        setAlumnos(listaAlumnos)
        setProfesores(listaProfesores)
        setGrupos(listaGrupos)
        setFranjas(listaFranjas)
      })
      .catch((cause: unknown) => { if (activo) setError(mensajeError(cause)) })
      .finally(() => { if (activo) setCargando(false) })
    return () => { activo = false }
  }, [])

  const comenzarEdicion = (id: number) => {
    setError('')
    setAviso('')
    setEditando(id)
    if (tipo === 'alumnos') {
      const alumno = alumnos.find((registro) => registro.id === id)
      if (alumno) setFormulario({
        ...formularioVacio(), dni: alumno.dni, nombre: alumno.nombre, apellidos: alumno.apellidos,
        email: alumno.email ?? '', grupoId: String(alumno.grupo.id),
      })
    } else if (tipo === 'profesores') {
      const profesor = profesores.find((registro) => registro.id === id)
      if (profesor) setFormulario({
        ...formularioVacio(), dni: profesor.dni, nombre: profesor.nombre, apellidos: profesor.apellidos,
        email: profesor.email ?? '',
      })
    } else if (tipo === 'grupos') {
      const grupo = grupos.find((registro) => registro.id === id)
      if (grupo) setFormulario({ ...formularioVacio(), curso: grupo.curso, seccion: grupo.seccion })
    } else {
      const franja = franjas.find((registro) => registro.id === id)
      if (franja) setFormulario({
        ...formularioVacio(), numero: String(franja.numero),
        horaInicio: franja.horaInicio.slice(0, 5), horaFin: franja.horaFin.slice(0, 5),
      })
    }
  }

  const cancelarEdicion = () => {
    setEditando(null)
    setFormulario(formularioVacio())
    setError('')
  }

  const guardar = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setAviso('')
    setGuardando(true)
    try {
      const id = editando
      if (tipo === 'alumnos') {
        const datos = {
          dni: formulario.dni.trim(), nombre: formulario.nombre.trim(), apellidos: formulario.apellidos.trim(),
          email: formulario.email.trim(), grupoId: Number(formulario.grupoId),
        }
        if (id === null) await api.crearAlumno(datos)
        else await api.actualizarAlumno(id, datos)
      } else if (tipo === 'profesores') {
        const datos = {
          dni: formulario.dni.trim(), nombre: formulario.nombre.trim(), apellidos: formulario.apellidos.trim(),
          email: formulario.email.trim(),
        }
        if (id === null) await api.crearProfesor(datos)
        else await api.actualizarProfesor(id, datos)
      } else if (tipo === 'grupos') {
        const datos = { curso: formulario.curso, seccion: formulario.seccion }
        if (id === null) await api.crearGrupo(datos)
        else await api.actualizarGrupo(id, datos)
      } else {
        const datos = {
          numero: Number(formulario.numero),
          horaInicio: formulario.horaInicio,
          horaFin: formulario.horaFin,
        }
        if (id === null) await api.crearFranja(datos)
        else await api.actualizarFranja(id, datos)
      }
      await cargarCatalogos()
      setAviso(`${titulos[tipo].slice(0, -1)} ${id === null ? 'creado' : 'actualizado'} correctamente.`)
      setEditando(null)
      setFormulario(formularioVacio())
    } catch (cause) {
      setError(mensajeError(cause))
    } finally {
      setGuardando(false)
    }
  }

  const eliminar = async (id: number, etiqueta: string) => {
    if (!window.confirm(`¿Seguro que quieres eliminar ${etiqueta}?`)) return
    setError('')
    setAviso('')
    try {
      if (tipo === 'alumnos') await api.eliminarAlumno(id)
      else if (tipo === 'profesores') await api.eliminarProfesor(id)
      else if (tipo === 'grupos') await api.eliminarGrupo(id)
      else await api.eliminarFranja(id)
      await cargarCatalogos()
      if (editando === id) cancelarEdicion()
      setAviso('Registro eliminado correctamente.')
    } catch (cause) {
      setError(mensajeError(cause))
    }
  }

  const cambiarTipo = (nuevoTipo: TipoCatalogo) => {
    setTipo(nuevoTipo)
    setEditando(null)
    setFormulario(formularioVacio())
    setError('')
    setAviso('')
  }

  const cambiarCampo = (campo: keyof Formulario, valor: string) =>
    setFormulario((actual) => ({ ...actual, [campo]: valor }))

  const formularioPersona = tipo === 'alumnos' || tipo === 'profesores'
  const listaVacia = tipo === 'alumnos' ? alumnos.length === 0
    : tipo === 'profesores' ? profesores.length === 0
      : tipo === 'grupos' ? grupos.length === 0 : franjas.length === 0
  const alumnosFiltrados = alumnos
    .filter((alumno) => {
      const nombreCompleto = normalizarBusqueda(`${alumno.nombre} ${alumno.apellidos}`)
      const terminos = normalizarBusqueda(filtroApellidos).split(/\s+/).filter(Boolean)
      return terminos.every((termino) => nombreCompleto.includes(termino)) &&
        (!filtroGrupoId || String(alumno.grupo.id) === filtroGrupoId)
    })
    .sort((a, b) => apellidosAscendentes
      ? compararPersonasPorApellidos(a, b)
      : compararPersonasPorApellidos(b, a))
  const profesoresFiltrados = profesores
    .filter((profesor) => {
      const nombreCompleto = normalizarBusqueda(`${profesor.nombre} ${profesor.apellidos}`)
      const terminos = normalizarBusqueda(filtroProfesores).split(/\s+/).filter(Boolean)
      return terminos.every((termino) => nombreCompleto.includes(termino))
    })
    .sort((a, b) => {
      return profesoresAscendentes
        ? compararPersonasPorApellidos(a, b)
        : compararPersonasPorApellidos(b, a)
    })
  const gruposFiltrados = grupos.filter((grupo) => {
    const nombreGrupo = normalizarBusqueda(`${grupo.codigo} ${grupo.curso} ${grupo.seccion}`)
    const terminos = normalizarBusqueda(filtroGrupos).split(/\s+/).filter(Boolean)
    return terminos.every((termino) => nombreGrupo.includes(termino))
  })

  return (
    <>
      <section className="page-heading">
        <div>
          <div className="eyebrow"><span className="eyebrow-line" />ADMINISTRACIÓN</div>
          <h1>Comunidad escolar</h1>
          <p>Gestiona alumnos, profesores, grupos y franjas horarias.</p>
        </div>
      </section>

      {error && <div className="alert alert-error" role="alert"><span>{error}</span><button className="alert-close" onClick={() => setError('')} aria-label="Cerrar error"><X size={16} /></button></div>}
      {aviso && <div className="alert alert-success" role="status"><Check size={17} /><span>{aviso}</span><button className="alert-close" onClick={() => setAviso('')} aria-label="Cerrar aviso"><X size={16} /></button></div>}

      <div className="catalog-tabs" role="tablist" aria-label="Catálogos">
        {(Object.keys(titulos) as TipoCatalogo[]).map((opcion) => (
          <button key={opcion} type="button" role="tab" aria-selected={tipo === opcion}
            className={`catalog-tab${tipo === opcion ? ' selected' : ''}`} onClick={() => cambiarTipo(opcion)}>
            {titulos[opcion]}
          </button>
        ))}
      </div>

      <section className="catalog-workspace">
        <article className="panel create-panel">
          <div className="panel-heading">
            <div className="panel-title-icon"><Plus size={18} /></div>
            <div><h2>{editando === null ? `Añadir ${titulos[tipo].toLocaleLowerCase('es').replace(/s$/, '')}` : `Editar ${titulos[tipo].toLocaleLowerCase('es').replace(/s$/, '')}`}</h2>
              <p>{editando === null ? 'Completa los datos del nuevo registro.' : 'Modifica los datos del registro seleccionado.'}</p></div>
          </div>
          {tipo === 'grupos' && <p className="form-note catalog-note">El código se genera automáticamente a partir del curso y la sección.</p>}
          {tipo === 'franjas' && <p className="form-note catalog-note">Hay seis franjas disponibles; indica la hora de inicio y fin de cada una.</p>}
          <form className="permission-form catalog-form" onSubmit={guardar}>
            {formularioPersona && <>
              <label className="field"><span>DNI <b>*</b></span>
                <input value={formulario.dni} onChange={(event) => cambiarCampo('dni', event.target.value)} pattern="[0-9]{8}[A-Za-z]" title="El DNI debe tener ocho números y una letra" maxLength={9} required /></label>
              <label className="field"><span>Nombre <b>*</b></span>
                <input value={formulario.nombre} onChange={(event) => cambiarCampo('nombre', event.target.value)} maxLength={80} required /></label>
              <label className="field"><span>Apellidos <b>*</b></span>
                <input value={formulario.apellidos} onChange={(event) => cambiarCampo('apellidos', event.target.value)} maxLength={120} required /></label>
              <label className="field"><span>Correo electrónico</span>
                <input type="email" value={formulario.email} onChange={(event) => cambiarCampo('email', event.target.value)} maxLength={150} /></label>
              {tipo === 'alumnos' && <label className="field"><span>Grupo <b>*</b></span>
                <div className="select-wrap"><select value={formulario.grupoId} onChange={(event) => cambiarCampo('grupoId', event.target.value)} required>
                  <option value="">Selecciona un grupo</option>
                  {grupos.map((grupo) => <option key={grupo.id} value={grupo.id}>{grupo.codigo}</option>)}
                </select></div>
                {!grupos.length && <small className="field-help">Crea un grupo antes de dar de alta alumnos.</small>}
              </label>}
            </>}
            {tipo === 'grupos' && <>
              <label className="field"><span>Curso <b>*</b></span><div className="select-wrap"><select value={formulario.curso} onChange={(event) => cambiarCampo('curso', event.target.value)} required>
                <option value="">Selecciona un curso</option>{cursos.map((curso) => <option key={curso}>{curso}</option>)}
              </select></div></label>
              <label className="field"><span>Sección <b>*</b></span><div className="select-wrap"><select value={formulario.seccion} onChange={(event) => cambiarCampo('seccion', event.target.value)} required>
                <option value="A">A</option><option value="B">B</option>
              </select></div></label>
            </>}
            {tipo === 'franjas' && <label className="field"><span>Número de franja <b>*</b></span><div className="select-wrap"><select value={formulario.numero} onChange={(event) => cambiarCampo('numero', event.target.value)} required>
              <option value="">Selecciona una franja</option>{[1, 2, 3, 4, 5, 6].map((numero) => <option key={numero} value={numero}>{numero}º Hora</option>)}
            </select></div></label>}
            {tipo === 'franjas' && <>
              <label className="field"><span>Hora de inicio <b>*</b></span>
                <input type="time" value={formulario.horaInicio} onChange={(event) => cambiarCampo('horaInicio', event.target.value)} required /></label>
              <label className="field"><span>Hora de fin <b>*</b></span>
                <input type="time" value={formulario.horaFin} onChange={(event) => cambiarCampo('horaFin', event.target.value)} required /></label>
            </>}
            <button className="submit-button" type="submit" disabled={guardando || cargando || (tipo === 'alumnos' && !grupos.length)}>
              {guardando ? <LoaderCircle size={17} className="spin" /> : editando === null ? <Plus size={17} /> : <Check size={17} />}
              {guardando ? 'Guardando…' : editando === null ? 'Añadir registro' : 'Guardar cambios'}
            </button>
            {editando !== null && <button className="cancel-button" type="button" onClick={cancelarEdicion}>Cancelar edición</button>}
          </form>
        </article>

        <article className="panel catalog-list-panel">
          <div className="history-heading">
            <div className="panel-heading"><div className="panel-title-icon panel-title-icon-light"><Plus size={18} /></div>
              <div><h2>{titulos[tipo]}</h2><p>{tipo === 'alumnos' ? 'Cada alumno pertenece a un grupo.' : 'Registros disponibles en el centro.'}</p></div></div>
            <span className="catalog-count">{tipo === 'alumnos' ? alumnosFiltrados.length : tipo === 'profesores' ? profesoresFiltrados.length : tipo === 'grupos' ? gruposFiltrados.length : franjas.length}</span>
          </div>
          {tipo === 'alumnos' && <div className="catalog-filters">
            <label className="search-box"><Search size={16} /><input value={filtroApellidos} onChange={(event) => setFiltroApellidos(event.target.value)} placeholder="Filtrar por nombre o apellidos…" aria-label="Filtrar alumnos por nombre o apellidos" /></label>
            <label className="catalog-group-filter"><span className="sr-only">Filtrar por grupo</span>
              <select value={filtroGrupoId} onChange={(event) => setFiltroGrupoId(event.target.value)} aria-label="Filtrar alumnos por grupo">
                <option value="">Todos los grupos</option>
                {grupos.map((grupo) => <option key={grupo.id} value={grupo.id}>{grupo.codigo}</option>)}
              </select>
            </label>
            <div className="catalog-filter-actions">
              <button className="icon-button catalog-sort" type="button" onClick={() => setApellidosAscendentes((ascendente) => !ascendente)}
                title={`Ordenar apellidos ${apellidosAscendentes ? 'descendente' : 'ascendente'}`}
                aria-label={`Ordenar apellidos ${apellidosAscendentes ? 'descendente' : 'ascendente'}`}>
                <ArrowDownUp size={16} /><span>{apellidosAscendentes ? 'A–Z' : 'Z–A'}</span>
              </button>
              <button className="clear-filters-button" type="button"
                onClick={() => { setFiltroApellidos(''); setFiltroGrupoId('') }}
                disabled={!filtroApellidos && !filtroGrupoId}>
                Limpiar
              </button>
            </div>
          </div>}
          {tipo === 'profesores' && <div className="catalog-filters">
            <label className="search-box"><Search size={16} /><input value={filtroProfesores} onChange={(event) => setFiltroProfesores(event.target.value)} placeholder="Filtrar por nombre o apellidos…" aria-label="Filtrar profesores por nombre o apellidos" /></label>
            <div className="catalog-filter-actions">
              <button className="icon-button catalog-sort" type="button" onClick={() => setProfesoresAscendentes((ascendente) => !ascendente)}
                title={`Ordenar apellidos ${profesoresAscendentes ? 'descendente' : 'ascendente'}`}
                aria-label={`Ordenar apellidos ${profesoresAscendentes ? 'descendente' : 'ascendente'}`}>
                <ArrowDownUp size={16} /><span>{profesoresAscendentes ? 'A–Z' : 'Z–A'}</span>
              </button>
              <button className="clear-filters-button" type="button" onClick={() => setFiltroProfesores('')} disabled={!filtroProfesores}>
                Limpiar
              </button>
            </div>
          </div>}
          {tipo === 'grupos' && <div className="catalog-filters">
            <label className="search-box"><Search size={16} /><input value={filtroGrupos} onChange={(event) => setFiltroGrupos(event.target.value)} placeholder="Filtrar por nombre del grupo…" aria-label="Filtrar grupos por nombre" /></label>
            <button className="clear-filters-button" type="button" onClick={() => setFiltroGrupos('')} disabled={!filtroGrupos}>
              Limpiar
            </button>
          </div>}
          {cargando ? <div className="table-state"><LoaderCircle className="spin" size={22} /><span>Cargando catálogos…</span></div>
            : listaVacia ? <div className="table-state empty-state"><strong>No hay {titulos[tipo].toLocaleLowerCase('es')}</strong><span>Añade el primer registro con el formulario.</span></div>
              : <div className="catalog-list">
                {tipo === 'alumnos' && (alumnosFiltrados.length ? alumnosFiltrados.map((alumno) => <div className="catalog-row" key={alumno.id}>
                  <div><strong>{alumno.apellidos}, {alumno.nombre}</strong><small>{alumno.dni}{alumno.email ? ` · ${alumno.email}` : ''}</small></div>
                  <span className="group-badge">{alumno.grupo.codigo}</span>
                  <RowActions onEdit={() => comenzarEdicion(alumno.id)} onDelete={() => eliminar(alumno.id, `a ${alumno.nombre} ${alumno.apellidos}`)} />
                </div>) : <div className="table-state empty-state"><strong>No hay alumnos que coincidan</strong><span>Cambia el nombre, los apellidos o selecciona otro grupo.</span></div>)}
                {tipo === 'profesores' && (profesoresFiltrados.length ? profesoresFiltrados.map((profesor) => <div className="catalog-row" key={profesor.id}>
                  <div><strong>{profesor.apellidos}, {profesor.nombre}</strong><small>{profesor.dni}{profesor.email ? ` · ${profesor.email}` : ''}</small></div>
                  <RowActions onEdit={() => comenzarEdicion(profesor.id)} onDelete={() => eliminar(profesor.id, `a ${profesor.nombre} ${profesor.apellidos}`)} />
                </div>) : <div className="table-state empty-state"><strong>No hay profesores que coincidan</strong><span>Cambia el nombre o los apellidos de búsqueda.</span></div>)}
                {tipo === 'grupos' && (gruposFiltrados.length ? gruposFiltrados.map((grupo) => <div className="catalog-row" key={grupo.id}>
                  <div><strong>{grupo.codigo}</strong><small>{grupo.curso} · Sección {grupo.seccion}</small></div>
                  <RowActions onEdit={() => comenzarEdicion(grupo.id)} onDelete={() => eliminar(grupo.id, `el grupo ${grupo.codigo}`)} />
                </div>) : <div className="table-state empty-state"><strong>No hay grupos que coincidan</strong><span>Cambia el nombre del grupo de búsqueda.</span></div>)}
                {tipo === 'franjas' && [...franjas].sort((a, b) => a.horaInicio.localeCompare(b.horaInicio)).map((franja) => <div className="catalog-row" key={franja.id}>
                  <div><strong>{franja.nombre}</strong><small>{franja.horaInicio.slice(0, 5)}–{franja.horaFin.slice(0, 5)}</small></div>
                  <RowActions onEdit={() => comenzarEdicion(franja.id)} onDelete={() => eliminar(franja.id, franja.nombre)} />
                </div>)}
              </div>}
        </article>
      </section>
      <footer className="page-footer">© {new Date().getFullYear()} IES Reyes <span>·</span> Gestión de la comunidad escolar</footer>
    </>
  )
}

function RowActions({ onEdit, onDelete }: { onEdit: () => void; onDelete: () => void }) {
  return <div className="catalog-actions">
    <button className="icon-button" type="button" onClick={onEdit} aria-label="Editar registro" title="Editar"><Pencil size={15} /></button>
    <button className="icon-button delete-button" type="button" onClick={onDelete} aria-label="Eliminar registro" title="Eliminar"><Trash2 size={15} /></button>
  </div>
}

export default GestionCatalogos
