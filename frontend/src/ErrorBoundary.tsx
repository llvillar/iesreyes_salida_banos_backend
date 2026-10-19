import { Component } from 'react'
import type { ErrorInfo, ReactNode } from 'react'

interface ErrorBoundaryProps {
  children: ReactNode
}

interface ErrorBoundaryState {
  hasError: boolean
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false }

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Error al renderizar la aplicación', error, info)
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="login-page">
          <section className="login-card" role="alert">
            <h1>No se pudo cargar la aplicación</h1>
            <p>Ha ocurrido un error al mostrar la página. Recárgala para volver a intentarlo.</p>
            <button className="submit-button" type="button" onClick={() => window.location.reload()}>
              Recargar la aplicación
            </button>
          </section>
        </main>
      )
    }

    return this.props.children
  }
}

export default ErrorBoundary
