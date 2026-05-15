<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const eventId = computed(() => route.params.id);

const bedrijven = ref([]);
const loading = ref(true);
const searchQuery = ref("");
const expandedBedrijfId = ref(null);

const fetchData = async () => {
  try {
    const res = await fetch(`/api/bedrijf/by-event/${eventId.value}/with-scans`);
    if (res.ok) {
      bedrijven.value = await res.json();
    }
  } catch (e) {
    console.error("Fout bij ophalen data:", e);
  } finally {
    loading.value = false;
  }
};

const totalScans = computed(() =>
  bedrijven.value.reduce((sum, b) => sum + b.scanCount, 0)
);


const filteredBedrijven = computed(() => {
  const q = searchQuery.value.toLowerCase().trim();
  if (!q) return bedrijven.value;
  return bedrijven.value.filter(
    (b) =>
      (b.companyName || "").toLowerCase().includes(q) ||
      (b.sector || "").toLowerCase().includes(q) ||
      b.scans.some((s) =>
        `${s.studentFirstname} ${s.studentLastname}`.toLowerCase().includes(q)
      )
  );
});

const toggleBedrijf = (id) => {
  expandedBedrijfId.value = expandedBedrijfId.value === id ? null : id;
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  return d.toLocaleDateString("nl-BE", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
};

onMounted(fetchData);
</script>

<template>
  <div>
    <h1 class="page-title">Dashboard</h1>
    <p class="subtitle">Overzicht van het matchingevent</p>

    <div class="export-row">
      <button class="export-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
          <polyline points="7 10 12 15 17 10" />
          <line x1="12" y1="15" x2="12" y2="3" />
        </svg>
        Export Excel
      </button>
      <button class="export-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
          <polyline points="7 10 12 15 17 10" />
          <line x1="12" y1="15" x2="12" y2="3" />
        </svg>
        Export PDF
      </button>
    </div>

    <div class="stats-row">
      <div class="stats-card">
        <div class="stats-info">
          <span class="stats-label">Totaal Scans</span>
          <span class="stats-number">{{ totalScans }}</span>
          <span class="stats-sub">Contactmomenten vandaag</span>
        </div>
        <div class="stats-icon-wrap">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#1a56db" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 7h4v4H3zM3 14h4v4H3zM10 7h4v4h-4zM10 14h4v4h-4zM17 7h4v4h-4zM17 14h4v4h-4z" />
          </svg>
        </div>
      </div>
    </div>

    <div class="section-card">
      <div class="section-header">
        <div>
          <h2 class="section-title">Bedrijven en Gescande Studenten</h2>
          <p class="section-subtitle">Klik op een bedrijf om de gescande studenten te zien</p>
        </div>
      </div>

      <div class="search-wrapper">
        <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          class="search-input"
          placeholder="Zoek op bedrijfsnaam, sector of studentnaam..."
        />
      </div>

      <div v-if="loading" class="empty-state">Gegevens laden...</div>

      <div v-else-if="filteredBedrijven.length === 0" class="empty-state">
        {{ searchQuery ? "Geen bedrijven gevonden." : "Nog geen bedrijven geregistreerd voor dit event." }}
      </div>

      <div v-else class="bedrijf-list">
        <div
          v-for="bedrijf in filteredBedrijven"
          :key="bedrijf.id"
          class="bedrijf-item"
          :class="{ expanded: expandedBedrijfId === bedrijf.id }"
        >
          <button class="bedrijf-row" @click="toggleBedrijf(bedrijf.id)">
            <div class="bedrijf-logo">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#1a56db" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="7" width="20" height="14" rx="2" />
                <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2" />
              </svg>
            </div>

            <div class="bedrijf-info">
              <div class="bedrijf-top-row">
                <span class="bedrijf-name">{{ bedrijf.companyName || "Onbekend bedrijf" }}</span>
                <span class="scan-badge">{{ bedrijf.scanCount }} {{ bedrijf.scanCount === 1 ? "scan" : "scans" }}</span>
              </div>
              <span class="bedrijf-sector-tag">{{ bedrijf.sector || "—" }}</span>

              <div v-if="bedrijf.scans.length > 0" class="preview-row">
                <div class="avatar-stack">
                  <div
                    v-for="(scan, i) in bedrijf.scans.slice(0, 3)"
                    :key="scan.studentEmail"
                    class="avatar-bubble"
                    :style="{ zIndex: 3 - i }"
                    :title="`${scan.studentFirstname} ${scan.studentLastname}`"
                  >
                    {{ scan.studentFirstname[0] }}{{ scan.studentLastname[0] }}
                  </div>
                  <div v-if="bedrijf.scans.length > 3" class="avatar-bubble avatar-more">
                    +{{ bedrijf.scans.length - 3 }}
                  </div>
                </div>
                <span class="preview-names">
                  {{ bedrijf.scans.slice(0, 2).map(s => s.studentFirstname).join(", ") }}
                  <span v-if="bedrijf.scans.length > 2"> en {{ bedrijf.scans.length - 2 }} anderen</span>
                </span>
              </div>
              <div v-else class="preview-empty">Nog geen studenten gescand</div>
            </div>

            <svg
              class="chevron"
              :class="{ rotated: expandedBedrijfId === bedrijf.id }"
              width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </button>

          <Transition name="expand">
            <div v-if="expandedBedrijfId === bedrijf.id" class="student-panel">
              <div v-if="bedrijf.scans.length === 0" class="no-scans">
                Nog geen studenten gescand door dit bedrijf.
              </div>
              <div v-else class="student-list">
                <div
                  v-for="scan in bedrijf.scans"
                  :key="scan.studentEmail"
                  class="student-row"
                >
                  <div class="student-avatar-lg">
                    {{ scan.studentFirstname[0] }}{{ scan.studentLastname[0] }}
                  </div>
                  <div class="student-info">
                    <span class="student-name">{{ scan.studentFirstname }} {{ scan.studentLastname }}</span>
                    <span class="student-edu">{{ scan.studentEducation }}</span>
                  </div>
                  <span class="scan-time">{{ formatDate(scan.scannedAt) }}</span>
                </div>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 26px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 4px;
}

.subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 18px;
}

.export-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}

.export-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 16px;
  border-radius: 10px;
  border: 1.5px solid #d1d5db;
  background: white;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s;
}

.export-btn:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
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
  padding: 18px 16px;
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

.stats-label {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.stats-number {
  font-size: 30px;
  font-weight: 800;
  color: #1a56db;
  line-height: 1.1;
}

.stats-sub {
  font-size: 11px;
  color: #9ca3af;
}

.stats-icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.section-card {
  background: white;
  border-radius: 14px;
  padding: 20px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.section-header {
  margin-bottom: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 2px;
}

.section-subtitle {
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
}

.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  margin-bottom: 14px;
}

.search-icon {
  position: absolute;
  left: 12px;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 10px 12px 10px 36px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f8f9fa;
  font-size: 14px;
  color: #1a1a1a;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.search-input:focus {
  border-color: #1a56db;
  background: white;
  box-shadow: 0 0 0 2px rgba(26, 86, 219, 0.1);
}

.search-input::placeholder {
  color: #a0aab4;
}

.empty-state {
  text-align: center;
  padding: 32px 16px;
  color: #9ca3af;
  font-size: 14px;
}

.bedrijf-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.bedrijf-item {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 8px;
  transition: box-shadow 0.15s;
}

.bedrijf-item:last-child {
  margin-bottom: 0;
}

.bedrijf-item.expanded {
  box-shadow: 0 2px 10px rgba(26, 86, 219, 0.08);
  border-color: #c7d2fe;
}

.bedrijf-row {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 14px;
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
  transition: background 0.12s;
}

.bedrijf-row:hover {
  background: #f8f9ff;
}

.bedrijf-logo {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.bedrijf-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.bedrijf-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.bedrijf-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bedrijf-sector-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 6px;
  padding: 2px 8px;
  width: fit-content;
}

.scan-badge {
  padding: 3px 10px;
  border-radius: 20px;
  background: #eef2ff;
  color: #1a56db;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}

.preview-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.avatar-stack {
  display: flex;
  flex-direction: row-reverse;
}

.avatar-bubble {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #c7d2fe;
  color: #1a56db;
  font-size: 9px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  margin-left: -6px;
  text-transform: uppercase;
  flex-shrink: 0;
}

.avatar-bubble:last-child {
  margin-left: 0;
}

.avatar-more {
  background: #e5e7eb;
  color: #6b7280;
  font-size: 9px;
}

.preview-names {
  font-size: 11px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preview-empty {
  font-size: 11px;
  color: #c0c5cc;
  font-style: italic;
  margin-top: 2px;
}

.chevron {
  transition: transform 0.2s ease;
  flex-shrink: 0;
  margin-top: 14px;
}

.chevron.rotated {
  transform: rotate(180deg);
}

.student-panel {
  border-top: 1px solid #f0f0f0;
  background: #fafbff;
  padding: 4px 0 8px;
}

.no-scans {
  padding: 14px 16px;
  font-size: 13px;
  color: #9ca3af;
  text-align: center;
}

.student-list {
  display: flex;
  flex-direction: column;
}

.student-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.1s;
}

.student-row:last-child {
  border-bottom: none;
}

.student-row:hover {
  background: #f0f4ff;
}

.student-avatar-lg {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #c7d2fe;
  color: #1a56db;
  font-size: 12px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  text-transform: uppercase;
}

.student-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.student-name {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-edu {
  font-size: 11px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.scan-time {
  font-size: 11px;
  color: #9ca3af;
  white-space: nowrap;
  flex-shrink: 0;
}

.expand-enter-active,
.expand-leave-active {
  transition: max-height 0.25s ease, opacity 0.2s ease;
  max-height: 600px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>
