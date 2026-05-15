<template>
  <div class="page-wrapper">
    <div class="phone-frame student-profile-page">
      <header class="profile-header">
        <h1>Bedrijven</h1>
        <p>Overzicht van deelnemende bedrijven</p>
      </header>
      <main class="profile-content">
        <section v-if="loading" class="card info-card">Bedrijven laden...</section>
        <section v-else-if="error" class="card info-card error">{{ error }}</section>
        <section v-else>
          <div class="search-wrapper">
            <input
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="Zoek op naam, sector, locatie..."
            />
          </div>
          <div v-if="filteredBedrijven.length === 0" class="card info-card">Geen bedrijven gevonden voor dit event.</div>
          <div v-else class="bedrijf-list">
            <div v-for="bedrijf in filteredBedrijven" :key="bedrijf.id" class="bedrijf-card">
              <div class="bedrijf-header">
                <div class="bedrijf-icon">
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#6b7280" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="7" height="18" rx="1" />
                    <rect x="14" y="8" width="7" height="13" rx="1" />
                    <line x1="6" y1="7" x2="6" y2="7.01" />
                    <line x1="6" y1="10" x2="6" y2="10.01" />
                    <line x1="6" y1="13" x2="6" y2="13.01" />
                    <line x1="6" y1="16" x2="6" y2="16.01" />
                    <line x1="17.5" y1="12" x2="17.5" y2="12.01" />
                    <line x1="17.5" y1="15" x2="17.5" y2="15.01" />
                    <line x1="17.5" y1="18" x2="17.5" y2="18.01" />
                  </svg>
                </div>
                <div class="bedrijf-title">
                  <span class="bedrijf-name">{{ displayCompanyName(bedrijf) }}</span>
                  <span v-if="bedrijf.sector" class="bedrijf-sector">{{ bedrijf.sector }}</span>
                </div>
              </div>
              <div class="bedrijf-details">
                <div class="detail-row">
                  <span class="detail-label">Contactpersoon</span>
                  <span class="detail-label">Email</span>
                </div>
                <div class="detail-row">
                  <span class="detail-value">{{ bedrijf.firstname }} {{ bedrijf.lastname }}</span>
                  <span class="detail-value email-value" :title="bedrijf.email">{{ bedrijf.email }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
      <StudentBottomNav />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import StudentBottomNav from "../components/StudentBottomNav.vue";

const bedrijven = ref([]);
const loading = ref(true);
const error = ref("");
const searchQuery = ref("");

const fallbackCompanyName = (email) => {
  if (!email) return "Onbekend";
  const domain = email.split("@")[1];
  if (!domain) return "Onbekend";
  const name = domain.split(".")[0];
  return name.charAt(0).toUpperCase() + name.slice(1);
};

const displayCompanyName = (bedrijf) => {
  const name = (bedrijf.companyName || "").trim();
  return name || fallbackCompanyName(bedrijf.email);
};

const filteredBedrijven = computed(() => {
  const q = searchQuery.value.toLowerCase().trim();
  if (!q) return bedrijven.value;
  return bedrijven.value.filter((b) => {
    const fullName = `${b.firstname} ${b.lastname}`.toLowerCase();
    const company = displayCompanyName(b).toLowerCase();
    const desc = (b.description || "").toLowerCase();
    const sector = (b.sector || "").toLowerCase();
    const location = (b.location || "").toLowerCase();
    return fullName.includes(q) || company.includes(q) || b.email.toLowerCase().includes(q) || desc.includes(q) || sector.includes(q) || location.includes(q);
  });
});

const fetchBedrijvenByEvent = async (eventId) => {
  try {
    const res = await fetch(`/api/bedrijf/by-event/${eventId}`);
    if (res.ok) {
      bedrijven.value = await res.json();
    } else {
      error.value = "Kon bedrijven niet laden.";
    }
  } catch {
    error.value = "Kan geen verbinding maken met de server.";
  } finally {
    loading.value = false;
  }
};

const fetchStudentProfile = async () => {
  const studentEmail = localStorage.getItem("studentEmail") || "";
  if (!studentEmail) {
    error.value = "Geen student gevonden.";
    loading.value = false;
    return;
  }
  try {
    const res = await fetch(`/api/student/profile?email=${encodeURIComponent(studentEmail)}`);
    if (res.ok) {
      const profile = await res.json();
      if (profile.joinedEventId) {
        fetchBedrijvenByEvent(profile.joinedEventId);
      } else {
        error.value = "Je neemt niet deel aan een evenement.";
        loading.value = false;
      }
    } else {
      error.value = "Kon studentprofiel niet laden.";
      loading.value = false;
    }
  } catch {
    error.value = "Kan geen verbinding maken met de server.";
    loading.value = false;
  }
};

onMounted(fetchStudentProfile);
</script>

<style scoped>
.student-profile-page { padding-bottom: 88px; }
.profile-header { background: #0e66c2; color: #fff; padding: 26px 20px 24px; }
.profile-header h1 { font-size: 28px; font-weight: 800; }
.profile-header p { margin-top: 6px; font-size: 15px; opacity: 0.9; }
.profile-content { padding: 16px; }
.card { border-radius: 14px; border: 1px solid #d9dde4; padding: 20px; margin-bottom: 16px; background: white; }
.info-card { font-size: 14px; color: #6b7280; }
.search-wrapper {
  margin-bottom: 16px;
    
}
.search-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  margin-bottom: 8px;
  outline: 0.02rem solid rgba(131, 191, 255, 0);
  transition: 0.05s ease-in-out;
}

.search-input::placeholder {
  opacity: 0.7;
}

.search-input:focus-visible {
  outline: 0.02rem solid rgb(72, 161, 255);
}

.bedrijf-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.bedrijf-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 0 solid rgba(131, 176, 255, 0);
  transition: 0.05s ease-in-out;
}

.bedrijf-card:hover {
  box-shadow: 0 0 15px rgba(82, 166, 255, 0.8);
  border: 0.2rem solid rgba(131, 176, 255, 0.6);
}

.bedrijf-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.bedrijf-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bedrijf-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bedrijf-name {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
}

.bedrijf-sector {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.bedrijf-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
}

.detail-row {
  display: flex;
  gap: 16px;
}

.detail-row > * {
  flex: 1;
  min-width: 0;
}

.detail-label {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 600;
}

.email-value {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card.error {
  color: #b91c1c;
  border-color: #fca5a5;
  background: #fef2f2;
}
</style>
