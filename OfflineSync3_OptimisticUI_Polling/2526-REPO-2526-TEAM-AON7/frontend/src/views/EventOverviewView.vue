<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useEventStore } from "../stores/eventStore.js";

const router = useRouter();
const { selectEvent } = useEventStore();

const events = ref([]);
const showForm = ref(false);
const isLoading = ref(false);
const successMessage = ref("");
const errorMessage = ref("");
const form = ref({ title: "", date: "", startTime: "", endTime: "", location: "" });
const formErrors = ref({});

const handleLogout = () => {
  localStorage.removeItem("coordinatorEmail");
  router.push("/");
};

const openForm = () => {
  showForm.value = true;
  successMessage.value = "";
  errorMessage.value = "";
  formErrors.value = {};
};

const closeForm = () => {
  showForm.value = false;
  form.value = { title: "", date: "", startTime: "", endTime: "", location: "" };
  formErrors.value = {};
};

const validateForm = () => {
  const errors = {};
  if (!form.value.title.trim()) errors.title = "Titel is verplicht";
  if (!form.value.date) errors.date = "Datum is verplicht";
  if (!form.value.startTime) errors.startTime = "Starttijd is verplicht";
  if (!form.value.endTime) errors.endTime = "Eindtijd is verplicht";
  if (!form.value.location.trim()) errors.location = "Locatie is verplicht";
  formErrors.value = errors;
  return Object.keys(errors).length === 0;
};

const submitEvent = async () => {
  if (!validateForm()) return;
  isLoading.value = true;
  errorMessage.value = "";

  try {
    const response = await fetch("/api/events", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(form.value),
    });

    if (response.ok) {
      const created = await response.json();
      events.value.unshift(created);
      successMessage.value = `"${created.title}" is aangemaakt!`;
      closeForm();
    } else {
      const data = await response.json().catch(() => ({}));
      errorMessage.value = data.error || "Er ging iets mis bij het opslaan.";
    }
  } catch {
    errorMessage.value = "Kan geen verbinding maken met de server.";
  } finally {
    isLoading.value = false;
  }
};

const fetchEvents = async () => {
  try {
    const response = await fetch("/api/events");
    if (response.ok) events.value = await response.json();
  } catch {
    /* events blijven leeg */
  }
};

const handleSelectEvent = (event) => {
  selectEvent(event);
  router.push(`/events/${event.id}/dashboard`);
};

const formatDate = (dateStr) => {
  return new Date(dateStr + "T00:00:00").toLocaleDateString("nl-BE", {
    day: "numeric", month: "long", year: "numeric",
  });
};

const formatTime = (t) => t ? t.substring(0, 5) : "";

onMounted(fetchEvents);
</script>

<template>
  <div class="page-wrapper">
    <div class="phone-frame">
      <header class="top-bar">
        <span class="top-bar-title">Handshake Stagecoördinator</span>
        <button class="logout-btn" @click="handleLogout">Uitloggen</button>
      </header>

      <main class="content">
        <h1>Handshake Events</h1>
        <p class="subtitle">Selecteer een event om verder te gaan</p>

        <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>

        <button v-if="!showForm" class="create-btn" @click="openForm">+ Nieuw event aanmaken</button>

        <div v-if="showForm" class="card">
          <h2 class="card-title">Handshake Event aanmaken</h2>
          <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

          <form @submit.prevent="submitEvent" class="event-form">
            <div class="form-group">
              <label for="title">Titel</label>
              <input id="title" v-model="form.title" type="text" placeholder="Bijv. Handshake Voorjaar 2026" :class="{ 'input-error': formErrors.title }" />
              <span v-if="formErrors.title" class="field-error">{{ formErrors.title }}</span>
            </div>

            <div class="form-group">
              <label for="date">Datum</label>
              <input id="date" v-model="form.date" type="date" :class="{ 'input-error': formErrors.date }" />
              <span v-if="formErrors.date" class="field-error">{{ formErrors.date }}</span>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="startTime">Starttijd</label>
                <input id="startTime" v-model="form.startTime" type="time" :class="{ 'input-error': formErrors.startTime }" />
                <span v-if="formErrors.startTime" class="field-error">{{ formErrors.startTime }}</span>
              </div>
              <div class="form-group">
                <label for="endTime">Eindtijd</label>
                <input id="endTime" v-model="form.endTime" type="time" :class="{ 'input-error': formErrors.endTime }" />
                <span v-if="formErrors.endTime" class="field-error">{{ formErrors.endTime }}</span>
              </div>
            </div>

            <div class="form-group">
              <label for="location">Locatie</label>
              <input id="location" v-model="form.location" type="text" placeholder="Bijv. PXL Campus, Gebouw B" :class="{ 'input-error': formErrors.location }" />
              <span v-if="formErrors.location" class="field-error">{{ formErrors.location }}</span>
            </div>

            <div class="form-actions">
              <button type="button" class="btn-secondary" @click="closeForm">Annuleren</button>
              <button type="submit" class="btn-primary" style="flex:1" :disabled="isLoading">
                {{ isLoading ? "Opslaan..." : "Event aanmaken" }}
              </button>
            </div>
          </form>
        </div>

        <section v-if="events.length > 0">
          <h2 class="section-title">Kies een event</h2>
          <div class="event-card" v-for="event in events" :key="event.id" @click="handleSelectEvent(event)">
            <div class="event-title">{{ event.title }}</div>
            <div class="event-details">
              <span>📅 {{ formatDate(event.date) }}</span>
              <span>🕐 {{ formatTime(event.startTime) }} – {{ formatTime(event.endTime) }}</span>
              <span>📍 {{ event.location }}</span>
            </div>
            <div class="event-arrow">→</div>
          </div>
        </section>

        <div v-else-if="!showForm" class="empty-state">
          <p>Nog geen events aangemaakt.</p>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 24px;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 6px;
}

.logout-btn:hover { background: rgba(255, 255, 255, 0.25); }

.create-btn {
  width: 100%;
  padding: 14px;
  border: 2px dashed #c7d2fe;
  border-radius: 12px;
  background: white;
  color: #1a56db;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 24px;
  transition: all 0.15s;
}

.create-btn:hover { background: #eef2ff; border-color: #1a56db; }

.card-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 20px;
}

.event-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-row .form-group { flex: 1; }

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 14px;
}

.event-card {
  background: white;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
  cursor: pointer;
  position: relative;
  border: 2px solid transparent;
  transition: all 0.15s;
}

.event-card:hover { border-color: #1a56db; box-shadow: 0 2px 8px rgba(26, 86, 219, 0.12); }

.event-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
}

.event-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.event-arrow {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  color: #9ca3af;
}

.event-card:hover .event-arrow { color: #1a56db; }

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
  font-size: 14px;
}
</style>
