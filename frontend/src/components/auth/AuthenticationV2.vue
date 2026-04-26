<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <h1>autimate Access</h1>
      <p class="sub">Safe access for parents and guardians</p>

      <div class="tabs">
        <button :class="['tab', currentForm === 'login' ? 'active' : '']" @click="currentForm = 'login'">Login</button>
        <button :class="['tab', currentForm === 'signup' ? 'active' : '']" @click="currentForm = 'signup'">Sign up</button>
      </div>

      <form v-if="currentForm === 'login'" @submit.prevent="login" class="form">
        <label>Phone</label>
        <input v-model="loginData.phone" type="text" placeholder="016XXXXXXXX" required>

        <label>Password</label>
        <input v-model="loginData.password" type="password" placeholder="Password" required>

        <button class="btn primary" type="submit">Login</button>
        <router-link class="helper" to="/auth/password/reset">Forgot password?</router-link>
      </form>

      <form v-else @submit.prevent="signup" class="form">
        <label>Full Name</label>
        <input v-model="signupData.fullName" type="text" placeholder="Guardian full name" required>

        <label>Phone</label>
        <input v-model="signupData.phone" type="text" placeholder="016XXXXXXXX" required>

        <label>Password</label>
        <input v-model="signupData.password" type="password" placeholder="Password" required>

        <button class="btn primary" type="submit">Create account</button>
        <router-link class="helper" to="/auth/otp">Verify with OTP</router-link>
      </form>

      <p v-if="message" class="msg success">{{ message }}</p>
      <p v-if="messageError" class="msg error">{{ messageError }}</p>

      <div class="demo-note">
        <strong>Demo mode:</strong> If external verification is unavailable, pseudo verification paths remain enabled.
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  name: 'AuthFormV2',
  data() {
    return {
      currentForm: 'login',
      signupData: { fullName: '', phone: '', password: '' },
      loginData: { phone: '', password: '' },
      message: '',
      messageError: '',
    };
  },
  methods: {
    async signup() {
      this.message = '';
      this.messageError = '';
      try {
        await axios.post('http://localhost:8080/api/v1/auth/register', {
          fullName: this.signupData.fullName,
          phone: this.signupData.phone,
          password: this.signupData.password,
        });
        this.message = 'Account created. Please verify OTP before first login.';
        this.$router.push({ path: '/auth/otp/', query: { phone: this.signupData.phone } });
      } catch {
        this.messageError = 'Sign up failed. Please try again.';
      }
    },

    async login() {
      this.message = '';
      this.messageError = '';
      try {
        const response = await axios.post('http://localhost:8080/api/v1/auth/login', {
          phone: this.loginData.phone,
          password: this.loginData.password,
        });

        if (response.data.verificationStatus === true) {
          Cookies.set('token', response.data.token, { expires: 7 });
          Cookies.set('name', response.data.fullName, { expires: 7 });
          this.$router.push({ path: '/dashboard/' });
          return;
        }

        this.messageError = 'Please verify your account via OTP first.';
      } catch {
        this.messageError = 'Incorrect phone/password';
      }
    },
  },
};
</script>

<style scoped>
.auth-wrap { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(160deg, #eff6ff, #f8fafc); padding: 20px; }
.auth-card { width: min(460px, 100%); background: #fff; border-radius: 14px; box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12); padding: 24px; }
.auth-card h1 { margin: 0; color: #0f172a; }
.sub { margin: 6px 0 14px; color: #64748b; }
.tabs { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 14px; }
.tab { height: 40px; border-radius: 10px; border: 1px solid #cbd5e1; background: #fff; font-weight: 600; }
.tab.active { background: #1d4ed8; border-color: #1d4ed8; color: #fff; }
.form { display: grid; gap: 8px; }
label { color: #334155; font-size: 0.9rem; font-weight: 600; }
input { height: 42px; border-radius: 10px; border: 1px solid #cbd5e1; padding: 0 12px; }
.btn.primary { margin-top: 8px; height: 42px; border: 0; border-radius: 10px; background: #2563eb; color: #fff; font-weight: 700; }
.helper { color: #1e40af; text-decoration: none; margin-top: 6px; font-size: 0.9rem; }
.msg { margin-top: 10px; font-size: 0.9rem; }
.msg.success { color: #166534; }
.msg.error { color: #b91c1c; }
.demo-note { margin-top: 12px; font-size: 0.84rem; color: #475569; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px; }
</style>
