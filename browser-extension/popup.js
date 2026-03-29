const API = "https://project-hub-mentorship-u5td.vercel.app/";

// State
let hours = 0;
let mins = 0;
let activities = [];
let accessToken = null;

// Elements
const loginView = document.getElementById("login-view");
const logView = document.getElementById("log-view");

// ── Helpers ──
async function apiCall(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };
  if (accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  }
  const res = await fetch(`${API}${path}`, { ...options, headers });
  return res;
}

// ── Init ──
document.addEventListener("DOMContentLoaded", async () => {
  // Load saved token
  chrome.storage.local.get(["accessToken"], async (result) => {
    if (result.accessToken) {
      accessToken = result.accessToken;
      // Verify token still works
      try {
        const res = await apiCall("/api/user/profile");
        if (res.ok) {
          showLogView();
        } else {
          accessToken = null;
          chrome.storage.local.remove("accessToken");
          showLoginView();
        }
      } catch {
        showLoginView();
      }
    } else {
      showLoginView();
    }
  });

  // Duration picker buttons
  document.getElementById("hr-up").addEventListener("click", () => {
    hours = Math.min(23, hours + 1);
    render();
  });
  document.getElementById("hr-down").addEventListener("click", () => {
    hours = Math.max(0, hours - 1);
    render();
  });
  document.getElementById("min-up").addEventListener("click", () => {
    mins = Math.min(55, mins + 5);
    render();
  });
  document.getElementById("min-down").addEventListener("click", () => {
    mins = Math.max(0, mins - 5);
    render();
  });

  // Login
  document.getElementById("login-btn").addEventListener("click", handleLogin);
  document.getElementById("password").addEventListener("keydown", (e) => {
    if (e.key === "Enter") handleLogin();
  });

  // Log
  document.getElementById("log-btn").addEventListener("click", handleLog);

  // Logout
  document.getElementById("logout-btn").addEventListener("click", handleLogout);
});

function render() {
  document.getElementById("hr-val").textContent = hours;
  document.getElementById("min-val").textContent = String(mins).padStart(
    2,
    "0",
  );
}

function showLoginView() {
  loginView.style.display = "block";
  logView.style.display = "none";
}

async function showLogView() {
  loginView.style.display = "none";
  logView.style.display = "block";
  await loadActivities();
}

// ── Load activities from all groups ──
async function loadActivities() {
  try {
    const groupsRes = await apiCall("/api/groups");
    if (!groupsRes.ok) {
      showLoginView();
      return;
    }
    const groups = await groupsRes.json();

    activities = [];
    for (const g of groups) {
      const actsRes = await apiCall(`/api/groups/${g.id}/activities`);
      if (actsRes.ok) {
        const acts = await actsRes.json();
        for (const a of acts) {
          activities.push({ ...a, groupName: g.name });
        }
      }
    }

    const select = document.getElementById("activity-select");
    select.innerHTML = '<option value="">What did you do?</option>';
    for (const a of activities) {
      const opt = document.createElement("option");
      opt.value = a.id;
      opt.textContent = `${a.name} · ${a.groupName}`;
      select.appendChild(opt);
    }
  } catch {
    showLoginView();
  }
}

// ── Login (uses JSON endpoint, not cookie endpoint) ──
async function handleLogin() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const errorEl = document.getElementById("login-error");
  errorEl.textContent = "";

  if (!username || !password) {
    errorEl.textContent = "Please fill in both fields.";
    return;
  }

  try {
    const res = await fetch(`${API}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (res.ok) {
      const data = await res.json();
      accessToken = data.access_token;
      chrome.storage.local.set({ accessToken });
      showLogView();
    } else {
      const text = await res.text().catch(() => "");
      try {
        const data = JSON.parse(text);
        errorEl.textContent = data?.message || `Error ${res.status}`;
      } catch {
        errorEl.textContent = `Error ${res.status}: ${text.substring(0, 80)}`;
      }
    }
  } catch (e) {
    errorEl.textContent = `Connection error: ${e.message}`;
  }
}

// ── Log activity ──
async function handleLog() {
  const activityId = document.getElementById("activity-select").value;
  const note = document.getElementById("note-input").value;
  const msgEl = document.getElementById("log-message");
  msgEl.textContent = "";
  msgEl.className = "message";

  const totalMins = hours * 60 + mins;

  if (!activityId) {
    msgEl.textContent = "Select an activity.";
    msgEl.className = "message error";
    return;
  }
  if (totalMins <= 0) {
    msgEl.textContent = "Set a duration.";
    msgEl.className = "message error";
    return;
  }

  try {
    const res = await apiCall("/api/logs", {
      method: "POST",
      body: JSON.stringify({ activityId, durationMins: totalMins, note }),
    });

    if (res.ok) {
      const actName =
        activities.find((a) => a.id === activityId)?.name || "Activity";
      msgEl.textContent = `✓ ${totalMins}m of ${actName} logged!`;
      msgEl.className = "message success";

      hours = 0;
      mins = 0;
      render();
      document.getElementById("activity-select").value = "";
      document.getElementById("note-input").value = "";

      setTimeout(() => {
        msgEl.textContent = "";
      }, 3000);
    } else {
      const data = await res.json().catch(() => null);
      msgEl.textContent = data?.message || "Failed to log.";
      msgEl.className = "message error";
    }
  } catch {
    msgEl.textContent = "Cannot connect to server.";
    msgEl.className = "message error";
  }
}

// ── Logout ──
async function handleLogout() {
  try {
    await apiCall("/api/auth/logout", { method: "POST" });
  } catch {
    /* ignore */
  }
  accessToken = null;
  chrome.storage.local.remove("accessToken");
  showLoginView();
}
