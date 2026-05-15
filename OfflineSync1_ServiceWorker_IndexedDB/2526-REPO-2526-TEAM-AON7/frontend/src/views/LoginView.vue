<script setup>
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

const email = ref("");
const password = ref("");
const errorMessage = ref("");
const isLoading = ref(false);
const route = useRoute();
const router = useRouter();
const showRegisterLink = computed(() => route.query.fromInvite !== "1");

const checkProfileAndRedirect = async (email, role) => {
  const endpoint = role === "STUDENT" ? "/api/student/profile" : "/api/bedrijf/profile";
  const defaultRedirect = role === "STUDENT" ? "/student/events" : "/bedrijf/events";
  const redirectIfJoined = role === "STUDENT" ? "/student/profiel" : "/bedrijf/profiel";

  try {
    const res = await fetch(`${endpoint}?email=${encodeURIComponent(email)}`);
    if (res.ok) {
      const profile = await res.json();
      if (profile.hasJoinedEvent && profile.joinedEventId) {
        await router.push(redirectIfJoined);
        return true; // indicates redirect happened
      }
    }
  } catch (e) {
    throw new Error(e);
  }

  await router.push(defaultRedirect);
  return false;
};

const handleLogin = async () => {
  errorMessage.value = "";
  isLoading.value = true;

  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email.value, password: password.value }),
    });

    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      errorMessage.value = data.error || "Ongeldige logingegevens. Probeer het opnieuw.";
      return;
    }

    const data = await response.json();
    if (data.role === "STUDENT") {
      localStorage.setItem("studentEmail", email.value);
    } else if (data.role === "BEDRIJF") {
      localStorage.setItem("bedrijfEmail", email.value);
    } else if (data.role === "COORDINATOR") {
      localStorage.setItem("coordinatorEmail", data.email || email.value);
    }

    if (data.role === "STUDENT" || data.role === "BEDRIJF") {
      await checkProfileAndRedirect(email.value, data.role);
    } else {
      await router.push("/events");
    }

  } catch {
    errorMessage.value = "Kan geen verbinding maken met de server.";
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">Handshake App</h1>
      <p class="login-subtitle">Verbind studenten en bedrijven via QR-codes</p>

      <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">Email</label>
          <input id="email" v-model="email" type="email" placeholder="naam@voorbeeld.be" required :disabled="isLoading" />
        </div>

        <div class="form-group">
          <label for="password">Wachtwoord</label>
          <input id="password" v-model="password" type="password" placeholder="••••••••" required :disabled="isLoading" />
        </div>

        <button type="submit" class="btn-primary login-submit" :disabled="isLoading">
          {{ isLoading ? "Bezig..." : "Inloggen" }}
        </button>

        <p v-if="showRegisterLink" class="login-help">
          Geen account?
          <RouterLink to="/register">Registreer je hier</RouterLink>
        </p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  background: white;
  padding: 40px 32px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  width: 100%;
  max-width: 400px;
}

.login-title {
  color: #0b57d0;
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  margin: 0 0 12px;
}

.login-subtitle {
  color: #5f6368;
  font-size: 15px;
  text-align: center;
  margin: 0 0 32px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.login-form .form-group label {
  font-size: 14px;
}

.login-form .form-group input {
  padding: 12px 16px;
  font-size: 16px;
}

.login-submit {
  padding: 14px;
  font-size: 16px;
  margin-top: 8px;
}

.login-help {
  text-align: center;
  color: #5f6368;
  font-size: 14px;
  margin: 0;
}
</style>
