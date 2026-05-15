const DB_NAME = 'handshake-poc2'
const DB_VERSION = 1

export const openDB = () => {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, DB_VERSION)

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

export const savePendingScan = async (scan) => {
  const db = await openDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction('pendingScans', 'readwrite')
    tx.objectStore('pendingScans').add({
      localId: crypto.randomUUID(),
      studentEmail: scan.studentEmail,
      bedrijfEmail: scan.bedrijfEmail,
      scannedAt: new Date().toISOString(),
      synced: false
    })
    tx.oncomplete = resolve
    tx.onerror = reject
  })
}

export const getPendingScans = async () => {
  const db = await openDB()
  return new Promise((resolve) => {
    const tx = db.transaction('pendingScans', 'readonly')
    const req = tx.objectStore('pendingScans').index('synced').getAll(false)
    req.onsuccess = () => resolve(req.result || [])
  })
}

export const getAllScans = async () => {
  const db = await openDB()
  return new Promise((resolve) => {
    const tx = db.transaction('pendingScans', 'readonly')
    const req = tx.objectStore('pendingScans').getAll()
    req.onsuccess = () => resolve(req.result || [])
  })
}

export const markAsSynced = async (localId) => {
  const db = await openDB();
  return new Promise((resolve) => {
    const tx = db.transaction("pendingScans", "readwrite");
    const store = tx.objectStore("pendingScans");
    const req = store.get(localId);
    req.onsuccess = () => {
      if (req.result) {
        req.result.synced = true;
        store.put(req.result);
      }
    };
    tx.oncomplete = resolve;
  });
};
