<template>
  <section class="therapy-shell">
    <div class="deco d1">🌈</div>
    <div class="deco d2">💜</div>
    <div class="deco d3">🧸</div>
    <div class="deco d4">✨</div>

    <header>
      <div class="hero-mascot">🧚</div>
      <h1>🩺 Therapy Activities</h1>
      <div class="speech-bubble">
        <p>Choose an activity and let's grow stronger together! 💪🌟</p>
        <div class="bubble-tail"></div>
      </div>
      <div class="age-badge">👶 {{ ageGroupLabel }}</div>
    </header>

    <div class="spotlight-row">
      <div class="spotlight-card">
        <div class="spot-title">Today&apos;s Therapy Spotlight</div>
        <div class="spot-value">{{ spotlightActivity }}</div>
      </div>
      <button class="spot-btn" @click="spinSpotlight">🔄 Change</button>
    </div>

    <div class="therapy-grid">
      <router-link to="/dashboard/therapy/socialskill" class="therapy-card" style="--accent:#FF6B6B;--accent-light:#FFE0E0">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🤝</div>
        <h3>Learn Social Skills</h3>
        <p>Fun stories about making friends and being kind! 💛</p>
        <span class="badge">3-15 Years</span>
      </router-link>

      <router-link to="/dashboard/therapy/drawing" class="therapy-card" style="--accent:#4ECDC4;--accent-light:#D4F5F2">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🎨</div>
        <h3>Drawing</h3>
        <p>Draw, color, and create your own masterpiece! 🖌️</p>
        <span class="badge">0-15 Years</span>
      </router-link>

      <router-link to="/dashboard/therapy/funalp" class="therapy-card" style="--accent:#45B7D1;--accent-light:#D6F0F7">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🔤</div>
        <h3>Learn Alphabet</h3>
        <p>A is for Apple, B is for Butterfly! Let's learn! 🍎</p>
        <span class="badge">0-15 Years</span>
      </router-link>

      <router-link to="/dashboard/therapy/breathing" class="therapy-card" style="--accent:#A855F7;--accent-light:#F0E6FF">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🌬️</div>
        <h3>Breathing Exercise</h3>
        <p>Breathe in like a flower, out like a candle! 🕯️🌸</p>
        <span class="badge">0-3 Years</span>
      </router-link>

      <router-link to="/dashboard/therapy/sensory-break" class="therapy-card" style="--accent:#FFD93D;--accent-light:#FFF8D6">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🧘</div>
        <h3>Sensory Break Guide</h3>
        <p>Feeling overwhelmed? Let's find our calm place! 🏖️</p>
        <span class="badge">0-15 Years</span>
      </router-link>

      <router-link
        to="/dashboard/therapy/speech-coach"
        class="therapy-card"
        :class="{ locked: ageGroupCode === '0-3' }"
        style="--accent:#6C63FF;--accent-light:#E0DEFF"
        @click="guardRoute($event, ageGroupCode === '0-3', 'Speech Coach is available for 3-15 years group')"
      >
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🗣️</div>
        <h3>Speech Coach</h3>
        <p>Practice words and sounds with your fun speech buddy! 🎤</p>
        <span class="badge">{{ ageGroupCode === '0-3' ? '🔒 Locked (3-15)' : '3-15 Years' }}</span>
      </router-link>

      <router-link
        to="/dashboard/therapy/routine-planner"
        class="therapy-card"
        :class="{ locked: ageGroupCode === '0-3' }"
        style="--accent:#FF85A1;--accent-light:#FFE0E8"
        @click="guardRoute($event, ageGroupCode === '0-3', 'Routine Planner is available for 3-15 years group')"
      >
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">📅</div>
        <h3>Routine Planner</h3>
        <p>Plan your day with stickers and checklists! ✅⭐</p>
        <span class="badge">{{ ageGroupCode === '0-3' ? '🔒 Locked (3-15)' : '3-15 Years' }}</span>
      </router-link>

      <router-link to="/dashboard/therapy/color-therapy" class="therapy-card" style="--accent:#F97316;--accent-light:#FFEDD5">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🌈</div>
        <h3>Color Therapy</h3>
        <p>Splash calming colors and paint your feelings! 🎨💜</p>
        <span class="badge">0-15 Years</span>
      </router-link>

      <router-link to="/dashboard/therapy/talking-tom" class="therapy-card" style="--accent:#9B8CC4;--accent-light:#F0E6FF">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🐱</div>
        <h3>Talking Tom</h3>
        <p>Talk to Tom and hear him repeat in a silly voice! 😹</p>
        <span class="badge">0-15 Years</span>
      </router-link>
    </div>

    <p v-if="infoMessage" class="info-msg">{{ infoMessage }}</p>
  </section>
</template>

<script>
import Cookies from 'js-cookie';

