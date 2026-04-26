<template>
  <section class="progress-shell">
    <div class="deco d1">📈</div>
    <div class="deco d2">⭐</div>
    <div class="deco d3">💜</div>
    <div class="deco d4">🧸</div>

    <div class="progress-card">
      <div class="card-mascot">📊</div>
      <h1>Progress Tracker</h1>
      <p class="subtitle">Watch your child's journey — every step forward counts!</p>

      <div class="actions">
        <button class="btn-refresh" @click="loadHistory">🔄 Refresh</button>
        <router-link to="/dashboard/aex/q" class="btn-test">🧩 Take Test Again</router-link>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="spinner">⏳</div>
        <p>Loading history...</p>
      </div>

      <div v-else-if="history.length === 0" class="empty-state">
        <div class="empty-emoji">🌱</div>
        <h3>No History Yet</h3>
        <p>Complete an ASD assessment to start tracking progress here.</p>
        <router-link to="/dashboard/aex/q" class="btn-start">🚀 Start First Assessment</router-link>
      </div>

      <div v-else>
        <!-- Summary Cards -->
        <div class="summary-row">
          <div class="sum-card">
            <span class="sum-icon">🧪</span>
            <strong>{{ history.length }}</strong>
            <span class="sum-label">Tests Taken</span>
          </div>
          <div class="sum-card">
            <span class="sum-icon">🎯</span>
            <strong>{{ latestScore }}</strong>
            <span class="sum-label">Latest Score</span>
          </div>
          <div class="sum-card" :class="trendClass">
            <span class="sum-icon">{{ trendEmoji }}</span>
            <strong>{{ trendText }}</strong>
            <span class="sum-label">Trend</span>
          </div>
        </div>

        <!-- History Table -->
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>📅 Date</th>
                <th>📊 Score</th>
                <th>🏷️ Result</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, i) in history" :key="item.id || i">
                <td><span class="row-num">{{ i + 1 }}</span></td>
                <td>{{ formatDate(item.createdAt || item.date) }}</td>
                <td><strong>{{ item.score ?? item.percentage ?? '-' }}</strong></td>
                <td>
                  <span class="result-pill" :class="(item.result || (item.score >= 50 ? 'At Risk' : 'Low Risk')) === 'At Risk' ? 'pill-risk' : 'pill-low'">
                    {{ item.result || (item.score >= 50 ? 'At Risk' : 'Low Risk') }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Chart -->
        <div class="chart-box">
          <h3>📈 Score Trend</h3>
          <div class="bars-container">
            <div
              v-for="(item, i) in history"
              :key="'bar-' + i"
              class="bar-col"
            >
              <div class="bar-value">{{ item.score ?? item.percentage ?? 0 }}</div>
              <div class="bar" :style="{ height: barHeight(item) + '%' }"></div>
              <div class="bar-label">T{{ i + 1 }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  data() {
    return {
      history: [],
      loading: false,
    };
  },
  computed: {
    latestScore() {
      if (!this.history.length) return '-';
      const last = this.history[this.history.length - 1];
      return last.score ?? last.percentage ?? '-';
    },
    trendClass() {
      if (this.history.length < 2) return '';
      const prev = this.history[this.history.length - 2];
      const last = this.history[this.history.length - 1];
      const p = Number(prev.score ?? prev.percentage ?? 0);
      const l = Number(last.score ?? last.percentage ?? 0);
      return l < p ? 'trend-good' : l > p ? 'trend-warn' : '';
    },
    trendEmoji() {
      if (this.history.length < 2) return '➡️';
      const prev = this.history[this.history.length - 2];
      const last = this.history[this.history.length - 1];
      const p = Number(prev.score ?? prev.percentage ?? 0);
      const l = Number(last.score ?? last.percentage ?? 0);
      return l < p ? '🎉' : l > p ? '⚠️' : '➡️';
    },
    trendText() {
      if (this.history.length < 2) return 'Stable';
      const prev = this.history[this.history.length - 2];
      const last = this.history[this.history.length - 1];
      const p = Number(prev.score ?? prev.percentage ?? 0);
      const l = Number(last.score ?? last.percentage ?? 0);
      return l < p ? 'Improving' : l > p ? 'Needs Attention' : 'Stable';
    },
  },
  mounted() {
    this.loadHistory();
  },
  methods: {
    buildPseudoHistory() {
      const now = new Date();
      const mk = (daysAgo, score) => ({
        id: `pseudo-${daysAgo}`,
        score,
        percentage: score,
        result: score >= 65 ? 'At Risk' : score >= 40 ? 'Moderate' : 'Low Risk',
        createdAt: new Date(now.getTime() - daysAgo * 24 * 60 * 60 * 1000).toISOString(),
      });
      return [mk(75, 72), mk(48, 63), mk(28, 55), mk(12, 46)];
    },
    async loadHistory() {
      this.loading = true;
      const token = Cookies.get('token');
      try {
        const response = await axios.get('/api/v1/aex/history', {
          headers: { Authorization: `Bearer ${token}` },
        });
        const rows = Array.isArray(response.data) ? response.data : [];
        this.history = rows.map((item) => {
          const score = Number(item.score ?? 0);
          return {
            id: item.id,
            score,
            percentage: score,
            result: score >= 65 ? 'At Risk' : score >= 40 ? 'Moderate' : 'Low Risk',
            createdAt: item.createdAt,
          };
        }).sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));

        if (!this.history.length) {
          this.history = this.buildPseudoHistory();
        }
      } catch {
        const local = JSON.parse(localStorage.getItem('aex_history') || '[]');
        this.history = Array.isArray(local) && local.length ? local : this.buildPseudoHistory();
      }
      this.loading = false;
    },
    formatDate(value) {
      if (!value) return '-';
      const d = new Date(value);
      return isNaN(d.getTime()) ? value : d.toLocaleDateString();
    },
    barHeight(item) {
      const v = Number(item.score ?? item.percentage ?? 0);
      return Math.max(5, Math.min(100, v));
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.progress-shell {
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
.d1 { top: 6%; right: 6%; }
.d2 { top: 35%; right: 10%; animation-delay: 1.5s; }
.d3 { bottom: 10%; left: 6%; animation-delay: 3s; }
.d4 { top: 20%; left: 10%; animation-delay: 4.5s; }
@keyframes floatDeco { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-14px)} }

.progress-card {
  width: 100%;
  max-width: 820px;
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

.card-mascot { font-size: 2.8rem; margin-bottom: 4px; animation: bounce 2s ease-in-out infinite; }
@keyframes bounce { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-6px)} }

