<template>
  <div class="breath-shell">
    <div class="float-deco d1">☁️</div>
    <div class="float-deco d2">🌸</div>
    <div class="float-deco d3">🌙</div>
    <div class="float-deco d4">💫</div>

    <header class="breath-header">
      <h1>🌬️ Breathing Buddy</h1>
      <p>Follow the balloon to calm your body and mind!</p>
    </header>

    <!-- Pattern selector -->
    <div class="pattern-row">
      <button
        v-for="p in patterns"
        :key="p.name"
        :class="['pattern-chip', { active: selectedPattern === p.name }]"
        @click="selectPattern(p)"
      >{{ p.emoji }} {{ p.name }}</button>
    </div>

    <!-- Breathing circle stage -->
    <div class="stage">
      <div class="breath-glow" :class="[phaseSlug, { active: running }]"></div>
      <div class="breath-circle" :class="[phaseSlug, { active: running }]">
        <div class="inner-face">
          <div class="face-emoji">{{ faceEmoji }}</div>
          <div class="phase-label">{{ phase }}</div>
          <div class="countdown-num">{{ countdown }}</div>
        </div>
      </div>
      <div class="guide-text">{{ guideText }}</div>
    </div>

    <!-- Stats row -->
    <div class="stats-row" v-if="cyclesDone > 0 || running">
      <div class="stat-pill">🔄 Cycles: {{ cyclesDone }}</div>
      <div class="stat-pill">⏱️ {{ formatTime(totalSeconds) }}</div>
    </div>

    <!-- Controls -->
    <div class="controls">
      <button v-if="!running" class="btn-start" @click="start">▶️ Start Breathing</button>
      <button v-else class="btn-stop" @click="stop">⏸️ Pause</button>
    </div>

    <!-- Encouragement after cycles -->
    <div class="encourage" v-if="cyclesDone >= 3 && !running">
      <div class="enc-card">
        <span class="enc-emoji">🌟</span>
        <p>Amazing! You did {{ cyclesDone }} breathing cycles! Your body is nice and relaxed now.</p>
      </div>
    </div>
  </div>
</template>

<script>
const PATTERNS = [
  { name: 'Calm', emoji: '🌊', phases: [{ label: 'Breathe In', dur: 4 }, { label: 'Hold', dur: 4 }, { label: 'Breathe Out', dur: 4 }] },
  { name: 'Relaxing', emoji: '😌', phases: [{ label: 'Breathe In', dur: 4 }, { label: 'Hold', dur: 7 }, { label: 'Breathe Out', dur: 8 }] },
  { name: 'Energize', emoji: '⚡', phases: [{ label: 'Breathe In', dur: 2 }, { label: 'Breathe Out', dur: 2 }] },
  { name: 'Sleep', emoji: '😴', phases: [{ label: 'Breathe In', dur: 4 }, { label: 'Hold', dur: 4 }, { label: 'Breathe Out', dur: 6 }, { label: 'Hold', dur: 2 }] },
];

