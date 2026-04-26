<template>
  <aside class="sidebar">
    <div class="brand">autimate Admin</div>

    <nav class="nav">
      <router-link to="/admin/home" class="link">Dashboard</router-link>
      <router-link to="/admin/doctors" class="link">Doctors</router-link>
      <router-link to="/admin/doctors/add" class="link">Add Doctor</router-link>
    </nav>

    <div class="footer">
      <p class="name">{{ adminName }}</p>
      <button class="logout" @click="logout">Log out</button>
    </div>
  </aside>
</template>

<script>
import Cookies from 'js-cookie';

export default {
  name: 'AdminSidebarV2',
  computed: {
    adminName() {
      return Cookies.get('admin_name') || 'Admin';
    },
  },
  methods: {
    logout() {
      Cookies.remove('role');
      Cookies.remove('admin_token');
      Cookies.remove('admin_name');
      this.$router.push('/admin/login');
    },
  },
};
</script>

<style scoped>
.sidebar { width: 250px; min-height: calc(100vh - 20px); background: #0f172a; color: #fff; border-radius: 14px; padding: 18px 14px; display: flex; flex-direction: column; }
.brand { font-size: 1.12rem; font-weight: 800; margin-bottom: 18px; }
.nav { display: grid; gap: 6px; }
.link { color: #cbd5e1; text-decoration: none; padding: 10px; border-radius: 10px; }
.link.router-link-active { color: #fff; background: #1e293b; }
.footer { margin-top: auto; border-top: 1px solid #334155; padding-top: 12px; }
.name { margin: 0 0 8px; color: #94a3b8; font-size: 0.88rem; }
.logout { width: 100%; height: 38px; border: 0; border-radius: 10px; background: #ef4444; color: #fff; font-weight: 700; }
@media (max-width: 900px) { .sidebar { width: 100%; min-height: auto; } }
</style>
