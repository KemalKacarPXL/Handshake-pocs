<script setup>
import {ref, onMounted, computed} from "vue";
import { useRouter } from "vue-router";
import BedrijfBottomNav from "../components/BedrijfBottomNav.vue";

const router = useRouter();
const bedrijfEmail = localStorage.getItem("bedrijfEmail");

const isLoading = ref(false);
const errorMessage = ref("");
const isEditing = ref(false);

const profile = ref({
  firstname: "",
  lastname: "",
  email: "",
  description: "",
  sector: "",
  website: "",
  phoneNumber: "",
  location: "",
});

const editedProfile = ref({ ...profile.value });

const fullName = computed(() => {
  const firstname = profile.value.firstname;
  const lastname = profile.value.lastname;
  const joined = `${firstname} ${lastname}`.trim();
  return joined || "Onbekende bedrijf"
})

const displayEmail = computed(() => profile.value.email || "-");
const displayDescription = computed(() => profile.value.description || "-");
const displaySector = computed(() => profile.value.sector || "-");
const displayWebsite = computed(() => profile.value.website || "-");
const displayPhoneNumber = computed(() => profile.value.phoneNumber || "-");
const displayLocation = computed(() => profile.value.location || "-");

const mapProfileData = (data) => ({
  firstname: data.firstname || "",
  lastname: data.lastname || "",
  email: data.email || bedrijfEmail,
  description: data.description || "",
  sector: data.sector || "",
  website: data.website || "",
  phoneNumber: data.phoneNumber || "",
  location: data.location || "",
});

const loadProfile = async () => {
  if (!bedrijfEmail) return;
  isLoading.value = true;
  try {
    const response = await fetch(`/api/bedrijf/profile?email=${encodeURIComponent(bedrijfEmail)}`);
    const data = await response.json();
    if (response.ok) {
      profile.value = mapProfileData(data);
    }
  } catch {
    errorMessage.value = "Profiel kon niet geladen worden.";
  } finally {
    isLoading.value = false;
  }
};

let oldEmail = "";
const handleEdit = () => {
  editedProfile.value = { ...profile.value };
  oldEmail = profile.value.email;
  isEditing.value = true;
};

const handleCancel = () => {
  isEditing.value = false;
};

const handleSave = async () => {
  isLoading.value = true;
  errorMessage.value = "";
  try {
    const payload = {
      ...editedProfile.value,
      oldEmail: oldEmail
    };
    const response = await fetch("/api/bedrijf/profile", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      throw new Error("Profiel kon niet opgeslagen worden.");
    }
    const data = await response.json();
    profile.value = mapProfileData(data);

    // Update localStorage als het e-mailadres is gewijzigd
    if (profile.value.email) {
      localStorage.setItem("bedrijfEmail", profile.value.email);
    }
    
    isEditing.value = false;
  } catch (e) {
    errorMessage.value = e.message || "Profiel kon niet opgeslagen worden.";
  } finally {
    isLoading.value = false;
  }
};

const handleLogout = async () => {
  localStorage.removeItem("role");
  localStorage.removeItem("bedrijfEmail");
  await router.push("/");
};

onMounted(loadProfile);
</script>

