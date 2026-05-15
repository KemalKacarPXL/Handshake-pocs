<script setup>
import { ref, watch } from "vue";
import { createCompanyInvite, sendCompanyInviteEmail } from "../services/companyService.js";

const props = defineProps({
  visible: Boolean,
});

const emit = defineEmits(["close"]);

const companyName = ref("");
const email = ref("");
const errors = ref({});
const isLoading = ref(false);
const generatedLink = ref("");
const inviteToken = ref("");
const copied = ref(false);
const errorMessage = ref("");
const mailSending = ref(false);
const mailSent = ref(false);
const mailError = ref("");

watch(
  () => props.visible,
  (val) => {
    if (val) {
      companyName.value = "";
      email.value = "";
      errors.value = {};
      generatedLink.value = "";
      inviteToken.value = "";
      copied.value = false;
      errorMessage.value = "";
      mailSending.value = false;
      mailSent.value = false;
      mailError.value = "";
    }
  }
);

const validateEmail = (val) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);

const validate = () => {
  const e = {};
  if (!companyName.value.trim()) e.companyName = "Bedrijfsnaam is verplicht";
  if (!email.value.trim()) {
    e.email = "E-mailadres is verplicht";
  } else if (!validateEmail(email.value.trim())) {
    e.email = "Ongeldig e-mailadres";
  }
  errors.value = e;
  return Object.keys(e).length === 0;
};

const handleGenerate = async () => {
  if (!validate()) return;
  isLoading.value = true;
  errorMessage.value = "";
  try {
    const result = await createCompanyInvite({
      companyName: companyName.value.trim(),
      email: email.value.trim(),
    });
    inviteToken.value = result.token;
    generatedLink.value = window.location.origin + "/register/invite/" + result.token;
  } catch (err) {
    errorMessage.value = err.message || "Er ging iets mis.";
  } finally {
    isLoading.value = false;
  }
};

const handleCopy = async () => {
  try {
    await navigator.clipboard.writeText(generatedLink.value);
    copied.value = true;
    setTimeout(() => (copied.value = false), 2000);
  } catch {
    const textarea = document.createElement("textarea");
    textarea.value = generatedLink.value;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
    copied.value = true;
    setTimeout(() => (copied.value = false), 2000);
  }
};

const handleSendMail = async () => {
  const coordinatorEmail = localStorage.getItem("coordinatorEmail");
  if (!coordinatorEmail) {
    mailError.value = "Log in als coördinator om e-mail te versturen.";
    return;
  }
  mailError.value = "";
  mailSending.value = true;
  mailSent.value = false;
  try {
    await sendCompanyInviteEmail({
      coordinatorEmail,
      token: inviteToken.value,
      inviteLink: generatedLink.value,
    });
    mailSent.value = true;
  } catch (err) {
    mailError.value = err.message || "E-mail kon niet worden verzonden.";
  } finally {
    mailSending.value = false;
  }
};
</script>

<template>
  <Transition name="modal-fade">
    <div v-if="visible" class="modal-overlay" @click.self="emit('close')">
      <div class="modal-card">
        <div class="modal-header">
          <h2>Uitnodigingslink genereren</h2>
          <button class="modal-close" @click="emit('close')">✕</button>
        </div>

        <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

        <template v-if="!generatedLink">
          <form @submit.prevent="handleGenerate" class="modal-form">
            <div class="form-group">
              <label for="inv-company">Bedrijfsnaam</label>
              <input
                id="inv-company"
                v-model="companyName"
                type="text"
                placeholder="Bijv. TechCorp NV"
                :class="{ 'input-error': errors.companyName }"
              />
              <span v-if="errors.companyName" class="field-error">{{ errors.companyName }}</span>
            </div>

            <div class="form-group">
              <label for="inv-email">E-mailadres</label>
              <input
                id="inv-email"
                v-model="email"
                type="email"
                placeholder="contact@bedrijf.be"
                :class="{ 'input-error': errors.email }"
              />
              <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
            </div>

            <button type="submit" class="btn-primary generate-btn" :disabled="isLoading">
              {{ isLoading ? "Bezig met genereren..." : "Genereer link" }}
            </button>
          </form>
        </template>

        <template v-else>
          <div class="result-section">
            <div class="result-label">Uitnodigingslink</div>
            <div class="result-link-box">
              <span class="result-link-text">{{ generatedLink }}</span>
            </div>
            <button class="btn-primary copy-btn" @click="handleCopy">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              {{ copied ? "Gekopieerd!" : "Kopieer link" }}
            </button>
            <button
              type="button"
              class="btn-secondary mail-btn"
              :disabled="mailSending || mailSent"
              @click="handleSendMail"
            >
              {{
                mailSent ? "Mail verzonden" : mailSending ? "Bezig met verzenden…" : "Mail versturen"
              }}
            </button>
            <p v-if="mailError" class="alert alert-error mail-feedback">{{ mailError }}</p>
            <p v-else-if="mailSent" class="mail-feedback mail-success">E-mail verzonden naar het bedrijfsadres.</p>
            <p class="result-info">
              Deze link is eenmalig bruikbaar en verloopt na 7 dagen.
            </p>
          </div>
        </template>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.modal-card {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 420px;
  padding: 28px 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.modal-header h2 {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #9ca3af;
  padding: 4px 8px;
  border-radius: 6px;
}

.modal-close:hover {
  color: #1a1a1a;
  background: #f3f4f6;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.generate-btn {
  margin-top: 4px;
  width: 100%;
}

.result-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.result-link-box {
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px 14px;
  word-break: break-all;
}

.result-link-text {
  font-size: 13px;
  color: #1a56db;
  font-weight: 500;
}

.copy-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.mail-btn {
  width: 100%;
  margin-top: 4px;
}

.btn-secondary {
  padding: 12px 16px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  border: 2px solid #1a56db;
  background: #fff;
  color: #1a56db;
  cursor: pointer;
}

.btn-secondary:hover:not(:disabled) {
  background: #eff6ff;
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.mail-feedback {
  font-size: 13px;
  margin: 0;
  text-align: center;
}

.mail-success {
  color: #047857;
  font-weight: 500;
}

.result-info {
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
  margin: 4px 0 0;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
