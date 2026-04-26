<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Cookies from 'js-cookie';
import '../../css/global.css';
import '../../css/page3.css';

const router = useRouter();
const isMenuOpen = ref(false);

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
};

const logout = () => {
  Cookies.remove('role');
  Cookies.remove('admin_token');
  Cookies.remove('admin_name');
  router.push('/admin/login');
};
</script>

<template>
  <nav class="navbar">
    <div class="nav-container">
      <button class="menu-toggle" @click="toggleMenu">☰</button>
      <div class="logo">Admin Panel</div>
      <div :class="['nav-links', { 'open': isMenuOpen }]">
        <router-link :style="{marginTop: '10px'}" to="/admin/home/" class="nav-item">Home</router-link>
        <router-link :style="{marginTop: '10px'}" to="/admin/doctors" class="nav-item">Doctors</router-link>
        <router-link :style="{marginTop: '10px'}" to="/admin/doctors/add" class="nav-item">Add Doctors</router-link>
        <button class="nav-item logout" :style="{color: 'red'}" @click="logout">Log Out</button>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  background-color: #2c3e50;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: white;
  margin-top: -6px;
  margin-left: -15px;
  width: calc(100vw + 15px);
}

.nav-container {
  display: flex;
  align-items: center;
  width: 100%;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  flex-grow: 1;
}

.nav-links {
  display: flex;
  gap: 15px;
}

.nav-item {
  color: white;
  text-decoration: none;
  font-size: 1rem;
  cursor: pointer;
}

.logout {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 1rem;
}

.menu-toggle {
  display: none;
  background: none;
  border: none;
  font-size: 1.5rem;
  color: white;
  cursor: pointer;
}

@media (max-width: 768px) {
  .menu-toggle {
    display: block;
  }

  .nav-links {
    display: none;
    flex-direction: column;
    position: absolute;
    top: 60px;
    left: 0;
    width: 100%;
    background-color: #2c3e50;
    padding: 10px;
  }

  .nav-links.open {
    display: flex;
  }
}
</style>