export default {
  data() {
    return {
      ageGroupCode: '',
      ageGroupLabel: 'Not selected',
      infoMessage: '',
      spotlightPool: [
        'Try 2 minutes of Breathing Exercise',
        'Play Talking Tom and repeat 5 words',
        'Draw your feeling in Drawing Therapy',
        'Use Color Therapy for calm time',
      ],
      spotlightActivity: 'Try 2 minutes of Breathing Exercise',
    };
  },
  mounted() {
    const stored = Cookies.get('child_age_group') || '';
    this.ageGroupLabel = stored || 'Not selected';
    this.ageGroupCode = stored.includes('0') ? '0-3' : (stored.includes('3-15') || stored.includes('15')) ? '3-15' : '';
  },
  methods: {
    guardRoute(event, isLocked, message) {
      if (isLocked) {
        event.preventDefault();
        this.infoMessage = message;
      } else {
        this.infoMessage = '';
      }
    },
    spinSpotlight() {
      const prev = this.spotlightActivity;
      let next = prev;
      if (this.spotlightPool.length === 1) return;
      while (next === prev) {
        next = this.spotlightPool[Math.floor(Math.random() * this.spotlightPool.length)];
      }
      this.spotlightActivity = next;
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.therapy-shell {
  width: 100%;
  min-height: calc(100vh - 120px);
  padding: 20px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
}

.deco { position: fixed; font-size: 2rem; opacity: 0.15; animation: floatDeco 6s ease-in-out infinite; pointer-events: none; z-index: 0; }
.d1 { top: 10%; right: 5%; animation-delay: 0s; }
.d2 { top: 40%; right: 12%; animation-delay: 1.5s; }
.d3 { bottom: 15%; left: 5%; animation-delay: 3s; }
.d4 { top: 20%; left: 10%; animation-delay: 4.5s; }
@keyframes floatDeco { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-18px)} }

header { position: relative; z-index: 1; text-align: center; }
.hero-mascot { font-size: 3.5rem; animation: fairyFloat 2.5s ease-in-out infinite; }
@keyframes fairyFloat { 0%,100%{transform:translateY(0) scale(1)} 50%{transform:translateY(-8px) scale(1.05)} }

header h1 {
  margin: 4px 0 0;
  color: #1A0B3E;
  font-weight: 800;
  font-size: 1.6rem;
}

/* Speech bubble */
.speech-bubble {
  display: inline-block;
  background: rgba(255,255,255,0.9);
  border: 2px solid #D4CCFF;
  border-radius: 20px;
  padding: 10px 20px;
  margin: 8px auto 12px;
  position: relative;
  max-width: 420px;
  box-shadow: 0 4px 16px rgba(100,50,200,0.06);
}
.speech-bubble p { margin: 0; color: #5A4690; font-size: 0.92rem; }
.bubble-tail {
  position: absolute; top: -10px; left: 50%; transform: translateX(-50%);
  width: 0; height: 0;
  border-left: 10px solid transparent; border-right: 10px solid transparent;
  border-bottom: 10px solid #D4CCFF;
}

header p {
  margin: 6px 0 12px;
  color: #5A4690;
}

.age-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(108, 99, 255, 0.08);
  border: 1.5px solid #D4CCFF;
  border-radius: 50px;
  padding: 6px 16px;
  color: #3D2B7A;
  font-size: 0.88rem;
  margin-bottom: 16px;
}

.spotlight-row {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  background: rgba(255,255,255,0.88);
  border: 2px solid #d4ccff;
  border-radius: 16px;
  padding: 10px 14px;
  margin-bottom: 12px;
}
.spotlight-card { color: #3D2B7A; }
.spot-title { font-size: 0.8rem; font-weight: 700; opacity: 0.8; }
.spot-value { font-size: 0.95rem; font-weight: 700; }
.spot-btn {
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  padding: 8px 14px;
  cursor: pointer;
}

.therapy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  position: relative;
  z-index: 1;
}

.therapy-card {
  text-decoration: none;
  color: inherit;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border-radius: 24px;
  padding: 22px;
  border: 2px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 10px 30px rgba(100, 50, 200, 0.08);
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}
.therapy-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 5px;
  background: var(--accent);
}

.therapy-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 18px 40px rgba(100, 50, 200, 0.15);
  border-color: var(--accent);
}
.therapy-card:hover .card-sparkle { opacity: 1; }

/* Sparkle on hover */
.card-sparkle {
  position: absolute; top: 10px; right: 14px; font-size: 1.2rem;
  opacity: 0; transition: opacity 0.3s; animation: sparkleRotate 2s linear infinite;
}
@keyframes sparkleRotate { 0%{transform:rotate(0deg) scale(1)} 50%{transform:rotate(180deg) scale(1.3)} 100%{transform:rotate(360deg) scale(1)} }

.therapy-card.locked {
  opacity: 0.55;
  filter: grayscale(0.4);
}
.therapy-card.locked:hover {
  transform: none;
  box-shadow: 0 10px 30px rgba(100, 50, 200, 0.08);
}

.card-emoji {
  font-size: 2.4rem;
  margin-bottom: 8px;
  animation: emojiFloat 3s ease-in-out infinite;
}
@keyframes emojiFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.therapy-card h3 {
  margin: 0;
  color: #1A0B3E;
  font-weight: 700;
  font-size: 1.1rem;
}

.therapy-card p {
  margin: 6px 0 10px;
  color: #5A4690;
  line-height: 1.55;
  font-size: 0.9rem;
}

.badge {
  display: inline-block;
  border-radius: 50px;
  background: var(--accent-light);
  color: var(--accent);
  padding: 5px 12px;
  font-size: 0.78rem;
  font-weight: 700;
  border: 1.5px solid var(--accent);
}

.info-msg {
  margin-top: 16px;
  color: #6C63FF;
  font-weight: 600;
  text-align: center;
  padding: 10px 16px;
  background: rgba(108, 99, 255, 0.06);
  border-radius: 16px;
  position: relative;
  z-index: 1;
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .therapy-shell { padding: 16px 12px; }
  .therapy-grid { grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; }
}
@media (max-width: 480px) {
  .therapy-shell { padding: 12px 8px; }
  header h1 { font-size: 1.3rem; }
  .therapy-grid { grid-template-columns: 1fr; }
  .therapy-card { padding: 18px; }
}
</style>
