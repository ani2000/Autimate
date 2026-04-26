<template>
  <aside class="sidebar">
    <div class="profile">
      <img class="avatar" src="@/assets/public/profile-image@2x.png" alt="profile" />
      <h3>{{ childName || 'Child Profile' }}</h3>
    </div>

    <nav class="menu">
      <router-link to="/dashboard/home">Home</router-link>
      <router-link to="/dashboard/aex/q">Testing</router-link>
      <router-link to="/dashboard/therapy">Therapy</router-link>
      <router-link to="/dashboard/game">Games</router-link>
      <router-link to="/dashboard/doctors">Nearby Doctors</router-link>
      <router-link to="/dashboard/chat">Video Chat</router-link>
    </nav>

    <button class="logout" @click="logout">Log Out</button>
  </aside>
</template>

<script>
import Cookies from 'js-cookie';

export default {
  name: 'SidebarV2',
  data() {
    return { childName: null };
  },
  mounted() {
    this.childName = Cookies.get('child_name');
  },
  methods: {
    logout() {
      Cookies.remove('token');
      Cookies.remove('name');
      Cookies.remove('child_id');
      Cookies.remove('child_name');
      this.$router.push({ path: '/auth' });
    },
  },
};
</script>

<style scoped>
.sidebar { width: 235px; min-height: 100vh; background: #111827; color: #fff; border-radius: 14px; padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.profile { text-align: center; padding: 10px 0 6px; border-bottom: 1px solid rgba(255,255,255,0.12); }
.avatar { width: 72px; height: 72px; border-radius: 50%; object-fit: cover; }
.profile h3 { margin: 10px 0 0; font-size: 1rem; }
.menu { display: grid; gap: 8px; margin-top: 6px; }
.menu a { color: #dbeafe; text-decoration: none; padding: 9px 10px; border-radius: 8px; }
.menu a.router-link-active { background: rgba(59,130,246,0.28); color: #fff; }
.logout { margin-top: auto; height: 40px; border-radius: 10px; border: 0; font-weight: 700; background: #ef4444; color: #fff; }
</style>
