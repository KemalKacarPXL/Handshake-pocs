<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const token = route.params.token;

const status = ref("loading");
const companyName = ref("");
const email = ref("");
const errorMessage = ref("");

const submitForm = async () => {
  formError.value = "";
  formSuccess.value = "";
  if (!firstName.value || !lastName.value || !password.value || !confirmPassword.value) {
    formError.value = "Gelieve alle velden in te vullen.";
    return;
  }
  if (password.value !== confirmPassword.value) {
    formError.value = "Wachtwoorden komen niet overeen.";
    return;
  }
  try {
    const response = await fetch(`/api/bedrijf/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        firstname: firstName.value,
        lastname: lastName.value,
        email: email.value,
        password: password.value,
        companyName: companyName.value,
      }),
    });
    let data = null;
    if (response.ok) {
      formSuccess.value = "Account succesvol aangemaakt!";
      await router.push({ path: "/", query: { fromInvite: "1" } });
      return;
    }
    formError.value = (data && data.error) || "Er is een fout opgetreden.";

  } catch {
    formError.value = "Kan geen verbinding maken met de server.";
  }
};

const firstName = ref("");
const lastName = ref("");
const password = ref("");
const confirmPassword = ref("");
const formError = ref("");
const formSuccess = ref("");

onMounted(async () => {
  try {
    const response = await fetch(`/api/company-invites/validate/${token}`);
    if (response.ok) {
      const data = await response.json();
      companyName.value = data.companyName;
      email.value = data.email;
      status.value = "valid";
    } else {
      status.value = "invalid";
      const data = await response.json().catch(() => ({}));
      errorMessage.value = data.error || "Deze uitnodigingslink is ongeldig of verlopen.";
    }
  } catch {
    status.value = "invalid";
    errorMessage.value = "Kan geen verbinding maken met de server.";
  }
});
</script>

<template>
  <div class="page-wrapper">
    <div class="phone-frame">
      <header class="top-bar">
        <span class="top-bar-title">Maak een account aan.</span>
      </header>

      <main class="content">
        <div v-if="status === 'loading'" class="status-card">
          <p class="loading-text">Uitnodiging controleren...</p>
        </div>

        <div v-else-if="status === 'invalid'" class="status-card invalid">
          <div class="status-icon">✕</div>
          <h1>Link ongeldig</h1>
          <p class="status-message">{{ errorMessage }}</p>
        </div>

        <div v-else class="status-card valid">
          <div class="status-icon success">✓</div>
          <h1>Account activeren</h1>
          <p class="status-detail"><strong>{{ companyName }}</strong></p>
          <p class="status-detail">{{ email }}</p>
          <form class="register-form" @submit.prevent="submitForm">
            <div class="form-group">
              <label for="firstName">Voornaam</label>
              <input id="firstName" v-model="firstName" type="text" required />
            </div>
            <div class="form-group">
              <label for="lastName">Achternaam</label>
              <input id="lastName" v-model="lastName" type="text" required />
            </div>
            <div class="form-group">
              <label for="password">Wachtwoord</label>
              <input id="password" v-model="password" type="password" required />
            </div>
            <div class="form-group">
              <label for="confirmPassword">Wachtwoord bevestigen</label>
              <input id="confirmPassword" v-model="confirmPassword" type="password" required />
            </div>
            <button type="submit" class="submit-btn">Account aanmaken</button>
            <p v-if="formError" class="form-error">{{ formError }}</p>
            <p v-if="formSuccess" class="form-success">{{ formSuccess }}</p>
          </form>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.status-card {
  background: white;
  border-radius: 14px;
  padding: 32px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.status-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #fef2f2;
  color: #dc2626;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  margin: 0 auto 16px;
}

.status-icon.success {
  background: #ecfdf5;
  color: #065f46;
}

.status-card h1 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px;
}

.status-detail {
  font-size: 15px;
  color: #374151;
  margin: 2px 0;
}

.status-message {
  font-size: 14px;
  color: #6b7280;
  margin: 12px 0 0;
}

.loading-text {
  font-size: 15px;
  color: #6b7280;
}

.register-form {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.form-group label {
  font-size: 14px;
  color: #374151;
  margin-bottom: 4px;
}

.form-group input {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 15px;
  width: 100%;
}

.submit-btn {
  background: #1a56db;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 10px 0;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
}

.submit-btn:hover {
  background: #1441a2;
}

.form-error {
  color: #dc2626;
  font-size: 14px;
  margin-top: 8px;
}

.form-success {
  color: #10b981;
  font-size: 14px;
  margin-top: 8px;
}
</style>
