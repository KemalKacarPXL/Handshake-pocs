<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import BedrijfBottomNav from "../components/BedrijfBottomNav.vue";

const router = useRouter();
const bedrijfEmail = localStorage.getItem("bedrijfEmail");
const scans = ref([]);
const searchQuery = ref("");
const isLoading = ref(false);
const errorMessage = ref("");

const filteredScans = computed(() => {
  if (!searchQuery.value) return scans.value;
  const q = searchQuery.value.toLowerCase();
  return scans.value.filter(
    (s) =>
      s.studentFirstname?.toLowerCase().includes(q) ||
      s.studentLastname?.toLowerCase().includes(q) ||
      s.studentEmail?.toLowerCase().includes(q)
  );
});

const loadScans = async () => {
  isLoading.value = true;
  errorMessage.value = "";
  try {
    const response = await fetch("/api/bedrijf/scans", {
      headers: { "bedrijf-email": bedrijfEmail },
    });
    if (response.ok) {
      scans.value = await response.json();
    } else {
      errorMessage.value = "Kon scans niet laden.";
    }
  } catch {
    errorMessage.value = "Kan geen verbinding maken met de server.";
  } finally {
    isLoading.value = false;
  }
};

const formatDate = (dateStr) => {
  const date = new Date(dateStr);
  return date.toLocaleDateString("nl-BE", {
    day: "numeric",
    month: "long",
    year: "numeric",
    hour: "numeric",
    minute: "numeric",
  });
};

const getInitials = (firstname, lastname) => {
  return `${firstname?.charAt(0) || ""}${lastname?.charAt(0) || ""}`.toUpperCase();
};

const openStudentDetail = (scan) => {
  router.push(`/bedrijf/studentinfo/${encodeURIComponent(scan.studentEmail)}`);
};

onMounted(loadScans);
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div class="header-inner">
        <h1>Gescande Studenten</h1>
        <p>Overzicht van contactmomenten</p>
      </div>
    </div>

    <div class="page-body">
      <div class="export-row">
        <button class="btn-export">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          Export Excel
        </button>
        <button class="btn-export">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          Export PDF
        </button>
      </div>

      <div class="search-bar">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Zoek studenten..."
        />
      </div>

      <div v-if="isLoading" class="state-message">Laden...</div>
      <div v-else-if="errorMessage" class="state-message error">{{ errorMessage }}</div>
      <div v-else-if="filteredScans.length === 0" class="state-message">
        {{ searchQuery ? "Geen studenten gevonden." : "Nog geen studenten gescand." }}
      </div>

      <div v-else class="scan-list">
        <div
          v-for="scan in filteredScans"
          :key="scan.studentEmail"
          class="scan-item"
          @click="openStudentDetail(scan)"
        >
          <div class="scan-avatar">
            {{ getInitials(scan.studentFirstname, scan.studentLastname) }}
          </div>
          <div class="scan-info">
            <div class="scan-name">{{ scan.studentFirstname }} {{ scan.studentLastname }}</div>
            <div class="scan-education">{{ scan.studentEducation }}</div>
            <div class="scan-date">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              Gescand op {{ formatDate(scan.scannedAt) }}
            </div>
          </div>
        </div>
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
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.export-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.btn-export {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: #374151;
  transition: background 0.2s;
}

.btn-export svg {
  width: 16px;
  height: 16px;
}

.btn-export:hover { background: #f9fafb; }

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 14px;
}

.search-bar svg {
  width: 18px;
  height: 18px;
  color: #9ca3af;
  flex-shrink: 0;
}

.search-bar input {
  border: none;
  outline: none;
  font-size: 14px;
  color: #374151;
  width: 100%;
  background: transparent;
}

.search-bar input::placeholder { color: #9ca3af; }

.state-message {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  padding: 32px 0;
}

.state-message.error { color: #b91c1c; }

.scan-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.scan-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border: 1px solid #e5e7eb;
  transition: box-shadow 0.2s;
}

.scan-item:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }

.scan-avatar {
  width: 44px;
  height: 44px;
  border-radius: 999px;
  background: #e0e7f3;
  color: #3b5ea6;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.scan-info {
  flex: 1;
  min-width: 0;
}

.scan-name {
  font-weight: 700;
  font-size: 15px;
  color: #111827;
}

.scan-education {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.scan-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.scan-date svg {
  width: 13px;
  height: 13px;
  flex-shrink: 0;
}

.cv-badge {
  background: #e0e7ff;
  color: #3730a3;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 999px;
  flex-shrink: 0;
}
</style>
