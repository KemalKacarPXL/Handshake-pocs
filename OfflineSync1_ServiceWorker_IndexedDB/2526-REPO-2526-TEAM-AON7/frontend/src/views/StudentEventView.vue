
<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const events = ref([]);
const isLoading = ref(false);
const errorMessage = ref("");

const fetchEvents = async () => {
	isLoading.value = true;
	errorMessage.value = "";
	try {
		const response = await fetch("/api/events");
		if (response.ok) {
			events.value = await response.json();
		} else {
			errorMessage.value = "Kon evenementen niet laden.";
		}
	} catch {
		errorMessage.value = "Kan geen verbinding maken met de server.";
	} finally {
		isLoading.value = false;
	}
};


const joinStates = ref({});

const joinEvent = async (event) => {
	joinStates.value[event.id] = { loading: true, message: "" };
	try {
		const response = await fetch("/api/student/join-event", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				"student-email": localStorage.getItem("studentEmail") || ""
			},
			body: JSON.stringify({ id: event.id })
		});
		const data = await response.json().catch(() => ({}));
		if (response.ok && data.success) {
			joinStates.value[event.id] = { loading: false, message: data.message || "Je neemt nu deel aan dit evenement!" };
			setTimeout(() => {
				router.push({ name: "student-profiel" });
			}, 1200);
		} else {
			joinStates.value[event.id] = { loading: false, message: data.message || "Deelname niet gelukt!" };
		}
	} catch {
		joinStates.value[event.id] = { loading: false, message: "Kan geen verbinding maken met de server." };
	}
};

const formatDate = (dateStr) => {
	return new Date(dateStr + "T00:00:00").toLocaleDateString("nl-BE", {
		day: "numeric", month: "long", year: "numeric",
	});
};

const handleLogout = async () => {
  localStorage.removeItem("role");
  localStorage.removeItem("studentEmail");
  await router.push("/");
};

const formatTime = (t) => t ? t.substring(0, 5) : "";

onMounted(async () => {
	// Check if student already joined an event
	const studentEmail = localStorage.getItem("studentEmail") || "";
	if (studentEmail) {
		try {
			const response = await fetch(`/api/student/profile?email=${encodeURIComponent(studentEmail)}`);
			if (response.ok) {
				const profile = await response.json();
				if (profile.joinedEventId || profile.hasJoinedEvent) {
					// If backend returns joinedEventId or hasJoinedEvent, redirect
					router.push({ name: "student-qr" });
					return;
				}
			}
		} catch (e) {
			throw new Error(e);
		}
	}
	fetchEvents();
});
</script>

<template>
	<div class="page-wrapper">
		<div class="phone-frame student-profile-page">
			<header class="top-bar">
				<span class="top-bar-title">Handshake Student</span>
				<button class="logout-btn" @click="handleLogout">Uitloggen</button>
			</header>
			<main class="profile-content">
				<section v-if="isLoading" class="card info-card">Evenementen laden...</section>
				<section v-else-if="errorMessage" class="card info-card error">{{ errorMessage }}</section>
				<section v-else>
					<div v-if="events.length > 0">
						<div class="event-card" v-for="event in events" :key="event.id">
							<div class="event-info">
								<div class="event-title">{{ event.title }}</div>
								<div class="event-details">
									<span>📍 {{ event.location }}</span>
									<span>📅 {{ formatDate(event.date) }}</span>
									<span>🕐 {{ formatTime(event.startTime) }} – {{ formatTime(event.endTime) }}</span>
								</div>
							</div>
							<div class="event-actions">
								<button class="join-btn" @click="joinEvent(event)" :disabled="joinStates[event.id]?.loading">
									Neem deel
								</button>
								<div v-if="joinStates[event.id]?.message" class="join-message">{{ joinStates[event.id].message }}</div>
							</div>
						</div>
					</div>
					<div v-else class="card info-card">Er zijn nog geen evenementen.</div>
				</section>
			</main>
		</div>
	</div>
</template>

<style scoped>
.student-profile-page { padding-bottom: 88px; }
.top-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: #0e66c2;
	color: white;
	padding: 16px 20px;
	position: sticky;
	top: 0;
	z-index: 10;
}
.top-bar-title {
	font-size: 17px;
	font-weight: 700;
	letter-spacing: -0.2px;
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
.profile-content { padding: 16px; }
.card { border-radius: 14px; border: 1px solid #d9dde4; padding: 20px; margin-bottom: 16px; background: white; }
.info-card { font-size: 14px; color: #6b7280; }
.event-card {
	background: white;
	border-radius: 12px;
	padding: 18px 20px;
	box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
	margin-bottom: 12px;
	display: flex;
	align-items: center;
	justify-content: space-between;
	border: 2px solid transparent;
	transition: border 0.15s, box-shadow 0.15s;
}
.event-info {
	flex: 1 1 auto;
}
.join-btn {
	background: #1a56db;
	color: #fff;
	border: none;
	border-radius: 8px;
	padding: 10px 18px;
	font-size: 15px;
	font-weight: 600;
	cursor: pointer;
	margin-left: 18px;
	transition: background 0.15s;
}
.join-btn:disabled {
	background: #a5b4fc;
	cursor: not-allowed;
}
.join-message {
	margin-left: 18px;
	color: #0e66c2;
	font-size: 14px;
}
.event-title { font-size: 16px; font-weight: 600; margin-bottom: 10px; }
.event-details { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #6b7280; }
.event-arrow { position: absolute; right: 20px; top: 50%; transform: translateY(-50%); font-size: 18px; color: #9ca3af; }
.event-card:hover .event-arrow { color: #1a56db; }
</style>