export default {
  name: 'BreathingExercise',
  data() {
    return {
      patterns: PATTERNS,
      selectedPattern: 'Calm',
      phaseIndex: 0,
      countdown: 4,
      running: false,
      timer: null,
      cyclesDone: 0,
      totalSeconds: 0,
      secondsTimer: null,
    };
  },
  computed: {
    currentPattern() { return this.patterns.find(p => p.name === this.selectedPattern); },
    phases() { return this.currentPattern.phases; },
    phase() { return this.phases[this.phaseIndex]?.label || 'Ready'; },
    phaseSlug() {
      const p = this.phase.toLowerCase().replace(/\s+/g, '-');
      return `phase-${p}`;
    },
    faceEmoji() {
      if (!this.running) return '😊';
      switch (this.phase) {
        case 'Breathe In': return '😤';
        case 'Hold': return '🫣';
        case 'Breathe Out': return '😌';
        default: return '😊';
      }
    },
    guideText() {
      if (!this.running) return 'Press start and follow the balloon!';
      switch (this.phase) {
        case 'Breathe In': return 'Slowly breathe in through your nose... 🌬️';
        case 'Hold': return 'Hold it gently... 🤫';
        case 'Breathe Out': return 'Slowly breathe out through your mouth... 💨';
        default: return '';
      }
    },
  },
  methods: {
    selectPattern(p) {
      this.selectedPattern = p.name;
      this.stop();
    },
    start() {
      this.running = true;
      this.phaseIndex = 0;
      this.countdown = this.phases[0].dur;
      this.totalSeconds = 0;
      this.cyclesDone = 0;
      this.secondsTimer = setInterval(() => { this.totalSeconds++; }, 1000);
      this.tick();
    },
    tick() {
      this.timer = setInterval(() => {
        if (this.countdown > 1) {
          this.countdown--;
          return;
        }
        // Next phase
        this.phaseIndex++;
        if (this.phaseIndex >= this.phases.length) {
          this.phaseIndex = 0;
          this.cyclesDone++;
        }
        this.countdown = this.phases[this.phaseIndex].dur;
      }, 1000);
    },
    stop() {
      this.running = false;
      clearInterval(this.timer);
      clearInterval(this.secondsTimer);
      this.timer = null;
      this.secondsTimer = null;
      this.phaseIndex = 0;
      this.countdown = this.phases[0].dur;
    },
    formatTime(s) {
      const m = Math.floor(s / 60);
      const sec = s % 60;
      return `${m}:${sec.toString().padStart(2, '0')}`;
    },
  },
  beforeUnmount() {
    clearInterval(this.timer);
    clearInterval(this.secondsTimer);
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.breath-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #E8F4FD 0%, #F0E6FF 50%, #FFF5E4 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 6s ease-in-out infinite; pointer-events: none; }
.d1 { top: 8%; right: 8%; } .d2 { top: 30%; left: 4%; animation-delay: 1.5s; }
.d3 { bottom: 20%; right: 6%; animation-delay: 3s; } .d4 { bottom: 40%; left: 7%; animation-delay: 0.8s; }
@keyframes floatBob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.breath-header { text-align: center; margin-bottom: 14px; }
.breath-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.breath-header p { margin: 6px 0 0; color: #5A4690; }

/* Pattern selector */
.pattern-row { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; margin-bottom: 20px; }
.pattern-chip {
  border: 2px solid #D4CCFF; border-radius: 50px; padding: 8px 16px;
  background: rgba(255,255,255,0.8); font-family: 'Syne', sans-serif;
  font-weight: 600; color: #5A4690; cursor: pointer; transition: all 0.25s ease;
}
.pattern-chip.active { background: linear-gradient(135deg,#6C63FF,#A855F7); color:#fff; border-color:transparent; }

/* Stage */
.stage { display: flex; flex-direction: column; align-items: center; gap: 16px; margin-bottom: 20px; }

.breath-glow {
  position: absolute; width: 260px; height: 260px; border-radius: 50%; filter: blur(40px); opacity: 0;
  transition: all 1s ease;
}
.breath-glow.active { opacity: 0.3; }
.breath-glow.phase-breathe-in { background: #93C5FD; transform: scale(1.3); }
.breath-glow.phase-hold { background: #C4B5FD; transform: scale(1.15); }
.breath-glow.phase-breathe-out { background: #86EFAC; transform: scale(0.9); }

.breath-circle {
  width: 220px; height: 220px; border-radius: 50%;
  background: linear-gradient(135deg, #A78BFA, #7C73E6);
  display: flex; align-items: center; justify-content: center;
  transition: all 1.5s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 40px rgba(108,99,255,0.25);
}
.breath-circle.active.phase-breathe-in {
  transform: scale(1.35); background: linear-gradient(135deg, #93C5FD, #6C63FF);
  box-shadow: 0 15px 50px rgba(108,99,255,0.35);
}
.breath-circle.active.phase-hold {
  transform: scale(1.15); background: linear-gradient(135deg, #C4B5FD, #A855F7);
}
.breath-circle.active.phase-breathe-out {
  transform: scale(0.85); background: linear-gradient(135deg, #86EFAC, #22c55e);
  box-shadow: 0 8px 30px rgba(34,197,94,0.25);
}

.inner-face { text-align: center; color: #fff; }
.face-emoji { font-size: 3rem; margin-bottom: 2px; }
.phase-label { font-weight: 700; font-size: 1.1rem; text-shadow: 0 2px 8px rgba(0,0,0,0.15); }
.countdown-num { font-size: 2rem; font-weight: 800; margin-top: 2px; }

.guide-text { color: #5A4690; font-weight: 600; font-size: 1rem; text-align: center; min-height: 24px; }

/* Stats */
.stats-row { display: flex; gap: 10px; justify-content: center; margin-bottom: 16px; }
.stat-pill {
  background: rgba(255,255,255,0.8); border: 2px solid #D4CCFF; border-radius: 50px;
  padding: 6px 16px; font-weight: 700; color: #3D2B7A; font-size: 0.88rem;
}

/* Controls */
.controls { text-align: center; margin-bottom: 20px; }
.btn-start, .btn-stop {
  border: none; border-radius: 50px; padding: 14px 36px;
  font-family: 'Syne', sans-serif; font-weight: 700; font-size: 1.1rem;
  cursor: pointer; transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1);
}
.btn-start {
  background: linear-gradient(135deg,#6C63FF,#A855F7); color:#fff;
  box-shadow: 0 6px 22px rgba(108,99,255,0.3);
}
.btn-start:hover { transform: translateY(-3px) scale(1.04); }
.btn-stop {
  background: linear-gradient(135deg,#FF6B6B,#FF4466); color:#fff;
  box-shadow: 0 6px 22px rgba(255,107,107,0.3);
}

/* Encouragement */
.encourage { display: flex; justify-content: center; }
.enc-card {
  background: rgba(255,255,255,0.9); backdrop-filter: blur(8px);
  border: 2px solid #D4CCFF; border-radius: 20px; padding: 20px 28px;
  text-align: center; max-width: 400px; animation: cardIn 0.5s ease;
}
@keyframes cardIn { from{opacity:0;transform:translateY(12px)} to{opacity:1;transform:translateY(0)} }
.enc-emoji { font-size: 2.5rem; }
.enc-card p { color: #3D2B7A; font-weight: 600; margin: 8px 0 0; }
</style>
