<script setup>
</script>

<template>
  <aside class="sidebar">
    <!-- Profile -->
    <div class="profile-area">
      <div class="avatar-ring">
        <img class="avatar-img" loading="lazy" alt="Profile" src="@/assets/public/profile-image@2x.png" />
      </div>
      <h3 class="child-name">{{ childName || 'Little Star' }}</h3>
      <span class="age-chip">{{ ageGroupLabel }}</span>
    </div>

    <!-- Navigation -->
    <nav class="nav-links">
      <router-link to="/dashboard/" class="nav-item">
        <span class="nav-emoji">🏠</span>
        <span>Home</span>
      </router-link>
      <router-link to="/dashboard/aex/q" class="nav-item">
        <span class="nav-emoji">🧪</span>
        <span>Testing</span>
      </router-link>
      <router-link to="/dashboard/therapy" class="nav-item">
        <span class="nav-emoji">🩺</span>
        <span>Therapy</span>
      </router-link>
      <router-link to="/dashboard/chat" class="nav-item">
        <span class="nav-emoji">📹</span>
        <span>Video Chat</span>
      </router-link>
      <router-link to="/dashboard/game" class="nav-item">
        <span class="nav-emoji">🎮</span>
        <span>Games</span>
      </router-link>
      <router-link to="/dashboard/doctors" class="nav-item">
        <span class="nav-emoji">👨‍⚕️</span>
        <span>Nearby Doctor</span>
      </router-link>
      <router-link to="/dashboard/progress" class="nav-item">
        <span class="nav-emoji">📊</span>
        <span>Progress</span>
      </router-link>
    </nav>

    <!-- Bottom -->
    <div class="bottom-links">
      <div class="nav-item bottom-item">
        <span class="nav-emoji">❓</span>
        <span>Help & Support</span>
      </div>
      <div class="nav-item bottom-item" @click="logout" style="cursor:pointer">
        <span class="nav-emoji">🚪</span>
        <span>Log Out</span>
      </div>
    </div>
  </aside>
</template>



<script>
import Cookies from 'js-cookie';

export default {
  name: 'Dashboard',
  data() {
    return {
      childId: null,
      childName: null,
      ageGroupLabel: 'Not selected'
    };
  },
  mounted() {
    this.childId = Cookies.get('child_id');
    this.childName = Cookies.get('child_name');
    this.ageGroupLabel = Cookies.get('child_age_group') || 'Not selected';
  },
  methods: {
    logout() {
      Cookies.remove('token');
      Cookies.remove('name');
      Cookies.remove('child_id');
      Cookies.remove('child_name');
      Cookies.remove('child_age');
      Cookies.remove('child_gender');
      Cookies.remove('child_age_group');

      this.$router.push({
        path: '/auth'
      });
    }
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.sidebar {
  width: 260px;
  min-height: 100dvh;
  background: linear-gradient(180deg, #2D1B69 0%, #1A0B3E 100%);
  display: flex;
  flex-direction: column;
  padding: 20px 14px;
  font-family: 'Syne', sans-serif;
  position: fixed;
  left: 0; top: 0;
  z-index: 190;
  overflow-y: auto;
  box-shadow: 14px 0 34px rgba(12, 8, 34, 0.35);
}

/* ---- Profile ---- */
.profile-area {
  text-align: center;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  margin-bottom: 12px;
}

.avatar-ring {
  width: 72px; height: 72px;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  padding: 3px;
}

.avatar-img {
  width: 100%; height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #1A0B3E;
}

.child-name {
  margin: 0;
  color: #fff;
  font-weight: 700;
  font-size: 1.1rem;
}

.age-chip {
  display: inline-block;
  margin-top: 6px;
  background: rgba(168, 85, 247, 0.2);
  color: #D4CCFF;
  border-radius: 50px;
  padding: 4px 14px;
  font-size: 0.75rem;
  font-weight: 600;
}

/* ---- Nav links ---- */
.nav-links {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: rgba(255,255,255,0.92);
  padding: 12px 14px;
  border-radius: 14px;
  font-size: 0.92rem;
  font-weight: 600;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.nav-item:hover {
  background: rgba(108, 99, 255, 0.18);
  color: #fff;
  transform: translateX(4px);
}

.nav-item.router-link-active,
.nav-item.router-link-exact-active {
  background: linear-gradient(135deg, rgba(108,99,255,0.35), rgba(168,85,247,0.25));
  color: #fff;
  box-shadow: 0 4px 16px rgba(108, 99, 255, 0.2);
}

.nav-emoji {
  font-size: 1.2rem;
  min-width: 24px;
  text-align: center;
}

/* ---- Bottom ---- */
.bottom-links {
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 12px;
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bottom-item {
  opacity: 0.92;
  font-size: 0.85rem;
}
.bottom-item:hover {
  opacity: 1;
}

/* ── Responsive (sidebar shown as overlay on mobile via DashboardLayout) ── */
@media (max-width: 900px) {
  .sidebar {
    width: min(82vw, 320px);
    background: linear-gradient(180deg, #2c2a8e 0%, #1f1b66 100%);
    padding-top: calc(18px + env(safe-area-inset-top, 0px));
    padding-bottom: calc(18px + env(safe-area-inset-bottom, 0px));
    box-shadow: 18px 0 36px rgba(7, 8, 35, 0.5);
  }
  .profile-area { margin-bottom: 16px; }
  .nav-item { padding: 14px 14px; font-size: 0.98rem; }
  .nav-emoji { font-size: 1.3rem; }

  .nav-item.router-link-active,
  .nav-item.router-link-exact-active {
    background: linear-gradient(135deg, rgba(138, 125, 255, 0.5), rgba(87, 217, 255, 0.24));
    box-shadow: 0 6px 18px rgba(61, 166, 255, 0.25);
  }
}

@media (max-width: 480px) {
  .child-name {
    font-size: 1rem;
  }
  .age-chip {
    font-size: 0.72rem;
    padding: 4px 12px;
  }
}
</style>
