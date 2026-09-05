import {
  useEffect,
  useState,
} from 'react'

import Navbar from '../components/Navbar'
import ServiceCard from '../components/ServiceCard'
import NodeCard from '../components/NodeCard'

import {
  getServices,
  getNodes,
  convertToHpa,
  convertToKnative,
} from '../services/api'


function Dashboard() {
  const [services, setServices] = useState([])
  const [nodes, setNodes] = useState([])

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [
    clusterConnected,
    setClusterConnected,
  ] = useState(false)

  const [
    convertingService,
    setConvertingService,
  ] = useState('')

  const [
    actionMessage,
    setActionMessage,
  ] = useState('')

  const [
    actionError,
    setActionError,
  ] = useState('')

  const [
    lastUpdated,
    setLastUpdated,
  ] = useState(null)


  async function loadDashboard(showLoading = false) {
    try {
      if (showLoading) {
        setLoading(true)
      }

      const [serviceData, nodeData] =
        await Promise.all([
          getServices(),
          getNodes(),
        ])

      setServices(serviceData)
      setNodes(nodeData)

      setClusterConnected(true)
      setError('')
      setLastUpdated(new Date())

    } catch (err) {
      console.error(err)

      setClusterConnected(false)

      setError(
        err.message ||
        'Unable to load dashboard data.'
      )

    } finally {
      if (showLoading) {
        setLoading(false)
      }
    }
  }


  useEffect(() => {
    // Load immediately when the page opens
    loadDashboard(true)

    // Then refresh every five seconds
    const intervalId = setInterval(() => {
      loadDashboard(false)
    }, 5000)

    // Stop the timer when this page is removed
    return () => {
      clearInterval(intervalId)
    }
  }, [])


  async function handleConvert(service, targetTypeOverride = null) {
    const isKnative =
      service.type === 'SCALE_TO_ZERO'

    // Determine target based on override or toggle default
    const targetType = targetTypeOverride
      ? (targetTypeOverride === 'HPA' ? 'HPA' : 'Scale-to-Zero')
      : (isKnative ? 'HPA' : 'Scale-to-Zero')

    const confirmed = window.confirm(
      `Convert "${service.name}" to ${targetType}?`
    )

    if (!confirmed) {
      return
    }

    try {
      setConvertingService(service.name)
      setActionMessage('')
      setActionError('')

      if (targetType === 'HPA') {
        await convertToHpa(service.name)
      } else {
        await convertToKnative(service.name)
      }

      // Immediately load the latest Kubernetes state
      await loadDashboard(false)

      setActionMessage(
        `"${service.name}" was converted to ${targetType} successfully.`
      )

    } catch (err) {
      console.error(err)

      setActionError(
        err.message ||
        'Service conversion failed.'
      )

    } finally {
      setConvertingService('')
    }
  }


  return (
    <div className="dashboard">

      <Navbar
        clusterConnected={clusterConnected}
      />


      <main className="dashboard-content">

        <div className="page-title">

          <div>
            <h2>Dashboard</h2>

            <p>
              Monitor Kubernetes services
              and scaling behaviour.
            </p>
          </div>


          <div className="refresh-area">

            {lastUpdated && (
              <span className="last-updated">
                Last updated:{' '}
                {lastUpdated.toLocaleTimeString()}
              </span>
            )}

            <button
              type="button"
              className="refresh-button"
              onClick={() => loadDashboard(false)}
            >
              Refresh
            </button>

          </div>

        </div>


        {actionMessage && (
          <div className="action-message">
            {actionMessage}
          </div>
        )}


        {actionError && (
          <div className="action-error">
            {actionError}
          </div>
        )}


        {loading && (
          <div className="message-box">
            Loading cluster information...
          </div>
        )}


        {error && !loading && (
          <div className="error-box">
            {error}
          </div>
        )}


        {!loading && (

          <div className="dashboard-grid">

            <section className="services-section">

              <div className="section-header">

                <div>
                  <h2>Services</h2>

                  <p>
                    {services.length}{' '}
                    services detected
                  </p>
                </div>

              </div>


              <div className="service-list">

                {services.length === 0 ? (

                  <div className="empty-state">
                    No services found.
                  </div>

                ) : (

                  services.map((service) => (

                    <ServiceCard
                      key={
                        `${service.type}-${service.name}`
                      }
                      service={service}
                      onConvert={handleConvert}
                      isConverting={
                        convertingService ===
                        service.name
                      }
                      lastUpdated={lastUpdated}
                    />

                  ))

                )}

              </div>

            </section>


            <section className="cluster-section">

              <div className="section-header">

                <div>
                  <h2>Cluster</h2>

                  <p>
                    {nodes.length}{' '}
                    nodes detected
                  </p>
                </div>

              </div>


              <div className="node-list">

                {nodes.length === 0 ? (

                  <div className="empty-state">
                    No cluster nodes found.
                  </div>

                ) : (

                  nodes.map((node) => (

                    <NodeCard
                      key={node.name}
                      node={node}
                    />

                  ))

                )}

              </div>

            </section>

          </div>

        )}

      </main>

    </div>
  )
}

export default Dashboard