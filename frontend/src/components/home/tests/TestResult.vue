<script>
import Cookies from 'js-cookie';

export default {
    data() {
        return {
            childName: 'Your Child',
            score: 66,
        };
    },
    computed: {
        advice() {
            if (this.score >= 70) return 'Good progress. Continue guided activities and follow-up checks.';
            if (this.score >= 40) return 'Moderate concern. Consider specialist consultation and home therapy routine.';
            return 'Need improvement. Book a specialist assessment soon and begin daily support activities.';
        },
        riskLabel() {
            if (this.score >= 70) return 'Low Risk';
            if (this.score >= 40) return 'Moderate Risk';
            return 'High Risk';
        },
    },
    mounted() {
        this.childName = Cookies.get('child_name') || 'Your Child';
        const fromQuery = Number(this.$route.query.score);
        if (Number.isFinite(fromQuery) && fromQuery >= 0 && fromQuery <= 100) {
            this.score = fromQuery;
        }
    },
};
</script>

<template>
    <section class="result-shell">
        <div class="deco d1">⭐</div>
        <div class="deco d2">💜</div>
        <div class="deco d3">🧸</div>

        <div class="result-card">
            <div class="mascot">📊</div>
            <h1>{{ childName }}'s Screening Score</h1>

            <div class="ring-wrap">
                <svg viewBox="0 0 120 120" class="ring">
                    <circle cx="60" cy="60" r="52" class="ring-bg" />
                    <circle cx="60" cy="60" r="52" class="ring-fill" :style="{ strokeDasharray: `${score * 3.27} 327` }" />
                </svg>
                <span class="ring-val">{{ score }}%</span>
            </div>

            <span class="risk-pill">{{ riskLabel }}</span>
            <p class="advice">{{ advice }}</p>

            <div class="actions">
                <router-link to="/dashboard/doctors" class="btn primary">👨‍⚕️ Find Nearby Doctors</router-link>
                <router-link to="/dashboard/therapy" class="btn">🧘 Start Therapy</router-link>
            </div>
        </div>
    </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.result-shell {
    width: 100%; min-height: calc(100vh - 120px); padding: 20px;
    background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
    display: flex; justify-content: center; align-items: flex-start; font-family: 'Syne', sans-serif;
    position: relative; overflow: hidden;
}
.deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatDeco 6s ease-in-out infinite; pointer-events: none; }
.d1 { top: 10%; right: 6%; } .d2 { top: 35%; left: 7%; animation-delay: 1.4s; } .d3 { bottom: 14%; right: 12%; animation-delay: 2.8s; }
@keyframes floatDeco { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.result-card {
    width: 100%; max-width: 700px; background: rgba(255,255,255,0.9); border: 2px solid rgba(255,255,255,0.7);
    backdrop-filter: blur(14px); border-radius: 28px; padding: 30px 24px; text-align: center;
    box-shadow: 0 20px 50px rgba(100, 50, 200, 0.1);
}
.mascot { font-size: 2.7rem; }
h1 { margin: 8px 0 16px; color: #1A0B3E; font-size: 1.5rem; font-weight: 800; }

.ring-wrap { position: relative; width: 130px; height: 130px; margin: 0 auto 14px; }
.ring { width: 100%; height: 100%; transform: rotate(-90deg); }
.ring-bg { fill: none; stroke: #E8E0FF; stroke-width: 10; }
.ring-fill { fill: none; stroke: #6C63FF; stroke-width: 10; stroke-linecap: round; transition: stroke-dasharray 0.8s ease; }
.ring-val {
    position: absolute; inset: 0; display: grid; place-items: center;
    color: #1A0B3E; font-weight: 800; font-size: 1.3rem;
}

.risk-pill {
    display: inline-block; padding: 6px 16px; border-radius: 999px;
    background: #E8E0FF; color: #6C63FF; font-weight: 700; font-size: 0.82rem;
}
.advice { margin: 12px auto 18px; color: #5A4690; max-width: 580px; line-height: 1.6; font-weight: 600; }

.actions { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
.btn {
    text-decoration: none; border-radius: 999px; padding: 12px 22px; font-weight: 700; font-size: 0.92rem;
    background: rgba(255,255,255,0.85); color: #6C63FF; border: 2px solid #D4CCFF;
}
.btn.primary { background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; border-color: transparent; }
</style>