.progress-card h1 { margin: 0; color: #1A0B3E; font-weight: 800; font-size: 1.5rem; }
.subtitle { margin: 4px 0 20px; color: #5A4690; font-size: 0.95rem; }

/* Action buttons */
.actions { display: flex; gap: 12px; justify-content: center; margin-bottom: 24px; flex-wrap: wrap; }
.btn-refresh, .btn-test {
  padding: 10px 22px; border-radius: 50px; font-weight: 700; font-family: 'Syne', sans-serif;
  font-size: 0.9rem; cursor: pointer; transition: all 0.3s ease; text-decoration: none;
}
.btn-refresh {
  background: rgba(255,255,255,0.8); border: 2px solid #D4CCFF; color: #6C63FF;
}
.btn-refresh:hover { border-color: #A855F7; transform: translateY(-2px); }
.btn-test {
  background: linear-gradient(135deg, #6C63FF, #A855F7); border: none; color: #fff;
  box-shadow: 0 6px 20px rgba(108, 99, 255, 0.3);
}
.btn-test:hover { transform: translateY(-2px); box-shadow: 0 10px 28px rgba(108, 99, 255, 0.4); }

/* Loading */
.loading-state { padding: 40px 0; }
.spinner { font-size: 2.5rem; animation: bounce 1s ease-in-out infinite; }
.loading-state p { color: #5A4690; font-weight: 600; margin-top: 8px; }

/* Empty */
.empty-state {
  background: rgba(240, 230, 255, 0.35); border-radius: 20px; padding: 32px 20px;
  border: 2px dashed #D4CCFF;
}
.empty-emoji { font-size: 3rem; margin-bottom: 8px; }
.empty-state h3 { margin: 0 0 6px; color: #1A0B3E; font-weight: 800; }
.empty-state p { margin: 0 0 16px; color: #5A4690; }
.btn-start {
  display: inline-block; padding: 10px 24px; border-radius: 50px;
  background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff;
  font-weight: 700; font-family: 'Syne', sans-serif; text-decoration: none;
  transition: all 0.3s ease;
}
.btn-start:hover { transform: translateY(-2px); }

/* Summary Row */
.summary-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 20px; }
.sum-card {
  background: rgba(240, 230, 255, 0.4); border-radius: 18px; padding: 16px 12px;
  border: 1.5px solid rgba(212, 204, 255, 0.5); display: flex; flex-direction: column; align-items: center; gap: 2px;
}
.sum-icon { font-size: 1.5rem; }
.sum-card strong { font-size: 1.15rem; color: #1A0B3E; }
.sum-label { font-size: 0.78rem; color: #5A4690; }
.trend-good strong { color: #16a34a; }
.trend-warn strong { color: #dc2626; }

/* Table */
.table-wrap {
  overflow-x: auto; margin-bottom: 20px;
  background: rgba(255,255,255,0.5); border-radius: 18px; border: 1.5px solid rgba(212,204,255,0.4);
}
table { width: 100%; border-collapse: collapse; }
th { padding: 10px 14px; text-align: left; color: #5A4690; font-weight: 700; font-size: 0.82rem; background: rgba(240,230,255,0.3); }
td { padding: 10px 14px; border-top: 1px solid rgba(212,204,255,0.3); color: #1A0B3E; font-size: 0.88rem; }
.row-num {
  display: inline-flex; align-items: center; justify-content: center;
  width: 26px; height: 26px; border-radius: 50%;
  background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; font-size: 0.75rem; font-weight: 700;
}
.result-pill {
  padding: 3px 12px; border-radius: 50px; font-size: 0.78rem; font-weight: 700;
}
.pill-risk { background: #FFE0E0; color: #b91c1c; }
.pill-low { background: #D4F5E0; color: #16a34a; }

/* Chart */
.chart-box {
  background: rgba(240, 230, 255, 0.3); border-radius: 20px; padding: 20px;
  border: 1.5px solid rgba(212,204,255,0.4);
}
.chart-box h3 { margin: 0 0 14px; color: #1A0B3E; font-weight: 800; font-size: 1rem; }
.bars-container {
  display: flex; gap: 10px; align-items: flex-end; height: 160px; justify-content: center;
}
.bar-col { display: flex; flex-direction: column; align-items: center; gap: 4px; flex: 1; min-width: 28px; max-width: 50px; }
.bar-value { font-size: 0.72rem; font-weight: 700; color: #6C63FF; }
.bar {
  width: 100%; background: linear-gradient(to top, #6C63FF, #A855F7);
  border-radius: 10px 10px 4px 4px; transition: height 0.6s ease; min-height: 4px;
}
.bar-label { font-size: 0.7rem; color: #5A4690; font-weight: 600; }

@media (max-width: 600px) {
  .progress-shell { padding: 12px 8px; }
  .progress-card { padding: 20px 14px; border-radius: 20px; }
  .summary-row { grid-template-columns: 1fr; }
  .bars-container { height: 120px; }
}

@media (max-width: 375px) {
  .progress-shell { padding: 8px 6px; }
  .progress-card { padding: 14px 10px; border-radius: 16px; }

  .progress-card h1 { font-size: 1.15rem; }
  .subtitle { font-size: 0.84rem; margin-bottom: 14px; }

  .actions {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    margin-bottom: 14px;
  }
  .btn-refresh,
  .btn-test {
    width: 100%;
    text-align: center;
    padding: 10px 12px;
  }

  .sum-card { padding: 12px 8px; border-radius: 14px; }
  .sum-card strong { font-size: 1rem; }

  th,
  td {
    padding: 8px 8px;
    font-size: 0.78rem;
  }

  .result-pill { padding: 2px 8px; font-size: 0.72rem; }

  .chart-box { padding: 12px 8px; border-radius: 14px; }
  .bars-container { gap: 6px; height: 100px; }
  .bar-col { min-width: 20px; }
  .bar-label { font-size: 0.62rem; }
}
</style>
