export async function createCompanyInvite({ companyName, email }) {
  const response = await fetch("/api/company-invites", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ companyName, email }),
  });
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.error || "Uitnodiging kon niet worden aangemaakt");
  }
  return response.json();
}

export async function sendCompanyInviteEmail({ coordinatorEmail, token, inviteLink }) {
  const response = await fetch("/api/company-invites/send-email", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ coordinatorEmail, token, inviteLink }),
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.error || "E-mail kon niet worden verzonden");
  }
  return data;
}
