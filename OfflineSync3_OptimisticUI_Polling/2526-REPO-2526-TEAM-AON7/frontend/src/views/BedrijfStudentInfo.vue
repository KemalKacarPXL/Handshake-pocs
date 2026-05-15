<script setup>

import {onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import BedrijfBottomNav from "@/components/BedrijfBottomNav.vue";

const route = useRoute();
const router = useRouter();
const isLoading = ref(false);
const errorMessage = ref("");

const student = ref({
  firstname: "",
  lastname: "",
  email: "",
  education: "",
  hasCv: false,
  cvFileName: "",
});

const getCvUrl = (mode = "open") => {
  if (!student.value.email) {
    return "#";
  }

  return `/api/student/profile/cv/${mode}?email=${encodeURIComponent(student.value.email)}`;
};

const downloadCv = () => {
  if (!student.value.hasCv) return;
  window.location.href = getCvUrl('download');
};

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
    return;
  }

  router.push('/bedrijf/studenten');
};

const loadStudentInfo = async () => {
  const studentEmail = decodeURIComponent(route.params.email);
  try {
    const response = await fetch(`/api/student/profile?email=${encodeURIComponent(studentEmail)}`);
    const data = await response.json();

    if (!response.ok) {
      errorMessage.value = data.message;
      return;
    }

    student.value = {
      firstname: data.firstname || "-",
      lastname: data.lastname || "-",
      email: data.email || "-",
      education: data.education || "-",
      hasCv: Boolean(data.hasCv),
      cvFileName: data.cvFileName || "-",
    }
  } catch {
    errorMessage.value = "Kan geen verbinding maken met de server";
  }
  finally {
    isLoading.value = false;
  }
}
onMounted(loadStudentInfo);
</script>

<template>

  <div class="page-wrapper">
    <div class="phone-frame student-profile-page">
      <header class="profile-header">
        <div class="header-top">
          <button
            type="button"
            class="back-button"
            @click="goBack"
            aria-label="Terug naar studentenlijst"
            title="Terug"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M15 6L9 12L15 18" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <h1>Student Profiel</h1>
        </div>
      </header>

      <main class="profile-content">
        <section class="card identity-card">
          <div class="avatar-circle" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="7.5" r="3.5" stroke="currentColor" stroke-width="1.7" />
              <path d="M5 19.5C5 15.9 7.6 13.8 12 13.8C16.4 13.8 19 15.9 19 19.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
            </svg>
          </div>
          <h2>{{ student.firstname + " " + student.lastname }}</h2>
          <p>{{ student.email }}</p>
        </section>

        <section v-if="isLoading" class="card info-card">
          Profiel wordt geladen...
        </section>

        <section v-else-if="errorMessage" class="card info-card error">
          {{ errorMessage }}
        </section>

        <section class="card details-card">
          <h3>Persoonlijke Gegevens</h3>

          <div class="field-group">
            <label>Naam</label>
            <div class="field-value">{{ student.firstname + " " + student.lastname }}</div>
          </div>

          <div class="field-group">
            <label>Email</label>
            <div class="field-value">{{ student.email }}</div>
          </div>

          <div class="field-group">
            <label>Opleiding</label>
            <div class="field-value">{{student.education}}</div>
          </div>
        </section>

        <section class="card cv-card">
          <h3>CV Upload</h3>
          <div v-if="student.hasCv" class="cv-file">
            <a
              class="cv-link"
              :href="getCvUrl('open')"
              target="_blank"
              rel="noopener noreferrer"
              title="Open CV"
            >
              <span class="file-icon" aria-hidden="true">PDF</span>
              <div class="file-meta">
                <div class="file-name">{{ student.cvFileName }}</div>
                <div class="file-hint">Klik om te openen</div>
              </div>
            </a>
            <button
              type="button"
              class="download-button"
              @click="downloadCv"
              title="Download CV"
              aria-label="Download CV"
            >
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <path d="M12 4V14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8.5 10.5L12 14L15.5 10.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M5 18H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>

          <p v-else class="cv-empty">Er is nog geen CV geupload.</p>
        </section>
      </main>

      <BedrijfBottomNav />
    </div>
  </div>

</template>

<style scoped>
.student-profile-page {
  padding-bottom: 88px;
}

.profile-header {
  background: #0e66c2;
  color: #fff;
  padding: 20px 20px 24px;
}

.header-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-button {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  display: grid;
  place-items: center;
  cursor: pointer;
  flex-shrink: 0;
}

.back-button svg {
  width: 20px;
  height: 20px;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.24);
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

.cv-file {
  border-radius: 10px;
  background: #eff2f6;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.cv-link {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}

.file-icon {
  width: 42px;
  height: 42px;
  border-radius: 7px;
  background: #dce4ef;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
}

.file-meta {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 700;
  font-size: 16px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-hint {
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}

.cv-link:hover .file-name {
  text-decoration: underline;
}

.download-button {
  margin-left: auto;
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #0e66c2, #0a54a0);
  color: #fff;
  display: grid;
  place-items: center;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(14, 102, 194, 0.22);
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
  flex-shrink: 0;
}

.download-button svg {
  width: 18px;
  height: 18px;
}

.download-button:hover {
  filter: brightness(1.05);
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(14, 102, 194, 0.28);
}

.download-button:active {
  transform: translateY(0);
}

.download-button:focus-visible {
  outline: 3px solid rgba(14, 102, 194, 0.25);
  outline-offset: 2px;
}

.cv-empty {
  margin-bottom: 12px;
  color: #6b7280;
  font-size: 14px;
}

@media (max-width: 480px) {
  .profile-header h1 {
    font-size: 39px;
  }

  .profile-header p {
    font-size: 28px;
  }
}
</style>
