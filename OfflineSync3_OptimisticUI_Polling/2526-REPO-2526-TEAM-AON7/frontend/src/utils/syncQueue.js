const QUEUE_KEY = 'handshake-poc3-queue'
const RETRY_INTERVAL = 30000 // 30 seconden

class SyncQueue {
  constructor() {
    this.queue = this.loadQueue()
    this.isProcessing = false
    this.retryTimer = null
    this.listeners = []
  }

  loadQueue() {
    try {
      const saved = localStorage.getItem(QUEUE_KEY)
      return saved ? JSON.parse(saved) : []
    } catch {
      return []
    }
  }

  saveQueue() {
    localStorage.setItem(QUEUE_KEY, JSON.stringify(this.queue))
    this.notifyListeners()
  }

  // Voeg scan toe — optimistic UI toont direct resultaat
  add(scan) {
    const item = {
      id: crypto.randomUUID(),
      studentEmail: scan.studentEmail,
      bedrijfEmail: scan.bedrijfEmail,
      scannedAt: new Date().toISOString(),
      status: 'pending' // pending | synced | failed
    }
    this.queue.push(item)
    this.saveQueue()

    // Probeer direct te synchroniseren
    this.process()
    return item
  }

  async process() {
    if (this.isProcessing || !navigator.onLine) return

    const pending = this.queue.filter(s => s.status === 'pending')
    if (pending.length === 0) return

    this.isProcessing = true

    for (const scan of pending) {
      try {
        const response = await fetch(
          `/api/bedrijf/scan?studentEmail=${encodeURIComponent(scan.studentEmail)}`,
          {
            method: 'POST',
            headers: { 'bedrijf-email': scan.bedrijfEmail },
            signal: AbortSignal.timeout(5000)
          }
        )

        if (response.ok || response.status === 409) {
          scan.status = 'synced'
        } else {
          scan.status = 'failed'
        }
      } catch {
        // Netwerk fout — blijft pending voor volgende poging
        console.log(`[POC3] Scan ${scan.id} mislukt, opnieuw proberen in ${RETRY_INTERVAL / 1000}s`)
      }
    }

    this.saveQueue()
    this.isProcessing = false
  }

  startPolling() {
    // Probeer elke 30 seconden te synchroniseren
    this.retryTimer = setInterval(() => {
      if (navigator.onLine) {
        this.process()
      }
    }, RETRY_INTERVAL)

    // Ook direct bij online event
    window.addEventListener('online', () => this.process())

    console.log('[POC3] Polling gestart — elke 30 seconden')
  }

  stopPolling() {
    if (this.retryTimer) {
      clearInterval(this.retryTimer)
      this.retryTimer = null
    }
  }

  // Luisteraars voor reactieve updates
  onChange(callback) {
    this.listeners.push(callback)
    return () => {
      this.listeners = this.listeners.filter(l => l !== callback)
    }
  }

  notifyListeners() {
    this.listeners.forEach(cb => cb(this.queue))
  }

  getAll() {
    return this.queue
  }

  getPending() {
    return this.queue.filter(s => s.status === 'pending')
  }

  getSynced() {
    return this.queue.filter(s => s.status === 'synced')
  }

  // Verwijder oude gesynchroniseerde scans om localStorage klein te houden
  cleanup() {
    const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString()
    this.queue = this.queue.filter(s =>
      s.status === 'pending' || s.scannedAt > oneDayAgo
    )
    this.saveQueue()
  }
}

// Singleton — één instantie voor de hele app
export const syncQueue = new SyncQueue()
