const CACHE_NAME = 'handshake-poc2-v1'
const URLS_TO_CACHE = [
  '/',
  '/index.html'
]

// Installeer en cache de app shell
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(URLS_TO_CACHE))
  )
  self.skipWaiting()
})

// Activeer en verwijder oude caches
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  )
  self.clients.claim()
})

// Onderschep fetch — Network First strategie
self.addEventListener('fetch', (event) => {
  if (event.request.url.includes('/api/')) {
    // API calls — probeer netwerk, val terug op cache
    event.respondWith(
      fetch(event.request)
        .then(response => {
          const clone = response.clone()
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone))
          return response
        })
        .catch(() => caches.match(event.request))
    )
  } else {
    // Statische bestanden — Cache First
    event.respondWith(
      caches.match(event.request).then(cached => cached || fetch(event.request))
    )
  }
})

// Background Sync — wordt aangeroepen door de browser zodra verbinding hersteld is
self.addEventListener('sync', (event) => {
  console.log('[SW] Background sync triggered:', event.tag)
  if (event.tag === 'sync-pending-scans') {
    event.waitUntil(syncPendingScans())
  }
})

// IndexedDB helpers in de service worker
function openDB() {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open('handshake-poc2', 1)
    req.onupgradeneeded = (e) => {
      const db = e.target.result
      if (!db.objectStoreNames.contains('pendingScans')) {
        const store = db.createObjectStore('pendingScans', { keyPath: 'localId' })
        store.createIndex('synced', 'synced')
      }
    }
    req.onsuccess = () => resolve(req.result)
    req.onerror = () => reject(req.error)
  })
}

function getPendingScans(db) {
  return new Promise((resolve) => {
    const tx = db.transaction('pendingScans', 'readonly')
    const req = tx.objectStore('pendingScans').index('synced').getAll(false)
    req.onsuccess = () => resolve(req.result || [])
  })
}

function markAsSynced(db, localId) {
  return new Promise((resolve) => {
    const tx = db.transaction('pendingScans', 'readwrite')
    const store = tx.objectStore('pendingScans')
    const req = store.get(localId)
    req.onsuccess = () => {
      if (req.result) {
        req.result.synced = true
        store.put(req.result)
      }
    }
    tx.oncomplete = resolve
  })
}

async function syncPendingScans() {
  const db = await openDB()
  const pending = await getPendingScans(db)

  console.log(`[SW] Syncing ${pending.length} pending scans`)

  for (const scan of pending) {
    try {
      const response = await fetch(
        `/api/bedrijf/scan?studentEmail=${encodeURIComponent(scan.studentEmail)}`,
        {
          method: 'POST',
          headers: { 'bedrijf-email': scan.bedrijfEmail }
        }
      )

      if (response.ok || response.status === 409) {
        await markAsSynced(db, scan.localId)
        console.log(`[SW] Synced scan for ${scan.studentEmail}`)
      }
    } catch (err) {
      console.log(`[SW] Sync failed for ${scan.studentEmail}:`, err)
      // Blijft pending — browser probeert opnieuw
    }
  }

  // Stuur bericht naar alle open tabs dat sync klaar is
  const clients = await self.clients.matchAll()
  clients.forEach(client => client.postMessage({ type: 'SYNC_COMPLETE' }))
}
