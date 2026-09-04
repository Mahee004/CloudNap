function Navbar({ clusterConnected }) {
  return (
    <header className="navbar">
      <div>
        <h1>CloudNap</h1>
        <p>Scale-to-Zero Management Dashboard</p>
      </div>

      <div
        className={
          clusterConnected
            ? 'cluster-status connected'
            : 'cluster-status disconnected'
        }
      >
        <span className="status-dot"></span>

        {clusterConnected
          ? 'Cluster Connected'
          : 'Cluster Disconnected'}
      </div>
    </header>
  )
}

export default Navbar