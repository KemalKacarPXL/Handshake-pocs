<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import QrcodeVue from "qrcode.vue";
import StudentBottomNav from "../components/StudentBottomNav.vue";

const storedEmail = localStorage.getItem("studentEmail");
const studentName = ref("");
const studentEducation = ref("");
const timeLeft = ref(60);
let qrTimer = null;

const qrTimestamp = ref(Date.now());

const qrValue = computed(() => {
  if (!storedEmail) return "";
  const base = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  return `${base}/scan-confirm?studentEmail=${encodeURIComponent(storedEmail)}&t=${qrTimestamp.value}`;
});

const handleRefresh = () => {
  timeLeft.value = 60;
  qrTimestamp.value = Date.now();
};

const formatTime = (s) =>
  `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;

onMounted(async () => {
  try {
    const response = await fetch(`/api/student/profile?email=${encodeURIComponent(storedEmail)}`);
    const data = await response.json();
    studentName.value = `${data.firstname || ""} ${data.lastname || ""}`.trim();
    studentEducation.value = `${data.education || ""}`.trim();
  } catch {
    studentName.value = storedEmail || "Student";
  }

  qrTimer = setInterval(() => {
    timeLeft.value--;
    if (timeLeft.value <= 0) {
      timeLeft.value = 60;
      qrTimestamp.value = Date.now();
    }
  }, 1000);
});

onUnmounted(() => clearInterval(qrTimer));
</script>

<template>
  <div class="page-wrapper">
    <div class="phone-frame student-profile-page">
      <header class="profile-header">
        <h1>Mijn QR-Code</h1>
        <p>Laat bedrijven je code scannen</p>
      </header>

      <main class="profile-content">
        <div class="qr-card">
          <div class="qr-card-header">
            <h2>Welkom, {{ studentName.split(" ")[0] || "Student" }}!</h2>
            <p>{{ studentEducation }}</p>
          </div>

          <div class="qr-card-content">
            <div class="qr-code-wrapper" v-if="qrValue">
              <qrcode-vue :value="qrValue" :size="240" level="H" />
            </div>
            <p v-else>QR code laden...</p>

            <div class="qr-timer">
              <p>Code vervalt over</p>
              <div class="qr-timer-badge">
                <span>{{ formatTime(timeLeft) }}</span>
              </div>
            </div>

            <button class="qr-refresh-btn" @click="handleRefresh">
              🔄 Vernieuw Code
            </button>
          </div>
        </div>

        <div class="qr-tip">
          💡 <strong>Tip:</strong> Zorg dat je profiel volledig is ingevuld en je CV geüpload is!
        </div>
      </main>
      <StudentBottomNav />
    </div>
  </div>
</template>

<style scoped>
.student-profile-page { padding-bottom: 88px; }
.profile-header { background: #0e66c2; color: #fff; padding: 26px 20px 24px; }
.profile-header h1 { font-size: 28px; font-weight: 800; }
.profile-header p { margin-top: 6px; font-size: 15px; opacity: 0.9; }
.profile-content { padding: 16px; }


.qr-header h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
}


.qr-card {
  background: linear-gradient(135deg, #eff6ff, white);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.qr-card-header {
  text-align: center;
  padding: 24px 24px 0;
}

.qr-card-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px;
}

.qr-card-header p {
  color: #5f6368;
  font-size: 14px;
  margin: 0;
}

.qr-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  gap: 20px;
}

.qr-code-wrapper {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.qr-timer {
  text-align: center;
}

.qr-timer p {
  font-size: 14px;
  color: #5f6368;
  margin: 0 0 8px;
}

.qr-timer-badge {
  display: inline-flex;
  background: rgba(11, 87, 208, 0.1);
  padding: 12px 24px;
  border-radius: 999px;
}

.qr-timer-badge span {
  font-size: 24px;
  font-weight: 700;
  color: #0b57d0;
}

.qr-refresh-btn {
  width: 100%;
  padding: 14px;
  border: 1px solid #dadce0;
  border-radius: 8px;
  background: white;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.qr-refresh-btn:hover {
  background: #f8f9fa;
}

.qr-tip {
  background: #e8f0fe;
  border: 1px solid #c5d4f5;
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
  text-align: center;
  color: #1a3a6b;
}

@media (max-width: 480px) {
  .qr-header h1 {
    font-size: 39px;
  }

  .qr-header p {
    font-size: 28px;
  }
}
</style>
