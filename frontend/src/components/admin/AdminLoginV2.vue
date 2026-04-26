<template>
  <div class="admin-auth-wrap">
    <div class="admin-auth-card">
      <h1>Admin Access</h1>
      <p class="sub">Sign in to manage doctors and platform data</p>

      <form class="form" @submit.prevent="login">
        <label>Phone</label>
        <input v-model="loginData.phone" type="text" placeholder="01XXXXXXXXX" required>

        <label>Password</label>
        <input v-model="loginData.password" type="password" placeholder="Password" required>

        <button class="btn" type="submit">Login</button>
      </form>

      <p v-if="message" class="error">{{ message }}</p>
      <router-link class="back" to="/">Back to main site</router-link>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  name: 'AdminLoginV2',
  data() {
    return {
      loginData: { phone: '', password: '' },
      message: '',
    };
  },
  methods: {
    async login() {
      this.message = '';
      try {
        const response = await axios.post('http://localhost:8080/api/v1/admin/auth/login', {
          phone: this.loginData.phone,
          password: this.loginData.password,
        });

        if (response.data.requestSuccess === true) {
          Cookies.set('role', 'admin', { expires: 7 });
          Cookies.set('admin_token', response.data.token, { expires: 7 });
          Cookies.set('admin_name', response.data.fullName, { expires: 7 });
          this.$router.push('/admin/home');
          return;
        }

        this.message = response.data.requestMessage || 'Login failed';
      } catch {
        this.message = 'Incorrect phone/password';
      }
    },
  },
};
</script>

<style scoped>
.admin-auth-wrap { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(160deg, #eef2ff, #f8fafc); padding: 20px; }
.admin-auth-card { width: min(440px, 100%); background: #fff; border-radius: 14px; box-shadow: 0 14px 30px rgba(15, 23, 42, 0.12); padding: 24px; }
h1 { margin: 0; color: #0f172a; }
.sub { margin: 8px 0 14px; color: #64748b; }
.form { display: grid; gap: 8px; }
label { color: #334155; font-size: 0.9rem; font-weight: 600; }
input { height: 42px; border: 1px solid #cbd5e1; border-radius: 10px; padding: 0 12px; }
.btn { margin-top: 8px; height: 42px; border: 0; border-radius: 10px; background: #1d4ed8; color: #fff; font-weight: 700; }
.error { margin-top: 10px; color: #b91c1c; }
.back { display: inline-block; margin-top: 8px; color: #1e40af; text-decoration: none; }
</style>
