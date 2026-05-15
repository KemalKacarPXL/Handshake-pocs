<script setup>
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const tabs = [
  { label: "Mijn QR", route: "student-qr", requiresCv: true },
  { label: "Bedrijven", route: "student-bedrijven", requiresCv: true },
  { label: "Scans", route: "student-scans", requiresCv: true },
  { label: "Profiel", route: "student-profiel", requiresCv: false },
];

const handleTabClick = (tab) => {
  router.push({ name: tab.route });
};
</script>

<template>
  <footer class="bottom-nav">
    <div
      v-for="tab in tabs"
      :key="tab.route"
      class="nav-item"
      :class="{
        active: route.name === tab.route,
      }"
      @click="handleTabClick(tab)"
    >
      <!-- Mijn QR -->
      <svg v-if="tab.route === 'student-qr'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="3" width="7" height="7" rx="1" />
        <rect x="3" y="14" width="7" height="7" rx="1" />
        <rect x="5" y="5" width="3" height="3" fill="currentColor" stroke="none" />
        <rect x="16" y="5" width="3" height="3" fill="currentColor" stroke="none" />
        <rect x="5" y="16" width="3" height="3" fill="currentColor" stroke="none" />
        <path d="M14 14h3v3h-3zM17 17h3v3h-3zM14 20h3" />
      </svg>

      <!-- Bedrijven -->
      <svg v-else-if="tab.route === 'student-bedrijven'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="7" width="18" height="13" rx="2" />
        <path d="M9 7V5a1.8 1.8 0 0 1 1.8-1.8h2.4A1.8 1.8 0 0 1 15 5v2" />
        <path d="M3 12h18" />
        <path d="M11 12v2h2v-2" />
      </svg>

      <!-- Scans -->
      <svg v-else-if="tab.route === 'student-scans'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="9" />
        <polyline points="12 7 12 12 15.5 15.5" />
      </svg>

      <!-- Profiel -->
      <svg v-else-if="tab.route === 'student-profiel'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="8" r="4" />
        <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
      </svg>

      <span>{{ tab.label }}</span>
    </div>
  </footer>
</template>

<style scoped>
.bottom-nav {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  bottom: 0;
  border-top: 1px solid #d9dde4;
  background: #fff;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: auto auto;
  z-index: 20;
}

.nav-item {
  position: relative;
  height: 66px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #9ca3af;
  font-size: 10px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.15s;
}
.nav-item.active {
  color: #0e66c2;
}
.nav-item svg {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
}
</style>
