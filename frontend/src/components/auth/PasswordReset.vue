<template>
  <div class="reset-page">
    <!-- Floating decorations -->
    <span class="float-deco d1">🔐</span>
    <span class="float-deco d2">✨</span>
    <span class="float-deco d3">🌟</span>
    <span class="float-deco d4">💜</span>

    <div class="reset-card">
      <!-- Header -->
      <div class="card-header">
        <div class="header-icon">🔒</div>
        <h1>Reset Password</h1>
        <p class="header-sub">Enter your phone number and we'll send you a code to reset your password</p>
      </div>

      <!-- Step 1: Phone & Send OTP -->
      <div class="form-section" v-if="step === 1">
        <div class="form-group">
          <label>📱 Phone Number</label>
          <input v-model="passResetData.phone" type="text" placeholder="01234567890" class="form-input" @keyup.enter="sendAnOTP">
        </div>
        <button class="btn-primary" @click="sendAnOTP" :disabled="!passResetData.phone">
          📤 Send OTP Code
        </button>
      </div>

      <!-- Step 2: OTP & New Password -->
      <div class="form-section" v-if="step === 2">
        <div class="phone-badge">
          <span>📱 {{ passResetData.phone }}</span>
          <button class="btn-change" @click="step = 1">Change</button>
        </div>
        <div class="form-group">
          <label>🔑 OTP Code</label>
          <input v-model="passResetData.otp" type="text" placeholder="Enter 4-digit code" class="form-input" maxlength="6">
        </div>
        <div class="form-group">
          <label>🔒 New Password</label>
          <input v-model="passResetData.password" :type="showPassword ? 'text' : 'password'" placeholder="Enter new password" class="form-input">
          <button class="toggle-pass" @click="showPassword = !showPassword">{{ showPassword ? '🙈' : '👁️' }}</button>
        </div>
        <div class="form-group" v-if="passResetData.password">
          <label>🔒 Confirm Password</label>
          <input v-model="confirmPassword" :type="showPassword ? 'text' : 'password'" placeholder="Confirm new password" class="form-input" @keyup.enter="requestPasswordChange">
          <p class="mismatch-warn" v-if="confirmPassword && confirmPassword !== passResetData.password">Passwords don't match</p>
        </div>
        <button class="btn-primary" @click="requestPasswordChange" :disabled="!canSubmit">
          ✅ Reset Password
        </button>
        <button class="btn-resend" @click="sendAnOTP">📤 Resend OTP</button>
      </div>

      <!-- Message -->
      <div v-if="message" :class="['message-box', { success: isSuccess }]">
        {{ message }}
      </div>

      <!-- Demo Hint -->
      <div class="demo-hint">
        <div class="hint-title">🎮 Demo Mode</div>
        <p>Use OTP: <strong>1234</strong> | Phone: <strong>01234567890</strong></p>
        <button class="btn-fill-demo" @click="fillDemo">Auto-fill Demo</button>
      </div>

      <!-- Back Link -->
      <div class="back-link">
        <router-link to="/auth/">← Back to Sign In</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'PasswordReset',
  data() {
    return {
      step: 1,
      passResetData: {
        otp: '',
        password: '',
        phone: '',
      },
      confirmPassword: '',
      showPassword: false,
      message: '',
      isSuccess: false,
    };
  },
  computed: {
    canSubmit() {
      return this.passResetData.otp && this.passResetData.password && this.passResetData.password === this.confirmPassword;
    },
  },
  mounted() {
    const phone = this.$route.query.phone || '';
    this.passResetData.phone = phone;
    if (phone) this.step = 2;
  },
  methods: {
    fillDemo() {
      this.passResetData.phone = '01234567890';
      this.passResetData.otp = '1234';
      this.passResetData.password = '123456';
      this.confirmPassword = '123456';
      this.step = 2;
    },

    async requestPasswordChange() {
      if (!this.canSubmit) return;
      try {
        const response = await axios.post('/api/v1/auth/reset_password', {
          phone: this.passResetData.phone,
          password: this.passResetData.password,
          otp: this.passResetData.otp,
        });

        if (response.data.requestSuccess === true) {
          this.message = '🎉 Password reset successful! Redirecting...';
          this.isSuccess = true;
          setTimeout(() => this.$router.push({ path: '/auth/' }), 2000);
        } else {
          this.message = response.data.requestMessage || 'Incorrect OTP. Please retry.';
          this.isSuccess = false;
        }
      } catch {
        this.message = 'Request failed. Please check your details and try again.';
        this.isSuccess = false;
      }
    },

    async sendAnOTP() {
      if (!this.passResetData.phone) return;
      try {
        await axios.get(`/api/v1/auth/send/otp/${encodeURIComponent(this.passResetData.phone)}`);
        this.message = '📨 OTP code has been sent to your phone!';
        this.isSuccess = true;
        this.step = 2;
        setTimeout(() => { this.message = ''; }, 3000);
      } catch {
        this.message = 'Failed to send OTP. Please try again.';
        this.isSuccess = false;
      }
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;500;600;700;800&display=swap');

.reset-page {
  position: relative;
  width: 100vw;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 50%, #f3e8ff 100%);
  font-family: 'Syne', sans-serif;
  overflow: hidden;
  padding: 20px;
}

/* Floating Decorations */
.float-deco {
  position: absolute;
  font-size: 32px;
  opacity: 0.15;
  animation: floatDeco 6s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}
.d1 { top: 10%; left: 8%; animation-delay: 0s; }
.d2 { top: 15%; right: 10%; animation-delay: 1.5s; }
.d3 { bottom: 20%; left: 12%; animation-delay: 3s; }
.d4 { bottom: 15%; right: 8%; animation-delay: 4.5s; }

@keyframes floatDeco {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-18px) rotate(12deg); }
}

