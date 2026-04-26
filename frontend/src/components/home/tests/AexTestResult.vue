<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
    data() {
        return {
            childId: null,
            childName: null,
            req_id: '',
            confidence: 0,
            q10_res: 0,
            video_confidence: 0,
            loading: true,
            riskLevel: '',
        };
    },
    async mounted() {
        this.childId = Cookies.get('childId');
        this.childName = Cookies.get('child_name') || 'Your Child';
        this.req_id = this.$route.query.req_id;

        if (this.req_id) {
            try {
                const token = Cookies.get('token');
                const response = await axios.get(`/api/v1/aex/res/${this.req_id}`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                const rdata = response.data;
                this.confidence = parseFloat((rdata.video_confidence * 100).toFixed(2));
                this.q10_res = parseFloat(rdata.q10_res);
                this.video_confidence = parseFloat(rdata.video_confidence);
                this.riskLevel = this.confidence > 60 ? 'High Risk' : this.confidence > 30 ? 'Moderate' : 'Low Risk';
            } catch (error) {
                console.error("Error fetching result:", error);
            }
        }
        this.loading = false;
    }
};
</script>

<template>
  <section class="result-shell">
    <div class="deco d1">⭐</div>
    <div class="deco d2">💜</div>
    <div class="deco d3">🌈</div>
    <div class="deco d4">🧸</div>

    <div class="result-card" v-if="!loading">
      <div class="card-mascot">📊</div>
      <h1>{{ childName }}'s ASD Screening Results</h1>
      <p class="subtitle">Combined analysis from questionnaire and behavioral video assessment</p>

      <!-- Score Overview -->
      <div class="score-grid">
        <div class="score-box main-score">
          <div class="score-ring">
            <svg viewBox="0 0 120 120">
              <circle cx="60" cy="60" r="52" class="ring-bg" />
              <circle cx="60" cy="60" r="52" class="ring-fill"
                :style="{ strokeDasharray: `${confidence * 3.27} 327` }" />
            </svg>
            <span class="ring-value">{{ confidence }}%</span>
          </div>
          <h3>Overall Confidence</h3>
          <span class="risk-badge" :class="riskLevel === 'High Risk' ? 'risk-high' : riskLevel === 'Moderate' ? 'risk-med' : 'risk-low'">
            {{ riskLevel }}
          </span>
        </div>

        <div class="score-box">
          <div class="mini-bar">
            <div class="mini-fill" :style="{ height: Math.max(8, q10_res * 100) + '%' }"></div>
          </div>
          <h3>📝 MCQ Score</h3>
          <span class="score-val">{{ (q10_res * 100).toFixed(1) }}%</span>
        </div>

        <div class="score-box">
          <div class="mini-bar">
            <div class="mini-fill video-fill" :style="{ height: Math.max(8, video_confidence * 100) + '%' }"></div>
          </div>
          <h3>🎬 Video Analysis</h3>
          <span class="score-val">{{ (video_confidence * 100).toFixed(1) }}%</span>
        </div>
      </div>

      <!-- Recommendations -->
      <div class="recommendations">
        <h2>🩺 What's Next?</h2>
        <div class="rec-grid">
          <router-link to="/dashboard/doctors" class="rec-card" style="--accent:#FF6B6B">
            <span class="rec-emoji">👨‍⚕️</span>
            <h4>Consult a Specialist</h4>
            <p>Find ASD specialists near you</p>
          </router-link>

          <router-link to="/dashboard/therapy" class="rec-card" style="--accent:#4ECDC4">
            <span class="rec-emoji">🧘</span>
            <h4>Start Therapy</h4>
            <p>Guided activities to strengthen skills</p>
          </router-link>

          <router-link to="/dashboard/game" class="rec-card" style="--accent:#A855F7">
            <span class="rec-emoji">🎮</span>
            <h4>Play Games</h4>
            <p>Fun games designed for development</p>
          </router-link>

          <router-link to="/dashboard/progress" class="rec-card" style="--accent:#FFD93D">
            <span class="rec-emoji">📈</span>
            <h4>Track Progress</h4>
            <p>Monitor improvement over time</p>
          </router-link>
        </div>
      </div>

      <button class="retake-btn" @click="$router.push('/dashboard/aex/q')">🔄 Retake Assessment</button>
    </div>

    <div v-else class="loading-state">
      <div class="loading-spinner">⏳</div>
      <p>Loading results...</p>
    </div>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.result-shell {
  width: 100%;
  min-height: calc(100vh - 120px);
  padding: 20px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.deco { position: fixed; font-size: 2rem; opacity: 0.12; animation: floatDeco 6s ease-in-out infinite; pointer-events: none; z-index: 0; }
.d1 { top: 8%; right: 5%; }
.d2 { top: 40%; right: 12%; animation-delay: 1.5s; }
.d3 { bottom: 12%; left: 5%; animation-delay: 3s; }
.d4 { top: 18%; left: 8%; animation-delay: 4.5s; }
@keyframes floatDeco { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-14px)} }