<template>
  <div class="page-wrapper">
    <div class="phone-frame bedrijf-profile-page">
      <header class="profile-header">
        <h1>Bedrijfsprofiel</h1>
        <p>Beheer je bedrijfsinformatie</p>
      </header>

      <main class="profile-content">
        <section class="card identity-card">
          <div class="avatar-circle" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="7.5" r="3.5" stroke="currentColor" stroke-width="1.7" />
              <path d="M5 19.5C5 15.9 7.6 13.8 12 13.8C16.4 13.8 19 15.9 19 19.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
            </svg>
          </div>
          <h2>{{ fullName }}</h2>
          <p>{{ displayEmail }}</p>
        </section>

        <section v-if="isLoading" class="card info-card">
          Profiel wordt geladen...
        </section>

        <section v-else-if="errorMessage" class="card info-card error">
          {{ errorMessage }}
        </section>

        <section class="card details-card">
          <h3>Bedrijfsgegevens</h3>

          <div class="field-group">
            <label>Naam</label>
            <div v-if="!isEditing" class="field-value">{{ fullName }}</div>
            <div v-else class="field-value editing">
              <input v-model="editedProfile.firstname" placeholder="Voornaam" class="edit-input half" />
              <input v-model="editedProfile.lastname" placeholder="Achternaam" class="edit-input half" />
            </div>
          </div>

          <div class="field-group">
            <label>Sector</label>
            <div v-if="!isEditing" class="field-value">{{ displaySector }}</div>
            <div v-else class="field-value editing">
              <input v-model="editedProfile.sector" placeholder="Sector" class="edit-input" />
            </div>
          </div>
          <div class="field-group">
            <label>Beschrijving</label>
            <div v-if="!isEditing" class="field-value">{{ displayDescription }}</div>
            <div v-else class="field-value editing">
              <textarea v-model="editedProfile.description" placeholder="Beschrijving" rows="2" class="edit-input"></textarea>
            </div>
          </div>
        </section>

        <section class="card details-card">
          <h3>Contactinformatie</h3>

          <div class="field-group">
            <label>E-mail</label>
            <div v-if="!isEditing" class="field-value">{{ displayEmail }}</div>
            <div v-else class="field-value editing">
              <input v-model="editedProfile.email" placeholder="Email" class="edit-input" />
            </div>
          </div>
          <div class="field-group">
            <label>Telefoon</label>
            <div v-if="!isEditing" class="field-value">{{ displayPhoneNumber }}</div>
            <div v-else class="field-value editing">
              <input v-model="editedProfile.phoneNumber" placeholder="Telefoon" class="edit-input" />
            </div>
          </div>
          <div class="field-group">
            <label>Website</label>
            <div v-if="!isEditing" class="field-value">{{ displayWebsite }}</div>
            <div v-else class="field-value editing">
              <input v-model="editedProfile.website" placeholder="Website" class="edit-input" />
            </div>
          </div>
          <div class="field-group">
            <label>Locatie</label>
            <div v-if="!isEditing" class="field-value">{{ displayLocation }}</div>
            <div v-else  class="field-value editing">
              <input v-model="editedProfile.location" placeholder="Locatie" class="edit-input" />
            </div>
          </div>
        </section>

        <div class="actions">
          <button v-if="!isEditing" type="button" class="btn-primary" @click="handleEdit">Bewerken</button>
          <template v-else>
            <button type="button" class="btn-primary" @click="handleSave" :disabled="isLoading">Opslaan</button>
            <button type="button" class="btn-secondary" @click="handleCancel" :disabled="isLoading">Annuleren</button>
          </template>
          <button type="button" class="btn-secondary logout-btn" @click="handleLogout">↪ Uitloggen</button>
        </div>
      </main>

    <BedrijfBottomNav />
    </div>
  </div>
</template>

<style scoped>
.bedrijf-profile-page {
  padding-bottom: 88px;
}

.profile-header {
  background: #0e66c2;
  color: #fff;
  padding: 26px 20px 24px;
}

.profile-header h1 {
  font-size: 42px;
  line-height: 1.05;
  font-weight: 800;
  letter-spacing: -0.8px;
}

.profile-header p {
  margin-top: 8px;
  font-size: 24px;
  opacity: 0.96;
}

.profile-content {
  padding: 16px;
}

.card {
  border-radius: 14px;
  border: 1px solid #d9dde4;
  box-shadow: none;
  margin-bottom: 16px;
}

.info-card {
  font-size: 14px;
  color: #334155;
}

.info-card.error {
  color: #b91c1c;
  border-color: #fecaca;
  background: #fef2f2;
}

.identity-card {
  text-align: center;
  padding-top: 24px;
  padding-bottom: 18px;
}

.avatar-circle {
  width: 96px;
  height: 96px;
  border-radius: 999px;
  background: #d9e4f1;
  color: #0e66c2;
  display: grid;
  place-items: center;
  margin: 0 auto 14px;
}

.avatar-circle svg {
  width: 42px;
  height: 42px;
}

.identity-card h2 {
  font-size: 15px;
  font-weight: 700;
}

.identity-card p {
  margin-top: 2px;
  color: #6b7280;
  font-size: 13px;
}

.details-card h3,
.cv-card h3 {
  font-size: 18px;
  margin-bottom: 18px;
}

.field-group {
  margin-bottom: 14px;
}

.field-group:last-child {
  margin-bottom: 0;
}

.field-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.field-value {
  background: #f4f6f8;
  border-radius: 8px;
  color: #6b7280;
  padding: 9px 12px;
  font-size: 15px;
  min-height: 40px;
  display: flex;
  align-items: center;
}

.actions {
  display: grid;
  gap: 12px;
}

.actions .btn-primary {
  border-radius: 9px;
  padding: 12px;
  font-size: 16px;
}

.logout-btn {
  border-radius: 9px;
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 480px) {
  .profile-header h1 {
    font-size: 39px;
  }

  .profile-header p {
    font-size: 28px;
  }
}
/* Verbeterde inputstijl voor bewerken */
.field-value.editing {
  background: hsl(207, 100%, 98%);
  box-shadow: 0 0 0 2px #0e66c233;
  color: #334155;
}
.edit-input {
  width: 100%;
  border: 1.5px solid #0e65c258;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 15px;
  background: #fff;
  color: #334155;
  outline: none;
  transition: box-shadow 0.2s, border-color 0.2s;
  box-shadow: 0 1px 2px #0e66c211;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}
textarea {
    field-sizing: content;
}
.edit-input:focus {
  border-color: #0e66c2;
  box-shadow: 0 0 0 2px #0e66c277;
}
.edit-input.half {
  width: 48%;
  display: inline-block;
  margin-right: 4%;
}
.edit-input.half:last-child {
  margin-right: 0;
}
</style>
