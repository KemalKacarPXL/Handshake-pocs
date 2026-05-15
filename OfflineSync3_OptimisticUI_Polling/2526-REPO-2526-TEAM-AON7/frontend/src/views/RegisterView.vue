<script setup>
import { ref, watch } from "vue";
import { useRouter } from "vue-router";

const firstname = ref("");
const lastname = ref("");
const email = ref("");
const education = ref("");
const educationCategory = ref("");
const password = ref("");
const passwordConfirm = ref("");
const errorMessage = ref("");
const isLoading = ref(false);
const router = useRouter();

const educationOptions = {
  'PXL-Business': [
    'Accountancy-fiscaliteit',
    'Financiën- en verzekeringswezen',
    'Marketing',
    'Rechtspraktijk',
    'Supply chain management',
    'Bedrijfsmanagement allround',
    'Organisatie en management – Business & Languages',
    'Organisatie en management – Business Translation & Interpreting',
    'Organisatie en management – Health Care Management',
    'Accounting administration',
    'Juridisch-administratieve ondersteuning',
    'Marketing (Sales)',
    'Marketing- en communicatiesupport',
    'Transport en logistiek',
    'Verkeerskunde en mobiliteit',
    'Winkelmanagement',
  ],
  'PXL-Digital': [
    'Toegepaste informatica',
    'Programmeren',
    'Systemen en netwerken',
  ],
  'PXL-Education': [
    'Onderwijs: kleuteronderwijs',
    'Onderwijs: lager onderwijs',
    'Onderwijs: secundair onderwijs',
    'Educatief graduaat in het secundair onderwijs',
    'Buitengewoon onderwijs',
    'Zorgverbreding en remediërend leren',
    'Schoolontwikkeling',
  ],
  'PXL-Green & Tech': [
    'Agro- en biotechnologie',
    'Bouwkunde',
    'Chemie',
    'Elektromechanica',
    'Elektronica-ICT',
    'Energietechnologie',
    'Elektromechanische systemen',
    'Internet of Things',
    'Mechanische ontwerp- en productietechnologie',
  ],
  'PXL-Healthcare': [
    'Ergotherapie',
    'Logopedie en audiologie',
    'Verpleegkunde',
    'Vroedkunde',
  ],
  'PXL-MAD': [
    'Architectuur',
    'Interieurarchitectuur',
  ],
  'PXL-Media & Tourism': [
    'Communicatiemanagement',
    'Journalistiek',
    'Toerisme- en recreatiemanagement',
  ],
  'PXL-Music': [
    'Muziek',
  ],
  'PXL-People & Society': [
    'Sociaal werk',
    'Orthopedagogie',
    'Toegepaste psychologie',
  ],
}
watch(educationCategory, () => {
  education.value = "";
});

const handleRegister = async () => {
  errorMessage.value = "";

  if (password.value !== passwordConfirm.value) {
    errorMessage.value = "Wachtwoorden komen niet overeen.";
    return;
  }
  if (password.value.length < 8){
    errorMessage.value = "Wachtwoord moet langer zijn dan 8 karakters"
    return
  }

  isLoading.value = true;

  try {
    const response = await fetch("/api/student", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        firstname: firstname.value,
        lastname: lastname.value,
        email: email.value,
        education: education.value,
        password: password.value,
      }),
    });

    if (response.ok) {
      await router.push("/");
    } else {
      const data = await response.json().catch(() => ({
      }));
      errorMessage.value = data.message || "Registratie mislukt. Probeer het opnieuw.";
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
      <p class="login-subtitle">Maak een nieuw account aan</p>

      <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

      <form @submit.prevent="handleRegister" class="login-form">
        <div class="form-group">
          <label for="name">Voornaam</label>
          <input id="firstname" v-model="firstname" type="text" placeholder="Jan" required :disabled="isLoading" />
        </div>

        <div class="form-group">
          <label for="name">Achternaam</label>
          <input id="lastname" v-model="lastname" type="text" placeholder="Janssen" required :disabled="isLoading" />
        </div>

        <div class="form-group">
          <label for="educationCategory">Departement</label>
          <select id="educationCategory" v-model="educationCategory" :disabled="isLoading">
            <option value="">Selecteer je departement</option>
            <option v-for="(list, key) in educationOptions" :key="key" :value="key">
              {{ key }}
            </option>
          </select>

          <template v-if="educationCategory">
            <label for="education">Opleiding</label>
            <select id="education" v-model="education" :disabled="isLoading">
              <option value="">Kies een opleiding</option>
              <option
                v-for="opl in educationOptions[educationCategory]"
                :key="opl"
                :value="opl"
              >
                {{ opl }}
              </option>
            </select>
          </template>
        </div>

        <div class="form-group">
          <label for="email">Email</label>
          <input id="email" v-model="email" type="email" placeholder="naam@voorbeeld.be" required :disabled="isLoading" />
        </div>

        <div class="form-group">
          <label for="password">Wachtwoord</label>
          <input id="password" v-model="password" type="password" placeholder="••••••••" required :disabled="isLoading" />
        </div>

        <div class="form-group">
          <label for="password-confirm">Wachtwoord bevestigen</label>
          <input id="password-confirm" v-model="passwordConfirm" type="password" placeholder="••••••••" required :disabled="isLoading" />
        </div>

        <button type="submit" class="btn-primary login-submit" :disabled="isLoading">
          {{ isLoading ? "Bezig..." : "Registreren" }}
        </button>

        <p class="login-help">
          Al een account?
          <RouterLink to="/">Inloggen</RouterLink>
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

.login-form .form-group select {
  padding: 12px;
  font-size: 16px;
  border-radius: 5px;
}

.login-form .form-group option {
  padding: 12px 16px;
  font-size: 16px;
}

.login-form .form-group optgroup {
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

.login-help a {
  color: #0b57d0;
  text-decoration: none;
  font-weight: 500;
}

.login-help a:hover {
  text-decoration: underline;
}
</style>