.result-card {
  width: 100%;
  max-width: 800px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  border: 2px solid rgba(255, 255, 255, 0.7);
  border-radius: 28px;
  box-shadow: 0 20px 50px rgba(100, 50, 200, 0.1);
  padding: 32px 28px;
  position: relative;
  z-index: 1;
  text-align: center;
  animation: cardPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes cardPop {
  from { opacity: 0; transform: scale(0.95) translateY(16px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.card-mascot { font-size: 2.8rem; margin-bottom: 6px; animation: bounce 2s ease-in-out infinite; }
@keyframes bounce { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-6px)} }

.result-card h1 { margin: 0; color: #1A0B3E; font-weight: 800; font-size: 1.5rem; }
.subtitle { margin: 6px 0 24px; color: #5A4690; }

/* Score Grid */
.score-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 28px;
}
.score-box {
  background: rgba(240, 230, 255, 0.4);
  border-radius: 20px;
  padding: 20px 16px;
  border: 1.5px solid rgba(212, 204, 255, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.score-box h3 { margin: 0; font-size: 0.88rem; color: #1A0B3E; font-weight: 700; }

/* Circular ring */
.score-ring {
  position: relative;
  width: 110px; height: 110px;
}
.score-ring svg { width: 100%; height: 100%; transform: rotate(-90deg); }
.ring-bg { fill: none; stroke: #E8E0FF; stroke-width: 10; }
.ring-fill { fill: none; stroke: #6C63FF; stroke-width: 10; stroke-linecap: round; transition: stroke-dasharray 1s ease; }
.ring-value {
  position: absolute; inset: 0; display: grid; place-items: center;
  font-size: 1.4rem; font-weight: 800; color: #1A0B3E;
}

.risk-badge {
  padding: 5px 16px;
  border-radius: 50px;
  font-size: 0.78rem;
  font-weight: 700;
}
.risk-high { background: #FFE0E0; color: #b91c1c; border: 1.5px solid #FF6B6B; }
.risk-med { background: #FFF6D4; color: #a16207; border: 1.5px solid #FFD93D; }
.risk-low { background: #D4F5E0; color: #16a34a; border: 1.5px solid #22c55e; }

/* Mini bars */
.mini-bar {
  width: 40px; height: 90px;
  background: #E8E0FF;
  border-radius: 20px;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
}
.mini-fill {
  width: 100%;
  background: linear-gradient(to top, #6C63FF, #A855F7);
  border-radius: 20px;
  transition: height 1s ease;
  min-height: 8px;
}
.mini-fill.video-fill {
  background: linear-gradient(to top, #FF6B6B, #FF85A1);
}
.score-val { font-size: 1.1rem; font-weight: 800; color: #6C63FF; }

/* Recommendations */
.recommendations { text-align: left; margin-bottom: 24px; }
.recommendations h2 { margin: 0 0 14px; color: #1A0B3E; font-weight: 800; font-size: 1.2rem; }
.rec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}
.rec-card {
  text-decoration: none; color: inherit;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 18px;
  padding: 16px;
  border: 2px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 6px 20px rgba(100, 50, 200, 0.06);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}
.rec-card::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
  background: var(--accent);
}
.rec-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(100, 50, 200, 0.12); border-color: var(--accent); }
.rec-emoji { font-size: 1.8rem; }
.rec-card h4 { margin: 6px 0 4px; color: #1A0B3E; font-weight: 700; font-size: 0.92rem; }
.rec-card p { margin: 0; color: #5A4690; font-size: 0.8rem; line-height: 1.4; }

.retake-btn {
  border: 2px solid #D4CCFF;
  background: rgba(255, 255, 255, 0.8);
  color: #6C63FF;
  padding: 12px 28px;
  border-radius: 50px;
  font-weight: 700;
  font-family: 'Syne', sans-serif;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
}
.retake-btn:hover { border-color: #A855F7; background: #fff; transform: translateY(-2px); }

.loading-state { text-align: center; padding: 60px 20px; z-index: 1; position: relative; }
.loading-spinner { font-size: 3rem; animation: bounce 1s ease-in-out infinite; }
.loading-state p { color: #5A4690; font-weight: 600; margin-top: 10px; font-family: 'Syne', sans-serif; }

@media (max-width: 640px) {
  .result-shell { padding: 12px 8px; }
  .result-card { padding: 20px 16px; border-radius: 20px; }
  .score-grid { grid-template-columns: 1fr; }
  .rec-grid { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 400px) {
  .rec-grid { grid-template-columns: 1fr; }
}

@media (max-width: 375px) {
  .result-shell { padding: 8px 6px; }
  .result-card { padding: 14px 10px; border-radius: 16px; }
  .result-card h1 { font-size: 1.1rem; }
  .subtitle { font-size: 0.84rem; margin-bottom: 14px; }

  .score-box { padding: 12px 8px; border-radius: 14px; }
  .score-ring { width: 90px; height: 90px; }
  .ring-value { font-size: 1.05rem; }
  .mini-bar { width: 32px; height: 76px; }

  .recommendations h2 { font-size: 1rem; }
  .rec-card { padding: 12px 10px; border-radius: 14px; }
  .rec-card h4 { font-size: 0.86rem; }
  .rec-card p { font-size: 0.74rem; }

  .retake-btn {
    width: 100%;
    padding: 10px 12px;
    font-size: 0.88rem;
  }
}
</style>
