<script setup>
import { ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useEventStore } from "../stores/eventStore.js";

const router = useRouter();
const route = useRoute();
const { selectedEvent, clearEvent } = useEventStore();
const menuOpen = ref(false);

const tabs = [
  { id: "dashboard", label: "Dashboard", icon: "⊞", route: "event-dashboard" },
  { id: "bedrijven", label: "Bedrijven", icon: "🏢", route: "event-bedrijven" },
  { id: "studenten", label: "Studenten", icon: "👥", route: "event-studenten" },
];

const toggleMenu = () => {
  menuOpen.value = !menuOpen.value;
};

const handleTabClick = (tab) => {
  if (tab.route && selectedEvent.value) {
    router.push({ name: tab.route, params: { id: selectedEvent.value.id } });
  }
  menuOpen.value = false;
};

const handleLogout = () => {
  localStorage.removeItem("coordinatorEmail");
  clearEvent();
  router.push("/");
};

const handleBackToEvents = () => {
  clearEvent();
  router.push("/events");
};
</script>

<template>
  <div class="page-wrapper">
    <div class="phone-frame">
      <header class="top-bar">
        <span class="top-bar-title">Handshake Stagecoördinator</span>
        <button class="menu-btn" @click="toggleMenu" aria-label="Menu">
          <span /><span /><span />
        </button>
      </header>

      <main class="content">
        <router-view />
      </main>

      <div class="overlay" v-if="menuOpen" @click="toggleMenu" />

      <Transition name="slide">
        <nav class="sidebar" v-if="menuOpen">
          <div class="sidebar-header">
            <div>
              <div class="sidebar-brand">Handshake</div>
              <div class="sidebar-brand">Stagecoördinator</div>
            </div>
            <button class="close-btn" @click="toggleMenu">✕</button>
          </div>

          <div v-if="selectedEvent" class="sidebar-badge">{{ selectedEvent.title }}</div>

          <ul class="sidebar-nav">
            <li
              v-for="tab in tabs"
              :key="tab.id"
              :class="{ active: tab.route && route.name === tab.route, disabled: !tab.route }"
              @click="handleTabClick(tab)"
            >
              <span class="icon">{{ tab.icon }}</span> {{ tab.label }}
            </li>
          </ul>

          <div class="sidebar-footer">
            <button class="sidebar-action" @click="handleBackToEvents">← Ander event</button>
            <button class="sidebar-action logout" @click="handleLogout">↩ Uitloggen</button>
          </div>
        </nav>
      </Transition>
    </div>
  </div>
</template>

<style scoped>
.menu-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 6px;
  border-radius: 6px;
}

.menu-btn:hover { background: rgba(255, 255, 255, 0.15); }

.menu-btn span {
  display: block;
  width: 20px;
  height: 2px;
  background: white;
  border-radius: 2px;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 20;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 300px;
  max-width: 80%;
  background: white;
  z-index: 30;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.12);
}

.slide-enter-active, .slide-leave-active { transition: transform 0.25s ease; }
.slide-enter-from, .slide-leave-to { transform: translateX(-100%); }

.sidebar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 28px 24px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-brand {
  font-size: 22px;
  font-weight: 800;
  color: #1a56db;
  line-height: 1.25;
}

.close-btn {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: #9ca3af;
  padding: 2px 6px;
  border-radius: 6px;
}

.close-btn:hover { color: #1a1a1a; background: #f3f4f6; }

.sidebar-badge {
  margin: 16px 16px 0;
  padding: 10px 14px;
  background: #eef2ff;
  color: #1a56db;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  text-align: center;
}

.sidebar-nav {
  list-style: none;
  padding: 16px;
  flex: 1;
}

.sidebar-nav li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: background 0.15s;
}

.sidebar-nav li:hover:not(.disabled) { background: #f0f4f8; }
.sidebar-nav li.active { background: #1a56db; color: white; }
.sidebar-nav li.disabled { color: #c0c5cc; cursor: default; }

.icon { font-size: 18px; width: 28px; text-align: center; flex-shrink: 0; }

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-action {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1.5px solid #e5e7eb;
  background: white;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: #374151;
  transition: all 0.15s ease;
}

.sidebar-action:hover { background: #eef2ff; color: #1a56db; border-color: #c7d2fe; }
.sidebar-action.logout:hover { background: #fef2f2; color: #dc2626; border-color: #fecaca; }
</style>
