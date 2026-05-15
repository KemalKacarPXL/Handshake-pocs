const DB_NAME = 'handshake-offline';
const DB_VERSION = 1;

export const openDB = () => {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = (event) => {
      const db = event.target.result;

      // Tabel voor offline scans die nog gesynchroniseerd moeten worden
      if (!db.objectStoreNames.contains('pendingScans')) {
        const store = db.createObjectStore('pendingScans', { keyPath: 'localId' });
        store.createIndex('synced', 'synced');
      }

      // Tabel voor al gesynchroniseerde scans (cache)
      if (!db.objectStoreNames.contains('syncedScans')) {
        db.createObjectStore('syncedScans', { keyPath: 'studentEmail' });
      }
    };

    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
};

export const savePendingScan = async (scan) => {
  const db = await openDB();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('pendingScans', 'readwrite');
    tx.objectStore('pendingScans').add({
      localId: crypto.randomUUID(),
      studentEmail: scan.studentEmail,
      bedrijfEmail: scan.bedrijfEmail,
      scannedAt: new Date().toISOString(),
      synced: false,
    });
    tx.oncomplete = resolve;
    tx.onerror = reject;
  });
};

export const getPendingScans = async () => {
  const db = await openDB();
  return new Promise((resolve) => {
    const tx = db.transaction('pendingScans', 'readonly');
    const request = tx.objectStore('pendingScans').index('synced').getAll(false);
    request.onsuccess = () => resolve(request.result);
  });
};

export const markAsSynced = async (localId) => {
  const db = await openDB();
  return new Promise((resolve) => {
    const tx = db.transaction('pendingScans', 'readwrite');
    const store = tx.objectStore('pendingScans');
    const request = store.get(localId);
    request.onsuccess = () => {
      const scan = request.result;
      scan.synced = true;
      store.put(scan);
    };
    tx.oncomplete = resolve;
  });
};

export const saveSyncedScans = async (scans) => {
  const db = await openDB();
  const tx = db.transaction('syncedScans', 'readwrite');
  const store = tx.objectStore('syncedScans');
  scans.forEach(scan => store.put(scan));
};

export const getSyncedScans = async () => {
  const db = await openDB();
  return new Promise((resolve) => {
    const tx = db.transaction('syncedScans', 'readonly');
    const request = tx.objectStore('syncedScans').getAll();
    request.onsuccess = () => resolve(request.result);
  });
};
