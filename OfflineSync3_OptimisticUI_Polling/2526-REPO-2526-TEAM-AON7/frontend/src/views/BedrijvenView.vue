<script setup>
import { ref, computed, onMounted } from "vue";
import CompanyInviteModal from "../components/CompanyInviteModal.vue";

const searchQuery = ref("");
const showInviteModal = ref(false);
const bedrijven = ref([]);
const loading = ref(true);

const fetchBedrijven = async () => {
  try {
    const res = await fetch("/api/bedrijf/all");
    if (res.ok) {
      bedrijven.value = await res.json();
    }
  } catch (e) {
    throw new Error("Fout bij ophalen bedrijven:", e);
  } finally {
    loading.value = false;
  }
};

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

onMounted(fetchBedrijven);
</script>

<template>
  <div>
    <h1>Bedrijven Beheer</h1>
    <p class="subtitle">Beheer bedrijven en stuur uitnodigingen</p>

    <div class="stats-card">
      <div class="stats-info">
        <span class="stats-number">{{ bedrijven.length }}</span>
        <span class="stats-label">Totaal Bedrijven</span>
      </div>
      <svg class="stats-icon" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#c7d2fe" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
        <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2" />
        <line x1="12" y1="12" x2="12" y2="12.01" />
      </svg>
    </div>

    <div class="action-card">
      <div class="search-wrapper">
        <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          class="search-input"
          placeholder="Zoek op naam, sector, locatie..."
        />
      </div>

      <button class="invite-btn" @click="showInviteModal = true">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
          <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
        </svg>
        Uitnodiging Link
      </button>
    </div>

    <div v-if="loading" class="loading-text">Bedrijven laden...</div>

    <div v-else-if="filteredBedrijven.length === 0" class="empty-text">
      {{ searchQuery ? "Geen bedrijven gevonden." : "Nog geen bedrijven geregistreerd." }}
    </div>

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

        <p v-if="bedrijf.description" class="bedrijf-description">{{ bedrijf.description }}</p>

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

    <CompanyInviteModal
      :visible="showInviteModal"
      @close="showInviteModal = false"
    />
  </div>
</template>

<style scoped>
.subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 20px;
}

.stats-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
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
  font-size: 32px;
  font-weight: 800;
  color: #1a56db;
  line-height: 1;
}

.stats-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.stats-icon {
  opacity: 0.7;
}

.action-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
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

.invite-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #1a56db;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  align-self: flex-start;
}

.invite-btn:hover {
  background: #1344b0;
}

.loading-text,
.empty-text {
  text-align: center;
  padding: 32px 16px;
  color: #9ca3af;
  font-size: 14px;
}

.bedrijf-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bedrijf-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.2s ease;
}

.bedrijf-card:hover {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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

.bedrijf-description {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin: 0 0 16px;
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

</style>
