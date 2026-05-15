<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import StudentBottomNav from "../components/StudentBottomNav.vue";

const currentEmail = ref(localStorage.getItem("studentEmail") || "");
const router = useRouter();
const errorMessage = ref("");
const isLoading = ref(false);
const isEditingProfile = ref(false);
const isSavingProfile = ref(false);
const profileMessage = ref("");
const profileErrorMessage = ref("");
const cvMessage = ref("");
const cvErrorMessage = ref("");
const isUploadingCv = ref(false);
const isDeletingCv = ref(false);
const selectedCvFile = ref(null);
const cvFileInput = ref(null);
const MAX_CV_FILE_SIZE_BYTES = 10 * 1024 * 1024;
const profile = ref({
  firstname: "",
  lastname: "",
  email: "",
  education: "",
  hasCv: false,
  cvFileName: "",
  cvFileSize: null,
});
const editForm = ref({
  firstname: "",
  lastname: "",
  email: "",
  education: "",
});

const fullName = computed(() => {
  const firstname = profile.value.firstname?.trim() || "";
  const lastname = profile.value.lastname?.trim() || "";
  const joined = `${firstname} ${lastname}`.trim();
  return joined || "Onbekende student";
});

const displayEmail = computed(() => profile.value.email || currentEmail.value || "-");
const displayEducation = computed(() => profile.value.education || "-");

const formattedCvSize = computed(() => {
  const size = profile.value.cvFileSize;

  if (!size || Number.isNaN(size)) {
    return "";
  }

  if (size < 1024) {
    return `${size} B`;
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }

  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
});


const mapStudentData = (data, fallback) => ({
  firstname: data.firstname || fallback.firstname || "",
  lastname: data.lastname || fallback.lastname || "",
  email: data.email || fallback.email || "",
  education: data.education || fallback.education || "",
  hasCv: Boolean(data.hasCv),
  cvFileName: data.cvFileName || "",
  cvFileSize: data.cvFileSize,
});


const getValidationError = (form) => {
  const { firstname, lastname, email, education } = form;
  if (!firstname.trim() || !lastname.trim() || !email.trim() || !education.trim()) {
    return "Vul voornaam, achternaam, e-mail en opleiding in.";
  }
  if (!isValidEmail(email)) {
    return "Vul een geldig e-mailadres in.";
  }
  return null;
};

const extractApiErrorMessage = async (response) => {
  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    const data = await response.json().catch(() => ({}));
    return data.message || data.error || data.detail || "";
  }

  const text = await response.text().catch(() => "");
  return text?.trim() || "";
};

const mapUploadErrorMessage = (status, backendMessage) => {
  if (backendMessage) {
    return backendMessage;
  }

  if (status === 413) {
    return "CV-bestand is te groot. Maximum 10 MB.";
  }

  if (status === 415) {
    return "Bestandstype niet ondersteund. Upload een PDF-bestand.";
  }

  if (status >= 500) {
    return "Serverfout tijdens uploaden. Probeer het later opnieuw.";
  }

  return "CV uploaden is mislukt.";
};

const isValidEmail = (email) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

const loadProfile = async () => {
  if (!currentEmail.value) {
    errorMessage.value = "Geen student-email gevonden in sessie.";
    return;
  }

  isLoading.value = true;
  errorMessage.value = "";

  try {
    const response = await fetch(`/api/student/profile?email=${encodeURIComponent(currentEmail.value)}`);
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      errorMessage.value = data.message || "Profiel kon niet geladen worden.";
      return;
    }

  
    profile.value = mapStudentData(data, { email: currentEmail.value });

    if (!isEditingProfile.value) {
      editForm.value = { ...profile.value };
    }

    localStorage.setItem("hasCv", String(profile.value.hasCv));
  } catch {
    errorMessage.value = "Kan geen verbinding maken met de server.";
  } finally {
    isLoading.value = false;
  }
};

const startEditProfile = () => {
  profileMessage.value = "";
  profileErrorMessage.value = "";
  editForm.value = {
    firstname: profile.value.firstname,
    lastname: profile.value.lastname,
    email: profile.value.email,
    education: profile.value.education,
  };
  isEditingProfile.value = true;
};

const cancelEditProfile = () => {
  profileMessage.value = "";
  profileErrorMessage.value = "";
  editForm.value = {
    firstname: profile.value.firstname,
    lastname: profile.value.lastname,
    email: profile.value.email,
    education: profile.value.education,
  };
  isEditingProfile.value = false;
};

