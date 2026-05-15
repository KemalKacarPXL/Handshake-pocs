<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const studenten = ref([]);
const loading = ref(true);
const searchQuery = ref("");

const eventId = computed(() => route.params.id);

const fetchStudenten = async () => {
  try {
    const res = await fetch(`/api/student/by-event/${eventId.value}`);
    if (res.ok) {
      studenten.value = await res.json();
    }
  } catch (e) {
    throw new Error("Fout bij ophalen studenten:", e);
  } finally {
    loading.value = false;
  }
};

const filteredStudenten = computed(() => {
  const q = searchQuery.value.toLowerCase().trim();
  if (!q) return studenten.value;
  return studenten.value.filter((s) => {
    const fullName = `${s.firstname} ${s.lastname}`.toLowerCase();
    const education = (s.education || "").toLowerCase();
    return fullName.includes(q) || education.includes(q);
  });
});

const totalStudenten = computed(() => studenten.value.length);
const totalCvs = computed(() => studenten.value.filter((s) => s.hasCv).length);

const downloadCv = (email) => {
  window.open(`/api/student/profile/cv/download?email=${encodeURIComponent(email)}`, "_blank");
};

onMounted(fetchStudenten);
</script>

<template>
  <div>
    <h1>Studenten Overzicht</h1>
    <p class="subtitle">Bekijk alle geregistreerde studenten</p>

    <div class="stats-row">
      <div class="stats-card">
        <div class="stats-info">
          <span class="stats-number">{{ totalStudenten }}</span>
          <span class="stats-label">Totaal Studenten</span>
        </div>
        <svg class="stats-icon" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#c7d2fe" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
          <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
      </div>

      <div class="stats-card">
        <div class="stats-info">
          <span class="stats-number">{{ totalCvs }}</span>
          <span class="stats-label">CV's Geüpload</span>
        </div>
        <svg class="stats-icon" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#c7d2fe" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
      </div>
    </div>

    <div class="search-card">
      <div class="search-wrapper">
        <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          class="search-input"
          placeholder="Zoek studenten op naam of richting..."
        />
      </div>
    </div>

    <div v-if="loading" class="loading-text">Studenten laden...</div>

    <div v-else-if="filteredStudenten.length === 0" class="empty-text">
      {{ searchQuery ? "Geen studenten gevonden." : "Nog geen studenten geregistreerd voor dit event." }}
    </div>

    <div v-else class="student-section">
      <h2 class="section-title">Studentenlijst</h2>

      <div class="student-list">
        <div v-for="student in filteredStudenten" :key="student.email" class="student-card">
          <div class="student-header">
            <div class="student-avatar">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </div>
            <div class="student-info">
              <span class="student-name">{{ student.firstname }} {{ student.lastname }}</span>
              <span class="student-email">{{ student.email }}</span>
              <span class="student-education">{{ student.education }}</span>
            </div>
            <button
              v-if="student.hasCv"
              class="cv-badge"
              @click="downloadCv(student.email)"
              title="Download CV"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              CV
            </button>
            <span v-else class="cv-badge cv-badge--none" title="Geen CV geüpload">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              —
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 20px;
}

.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.stats-card {
  flex: 1;
  background: white;
  border-radius: 14px;
  padding: 20px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stats-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stats-number {
  font-size: 28px;
  font-weight: 800;
  color: #1a56db;
  line-height: 1;
}

.stats-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.stats-icon {
  opacity: 0.7;
  flex-shrink: 0;
}

.search-card {
  background: white;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 14px;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 12px 14px 12px 42px;
  border-radius: 10px;
  border: 1px solid #e0e0e0;
  background: #f8f9fa;
  font-size: 15px;
  color: #1f1f1f;
  outline: none;
  transition: all 0.2s ease;
}

.search-input:focus {
  border-color: #1a56db;
  background: white;
  box-shadow: 0 0 0 2px rgba(26, 86, 219, 0.1);
}

.search-input::placeholder {
  color: #a0aab4;
}

.loading-text,
.empty-text {
  text-align: center;
  padding: 32px 16px;
  color: #9ca3af;
  font-size: 14px;
}

.student-section {
  background: white;
  border-radius: 14px;
  padding: 20px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 16px;
}

.student-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.student-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  transition: box-shadow 0.2s ease;
}

.student-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.student-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.student-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.student-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.student-name {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
}

.student-email {
  font-size: 13px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.student-education {
  font-size: 13px;
  color: #1a56db;
  font-weight: 600;
  margin-top: 2px;
}

.cv-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  background: #eef2ff;
  color: #1a56db;
  border: 1px solid #c7d2fe;
  cursor: pointer;
  transition: all 0.15s;
}

.cv-badge:hover {
  background: #1a56db;
  color: white;
}

.cv-badge--none {
  background: #f3f4f6;
  color: #9ca3af;
  border-color: #e5e7eb;
  cursor: default;
}

.cv-badge--none:hover {
  background: #f3f4f6;
  color: #9ca3af;
}
</style>
