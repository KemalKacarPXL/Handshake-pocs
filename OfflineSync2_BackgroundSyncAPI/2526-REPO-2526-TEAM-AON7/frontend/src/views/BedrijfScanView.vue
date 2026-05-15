<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { Html5Qrcode } from "html5-qrcode";
import BedrijfBottomNav from "../components/BedrijfBottomNav.vue";
import { useRouter } from "vue-router";
import { requestBackgroundSync } from "@/utils/backgroundSync.js";
import { getAllScans } from "@/utils/db.js";

const router = useRouter();
const bedrijfEmail = localStorage.getItem("bedrijfEmail");
const isScanning = ref(false);
const isOnline = ref(navigator.onLine);
const errorMessage = ref("");
const successMessage = ref("");
const pendingCount = ref(0);
let html5QrCode = null;

// Online/offline status bijhouden
const handleOnline = async () => {
  isOnline.value = true;
  // Wacht 2 seconden dan sync forceren
  await new Promise(resolve => setTimeout(resolve, 2000));
  await forceSyncAndUpdate();
};

const forceSyncAndUpdate = async () => {
  const pending = await getPendingScans();

  if (pending.length === 0) {
    await updatePendingCount();
    return;
  }

  for (const scan of pending) {
    try {
      const response = await fetch(
        `/api/bedrijf/scan?studentEmail=${encodeURIComponent(scan.studentEmail)}`,
        {
          method: "POST",
          headers: { "bedrijf-email": scan.bedrijfEmail },
          signal: AbortSignal.timeout(5000),
        }
      );

      if (response.ok || response.status === 409) {
        await markAsSynced(scan.localId);
      }
    } catch {
      // Stille fout — probeer volgende keer
    }
  }

  await updatePendingCount();
};

const handleOffline = () => {
  isOnline.value = false;
};

window.addEventListener("online", handleOnline);
window.addEventListener("offline", handleOffline);

// Luister naar sync-complete event van service worker
window.addEventListener("sync-complete", async () => {
  await updatePendingCount();
  successMessage.value = "✅ Offline scans gesynchroniseerd!";
});

const updatePendingCount = async () => {
  const scans = await getAllScans();
  pendingCount.value = scans.filter(s => !s.synced).length;
};

const startScanner = async () => {
  errorMessage.value = "";
  successMessage.value = "";
  isScanning.value = true;

  html5QrCode = new Html5Qrcode("qr-reader");

  try {
    await html5QrCode.start(
      { facingMode: "environment" },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      async (decodedText) => {
        await stopScanner();
        await handleScan(decodedText);
      },
      () => {}
    );
  } catch (err) {
    errorMessage.value = "Kan camera niet openen: " + err;
    isScanning.value = false;
  }
};

const stopScanner = async () => {
  if (html5QrCode) {
    await html5QrCode.stop();
    html5QrCode = null;
  }
  isScanning.value = false;
};

const handleScan = async (decodedText) => {
  try {
    let studentEmail = "";

    try {
      const url = new URL(decodedText);
      studentEmail =
        url.searchParams.get("studentEmail") ||
        url.searchParams.get("email") ||
        "";
    } catch {
      studentEmail = decodedText;
    }

    if (!studentEmail) {
      errorMessage.value = "Ongeldige QR code.";
      return;
    }

    // Background Sync — werkt online én offline
    await requestBackgroundSync({ studentEmail, bedrijfEmail });
    await updatePendingCount();

    if (isOnline.value) {
      successMessage.value = "✅ Scan geregistreerd!";
      // Online — navigeer direct naar studentinfo
      await router.push(`/bedrijf/studentinfo/${encodeURIComponent(studentEmail)}`);
    } else {
      // Offline — toon bericht, navigeer niet want data is nog niet op server
      successMessage.value = "📱 Offline opgeslagen — synchroniseert automatisch bij herstel";
    }
  } catch {
    errorMessage.value = "Ongeldige QR code.";
  }
};

onMounted(updatePendingCount);

