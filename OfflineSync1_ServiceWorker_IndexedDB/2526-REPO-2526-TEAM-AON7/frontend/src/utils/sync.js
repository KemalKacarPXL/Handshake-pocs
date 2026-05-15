import { getPendingScans, markAsSynced, saveSyncedScans } from './db.js';

export const syncWithServer = async () => {
  if (!navigator.onLine) return;

  const pending = await getPendingScans();
  if (pending.length === 0) return;

  try {
    const token = localStorage.getItem('token');
    const response = await fetch('/api/sync/scans', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(pending)
    });

    if (response.ok) {
      const { syncedIds } = await response.json();
      for (const id of syncedIds) {
        await markAsSynced(id);
      }
      console.log(`${syncedIds.length} scans gesynchroniseerd`);
    }
  } catch (err) {
    console.log('Sync mislukt, wordt later opnieuw geprobeerd');
  }
};

// Luister naar online event
window.addEventListener('online', () => {
  console.log('Verbinding hersteld — synchroniseren...');
  syncWithServer();
});