const syncProfileState = (updatedData) => {
  profile.value = mapStudentData(updatedData, editForm.value);
  editForm.value = { ...profile.value };
  currentEmail.value = profile.value.email;
  
  localStorage.setItem("studentEmail", currentEmail.value);
  localStorage.setItem("hasCv", String(profile.value.hasCv));
  window.dispatchEvent(new Event("storage"));
};

const saveProfile = async () => {
  const validationError = getValidationError(editForm.value);
  if (validationError) {
    profileErrorMessage.value = validationError;
    return;
  }

  isSavingProfile.value = true;
  profileErrorMessage.value = "";

  try {
    const response = await fetch(`/api/student/profile?email=${encodeURIComponent(currentEmail.value)}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(editForm.value),
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      profileErrorMessage.value = data.message || "Profiel opslaan is mislukt.";
      return;
    }

    syncProfileState(data);
    profileMessage.value = "Profiel succesvol bijgewerkt.";
    isEditingProfile.value = false;
  } catch {
    profileErrorMessage.value = "Kan geen verbinding maken met de server.";
  } finally {
    isSavingProfile.value = false;
  }
};

const onCvSelected = (event) => {
  cvMessage.value = "";
  cvErrorMessage.value = "";

  const [file] = event.target.files || [];
  if (!file) {
    selectedCvFile.value = null;
    return;
  }

  const fileName = file.name?.toLowerCase() || "";
  const isPdf = file.type === "application/pdf" || fileName.endsWith(".pdf");

  if (!isPdf) {
    cvErrorMessage.value = "Selecteer een PDF-bestand.";
    selectedCvFile.value = null;
    event.target.value = "";
    return;
  }

  if (file.size > MAX_CV_FILE_SIZE_BYTES) {
    cvErrorMessage.value = "CV-bestand is te groot. Maximum 10 MB.";
    selectedCvFile.value = null;
    event.target.value = "";
    return;
  }

  selectedCvFile.value = file;
  uploadCv();
};

const openCvFilePicker = () => {
  cvMessage.value = "";
  cvErrorMessage.value = "";
  cvFileInput.value?.click();
};

const handleCvUploadAction = () => {
  if (isUploadingCv.value) {
    return;
  }

  if (!selectedCvFile.value) {
    openCvFilePicker();
    return;
  }

  uploadCv();
};

const uploadCv = async () => {
  if (!currentEmail.value) {
    cvErrorMessage.value = "Geen student-email gevonden in sessie.";
    return;
  }

  if (!selectedCvFile.value) {
    cvErrorMessage.value = "Selecteer eerst een PDF-bestand.";
    return;
  }

  cvMessage.value = "";
  cvErrorMessage.value = "";
  isUploadingCv.value = true;

  try {
    const formData = new FormData();
    formData.append("file", selectedCvFile.value);

    const response = await fetch(`/api/student/profile/cv?email=${encodeURIComponent(currentEmail.value)}`, {
      method: "POST",
      body: formData,
    });

    if (!response.ok) {
      const backendMessage = await extractApiErrorMessage(response);
      cvErrorMessage.value = mapUploadErrorMessage(response.status, backendMessage);
      return;
    }

    cvMessage.value = "CV succesvol geupload.";
    localStorage.setItem("hasCv", "true");
    window.dispatchEvent(new Event("storage"));
    selectedCvFile.value = null;
    if (cvFileInput.value) {
      cvFileInput.value.value = "";
    }
    await loadProfile();
  } catch {
    cvErrorMessage.value = "Kan geen verbinding maken met de server.";
  } finally {
    isUploadingCv.value = false;
  }
};

const deleteCv = async () => {
  if (!currentEmail.value) {
    cvErrorMessage.value = "Geen student-email gevonden in sessie.";
    return;
  }

  cvMessage.value = "";
  cvErrorMessage.value = "";
  isDeletingCv.value = true;

  try {
    const response = await fetch(`/api/student/profile/cv?email=${encodeURIComponent(currentEmail.value)}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      const backendMessage = await extractApiErrorMessage(response);
      cvErrorMessage.value = backendMessage || "CV verwijderen is mislukt.";
      return;
    }

    cvMessage.value = "CV verwijderd.";
    localStorage.setItem("hasCv", "false");
    window.dispatchEvent(new Event("storage"));
    await loadProfile();
  } catch {
    cvErrorMessage.value = "Kan geen verbinding maken met de server.";
  } finally {
    isDeletingCv.value = false;
  }
};

const handleLogout = async () => {
  localStorage.removeItem("role");
  localStorage.removeItem("studentEmail");
  await router.push("/");
};

onMounted(loadProfile);
</script>

