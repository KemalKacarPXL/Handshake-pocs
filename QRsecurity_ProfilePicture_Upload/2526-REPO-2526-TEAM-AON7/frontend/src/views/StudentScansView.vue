<script setup>
import { ref, computed, onMounted } from "vue";
import StudentBottomNav from "../components/StudentBottomNav.vue";
import { utils, writeFile } from 'xlsx';
import jsPDF from 'jspdf';

const studentEmail = localStorage.getItem("studentEmail");
const scans = ref([]);
const searchQuery = ref("");
const isLoading = ref(false);
const errorMessage = ref("");

const filteredScans = computed(() => {
  if (!searchQuery.value) return scans.value;
  const q = searchQuery.value.toLowerCase();
  return scans.value.filter(
    (s) =>
      s.bedrijfName?.toLowerCase().includes(q) ||
      s.bedrijfSector?.toLowerCase().includes(q) ||
      s.bedrijfEmail?.toLowerCase().includes(q)
  );
});

const exportToExcel = () => {
  const data = filteredScans.value.map(scan => ({
    Bedrijf: scan.bedrijfName,
    Sector: scan.bedrijfSector,
    Email: scan.bedrijfEmail,
    'Gescand op': formatDate(scan.scannedAt)
  }));
  
  const worksheet = utils.json_to_sheet(data);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "Scans");
  writeFile(workbook, `scans_${new Date().toISOString().split('T')[0]}.xlsx`);
};

const exportToPDF = () => {
  const doc = new jsPDF();
  let yPosition = 15;
  
  // Title
  doc.setFontSize(16);
  doc.text("Mijn Scans", 10, yPosition);
  yPosition += 10;
  
  // Date generated
  doc.setFontSize(10);
  doc.setTextColor(128);
  doc.text(`Gegenereerd op: ${formatDate(new Date())}`, 10, yPosition);
  yPosition += 8;
  
  // Headers
  doc.setTextColor(0);
  doc.setFont(undefined, 'bold');
  doc.text("Bedrijf", 10, yPosition);
  doc.text("Sector", 60, yPosition);
  doc.text("Email", 110, yPosition);
  doc.text("Gescand op", 160, yPosition);
  yPosition += 7;
  
  // Line under headers
  //doc.setDrawColor(200);
  //doc.line(10, yPosition - 1, 200, yPosition - 1);
  
  // Data rows
  doc.setFont(undefined, 'normal');
  doc.setFontSize(9);
  doc.setTextColor(50);
  
  filteredScans.value.forEach((scan) => {
    if (yPosition > 270) {
      doc.addPage();
      yPosition = 15;
    }
    
    doc.text(scan.bedrijfName, 10, yPosition);
    doc.text(scan.bedrijfSector, 60, yPosition);
    doc.text(scan.bedrijfEmail, 110, yPosition);
    doc.text(formatDate(scan.scannedAt), 160, yPosition);
    yPosition += 7;
  });
  
  doc.save(`scans_${new Date().toISOString().split('T')[0]}.pdf`);
};

const loadScans = async () => {
  isLoading.value = true;
  errorMessage.value = "";
  try {
    const response = await fetch(`/api/student/scans?email=${encodeURIComponent(studentEmail)}`);
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
  return new Date(dateStr).toLocaleDateString("nl-BE", {
    day: "numeric",
    month: "long",
    year: "numeric",
    hour: "numeric",
    minute: "numeric",
  });
};

const getInitials = (name) => {
  return (name || "?").charAt(0).toUpperCase();
};

onMounted(loadScans);
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div class="header-inner">
        <h1>Mijn Scans</h1>
        <p>Bedrijven die jou gescand hebben</p>
      </div>
    </div>

    <div class="page-body">
      <div class="export-buttons" v-if="filteredScans.length > 0">
  <button @click="exportToExcel" class="btn btn-excel">Export Excel</button>
  <button @click="exportToPDF" class="btn btn-pdf">Export PDF</button>
</div>
      <div class="search-bar">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input v-model="searchQuery" type="text" placeholder="Zoek bedrijven..." />
      </div>

      <div v-if="isLoading" class="state-message">Laden...</div>
      <div v-else-if="errorMessage" class="state-message error">{{ errorMessage }}</div>
      <div v-else-if="filteredScans.length === 0" class="state-message">
        {{ searchQuery ? "Geen bedrijven gevonden." : "Nog geen bedrijven hebben jou gescand." }}
      </div>

      <div v-else class="scan-list">
        <div
          v-for="scan in filteredScans"
          :key="scan.bedrijfEmail + scan.scannedAt"
          class="scan-item"
        >
          <div class="scan-avatar">{{ getInitials(scan.bedrijfName) }}</div>
          <div class="scan-info">
            <div class="scan-name">{{ scan.bedrijfName }}</div>
            <div class="scan-sector">{{ scan.bedrijfSector }}</div>
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
          <div class="interest-badge">Interesse</div>
        </div>
      </div>
    </div>

    <StudentBottomNav />
  </div>
</template>

<style scoped>
.export-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.btn {
  flex: 1;
  padding: 10px 14px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-excel {
  background: #0e66c2;
  color: white;
}

.btn-excel:hover {
  background: #0e66c2;
  transform: translateY(-1px);
}

.btn-pdf {
  background: #0e66c2;
  color: white;
}

.btn-pdf:hover {
  background: #0e66c2;
  transform: translateY(-1px);
}
.page {
  min-height: 100vh;
  background: #f0f4ff;
  padding-bottom: 80px;
}

.page-header {
  background: #0e66c2;
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
  border: 1px solid #e5e7eb;
  transition: box-shadow 0.2s;
}

.scan-item:hover { box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08); }

.scan-avatar {
  width: 44px;
  height: 44px;
  border-radius: 999px;
  background: #e0e7f3;
  color: #3b5ea6;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 16px;
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

.scan-sector {
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

.interest-badge {
  background: #e0e7ff;
  color: #3730a3;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 999px;
  flex-shrink: 0;
}
</style>
