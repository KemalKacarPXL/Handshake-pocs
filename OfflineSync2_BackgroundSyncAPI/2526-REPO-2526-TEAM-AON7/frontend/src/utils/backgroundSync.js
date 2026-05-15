import { savePendingScan } from './db.js'

export const registerServiceWorker = async () => {
  if (!('serviceWorker' in navigator)) {
    console.warn('Service Worker niet ondersteund')
    return null
  }

  try {
    const registration = await navigator.serviceWorker.register('/sw.js')
    console.log('[App] Service Worker geregistreerd')

    // Luister naar berichten van de service worker
    navigator.serviceWorker.addEventListener('message', (event) => {
      if (event.data?.type === 'SYNC_COMPLETE') {
        console.log('[App] Background sync voltooid')
        window.dispatchEvent(new CustomEvent('sync-complete'))
      }
    })

    return registration
  } catch (err) {
    console.error('[App] Service Worker registratie mislukt:', err)
    return null
  }
}

export const requestBackgroundSync = async (scanData) => {
  // Altijd opslaan in IndexedDB
  await savePendingScan(scanData)
  console.log('[App] Scan opgeslagen in IndexedDB')

  // Vraag Background Sync aan
  if ('serviceWorker' in navigator && 'SyncManager' in window) {
    try {
      const registration = await navigator.serviceWorker.ready
      await registration.sync.register('sync-pending-scans')
      console.log('[App] Background sync aangevraagd')
    } catch (err) {
      console.warn('[App] Background sync niet beschikbaar, fallback naar online event')
      // Fallback — probeer direct als online
      if (navigator.onLine) {
        await directSync(scanData)
      }
    }
  } else {
    // Fallback voor browsers zonder Background Sync (iOS Safari)
    console.warn('[App] Background Sync API niet beschikbaar')
    if (navigator.onLine) {
      await directSync(scanData)
    }
  }
}

// Directe sync als fallback
const directSync = async (scan) => {
  try {
    const response = await fetch(
      `/api/bedrijf/scan?studentEmail=${encodeURIComponent(scan.studentEmail)}`,
      {
        method: 'POST',
        headers: { 'bedrijf-email': scan.bedrijfEmail }
      }
    )
    if (response.ok) {
      console.log('[App] Direct sync gelukt')
    }
  } catch (err) {
    console.log('[App] Direct sync mislukt, blijft in queue')
  }
}