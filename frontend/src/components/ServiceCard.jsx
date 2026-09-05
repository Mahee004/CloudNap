import { useEffect, useState } from 'react'
import { getRecommendation } from '../services/api'

function ServiceCard({
  service,
  onConvert,
  isConverting,
  lastUpdated,
}) {
  const isKnative =
    service.type === 'SCALE_TO_ZERO'

  const buttonText = isKnative
    ? 'Convert to HPA'
    : 'Convert to Scale-to-Zero'

  const [recommendation, setRecommendation] = useState(null)
  const [recommendationError, setRecommendationError] = useState(null)
  const [isLoadingRecommendation, setIsLoadingRecommendation] = useState(true)

  useEffect(() => {
    let cancelled = false

    setIsLoadingRecommendation(true)
    setRecommendationError(null)

    getRecommendation(service.name)
      .then((data) => {
        if (!cancelled) {
          setRecommendation(data)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setRecommendationError(error.message)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoadingRecommendation(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [service.name, lastUpdated])

  const showApplyButton =
    recommendation &&
    recommendation.actionRequired &&
    recommendation.recommendedType !== service.type

  const applyButtonText =
    recommendation?.recommendedType === 'HPA'
      ? 'Apply Recommendation (Switch to HPA)'
      : 'Apply Recommendation (Switch to Scale-to-Zero)'


  return (
    <div className="service-card">

      <div className="service-header">

        <div>
          <h3>{service.name}</h3>

          <span
            className={
              isKnative
                ? 'type-badge knative'
                : 'type-badge hpa'
            }
          >
            {isKnative
              ? 'Scale to Zero'
              : 'HPA'}
          </span>
        </div>


        {service.ready !== undefined && (

          <span
            className={
              service.ready
                ? 'ready-badge'
                : 'not-ready-badge'
            }
          >
            {service.ready
              ? 'Ready'
              : 'Not Ready'}
          </span>

        )}

      </div>


      <div className="service-stats">

        <div>
          <span>Pods</span>

          <strong>
            {service.pods ?? 0}
          </strong>
        </div>


        <div>
          <span>Running</span>

          <strong>
            {service.runningPods ?? 0}
          </strong>
        </div>

      </div>


      {!isKnative && (

        <div className="hpa-info">

          <div>
            <span>Min replicas</span>

            <strong>
              {service.minReplicas ?? 1}
            </strong>
          </div>


          <div>
            <span>Max replicas</span>

            <strong>
              {service.maxReplicas ?? 5}
            </strong>
          </div>


          <div>
            <span>Desired</span>

            <strong>
              {service.desiredReplicas ?? 1}
            </strong>
          </div>

        </div>

      )}


      <div className="recommendation-section">

        {isLoadingRecommendation && (
          <p className="recommendation-loading">
            Checking traffic...
          </p>
        )}

        {recommendationError && (
          <p className="recommendation-error">
            {recommendationError}
          </p>
        )}

        {recommendation && !isLoadingRecommendation && (

          <>
            <div className="traffic-row">
              <span>Traffic</span>

              <span
                className={
                  recommendation.trafficStatus === 'ACTIVE'
                    ? 'traffic-status active'
                    : recommendation.trafficStatus === 'IDLE'
                      ? 'traffic-status idle'
                      : 'traffic-status no-data'
                }
              >
                {recommendation.trafficStatus === 'ACTIVE' && '🟢 ACTIVE'}
                {recommendation.trafficStatus === 'IDLE' && '⚪ IDLE'}
                {recommendation.trafficStatus === 'NO_DATA' && '— NO DATA'}
              </span>
            </div>

            {recommendation.trafficStatus !== 'NO_DATA' && (
              <p className="traffic-detail">
                {recommendation.requestsInWindow} requests / {recommendation.window}
              </p>
            )}

            <div className="recommendation-row">
              <span>💡 Recommendation</span>

              <strong>
                {recommendation.recommendedType === 'HPA'
                  ? 'Switch to HPA'
                  : 'Keep Scale to Zero'}
              </strong>
            </div>

            <p className="recommendation-reason">
              {recommendation.reason}
            </p>

            {showApplyButton && (
              <button
                type="button"
                className="apply-recommendation-button"
                onClick={() => onConvert(service)}
                disabled={isConverting}
              >
                {isConverting
                  ? 'Applying...'
                  : applyButtonText}
              </button>
            )}
          </>

        )}

      </div>


      <button
        type="button"
        className={
          isKnative
            ? 'convert-button to-hpa'
            : 'convert-button to-knative'
        }
        onClick={() => onConvert(service)}
        disabled={isConverting}
      >
        {isConverting
          ? 'Converting...'
          : buttonText}
      </button>

    </div>
  )
}

export default ServiceCard