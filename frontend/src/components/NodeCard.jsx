function NodeCard({ node }) {
  return (
    <div className="node-card">

      <div className="node-header">
        <div>
          <span className="node-label">NODE</span>
          <h3>{node.name}</h3>
        </div>

        <span
          className={
            node.status === 'Ready'
              ? 'ready-badge'
              : 'not-ready-badge'
          }
        >
          {node.status}
        </span>
      </div>


      <div className="node-resources">

        <div className="resource-box">
          <span>CPU Capacity</span>

          <strong>
            {node.cpu}
          </strong>

          <small>cores</small>
        </div>


        <div className="resource-box">
          <span>Memory Capacity</span>

          <strong>
            {node.memoryGiB}
          </strong>

          <small>GiB</small>
        </div>

      </div>

    </div>
  )
}

export default NodeCard