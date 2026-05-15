<script setup>
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const navItems = [
  { path: "/bedrijf/scan", label: "Scannen", icon: "scan" },
  { path: "/bedrijf/studenten", label: "Studenten", icon: "users" },
  { path: "/bedrijf/profiel", label: "Profiel", icon: "building" },
];

const isActive = (path) => {
  if (path === "/bedrijf/studenten") {
    return route.path.startsWith("/bedrijf/studenten") || route.path.startsWith("/bedrijf/studentinfo");
  }

  return route.path.startsWith(path);
};
</script>

<template>
  <nav class="bottom-nav">
    <div class="bottom-nav-inner">
      <button
        v-for="item in navItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        @click="router.push(item.path)"
      >
        <!-- Scan icon -->
        <svg v-if="item.icon === 'scan'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
          <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
          <line x1="7" y1="12" x2="17" y2="12"/>
        </svg>

        <!-- Users icon -->
        <svg v-if="item.icon === 'users'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>

        <!-- Building icon -->
        <svg v-if="item.icon === 'building'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 3v18"/><path d="M3 9h6"/><path d="M3 15h6"/>
          <path d="M13 7h2"/><path d="M13 11h2"/><path d="M13 15h2"/>
        </svg>

        <span>{{ item.label }}</span>
      </button>
    </div>
  </nav>
</template>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.bottom-nav-inner {
  max-width: 480px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 8px;
  border: none;
  background: none;
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.2s;
  gap: 4px;
}

.nav-item svg {
  width: 24px;
  height: 24px;
}

.nav-item span {
  font-size: 11px;
  font-weight: 500;
}

.nav-item.active {
  color: #0b57d0;
}

.nav-item:hover:not(.active) {
  color: #374151;
}
</style>
