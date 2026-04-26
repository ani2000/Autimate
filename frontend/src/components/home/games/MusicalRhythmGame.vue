<template>
  <section class="music-wrap">
    <div class="float-deco d1">🎵</div>
    <div class="float-deco d2">🎶</div>
    <div class="float-deco d3">🌟</div>
    <div class="float-deco d4">🎸</div>

    <header class="game-header">
      <h1>🎹 Musical Rhythm!</h1>
      <p>Watch the lights, remember the tune, then tap them back!</p>
    </header>

    <!-- Level / Score bar -->
    <div class="stats-bar">
      <div class="stat-pill level-pill">
        <span class="stat-emoji">🏆</span>
        <span>Level <strong>{{ level }}</strong></span>
      </div>
      <div class="stat-pill">
        <span class="stat-emoji">⭐</span>
        <span>Score <strong>{{ score }}</strong></span>
      </div>
      <div class="stat-pill">
        <span class="stat-emoji">🔥</span>
        <span>Best <strong>{{ best }}</strong></span>
      </div>
      <div class="stat-pill streak" v-if="streak >= 3">
        <span class="stat-emoji">💥</span>
        <span>{{ streak }} streak!</span>
      </div>
    </div>

    <!-- Level progress dots -->
    <div class="level-progress">
      <div
        v-for="i in sequence.length"
        :key="i"
        class="prog-dot"
        :class="{ filled: userInput.length >= i, current: userInput.length === i - 1 && !showingSequence && playing }"
      ></div>
    </div>

    <!-- Pads grid -->
    <div class="pads" :class="padGridClass">
      <button
        v-for="(pad, i) in activePads"
        :key="i"
        class="pad"
        :class="{ lit: litIndex === i, wrong: wrongIndex === i, success: successFlash === i }"
        :style="{ '--pad-color': pad.color, '--pad-glow': pad.glow }"
        @click="tapPad(i)"
        :disabled="inputLocked"
      >
        <span class="pad-emoji">{{ pad.emoji }}</span>
        <span class="pad-label">{{ pad.label }}</span>
      </button>
    </div>

    <!-- Controls -->
    <div class="controls">
      <button v-if="!playing" class="start-btn" @click="startGame">
        {{ gameOver ? '🔄 Play Again!' : '🎮 Start!' }}
      </button>
      <div v-if="gameMessage" class="msg-bubble" :class="msgType">
        <span>{{ gameMessage }}</span>
      </div>
    </div>

    <!-- Combo / celebration -->
    <div class="celebration" v-if="showCelebration">
      <div class="confetti" v-for="n in 12" :key="n" :style="confettiStyle(n)">🎉</div>
      <div class="celeb-text">{{ celebrationText }}</div>
    </div>

    <!-- How to play -->
    <div class="how-card">
      <h4>🎯 How to play</h4>
      <div class="how-steps">
        <div class="how-step"><span class="step-num">1</span><span>Press <strong>Start</strong> and watch the pads light up 👀</span></div>
        <div class="how-step"><span class="step-num">2</span><span>Listen to the melody carefully 🎧</span></div>
        <div class="how-step"><span class="step-num">3</span><span>Tap the same pads in the same order 🎹</span></div>
        <div class="how-step"><span class="step-num">4</span><span>Each level adds one more note! Can you keep up? 🚀</span></div>
      </div>
    </div>
  </section>
</template>

<script>
const ALL_PADS = [
  { color: '#ef4444', glow: '#fca5a5', label: 'Do', emoji: '🔴', freq: 262 },
  { color: '#f59e0b', glow: '#fcd34d', label: 'Re', emoji: '🟡', freq: 294 },
  { color: '#22c55e', glow: '#86efac', label: 'Mi', emoji: '🟢', freq: 330 },
  { color: '#3b82f6', glow: '#93c5fd', label: 'Fa', emoji: '🔵', freq: 349 },
  { color: '#a855f7', glow: '#d8b4fe', label: 'Sol', emoji: '🟣', freq: 392 },
  { color: '#ec4899', glow: '#f9a8d4', label: 'La', emoji: '🩷', freq: 440 },
  { color: '#14b8a6', glow: '#99f6e4', label: 'Ti', emoji: '🩵', freq: 494 },
  { color: '#f97316', glow: '#fdba74', label: 'Do+', emoji: '🟠', freq: 523 },
];

