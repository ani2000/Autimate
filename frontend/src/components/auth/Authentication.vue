<template>
  <div class="auth-shell">
    <!-- Floating decorations -->
    <div class="deco d1">🧩</div>
    <div class="deco d2">⭐</div>
    <div class="deco d3">💜</div>
    <div class="deco d4">☁️</div>
    <div class="deco d5">🌈</div>

    <div class="auth-card">
      <div class="card-mascot">🤗</div>
      <p class="brand">autimate</p>
      <h1>{{ currentForm === 'login' ? 'Welcome Back!' : 'Create Account' }}</h1>
      <p class="subtitle">Secure parent access for child screening &amp; care tools</p>

      <div class="tabs">
        <button :class="['tab', currentForm === 'login' ? 'active' : '']" @click="toggleForm('login')">
          <span class="tab-icon">🔑</span> Sign in
        </button>
        <button :class="['tab', currentForm === 'signup' ? 'active' : '']" @click="toggleForm('signup')">
          <span class="tab-icon">✨</span> Sign up
        </button>
      </div>

      <!-- ===== SIGN UP FORM ===== -->
      <form v-if="currentForm === 'signup'" class="form" @submit.prevent="signup">
        <!-- Step 1: NID Verification -->
        <template v-if="signupStep === 1">
          <div class="step-badge">Step 1 of 2 — Identity Check</div>
          <label>👤 Full name</label>
          <input v-model="signupData.fullName" type="text" placeholder="Guardian full name" required>

          <label>🪪 NID Number</label>
          <input v-model="signupData.nid" type="text" placeholder="Any 13-digit number" required>

          <label>📅 NID Date of Birth</label>
          <input v-model="signupData.nidDob" type="date" required>

          <button class="submit-btn wiggle-hover" type="button" @click="verifyNid" :disabled="loading">
            <span v-if="!loading">🔍 Verify identity</span>
            <span v-else class="spinner">⏳ Checking...</span>
          </button>
          <p class="sample-note">💡 Enter any name, NID, and date — demo mode accepts all</p>
        </template>

        <!-- Step 2: Phone & Password -->
        <template v-if="signupStep === 2">
          <div class="step-badge">Step 2 of 2 — Set up login</div>
          <div class="verified-badge">✅ Identity verified as <strong>{{ signupData.fullName }}</strong></div>

          <label>📱 Phone</label>
          <input v-model="signupData.phone" type="text" placeholder="01XXXXXXXXX" required>

          <label>🔒 Password</label>
          <input v-model="signupData.password" type="password" placeholder="Create password (min 4 chars)" required>

          <label>🔒 Confirm Password</label>
          <input v-model="signupData.confirmPassword" type="password" placeholder="Repeat password" required>

          <button class="submit-btn wiggle-hover" type="submit" :disabled="loading">
            <span v-if="!loading">🚀 Create account</span>
            <span v-else class="spinner">⏳ Creating...</span>
          </button>
          <button type="button" class="back-btn" @click="signupStep = 1">← Back to identity</button>
        </template>
      </form>

      <!-- ===== LOGIN FORM ===== -->
      <form v-else class="form" @submit.prevent="login">
        <label>📱 Phone</label>
        <input v-model="loginData.phone" type="text" placeholder="01XXXXXXXXX" required>

        <label>🔒 Password</label>
        <input v-model="loginData.password" type="password" placeholder="Your password" required>

        <button class="submit-btn wiggle-hover" type="submit" :disabled="loading">
          <span v-if="!loading">🎉 Sign in</span>
          <span v-else class="spinner">⏳ Signing in...</span>
        </button>
        <router-link to="/auth/password/reset/" class="helper-link">🔓 Forgot password?</router-link>
      </form>

      <div class="demo-box">
        <p class="demo-title">🧪 Quick Demo Access</p>
        <p class="demo-line">Phone: <strong>01643806870</strong></p>
        <p class="demo-line">Password: <strong>user</strong></p>
        <p class="demo-line">OTP: <strong>1234</strong></p>
        <button type="button" class="demo-fill-btn" @click="fillDemo">⚡ Auto-fill demo credentials</button>
      </div>

      <p v-if="message" class="msg success">{{ message }}</p>
      <p v-if="messageError" class="msg error">{{ messageError }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

const API = '';

export default {
  name: 'AuthForm',
  data() {
    return {
      currentForm: 'login',
      signupStep: 1,
      loading: false,
      signupData: {
        fullName: '',
        nid: '',
        nidDob: '',
        phone: '',
        password: '',
        confirmPassword: ''
      },
      loginData: {
        phone: '',
        password: ''
      },
      message: '',
      messageError: '',
    };
  },
  methods: {
    normalizePhone(value) {
      return (value || '').replace(/[\s-]/g, '').trim();
    },

    toggleForm(form) {
      this.currentForm = form;
      this.signupStep = 1;
      this.message = '';
      this.messageError = '';
    },

    fillDemo() {
      if (this.currentForm === 'login') {
        this.loginData.phone = '01643806870';
        this.loginData.password = 'user';
      } else {
        this.signupData.fullName = 'Demo Parent';
        this.signupData.nid = '1234567890123';
        this.signupData.nidDob = '1990-01-15';
        this.signupData.phone = '01234567890';
        this.signupData.password = '123456';
        this.signupData.confirmPassword = '123456';
      }
      this.message = 'Demo credentials filled!';
      this.messageError = '';
    },

    async verifyNid() {
      this.message = '';
      this.messageError = '';

      if (!this.signupData.fullName.trim() || !this.signupData.nid.trim() || !this.signupData.nidDob) {
        this.messageError = 'Please fill all identity fields.';
        return;
      }

      this.loading = true;
      try {
        const res = await axios.post(`${API}/api/v1/verification/nid/pseudo`, {
          fullName: this.signupData.fullName,
          nid: this.signupData.nid,
          dob: this.signupData.nidDob,
        });

        if (res.data?.verified) {
          this.message = `✅ ${res.data.message || 'Identity verified!'}`;
          this.signupStep = 2;
        } else {
          this.messageError = res.data?.message || 'Verification failed. Please fill all fields.';
        }
      } catch (err) {
        const status = err.response?.status;
        if (status === 403) {
          this.messageError = 'Server rejected the request (403). Backend may need restart.';
        } else if (!err.response) {
          this.messageError = 'Cannot reach backend. Is the server running on port 8080?';
        } else {
          this.messageError = `Verification error (${status}): ${err.response?.data?.message || err.message}`;
        }
      } finally {
        this.loading = false;
      }
    },

    async signup() {
      this.message = '';
      this.messageError = '';

      const normalizedPhone = this.normalizePhone(this.signupData.phone);

      if (this.signupData.password !== this.signupData.confirmPassword) {
        this.messageError = 'Passwords do not match.';
        return;
      }
      if (this.signupData.password.length < 4) {
        this.messageError = 'Password must be at least 4 characters.';
        return;
      }
      if (!/^0\d{9,10}$/.test(normalizedPhone)) {
        this.messageError = 'Phone must be 10-11 digits starting with 0 (e.g. 01234567890).';
        return;
      }

      this.loading = true;
      try {
        const res = await axios.post(`${API}/api/v1/auth/register`, {
          fullName: this.signupData.fullName,
          phone: normalizedPhone,
          password: this.signupData.password
        });

        const data = res.data;
        if (data.requestSuccess === false) {
          this.messageError = data.requestMessage || 'Registration failed.';
          return;
        }

        this.message = '🎉 Account created! Redirecting to OTP verification...';
        setTimeout(() => {
          this.$router.push({
            path: '/auth/otp/',
            query: { phone: normalizedPhone }
          });
        }, 800);
      } catch (err) {
        if (!err.response) {
          this.messageError = 'Cannot reach backend. Is the server running on port 8080?';
        } else {
          this.messageError = err.response?.data?.requestMessage || 'Registration failed. Please try again.';
        }
      } finally {
        this.loading = false;
      }
    },

    async login() {
      this.message = '';
      this.messageError = '';

      const normalizedPhone = this.normalizePhone(this.loginData.phone);

      if (!normalizedPhone || !this.loginData.password.trim()) {
        this.messageError = 'Please enter phone and password.';
        return;
      }

      this.loading = true;
      try {
        const res = await axios.post(`${API}/api/v1/auth/login`, {
          phone: normalizedPhone,
          password: this.loginData.password
        });

        const data = res.data;

        if (data.verificationStatus === true && data.token) {
          Cookies.set('token', data.token, { expires: 7 });
          Cookies.set('name', data.fullName, { expires: 7 });
          this.message = `Welcome back, ${data.fullName}! Redirecting...`;
          setTimeout(() => this.$router.push({ path: '/dashboard/' }), 600);
          return;
        }

        if (data.verificationStatus === false) {
          this.message = 'Account not yet verified. Sending OTP...';
          try {
            await axios.get(`${API}/api/v1/auth/send/otp/${this.loginData.phone}`);
          } catch { /* OTP send is best-effort */ }
          setTimeout(() => {
            this.$router.push({
              path: '/auth/otp/',
              query: { phone: normalizedPhone }
            });
          }, 800);
          return;
        }

        this.messageError = data.requestMessage || 'Login failed. Check your credentials.';
      } catch (err) {
        if (!err.response) {
          this.messageError = 'Cannot reach backend. Is the server running on port 8080?';
        } else if (err.response.status === 403) {
          this.messageError = 'Invalid phone or password.';
        } else {
          this.messageError = err.response?.data?.requestMessage || 'Login failed. Check your credentials.';
        }
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.auth-shell {
  min-height: 100vh;
  width: 100%;
  display: grid;
  place-items: center;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 35%, #F0E6FF 70%, #FFF0F5 100%);
  padding: 20px;
  font-family: 'Syne', sans-serif;
  position: relative;
  overflow: hidden;
}

/* Floating decorations */
.deco {
  position: fixed;
  font-size: 2rem;
  opacity: 0.3;
  pointer-events: none;
  animation: floatDeco 6s ease-in-out infinite;
}
.d1 { top: 10%; left: 8%; animation-delay: 0s; }
.d2 { top: 25%; right: 10%; animation-delay: 1s; font-size: 1.6rem; }
.d3 { bottom: 20%; left: 5%; animation-delay: 2s; }
.d4 { top: 60%; right: 6%; animation-delay: 3s; font-size: 2.4rem; }
.d5 { bottom: 8%; right: 15%; animation-delay: 1.5s; font-size: 1.8rem; }

@keyframes floatDeco {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-16px) rotate(6deg); }
}

.auth-card {
  width: min(480px, 100%);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  box-shadow: 0 20px 60px rgba(100, 50, 200, 0.12), 0 0 0 1px rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(255, 255, 255, 0.7);
  padding: 28px;
  animation: cardPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  z-index: 1;
}

.card-mascot {
  font-size: 2.8rem;
  text-align: center;
  animation: mascotBounce 2s ease-in-out infinite;
}

@keyframes mascotBounce {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-6px) scale(1.1); }
}

@keyframes cardPop {
  from { opacity: 0; transform: scale(0.9) translateY(20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.brand {
  margin: 4px 0 0;
  text-align: center;
  font-weight: 800;
  font-size: 1.3rem;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

h1 {
  margin: 8px 0 6px;
  color: #1A0B3E;
  text-align: center;
  font-weight: 800;
}

.subtitle {
  margin: 0 0 16px;
  color: #5A4690;
  font-size: 0.92rem;
  text-align: center;
}

.tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 16px;
}

.tab {
  border: 2px solid #D4CCFF;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 50px;
  height: 44px;
  font-weight: 700;
  color: #5A4690;
  font-family: 'Syne', sans-serif;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;
}
.tab-icon { font-size: 1.05rem; }

.tab.active {
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  border-color: transparent;
  transform: scale(1.04);
  box-shadow: 0 6px 20px rgba(108, 99, 255, 0.3);
}

/* Step badge */
.step-badge {
  display: inline-block;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-size: 0.78rem;
  font-weight: 700;
  padding: 3px 14px;
  border-radius: 50px;
  margin-bottom: 8px;
}

/* Verified badge */
.verified-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: rgba(22, 163, 74, 0.1);
  color: #16a34a;
  font-size: 0.82rem;
  font-weight: 700;
  padding: 4px 14px;
  border-radius: 50px;
  margin-bottom: 8px;
}

/* Back button */
.back-btn {
  background: none;
  border: 2px solid #D4CCFF;
  border-radius: 50px;
  height: 40px;
  padding: 0 18px;
  font-family: 'Syne', sans-serif;
  font-weight: 600;
  font-size: 0.88rem;
  color: #5A4690;
  cursor: pointer;
  transition: all 0.25s ease;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.back-btn:hover {
  border-color: #A855F7;
  color: #A855F7;
  background: rgba(168, 85, 247, 0.05);
}

.form {
  display: grid;
  gap: 10px;
}

label {
  color: #3D2B7A;
  font-size: 0.88rem;
  font-weight: 600;
}

input {
  height: 46px;
  border-radius: 16px;
  border: 2px solid #E0D8FF;
  padding: 0 14px;
  font-family: 'Syne', sans-serif;
  font-size: 0.95rem;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.25s ease;
}

input:focus {
  outline: none;
  border-color: #A855F7;
  box-shadow: 0 0 0 4px rgba(168, 85, 247, 0.15);
  background: #fff;
}

input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn {
  margin-top: 10px;
  border: 0;
  border-radius: 50px;
  height: 48px;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-weight: 700;
  font-family: 'Syne', sans-serif;
  font-size: 1rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(108, 99, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: wait;
  transform: none;
  box-shadow: 0 4px 15px rgba(108, 99, 255, 0.15);
}

.wiggle-hover:hover:not(:disabled) {
  transform: translateY(-3px) scale(1.03);
  box-shadow: 0 12px 32px rgba(108, 99, 255, 0.4);
}

/* Spinner animation for loading buttons */
.spinner {
  animation: spin 1s linear infinite;
  display: inline-block;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Demo box */
.demo-box {
  margin-top: 14px;
  background: linear-gradient(135deg, rgba(108, 99, 255, 0.06), rgba(168, 85, 247, 0.06));
  border: 2px dashed #D4CCFF;
  border-radius: 18px;
  padding: 14px 16px;
}

.demo-title {
  font-size: 0.82rem;
  font-weight: 700;
  color: #6C63FF;
  margin-bottom: 6px;
}

.demo-line {
  font-size: 0.8rem;
  color: #5A4690;
  margin: 2px 0;
  font-family: 'Courier New', monospace;
}

.demo-fill-btn {
  margin-top: 10px;
  width: 100%;
  border: 2px solid #6C63FF;
  background: rgba(108, 99, 255, 0.08);
  border-radius: 50px;
  height: 40px;
  font-weight: 700;
  font-family: 'Syne', sans-serif;
  font-size: 0.88rem;
  color: #6C63FF;
  cursor: pointer;
  transition: all 0.25s ease;
}
.demo-fill-btn:hover {
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 6px 20px rgba(108, 99, 255, 0.3);
}

.helper-link {
  margin-top: 8px;
  text-decoration: none;
  color: #6C63FF;
  font-size: 0.88rem;
  font-weight: 600;
  text-align: center;
  transition: color 0.2s;
}
.helper-link:hover { color: #A855F7; }

.msg {
  margin-top: 12px;
  font-size: 0.9rem;
  font-weight: 600;
  text-align: center;
  padding: 8px;
  border-radius: 12px;
}

.msg.success { color: #166534; background: rgba(22, 101, 52, 0.08); }
.msg.error { color: #b91c1c; background: rgba(185, 28, 28, 0.08); }

/* ── Responsive ── */
@media (max-width: 640px) {
  .auth-shell { padding: 14px; }
  .auth-card { padding: 20px 16px; border-radius: 22px; }
  h1 { font-size: 1.4rem; }
  .subtitle { font-size: 0.85rem; }
  .tab { height: 40px; font-size: 0.88rem; }
  input { height: 42px; font-size: 0.9rem; }
  .submit-btn { height: 44px; font-size: 0.95rem; }
  .deco { font-size: 1.4rem; }
  .demo-box { padding: 10px 12px; }
}
@media (max-width: 400px) {
  .auth-card { padding: 16px 12px; }
  .tabs { gap: 6px; }
  .brand { font-size: 1.1rem; }
}
</style>
