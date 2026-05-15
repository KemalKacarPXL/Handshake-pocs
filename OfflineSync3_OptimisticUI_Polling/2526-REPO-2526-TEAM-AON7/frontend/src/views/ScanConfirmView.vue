<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const loading = ref(true)
const success = ref(false)
const alreadyScanned = ref(false)
const errorMsg = ref('')

onMounted(async () => {
  const studentEmail = route.query.studentEmail
  const bedrijfEmail = localStorage.getItem('bedrijfEmail')

  if (!studentEmail) {
    errorMsg.value = 'Geen student email gevonden in QR code.'
    loading.value = false
    return
  }

  if (!bedrijfEmail) {
    errorMsg.value = 'Je bent niet ingelogd als bedrijf.'
    loading.value = false
    return
  }

  try {
    const res = await fetch(
      `/api/bedrijf/scan?studentEmail=${encodeURIComponent(studentEmail)}`,
      {
        method: 'POST',
        headers: { 'bedrijf-email': bedrijfEmail }
      }
    )

    if (res.status === 409) {
      alreadyScanned.value = true
      return
    }

    if (!res.ok) {
      errorMsg.value = 'Scannen mislukt.'
      return
    }

    success.value = true
  } catch {
    errorMsg.value = 'Kan geen verbinding maken met de server.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="confirm-page">
    <div class="confirm-card">
      <div v-if="loading" class="state">
        <div class="spinner"></div>
        <p>Scan wordt verwerkt...</p>
      </div>

      <div v-else-if="success" class="state success">
        <div class="icon">✅</div>
        <h2>Scan gelukt!</h2>
        <p>De student is succesvol opgeslagen in je scanlijst.</p>
        <button @click="$router.push('/bedrijf/studenten')">Bekijk scans</button>
      </div>

      <div v-else-if="alreadyScanned" class="state warning">
        <div class="icon">⚠️</div>
        <h2>Al gescand</h2>
        <p>Deze student werd al eerder gescand.</p>
        <button @click="$router.push('/bedrijf/studenten')">Bekijk scans</button>
      </div>

      <div v-else class="state error">
        <div class="icon">❌</div>
        <h2>Fout</h2>
        <p>{{ errorMsg }}</p>
        <button @click="$router.push('/')">Terug naar home</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.confirm-page {
  min-height: 100vh;
  background: #f0f4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.confirm-card {
  background: white;
  border-radius: 16px;
  padding: 40px 32px;
  max-width: 400px;
  width: 100%;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  text-align: center;
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.icon { font-size: 64px; }

.state h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
}

.state p {
  color: #6b7280;
  font-size: 15px;
  margin: 0;
}

.state button {
  margin-top: 8px;
  padding: 12px 28px;
  background: #0b57d0;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.state button:hover { background: #0944a8; }

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top-color: #0b57d0;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
