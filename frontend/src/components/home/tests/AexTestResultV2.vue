<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  data() {
    return { childName: null, req_id: '', confidence: 0, q10_res: 0, video_confidence: 0 };
  },
  computed: {
    riskLabel() { return this.confidence >= 50 ? 'Needs closer ASD evaluation' : 'Lower ASD likelihood'; },
    riskClass() { return this.confidence >= 50 ? 'risk-high' : 'risk-low'; }
  },
  async mounted() {
    this.childName = Cookies.get('child_name');
    this.req_id = this.$route.query.req_id;
    if (this.req_id) {
      try {
        const token = Cookies.get('token');
        const response = await axios.get(`http://localhost:8080/api/v1/aex/res/${this.req_id}`, { headers: { Authorization: `Bearer ${token}` } });
        const rdata = response.data;
        this.confidence = parseFloat((Number(rdata.video_confidence || 0) * 100).toFixed(2));
        this.q10_res = parseFloat(rdata.q10_res || 0);
        this.video_confidence = parseFloat(rdata.video_confidence || 0);
      } catch (error) {
        console.error('Error fetching result:', error);
        alert('Error fetching result. Please try again.');
      }
    }
  }
};
</script>

<template>
  <div class="result-page">
    <section class="summary-card">
      <h2>{{ childName || 'Child' }}’s ASD Screening Result</h2>
      <div class="risk-chip" :class="riskClass">{{ riskLabel }}</div>
      <div class="metrics">
        <div class="metric"><span>Overall Confidence</span><strong>{{ confidence }}%</strong></div>
        <div class="metric"><span>Questionnaire (Q10)</span><strong>{{ (q10_res * 100).toFixed(2) }}%</strong></div>
        <div class="metric"><span>Behavioral Video</span><strong>{{ (video_confidence * 100).toFixed(2) }}%</strong></div>
      </div>
    </section>

    <section class="next-steps">
      <h3>Recommended Next Steps</h3>
      <ul>
        <li>Consult a specialist for final clinical evaluation.</li>
        <li>Start therapy/games for regular behavioral support.</li>
        <li>Track progress by repeating the test periodically.</li>
      </ul>
      <div class="actions">
        <router-link to="/dashboard/doctors" class="action-btn">Check Nearby Doctors</router-link>
        <router-link to="/dashboard/therapy" class="action-btn secondary">Open Therapy</router-link>
      </div>
    </section>
  </div>
</template>

<style scoped>
.result-page { width: calc(100vw - 290px); padding: 24px; display: grid; gap: 16px; }
.summary-card, .next-steps { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08); }
.summary-card h2 { margin: 0 0 10px; color: #0f172a; }
.risk-chip { display: inline-block; border-radius: 999px; padding: 6px 12px; font-weight: 700; font-size: 0.86rem; margin-bottom: 14px; }
.risk-high { background: #fee2e2; color: #b91c1c; }
.risk-low { background: #dcfce7; color: #166534; }
.metrics { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.metric { background: #f8fafc; border-radius: 10px; padding: 12px; display: flex; flex-direction: column; gap: 6px; }
.metric span { color: #64748b; font-size: 0.88rem; }
.metric strong { color: #1e293b; font-size: 1.1rem; }
.next-steps ul { margin: 0; padding-left: 18px; line-height: 1.8; color: #475569; }
.actions { margin-top: 14px; display: flex; gap: 10px; }
.action-btn { text-decoration: none; background: #2563eb; color: #fff; padding: 10px 14px; border-radius: 10px; font-weight: 600; }
.action-btn.secondary { background: #0f172a; }
@media (max-width: 900px) { .metrics { grid-template-columns: 1fr; } .actions { flex-direction: column; } }
</style>