onUnmounted(() => {
  if (html5QrCode) html5QrCode.stop();
  window.removeEventListener("online", handleOnline);
  window.removeEventListener("offline", handleOffline);
  window.removeEventListener("sync-complete", updatePendingCount);
});
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div class="header-inner">
        <h1>QR Scanner</h1>
        <p>Scan studenten QR-codes</p>
      </div>
    </div>

    <div class="page-body">
      <!-- Offline banner -->
      <div v-if="!isOnline" class="banner banner-offline">
        📵 Geen internet — scans worden lokaal opgeslagen en automatisch gesynchroniseerd
      </div>

      <!-- Pending sync banner -->
      <div v-if="pendingCount > 0 && isOnline" class="banner banner-sync">
        🔄 {{ pendingCount }} scan(s) worden gesynchroniseerd...
      </div>

      <div class="scanner-card">
        <!-- QR reader container -->
        <div class="qr-frame" :class="{ active: isScanning }">
          <div id="qr-reader" v-show="isScanning" class="qr-reader-el"></div>

          <div v-if="!isScanning" class="qr-placeholder">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
              <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
              <rect x="7" y="7" width="4" height="4" rx="0.5"/>
              <rect x="13" y="7" width="4" height="4" rx="0.5"/>
              <rect x="7" y="13" width="4" height="4" rx="0.5"/>
              <path d="M13 13h1v1h-1z M15 13h2v2h-2z M13 15h2v2h-2z M15 17h2v2h-2z"/>
            </svg>
            <p>Richt de camera op de QR-code</p>
          </div>

          <!-- Corner decorations -->
          <div class="corner top-left"></div>
          <div class="corner top-right"></div>
          <div class="corner bottom-left"></div>
          <div class="corner bottom-right"></div>
        </div>

        <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>
        <div v-if="errorMessage" class="alert alert-error">❌ {{ errorMessage }}</div>

        <button v-if="!isScanning" class="btn-primary" @click="startScanner">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
            <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
            <line x1="7" y1="12" x2="17" y2="12"/>
          </svg>
          Start Scannen
        </button>
        <button v-else class="btn-secondary" @click="stopScanner">
          ✋ Stop Scannen
        </button>
      </div>
    </div>

    <BedrijfBottomNav />
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #f0f4ff;
  padding-bottom: 80px;
}

.page-header {
  background: #0b57d0;
  color: white;
  padding: 24px 20px;
}

.header-inner {
  max-width: 480px;
  margin: 0 auto;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px;
}

.page-header p {
  opacity: 0.8;
  margin: 0;
  font-size: 14px;
}

.page-body {
  max-width: 480px;
  margin: 0 auto;
  padding: 16px;
}

.banner {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
}

.banner-offline {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fcd34d;
}

.banner-sync {
  background: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

.scanner-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.qr-frame {
  width: 100%;
  aspect-ratio: 1;
  background: #0f172a;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-reader-el {
  width: 100%;
  height: 100%;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #64748b;
}

.qr-placeholder svg {
  width: 64px;
  height: 64px;
  color: #475569;
}

.qr-placeholder p {
  font-size: 14px;
  margin: 0;
}

.corner {
  position: absolute;
  width: 24px;
  height: 24px;
  border-color: #0b57d0;
  border-style: solid;
}

.top-left { top: 12px; left: 12px; border-width: 3px 0 0 3px; border-radius: 4px 0 0 0; }
.top-right { top: 12px; right: 12px; border-width: 3px 3px 0 0; border-radius: 0 4px 0 0; }
.bottom-left { bottom: 12px; left: 12px; border-width: 0 0 3px 3px; border-radius: 0 0 0 4px; }
.bottom-right { bottom: 12px; right: 12px; border-width: 0 3px 3px 0; border-radius: 0 0 4px 0; }

.alert {
  width: 100%;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
}

.alert-success {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.alert-error {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.btn-primary {
  width: 100%;
  padding: 14px;
  background: #0b57d0;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-primary svg {
  width: 20px;
  height: 20px;
}

.btn-primary:hover { background: #0944a8; }

.btn-secondary {
  width: 100%;
  padding: 14px;
  background: white;
  color: #0b57d0;
  border: 1px solid #0b57d0;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-secondary:hover { background: #f0f4ff; }
</style>