/* Card */
.reset-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  border-radius: 28px;
  border: 1px solid rgba(196, 181, 253, 0.3);
  box-shadow: 0 20px 60px rgba(108, 99, 255, 0.15);
  padding: 36px 32px;
  animation: cardIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes cardIn {
  from { transform: scale(0.92) translateY(20px); opacity: 0; }
  to { transform: scale(1) translateY(0); opacity: 1; }
}

/* Header */
.card-header {
  text-align: center;
  margin-bottom: 28px;
}
.header-icon {
  font-size: 48px;
  margin-bottom: 12px;
  animation: iconBounce 2s ease-in-out infinite;
}
@keyframes iconBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}
.card-header h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 800;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.header-sub {
  margin: 8px 0 0;
  color: #7c3aed;
  font-size: 0.88rem;
  font-weight: 500;
  line-height: 1.4;
}

/* Form */
.form-section { margin-bottom: 16px; }
.form-group {
  position: relative;
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  font-size: 0.82rem;
  font-weight: 700;
  color: #5b21b6;
  margin-bottom: 6px;
}
.form-input {
  width: 100%;
  height: 48px;
  border: 2px solid #ddd6fe;
  border-radius: 14px;
  padding: 0 14px;
  font-family: 'Syne', sans-serif;
  font-size: 0.95rem;
  color: #374151;
  background: #fff;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}
.form-input:focus {
  border-color: #8b5cf6;
  box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.1);
}
.toggle-pass {
  position: absolute;
  right: 12px;
  top: 34px;
  background: none;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 4px;
}
.mismatch-warn {
  margin: 4px 0 0;
  font-size: 0.78rem;
  color: #dc2626;
  font-weight: 600;
}
.phone-badge {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f5f3ff;
  border-radius: 12px;
  padding: 10px 14px;
  margin-bottom: 16px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #5b21b6;
}
.btn-change {
  background: none;
  border: 1px solid #c4b5fd;
  border-radius: 8px;
  padding: 4px 10px;
  font-size: 0.78rem;
  font-weight: 700;
  color: #7c3aed;
  cursor: pointer;
  font-family: 'Syne', sans-serif;
}
.btn-change:hover { background: #ede9fe; }

/* Buttons */
.btn-primary {
  width: 100%;
  height: 50px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 8px;
}
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(108, 99, 255, 0.35); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; transform: none; box-shadow: none; }
.btn-resend {
  width: 100%;
  height: 40px;
  border: 2px solid #ddd6fe;
  border-radius: 50px;
  background: transparent;
  color: #7c3aed;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-resend:hover { background: #f5f3ff; border-color: #a78bfa; }

/* Message */
.message-box {
  margin: 16px 0;
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 0.88rem;
  font-weight: 600;
  text-align: center;
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
  animation: fadeIn 0.3s ease;
}
.message-box.success {
  background: #ecfdf5;
  color: #059669;
  border-color: #a7f3d0;
}
@keyframes fadeIn { from { opacity: 0; transform: translateY(-8px); } to { opacity: 1; transform: translateY(0); } }

/* Demo Hint */
.demo-hint {
  margin-top: 20px;
  padding: 14px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  border-radius: 16px;
  text-align: center;
  border: 1px solid #fbbf24;
}
.hint-title {
  font-weight: 800;
  font-size: 0.88rem;
  color: #92400e;
  margin-bottom: 4px;
}
.demo-hint p {
  margin: 4px 0 8px;
  font-size: 0.82rem;
  color: #92400e;
}
.demo-hint strong { color: #78350f; }
.btn-fill-demo {
  background: #f59e0b;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 6px 18px;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-fill-demo:hover { background: #d97706; transform: translateY(-1px); }

/* Back Link */
.back-link {
  text-align: center;
  margin-top: 16px;
}
.back-link a {
  color: #7c3aed;
  font-size: 0.88rem;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}
.back-link a:hover { color: #5b21b6; }

/* Responsive */
@media (max-width: 480px) {
  .reset-card { padding: 28px 20px; }
  .card-header h1 { font-size: 1.5rem; }
}
</style>