export default {
  data() {
    return {
      sequence: [],
      userInput: [],
      level: 1,
      score: 0,
      streak: 0,
      best: Number(localStorage.getItem('musical_best') || 0),
      playing: false,
      gameOver: false,
      showingSequence: false,
      inputLocked: true,
      litIndex: -1,
      wrongIndex: -1,
      successFlash: -1,
      gameMessage: '',
      msgType: '',
      showCelebration: false,
      celebrationText: '',
      audioCtx: null,
      processingTap: false,
      lastTapAt: 0,
      lastTapIndex: -1,
    };
  },
  computed: {
    padCount() {
      if (this.level <= 3) return 4;
      if (this.level <= 6) return 6;
      return 8;
    },
    activePads() {
      return ALL_PADS.slice(0, this.padCount);
    },
    padGridClass() {
      if (this.padCount <= 4) return 'grid-2x2';
      if (this.padCount <= 6) return 'grid-3x2';
      return 'grid-4x2';
    },
  },
  methods: {
    startGame() {
      this.sequence = [];
      this.userInput = [];
      this.level = 1;
      this.score = 0;
      this.streak = 0;
      this.gameMessage = '';
      this.msgType = '';
      this.wrongIndex = -1;
      this.gameOver = false;
      this.playing = true;
      this.showCelebration = false;
      this.nextRound();
    },
    nextRound() {
      this.userInput = [];
      this.inputLocked = true;
      this.sequence.push(Math.floor(Math.random() * this.padCount));
      this.showSequence();
    },
    async showSequence() {
      this.showingSequence = true;
      this.gameMessage = '👀 Watch carefully...';
      this.msgType = 'info';
      const delay = Math.max(250, 500 - this.level * 20);
      const gap = Math.max(100, 200 - this.level * 10);
      await this.sleep(500);
      for (let i = 0; i < this.sequence.length; i++) {
        this.litIndex = this.sequence[i];
        this.playTone(this.sequence[i]);
        await this.sleep(delay);
        this.litIndex = -1;
        await this.sleep(gap);
      }
      this.showingSequence = false;
      this.inputLocked = false;
      this.gameMessage = '🎹 Your turn! Tap the pads!';
      this.msgType = 'action';
    },
    tapPad(index) {
      if (this.inputLocked || !this.playing || this.processingTap) return;
      if (this.userInput.length >= this.sequence.length) return;

      const now = Date.now();
      if (this.lastTapIndex === index && now - this.lastTapAt < 120) return;
      this.lastTapAt = now;
      this.lastTapIndex = index;
      this.processingTap = true;

      this.playTone(index);

      const pos = this.userInput.length;
      const expected = this.sequence[pos];
      if (index !== expected) {
        this.wrongIndex = index;
        this.inputLocked = true;
        this.processingTap = false;
        this.streak = 0;
        const messages = [
          `Oops! 😅 You reached level ${this.level}!`,
          `So close! 💪 Score: ${this.score}`,
          `Nice try! 🌟 Level ${this.level} is tricky!`,
        ];
        this.gameMessage = messages[Math.floor(Math.random() * messages.length)];
        this.msgType = 'fail';
        this.playing = false;
        this.gameOver = true;
        if (this.score > this.best) {
          this.best = this.score;
          localStorage.setItem('musical_best', String(this.best));
          this.gameMessage += ' NEW BEST! 🏆';
        }
        setTimeout(() => { this.wrongIndex = -1; }, 600);
        return;
      }

      this.successFlash = index;
      setTimeout(() => { this.successFlash = -1; }, 200);
      this.userInput.push(index);
      this.processingTap = false;

      if (this.userInput.length === this.sequence.length) {
        this.inputLocked = true;
        const levelPoints = this.level * 10;
        const streakBonus = this.streak >= 3 ? this.streak * 5 : 0;
        this.score += levelPoints + streakBonus;
        this.streak += 1;
        this.level += 1;

        const cheers = ['Amazing! 🎉', 'Awesome! 🌟', 'Super! 🚀', 'Brilliant! 💎', 'Wow! 🎸'];
        this.gameMessage = cheers[Math.floor(Math.random() * cheers.length)];
        this.msgType = 'success';

        if (this.level % 3 === 1 && this.level > 1) {
          this.celebrate(this.level <= 4 ? 'New pads unlocked! 🎹' : 'You\'re a music star! ⭐');
        }

        setTimeout(() => this.nextRound(), 1200);
      }
    },
    playTone(index) {
      try {
        if (!this.audioCtx) {
          this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        const ctx = this.audioCtx;
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.frequency.value = ALL_PADS[index].freq;
        osc.type = this.level > 6 ? 'triangle' : 'sine';
        gain.gain.setValueAtTime(0.35, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.3);
        osc.start(ctx.currentTime);
        osc.stop(ctx.currentTime + 0.3);
      } catch { /* silent fallback */ }
    },
    celebrate(text) {
      this.celebrationText = text;
      this.showCelebration = true;
      setTimeout(() => { this.showCelebration = false; }, 2000);
    },
    confettiStyle(n) {
      const x = Math.random() * 100;
      const delay = Math.random() * 0.5;
      const dur = 1 + Math.random();
      return {
        left: x + '%',
        animationDelay: delay + 's',
        animationDuration: dur + 's',
      };
    },
    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.music-wrap {
  position: relative;
  padding: 24px;
  max-width: 700px;
  margin: 0 auto;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  min-height: calc(100vh - 120px);
  overflow: hidden;
}

/* Floating decorations */
.float-deco { position: absolute; font-size: 2rem; opacity: 0.15; animation: floatBob 4s ease-in-out infinite; pointer-events: none; }
.d1 { top: 10%; right: 8%; animation-delay: 0s; }
.d2 { top: 40%; left: 5%; animation-delay: 1s; }
.d3 { bottom: 15%; right: 12%; animation-delay: 2s; }
.d4 { bottom: 30%; left: 10%; animation-delay: 0.5s; }
@keyframes floatBob {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-14px) rotate(8deg); }
}

.game-header { text-align: center; margin-bottom: 18px; }
.game-header h1 { margin: 0; color: #1A0B3E; font-weight: 800; font-size: 1.8rem; }
.game-header p { margin: 6px 0 0; color: #5A4690; font-size: 1rem; }

/* Stats bar */
.stats-bar {
  display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; margin-bottom: 14px;
}
.stat-pill {
  display: flex; align-items: center; gap: 6px;
  background: rgba(255,255,255,0.85); backdrop-filter: blur(8px);
  border: 2px solid #E0D8FF; border-radius: 50px;
  padding: 6px 16px; font-weight: 600; color: #3D2B7A; font-size: 0.92rem;
}
.stat-pill strong { color: #6C63FF; }
.stat-emoji { font-size: 1.1rem; }
.streak { background: linear-gradient(135deg, #FFD93D, #FF6B6B); color: #fff; border-color: transparent; animation: pulse 0.6s ease-in-out infinite; }
.streak strong { color: #fff; }
@keyframes pulse { 0%,100%{transform:scale(1)} 50%{transform:scale(1.06)} }

/* Level progress dots */
.level-progress { display: flex; gap: 6px; justify-content: center; margin-bottom: 18px; }
.prog-dot {
  width: 14px; height: 14px; border-radius: 50%;
  background: rgba(108,99,255,0.15); border: 2px solid #D4CCFF;
  transition: all 0.3s ease;
}
.prog-dot.filled { background: #6C63FF; border-color: #6C63FF; transform: scale(1.1); }
.prog-dot.current { border-color: #A855F7; animation: dotPulse 0.8s ease-in-out infinite; }
@keyframes dotPulse { 0%,100%{box-shadow:0 0 0 0 rgba(168,85,247,0.4)} 50%{box-shadow:0 0 0 6px rgba(168,85,247,0)} }

/* Pads grid */
.pads { display: grid; gap: 14px; max-width: 420px; margin: 0 auto 18px; }
.grid-2x2 { grid-template-columns: 1fr 1fr; }
.grid-3x2 { grid-template-columns: 1fr 1fr 1fr; }
.grid-4x2 { grid-template-columns: 1fr 1fr 1fr 1fr; }

.pad {
  position: relative; height: 110px; border: none; border-radius: 20px;
  background: var(--pad-color); color: #fff;
  font-family: 'Syne', sans-serif; font-weight: 800;
  cursor: pointer; transition: all 0.15s ease;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.15), inset 0 -4px 0 rgba(0,0,0,0.15);
  overflow: hidden;
}
.pad::after {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 40%;
  background: linear-gradient(180deg, rgba(255,255,255,0.25), transparent); border-radius: 20px 20px 0 0;
  pointer-events: none;
}
.pad-emoji { font-size: 1.4rem; }
.pad-label { font-size: 1rem; }
.pad:active:not(:disabled) { transform: scale(0.92); box-shadow: 0 2px 8px rgba(0,0,0,0.2); }
.pad:disabled { cursor: default; opacity: 0.7; }
.pad.lit {
  filter: brightness(1.4); transform: scale(1.1);
  box-shadow: 0 0 30px var(--pad-glow), 0 0 60px var(--pad-glow);
}
.pad.wrong { animation: shake 0.4s ease; filter: grayscale(0.8) brightness(0.5); }
.pad.success { filter: brightness(1.3); transform: scale(1.06); }
@keyframes shake { 0%,100%{transform:translateX(0)} 20%{transform:translateX(-8px)} 40%{transform:translateX(8px)} 60%{transform:translateX(-5px)} 80%{transform:translateX(5px)} }

/* Controls */
.controls { text-align: center; margin-bottom: 20px; }
.start-btn {
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff; border: none; border-radius: 50px;
  padding: 14px 40px; font-weight: 700; font-size: 1.15rem;
  font-family: 'Syne', sans-serif; cursor: pointer;
  box-shadow: 0 8px 28px rgba(108,99,255,0.35);
  transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1);
}
.start-btn:hover { transform: translateY(-3px) scale(1.05); box-shadow: 0 14px 40px rgba(108,99,255,0.45); }

.msg-bubble {
  display: inline-block; padding: 10px 24px; border-radius: 50px;
  font-weight: 700; font-size: 1rem; margin-top: 12px;
  animation: bubblePop 0.3s ease;
}
.msg-bubble.info { background: rgba(108,99,255,0.1); color: #6C63FF; }
.msg-bubble.action { background: rgba(34,197,94,0.1); color: #16a34a; }
.msg-bubble.success { background: rgba(34,197,94,0.15); color: #15803d; }
.msg-bubble.fail { background: rgba(239,68,68,0.1); color: #dc2626; }
@keyframes bubblePop { from{opacity:0;transform:scale(0.8)} to{opacity:1;transform:scale(1)} }

/* Celebration */
.celebration { position: fixed; top: 0; left: 0; right: 0; bottom: 0; pointer-events: none; z-index: 100; }
.confetti { position: absolute; top: -20px; font-size: 1.5rem; animation: confettiFall linear forwards; }
@keyframes confettiFall { 0%{transform:translateY(0) rotate(0deg);opacity:1} 100%{transform:translateY(100vh) rotate(720deg);opacity:0} }
.celeb-text {
  position: absolute; top: 40%; left: 50%; transform: translate(-50%,-50%);
  background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff;
  padding: 18px 40px; border-radius: 24px; font-weight: 800; font-size: 1.4rem;
  box-shadow: 0 10px 40px rgba(108,99,255,0.4); animation: celebBounce 0.5s ease;
}
@keyframes celebBounce { 0%{transform:translate(-50%,-50%) scale(0)} 50%{transform:translate(-50%,-50%) scale(1.15)} 100%{transform:translate(-50%,-50%) scale(1)} }

/* How to play card */
.how-card {
  background: rgba(255,255,255,0.8); backdrop-filter: blur(8px);
  border: 2px solid #E0D8FF; border-radius: 20px; padding: 18px 24px;
}
.how-card h4 { margin: 0 0 12px; color: #1A0B3E; font-weight: 700; font-size: 1.05rem; }
.how-steps { display: flex; flex-direction: column; gap: 8px; }
.how-step {
  display: flex; align-items: center; gap: 12px;
  color: #5A4690; font-size: 0.95rem;
}
.step-num {
  flex-shrink: 0; width: 28px; height: 28px; border-radius: 50%;
  background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff;
  display: grid; place-items: center; font-weight: 800; font-size: 0.85rem;
}
</style>
