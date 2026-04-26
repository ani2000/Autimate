<template>
  <div class="profile-page">
    <div class="profile-card">
      <h2>Create Child Profile</h2>
      <p class="subtitle">You can use pseudo verification if official verification data is unavailable.</p>

      <form @submit.prevent="submitChildForm" class="form-grid">
        <label>Child Name</label>
        <input type="text" v-model="newChild.name" placeholder="Full name" required>

        <label>Gender</label>
        <select v-model="newChild.gender" required>
          <option value="" disabled>Select Gender</option>
          <option value="female">Female</option>
          <option value="male">Male</option>
        </select>

        <label>Date of Birth</label>
        <input type="date" v-model="newChild.dob" required>

        <label>Guardian NID (Optional)</label>
        <input type="text" v-model="guardianNid" placeholder="13 or 17 digit NID">

        <div class="verify-row">
          <button type="button" class="btn secondary" @click="verifyNidPseudo">Verify NID (Pseudo)</button>
          <span :class="['status-text', verified ? 'ok' : 'warn']">{{ verificationMessage }}</span>
        </div>

        <button type="submit" class="btn primary">+ Add Child</button>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  data() {
    return {
      newChild: { name: '', dob: '', gender: '' },
      guardianNid: '',
      verificationMessage: 'Not verified yet (allowed in demo mode).',
      verified: false,
    };
  },
  methods: {
    async verifyNidPseudo() {
      try {
        const response = await axios.post('http://localhost:8080/api/v1/verification/nid/pseudo', {
          nid: this.guardianNid,
          dob: this.newChild.dob,
          fullName: Cookies.get('name') || ''
        });
        this.verified = !!response.data.verified;
        this.verificationMessage = response.data.message || 'Pseudo verification checked.';
      } catch (error) {
        this.verified = false;
        this.verificationMessage = 'Verification service unavailable. Continuing in pseudo mode.';
      }
    },
    async submitChildForm() {
      try {
        const childData = { name: this.newChild.name, dob: this.newChild.dob, gender: this.newChild.gender };
        const token = Cookies.get('token');
        await axios.post(`http://localhost:8080/api/v1/child/add`, childData, {
          headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
        });
        alert('Child profile created successfully!');
        this.$router.push('/dashboard/home');
      } catch (error) {
        console.error('Error creating child profile:', error);
        alert('Failed to create child profile.');
      }
    },
  },
};
</script>

<style scoped>
.profile-page { width: calc(100vw - 290px); min-height: 100vh; display: flex; justify-content: center; align-items: center; padding: 24px; }
.profile-card { width: min(520px, 100%); background: #fff; border-radius: 14px; padding: 22px; box-shadow: 0 10px 24px rgba(15, 23, 42, 0.1); }
.subtitle { margin: 6px 0 14px; color: #64748b; }
.form-grid { display: flex; flex-direction: column; gap: 8px; }
label { margin-top: 6px; font-size: 0.92rem; font-weight: 600; color: #334155; }
input, select { height: 44px; border-radius: 10px; border: 1px solid #cbd5e1; padding: 0 12px; }
.verify-row { margin-top: 10px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.btn { height: 44px; border-radius: 10px; border: none; font-weight: 700; padding: 0 14px; }
.btn.primary { margin-top: 12px; background: #2563eb; color: #fff; }
.btn.secondary { background: #e2e8f0; color: #1e293b; }
.status-text { font-size: 0.88rem; }
.status-text.ok { color: #15803d; }
.status-text.warn { color: #b45309; }
</style>
