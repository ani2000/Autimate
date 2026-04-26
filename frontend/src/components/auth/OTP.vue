<template>
  <div class="otp-shell">
    <div class="deco d1">🔐</div>
    <div class="deco d2">⭐</div>
    <div class="deco d3">💜</div>

    <div class="otp-card">
      <div class="card-mascot">📱</div>
      <p class="brand">autimate</p>
      <h1>Phone Verification</h1>
      <p class="desc">Enter the OTP code sent to your phone number.</p>
      <div class="demo-badge">💡 Demo OTP is always: <strong>1234</strong></div>

      <form class="form" @submit.prevent="verifyOTP">
        <label>📱 Phone</label>
        <input v-model="verifData.phone" type="text" placeholder="01XXXXXXXXX" :readonly="phoneLocked" required>

        <label>🔢 OTP code</label>
        <input v-model="verifData.otp" type="text" placeholder="Enter 1234" maxlength="6" required>

        <button class="submit wiggle-hover" type="submit" :disabled="loading">
          <span v-if="loading" class="spinner">⏳</span>
          <template v-else><span>✅</span> Verify</template>
        </button>
      </form>

      <div class="actions">
        <button class="link-btn" @click="resendOtp" :disabled="loading">🔄 Resend code</button>
        <router-link to="/auth/" class="link">↩️ Use another number</router-link>
      </div>

      <p v-if="message" :class="['msg', messageType]">{{ message }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

const API = '';

export default {
  name: 'OtpVerifyPage',
  data() {
    return {
      verifData: { otp: '1234', phone: '' },
      message: '',
      messageType: 'info',
      loading: false,
      phoneLocked: false,
    };
  },
  mounted() {
    const phone = this.$route.query.phone || '';
    if (phone) {
      this.verifData.phone = phone;
      this.phoneLocked = true;
    }
  },
  methods: {
    async verifyOTP() {
      this.message = '';
      if (!this.verifData.phone.trim() || !this.verifData.otp.trim()) {
        this.message = 'Please enter both phone and OTP.';
        this.messageType = 'error';
        return;
      }

      this.loading = true;
      try {
        const res = await axios.get(
          `${API}/api/v1/auth/verify/${this.verifData.phone}/${this.verifData.otp}`
        );

        if (res.data.verificationStatus === true && res.data.token) {
          this.message = '✅ Verified! Redirecting to dashboard...';
          this.messageType = 'success';
          Cookies.set('token', res.data.token, { expires: 7 });
          Cookies.set('name', res.data.fullName, { expires: 7 });
          setTimeout(() => this.$router.push({ path: '/dashboard/' }), 600);
          return;
        }

        this.message = "Code didn't match. Use 1234 for demo.";
        this.messageType = 'error';
      } catch (err) {
        if (!err.response) {
          this.message = 'Cannot reach backend. Is the server running on port 8080?';
        } else {
          this.message = err.response?.data?.requestMessage || 'Verification failed. Please try again.';
        }
        this.messageType = 'error';
      } finally {
        this.loading = false;
      }
    },
    async resendOtp() {
      this.loading = true;
      try {
        await axios.get(`${API}/api/v1/auth/send/otp/${this.verifData.phone}`);
        this.message = '📨 New code sent! (Use 1234 for demo)';
        this.messageType = 'info';
      } catch {
        this.message = 'Failed to resend. Check your phone number.';
        this.messageType = 'error';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.otp-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 20px;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 35%, #F0E6FF 70%, #FFF0F5 100%);
  font-family: 'Syne', sans-serif;
  position: relative;
  overflow: hidden;
}

.deco {
  position: fixed;
  font-size: 2rem;
  opacity: 0.3;
  pointer-events: none;
  animation: floatDeco 6s ease-in-out infinite;
}
.d1 { top: 12%; left: 8%; }
.d2 { top: 30%; right: 10%; animation-delay: 1.5s; }
.d3 { bottom: 15%; left: 12%; animation-delay: 2.5s; }
@keyframes floatDeco {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-16px) rotate(6deg); }
}

.otp-card {
  width: min(460px, 100%);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  border-radius: 28px;
  border: 2px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 20px 60px rgba(100, 50, 200, 0.12);
  padding: 28px;
  animation: cardPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  z-index: 1;
}
@keyframes cardPop {
  from { opacity: 0; transform: scale(0.9) translateY(20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
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
h1 { margin: 8px 0 4px; color: #1A0B3E; text-align: center; font-weight: 800; }
.desc { margin: 0 0 10px; color: #5A4690; text-align: center; }

.demo-badge {
  text-align: center;
  font-size: 0.88rem;
  color: #5A4690;
  background: rgba(108, 99, 255, 0.06);
  padding: 8px 14px;
  border-radius: 50px;
  margin-bottom: 14px;
}

.form { display: grid; gap: 10px; }
label { color: #3D2B7A; font-size: 0.88rem; font-weight: 600; }
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

.submit {
  margin-top: 10px;
  height: 48px;
  border: 0;
  border-radius: 50px;
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
.wiggle-hover:hover {
  transform: translateY(-3px) scale(1.03);
  box-shadow: 0 12px 32px rgba(108, 99, 255, 0.4);
}

.actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.link-btn {
  border: 0;
  background: transparent;
  color: #6C63FF;
  font-weight: 600;
  padding: 0;
  font-family: 'Syne', sans-serif;
  cursor: pointer;
  transition: color 0.2s;
}
.link-btn:hover { color: #A855F7; }
.link { text-decoration: none; color: #5A4690; font-size: 0.9rem; }
.link:hover { color: #6C63FF; }
.msg {
  margin-top: 12px;
  font-weight: 600;
  text-align: center;
  padding: 8px;
  border-radius: 12px;
}
.msg.info { color: #6C63FF; background: rgba(108, 99, 255, 0.06); }
.msg.success { color: #166534; background: rgba(22, 101, 52, 0.08); }
.msg.error { color: #b91c1c; background: rgba(185, 28, 28, 0.08); }

.spinner { animation: spin 1s linear infinite; display: inline-block; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.submit:disabled, .link-btn:disabled { opacity: 0.6; cursor: wait; }

input:read-only { background: rgba(108, 99, 255, 0.04); color: #5A4690; }

/* ── Responsive ── */
@media (max-width: 640px) {
  .otp-shell { padding: 14px; }
  .otp-card { padding: 20px 16px; border-radius: 22px; }
  h1 { font-size: 1.3rem; }
  input { height: 42px; font-size: 0.9rem; }
  .submit { height: 44px; font-size: 0.95rem; }
  .actions { flex-direction: column; text-align: center; }
  .deco { font-size: 1.4rem; }
}
@media (max-width: 400px) {
  .otp-card { padding: 16px 12px; }
  .brand { font-size: 1.1rem; }
}
</style>
