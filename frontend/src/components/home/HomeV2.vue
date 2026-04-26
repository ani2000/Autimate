<template>
  <div class="home-page">
    <section class="top-row" v-if="children && children.length > 0">
      <div>
        <h1>Child Dashboard</h1>
        <p class="muted">Manage active profile, review screening history, and continue therapy flows.</p>
      </div>
      <select v-model="activeChildId" @change="handleSelect" class="child-select">
        <option v-for="child in children" :key="child.id" :value="child.id">{{ child.name }}</option>
        <option value="newchprofile">+ Add New Profile</option>
      </select>
    </section>

    <section class="cards">
      <div class="card"><h3>Total Tests</h3><p>{{ testResults.length }}</p></div>
      <div class="card"><h3>Latest Confidence</h3><p>{{ latestConfidence }}</p></div>
      <div class="card"><h3>Active Child</h3><p>{{ activeChildName }}</p></div>
    </section>

    <section class="history" v-if="children && children.length > 0">
      <h2>Previous Test Results</h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>#</th><th>Date</th><th>Q10 Risk</th><th>Result</th><th>Video Confidence</th><th>Therapies</th><th>Games</th></tr></thead>
          <tbody>
            <tr v-for="(test, index) in testResults" :key="test.id">
              <td>{{ index + 1 }}</td>
              <td>{{ formatDate(test.testDate) }}</td>
              <td>{{ Number(test.q10 * 100).toFixed(2) }}%</td>
              <td>{{ getResultDescription(test.asdStatus) }}</td>
              <td>{{ (test.confidence * 100).toFixed(2) }}%</td>
              <td>{{ test.therapies }}</td>
              <td>{{ test.games }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  data() {
    return { testResults: [], children: null, activeChildId: null };
  },
  computed: {
    latestConfidence() { if (!this.testResults.length) return 'N/A'; return `${(this.testResults[0].confidence * 100).toFixed(2)}%`; },
    activeChildName() { return Cookies.get('child_name') || 'Not selected'; },
  },
  methods: {
    async fetchTestResults() {
      try {
        const token = Cookies.get('token');
        const response = await axios.get(`http://localhost:8080/api/v1/aex/lists`, { headers: { Authorization: `Bearer ${token}` } });
        this.testResults = response.data.map(test => ({ id: test.id, testDate: test.testDate, asdStatus: test.vid_res, confidence: Number(test.vid_confid || 0), q10: Number(test.q10 || 0), therapies: test.suggested_therapies || '-', games: test.suggested_games || '-' }));
      } catch (error) { console.error('Error fetching test results:', error); }
    },
    async fetchChildren() {
      try {
        const token = Cookies.get('token');
        const response = await axios.get(`http://localhost:8080/api/v1/child/list`, { headers: { Authorization: `Bearer ${token}` } });
        this.children = response.data || [];
        if (this.children.length === 0) this.$router.push('/dashboard/newchprofile');
        else this.activateChildSession();
      } catch (error) { console.error('Error fetching child profiles:', error); }
    },
    formatDate(dateString) { return new Date(dateString).toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' }); },
    getResultDescription(asdStatus) { return asdStatus === '1' ? 'Lower ASD likelihood' : 'Needs closer ASD evaluation'; },
    async activateChildSession() {
      const activeChild = this.children.find(child => child.activeSession);
      if (activeChild) { Cookies.set('child_id', activeChild.id); Cookies.set('child_name', activeChild.name); }
      this.activeChildId = Cookies.get('child_id') || (this.children.length ? this.children[0].id : null);
    },
    handleSelect(event) {
      const selectedId = event.target.value;
      if (selectedId === 'newchprofile') this.$router.push('newchprofile');
      else {
        const selectedChild = this.children.find(child => child.id === selectedId);
        if (selectedChild) this.switchProfile(selectedChild);
      }
    },
    async switchProfile(child) {
      try {
        const token = Cookies.get('token');
        await axios.post(`http://localhost:8080/api/v1/child/toggle_active_session/${child.id}`, {}, { headers: { Authorization: `Bearer ${token}` } });
        Cookies.set('child_id', child.id); Cookies.set('child_name', child.name); window.location.reload();
      } catch (error) { console.error('Error switching profile:', error); }
    },
  },
  mounted() { this.fetchTestResults(); this.fetchChildren(); },
};
</script>

<style scoped>
.home-page { padding: 22px; width: calc(100vw - 290px); }
.top-row { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.top-row h1 { margin: 0; color: #0f172a; }
.muted { margin-top: 4px; color: #64748b; }
.child-select { min-width: 240px; height: 42px; border-radius: 10px; border: 1px solid #cbd5e1; padding: 0 10px; background: #fff; }
.cards { display: grid; grid-template-columns: repeat(3, minmax(180px, 1fr)); gap: 12px; margin-bottom: 18px; }
.card { background: #fff; border-radius: 12px; padding: 14px; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08); }
.card h3 { margin: 0 0 6px; color: #334155; font-size: 0.95rem; }
.card p { margin: 0; font-size: 1.25rem; font-weight: 700; color: #1e293b; }
.history { background: #fff; border-radius: 12px; padding: 14px; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08); }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
th, td { border-bottom: 1px solid #e2e8f0; text-align: left; padding: 10px; vertical-align: top; }
th { background: #f8fafc; color: #334155; }
@media (max-width: 900px) { .cards { grid-template-columns: 1fr; } .top-row { flex-direction: column; align-items: flex-start; } .child-select { width: 100%; } }
</style>