<template>
  <div class="page-wrapper">
    <div class="phone-frame student-profile-page">
      <header class="profile-header">
        <h1>Mijn Profiel</h1>
        <p>Beheer je persoonlijke informatie</p>
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
          <h3>Persoonlijke Gegevens</h3>

          <div class="field-group">
            <label>Naam</label>
            <template v-if="isEditingProfile">
              <input
                v-model="editForm.firstname"
                type="text"
                class="field-input"
                placeholder="Voornaam"
              />
              <input
                v-model="editForm.lastname"
                type="text"
                class="field-input"
                placeholder="Achternaam"
              />
            </template>
            <div v-else class="field-value">{{ fullName }}</div>
          </div>

          <div class="field-group">
            <label>Email</label>
            <input
              v-if="isEditingProfile"
              v-model="editForm.email"
              type="email"
              class="field-input"
              placeholder="E-mail"
            />
            <div v-else class="field-value">{{ displayEmail }}</div>
          </div>

          <div class="field-group">
            <label>Opleiding</label>
            <input
              v-if="isEditingProfile"
              v-model="editForm.education"
              type="text"
              class="field-input"
              placeholder="Opleiding"
            />
            <div v-else class="field-value">{{ displayEducation }}</div>
          </div>

          <p v-if="profileMessage" class="profile-feedback success">{{ profileMessage }}</p>
          <p v-if="profileErrorMessage" class="profile-feedback error">{{ profileErrorMessage }}</p>
        </section>

        <section class="card cv-card">
          <h3>CV Upload</h3>
          <div v-if="profile.hasCv" class="cv-file">
            <span class="file-icon" aria-hidden="true">PDF</span>
            <div class="file-meta">
              <div class="file-name">{{ profile.cvFileName }}</div>
              <div class="file-size">PDF <span v-if="formattedCvSize">• {{ formattedCvSize }}</span></div>
            </div>
          </div>

          <p v-else class="cv-empty">Er is nog geen CV geupload.</p>

          <div class="cv-actions">
            <input
              ref="cvFileInput"
              type="file"
              accept="application/pdf,.pdf"
              class="cv-input"
              @change="onCvSelected"
            />
            <button
              type="button"
              class="btn-primary"
              :disabled="isUploadingCv"
              @click="handleCvUploadAction"
            >
              {{ isUploadingCv ? "Uploaden..." : "CV uploaden" }}
            </button>
            <button
              type="button"
              class="btn-secondary"
              :disabled="!profile.hasCv || isDeletingCv"
              @click="deleteCv"
            >
              {{ isDeletingCv ? "Verwijderen..." : "CV verwijderen" }}
            </button>
          </div>

          <p v-if="cvMessage" class="cv-feedback success">{{ cvMessage }}</p>
          <p v-if="cvErrorMessage" class="cv-feedback error">{{ cvErrorMessage }}</p>
        </section>

        <div class="actions">
          <button
            v-if="!isEditingProfile"
            type="button"
            class="btn-primary"
            @click="startEditProfile"
          >
            Bewerken
          </button>
          <button
            v-else
            type="button"
            class="btn-primary"
            :disabled="isSavingProfile"
            @click="saveProfile"
          >
            {{ isSavingProfile ? "Opslaan..." : "Opslaan" }}
          </button>
          <button
            v-if="isEditingProfile"
            type="button"
            class="btn-secondary"
            :disabled="isSavingProfile"
            @click="cancelEditProfile"
          >
            Annuleren
          </button>
          <button type="button" class="btn-secondary logout-btn" @click="handleLogout">↪ Uitloggen</button>
        </div>
      </main>

      <StudentBottomNav />
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

.field-input {
  width: 100%;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  color: #111827;
  padding: 9px 12px;
  font-size: 15px;
  min-height: 40px;
}

.field-input + .field-input {
  margin-top: 8px;
}

.profile-feedback {
  margin-top: 12px;
  font-size: 14px;
}

.profile-feedback.success {
  color: #166534;
}

.profile-feedback.error {
  color: #b91c1c;
}

.cv-file {
  border-radius: 10px;
  background: #eff2f6;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
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

.file-name {
  font-weight: 700;
  font-size: 16px;
}

.file-size {
  color: #6b7280;
  font-size: 14px;
}

.cv-empty {
  margin-bottom: 12px;
  color: #6b7280;
  font-size: 14px;
}

.cv-actions {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.cv-input {
  display: none;
}

.cv-feedback {
  margin-top: 10px;
  font-size: 14px;
}

.cv-feedback.success {
  color: #166534;
}

.cv-feedback.error {
  color: #b91c1c;
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
</style>
