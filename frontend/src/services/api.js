async function readResponse(response, fallbackMessage) {
  let data = null

  try {
    data = await response.json()
  } catch {
    data = null
  }

  if (!response.ok) {
    const message = data?.message || fallbackMessage
    const details = data?.details

    throw new Error(
      details
        ? `${message}: ${details}`
        : message
    )
  }

  return data
}


export async function getServices() {
  const response = await fetch(
    '/api/dashboard/services'
  )

  return readResponse(
    response,
    'Failed to load services'
  )
}


export async function getNodes() {
  const response = await fetch(
    '/api/cluster/nodes'
  )

  return readResponse(
    response,
    'Failed to load cluster nodes'
  )
}


export async function convertToHpa(serviceName) {
  const encodedName =
    encodeURIComponent(serviceName)

  const response = await fetch(
    `/api/convert/${encodedName}/to-hpa`,
    {
      method: 'POST',
    }
  )

  return readResponse(
    response,
    'Failed to convert service to HPA'
  )
}


export async function convertToKnative(serviceName) {
  const encodedName =
    encodeURIComponent(serviceName)

  const response = await fetch(
    `/api/convert/${encodedName}/to-knative`,
    {
      method: 'POST',
    }
  )

  return readResponse(
    response,
    'Failed to convert service to Scale-to-Zero'
  )
}


export async function getRecommendation(serviceName) {
  const encodedName =
    encodeURIComponent(serviceName)

  const response = await fetch(
    `/api/recommendations/${encodedName}`
  )

  return readResponse(
    response,
    'Failed to load recommendation'
  )
}