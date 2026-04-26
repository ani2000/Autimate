<template>
  <div :class="['tap-shell']" :style="rootBgStyle">
    <div class="float-deco d1">🎯</div>
    <div class="float-deco d2">💫</div>
    <div class="float-deco d3">🌟</div>

    <!-- HUD -->
    <div class="hud" v-if="!gameOver">
      <div class="hud-pill level-pill">Level {{ level }}</div>
      <div class="hud-pill">⏱️ {{ timeLeft }}s</div>
      <div class="hud-pill">⭐ {{ score }}</div>
      <div class="hud-pill">🔥 {{ streak }}</div>
    </div>

    <!-- Play area -->
    <div class="play-area" ref="area" v-if="!gameOver">
      <transition-group name="pop">
        <button
          v-for="t in targets"
          :key="t.id"
          class="target"
          :style="targetStyle(t)"
          @click="tapTarget(t)"
        >
          <span class="target-emoji">{{ t.emoji }}</span>
        </button>
      </transition-group>

      <!-- Floating score popups -->
      <transition-group name="scoreUp">
        <div v-for="p in popups" :key="p.id" class="score-popup" :style="{ top: p.y + 'px', left: p.x + 'px' }">
          +{{ p.pts }}
        </div>
      </transition-group>
    </div>

    <!-- Start / Level banner -->
    <div v-if="!started && !gameOver" class="start-overlay">
      <div class="start-card">
        <h1>🎯 Tap Frenzy!</h1>
        <p>Tap the appearing targets as fast as you can!</p>
        <div class="diff-row">
          <button v-for="d in difficulties" :key="d.name" :class="['diff-btn', {active: difficulty === d.name}]" @click="difficulty = d.name">
            {{ d.emoji }} {{ d.name }}
          </button>
        </div>
        <div class="theme-row">
          <label class="theme-label">Theme:</label>
          <select v-model="selectedTheme" class="ctrl-select">
            <option v-for="t in availableThemes" :key="t.key" :value="t.key">{{ t.label }}</option>
          </select>
        </div>
        <button class="btn-start" @click="startGame">▶️ Start!</button>
      </div>
    </div>

    <!-- Game Over -->
    <div v-if="gameOver" class="gameover-overlay">
      <div class="confetti-wrap">
        <span v-for="n in 20" :key="n" class="conf" :style="confStyle(n)"></span>
      </div>
      <div class="gameover-card">
        <div class="go-emoji">{{ score >= 30 ? '🏆' : score >= 15 ? '⭐' : '💪' }}</div>
        <h2>Game Over!</h2>
        <p>Score: <strong>{{ score }}</strong> &middot; Best Streak: <strong>{{ bestStreak }}</strong></p>
        <p>Level Reached: <strong>{{ level }}</strong></p>
        <div class="go-btns">
          <button class="btn-replay" @click="resetAll">🔄 Play Again</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const EMOJIS = ['🎈', '⭐', '🌸', '🍎', '🐱', '🦋', '🍩', '🎵', '🌈', '💎', '🍉', '🚀', '🎯', '🐸', '🧁'];
const DIFFICULTIES = [
  { name: 'Easy', emoji: '🐢', interval: 2200, lifetime: 3000, maxTargets: 1 },
  { name: 'Normal', emoji: '🐇', interval: 1500, lifetime: 2200, maxTargets: 2 },
  { name: 'Hard', emoji: '🔥', interval: 900, lifetime: 1500, maxTargets: 3 },
];

let nextId = 0;

export default {
  name: 'TapGame',
  data() {
    return {
      started: false,
      gameOver: false,
      difficulty: 'Normal',
      difficulties: DIFFICULTIES,
      score: 0,
      streak: 0,
      bestStreak: 0,
      level: 1,
      timeLeft: 30,
      targets: [],
      popups: [],
      spawnTimer: null,
      tickTimer: null,
      // Theme & backgrounds
      availableThemes: [
        { key: 'default', label: 'Colorful Gradient' },
        { key: 'back1', label: 'Playful Park' },
        { key: 'back2', label: 'Cute Shapes' },
        { key: 'back3', label: 'Pastel Clouds' },
        { key: 'back4', label: 'Retro Dots' },
        { key: 'pokemon', label: 'Pokemon (drop-in)' },
        { key: 'doraemon', label: 'Doraemon (drop-in)' },
        { key: 'space', label: 'Space' },
      ],
      selectedTheme: 'back1',
      themeImages: [
        '/tapgame/back1.png',
        '/tapgame/back2.png',
        '/tapgame/back3.png',
        '/tapgame/back4.png',
        '/tapgame/theme_pokemon.png',
        '/tapgame/theme_doraemon.png',
      ],
    };
  },
  computed: {
    diffConfig() { return DIFFICULTIES.find(d => d.name === this.difficulty); },
    rootBgStyle() {
      // Use image background when available theme selected, otherwise animated gradient
      if (this.selectedTheme === 'default') return { background: 'linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%)' };
      if (['back1','back2','back3','back4'].includes(this.selectedTheme)) {
        const idx = ['back1','back2','back3','back4'].indexOf(this.selectedTheme);
        return { backgroundImage: `url(/tapgame/back${idx+1}.png)`, backgroundSize: 'cover', backgroundPosition: 'center' };
      }
      if (this.selectedTheme === 'pokemon') return { backgroundImage: `url(/tapgame/theme_pokemon.png)`, backgroundSize: 'cover', backgroundPosition: 'center' };
      if (this.selectedTheme === 'doraemon') return { backgroundImage: `url(/tapgame/theme_doraemon.png)`, backgroundSize: 'cover', backgroundPosition: 'center' };
      if (this.selectedTheme === 'space') return { background: 'radial-gradient(circle at 20% 20%, #00111a, #07142b 40%, #0b1b2a 100%)' };
      return {};
    }
  },
  methods: {
    startGame() {
      this.started = true;
      this.gameOver = false;
      this.score = 0;
      this.streak = 0;
      this.bestStreak = 0;
      this.level = 1;
      this.timeLeft = 30;
      this.targets = [];
      this.popups = [];
      // ensure selected theme images exist (non-blocking)
      this.preloadThemeImages();
      this.scheduleSpawn();
      this.tickTimer = setInterval(() => {
        this.timeLeft--;
        // Level up every 10s
        if (this.timeLeft > 0 && this.timeLeft % 10 === 0) {
          this.level++;
          // rotate theme a bit for variety
          this.rotateThemeOnLevelUp();
        }
        if (this.timeLeft <= 0) this.endGame();
      }, 1000);
    },
    scheduleSpawn() {
      const cfg = this.diffConfig;
      const interval = Math.max(400, cfg.interval - this.level * 80);
      this.spawnTimer = setTimeout(() => {
        this.spawnTarget();
        if (!this.gameOver) this.scheduleSpawn();
      }, interval);
    },
    spawnTarget() {
      const cfg = this.diffConfig;
      const maxT = cfg.maxTargets + Math.floor(this.level / 2);
      if (this.targets.length >= maxT) return;
      const area = this.$refs.area;
      if (!area) return;
      const w = area.clientWidth - 60;
      const h = area.clientHeight - 60;
      const size = Math.max(40, 65 - this.level * 3);
      const t = {
        id: nextId++,
        x: Math.random() * w + 10,
        y: Math.random() * h + 10,
        size,
        emoji: EMOJIS[Math.floor(Math.random() * EMOJIS.length)],
      };
      this.targets.push(t);
      const lifetime = Math.max(600, cfg.lifetime - this.level * 100);
      setTimeout(() => {
        const idx = this.targets.findIndex(tt => tt.id === t.id);
        if (idx !== -1) {
          this.targets.splice(idx, 1);
          this.streak = 0; // missed
        }
      }, lifetime);
    },
    tapTarget(t) {
      const idx = this.targets.findIndex(tt => tt.id === t.id);
      if (idx === -1) return;
      this.targets.splice(idx, 1);
      this.streak++;
      if (this.streak > this.bestStreak) this.bestStreak = this.streak;
      const bonus = this.streak >= 5 ? 3 : this.streak >= 3 ? 2 : 1;
      const pts = bonus * this.level;
      this.score += pts;
      this.popups.push({ id: nextId++, x: t.x, y: t.y, pts });
      setTimeout(() => this.popups.shift(), 800);
    },
    targetStyle(t) {
      return {
        position: 'absolute',
        top: t.y + 'px',
        left: t.x + 'px',
        width: t.size + 'px',
        height: t.size + 'px',
      };
    },
    endGame() {
      this.gameOver = true;
      clearTimeout(this.spawnTimer);
      clearInterval(this.tickTimer);
      this.targets = [];
    },
    resetAll() {
      this.started = false;
      this.gameOver = false;
    },
    confStyle(n) {
      return { left: Math.random() * 100 + '%', animationDelay: Math.random() * 2 + 's', background: `hsl(${(n*37)%360},80%,60%)` };
    },
    preloadThemeImages() {
      // Attempt to preload custom theme images (if present in public/tapgame)
      for (const p of this.themeImages) {
        const img = new Image();
        img.src = p;
        // no need to handle errors here — browser will 404 if missing
      }
    },
    rotateThemeOnLevelUp() {
      // rotate among back1..back4 and space for vibrancy
      const pool = ['back1','back2','back3','back4','space'];
      const next = pool[(Math.floor(Math.random() * pool.length))];
      this.selectedTheme = next;
    },
  },
  beforeUnmount() {
    clearTimeout(this.spawnTimer);
    clearInterval(this.tickTimer);
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.tap-shell {
  position: relative; width: calc(100vw - 300px); height: calc(100vh - 120px);
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.1; animation: floatBob 5s ease-in-out infinite; pointer-events: none; z-index: 0; }
.d1 { top: 4%; right: 5%; } .d2 { bottom: 10%; left: 4%; animation-delay: 1.5s; } .d3 { top: 55%; right: 8%; animation-delay: 2.5s; }
@keyframes floatBob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

/* HUD */
.hud { position: absolute; top: 12px; left: 50%; transform: translateX(-50%); display: flex; gap: 10px; z-index: 5; }
.hud-pill {
  background: rgba(255,255,255,0.85); backdrop-filter: blur(6px);
  border: 2px solid #D4CCFF; border-radius: 50px; padding: 6px 16px;
  font-weight: 700; color: #3D2B7A; font-size: 0.9rem;
}
.level-pill { background: linear-gradient(135deg,#6C63FF,#A855F7); color:#fff; border-color:transparent; }

/* Play area */
.play-area { position: absolute; inset: 60px 12px 12px 12px; }

.target {
  border: none; border-radius: 50%; cursor: pointer;
  background: rgba(255,255,255,0.85); backdrop-filter: blur(4px);
  border: 3px solid #D4CCFF; display: flex; align-items: center; justify-content: center;
  animation: targetIn 0.3s cubic-bezier(0.34,1.56,0.64,1);
  transition: transform 0.15s ease;
}
.target:hover { transform: scale(1.2); }
.target-emoji { font-size: 1.6rem; pointer-events: none; }
@keyframes targetIn { from{transform:scale(0);opacity:0} to{transform:scale(1);opacity:1} }

.pop-enter-active { animation: targetIn 0.3s ease; }
.pop-leave-active { animation: targetOut 0.2s ease forwards; }
@keyframes targetOut { to{transform:scale(0);opacity:0} }

/* Score popup */
.score-popup {
  position: absolute; color: #6C63FF; font-weight: 800; font-size: 1.2rem;
  pointer-events: none; animation: floatUp 0.8s ease forwards;
}
@keyframes floatUp { from{opacity:1;transform:translateY(0)} to{opacity:0;transform:translateY(-40px)} }
.scoreUp-enter-active { animation: floatUp 0.8s ease; }

/* Start overlay */
.start-overlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 10; }
.start-card {
  background: rgba(255,255,255,0.92); backdrop-filter: blur(12px);
  border: 2px solid #D4CCFF; border-radius: 28px; padding: 36px 44px; text-align: center;
}
.start-card h1 { color: #1A0B3E; font-weight: 800; margin: 0 0 8px; }
.start-card p { color: #5A4690; margin: 0 0 16px; }
.theme-row { margin-top: 12px; display: flex; gap: 8px; justify-content: center; align-items: center; flex-wrap: wrap; }
.theme-label { color: #5A4690; font-weight: 600; }
.ctrl-select {
  min-width: 160px;
  height: 40px;
  border: 2px solid #D4CCFF;
  border-radius: 12px;
  padding: 0 10px;
  font-family: 'Syne', sans-serif;
  color: #3D2B7A;
  background: rgba(255,255,255,0.92);
}
.diff-row { display: flex; gap: 8px; justify-content: center; margin-bottom: 18px; }
.diff-btn {
  border: 2px solid #D4CCFF; border-radius: 50px; padding: 8px 18px;
  background: rgba(255,255,255,0.8); font-family: 'Syne', sans-serif;
  font-weight: 600; color: #5A4690; cursor: pointer; transition: all 0.2s;
}
.diff-btn.active { background: linear-gradient(135deg,#6C63FF,#A855F7); color:#fff; border-color:transparent; }
.btn-start {
  border: none; border-radius: 50px; padding: 14px 36px;
  background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff;
  font-weight: 700; font-size: 1.1rem; font-family: 'Syne', sans-serif;
  cursor: pointer; box-shadow: 0 6px 22px rgba(108,99,255,0.3);
  transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1);
}
.btn-start:hover { transform: translateY(-3px) scale(1.04); }

/* Game over */
.gameover-overlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.4); backdrop-filter: blur(4px); z-index: 10; }
.gameover-card {
  background: rgba(255,255,255,0.95); backdrop-filter: blur(12px);
  border: 2px solid #D4CCFF; border-radius: 28px; padding: 32px 40px; text-align: center; z-index: 2;
  animation: cardIn 0.5s ease;
}
@keyframes cardIn { from{opacity:0;transform:translateY(16px) scale(0.95)} to{opacity:1;transform:translateY(0) scale(1)} }
.go-emoji { font-size: 3.5rem; }
.gameover-card h2 { color: #1A0B3E; font-weight: 800; margin: 6px 0; }
.gameover-card p { color: #5A4690; }
.gameover-card strong { color: #6C63FF; }
.go-btns { margin-top: 16px; }
.btn-replay {
  border: none; border-radius: 50px; padding: 12px 28px;
  background: linear-gradient(135deg,#6C63FF,#A855F7); color:#fff;
  font-weight: 700; font-family: 'Syne', sans-serif; font-size: 1rem; cursor: pointer;
  box-shadow: 0 5px 18px rgba(108,99,255,0.3); transition: all 0.3s ease;
}
.btn-replay:hover { transform: translateY(-2px); }

/* Confetti */
.confetti-wrap { position: absolute; inset: 0; overflow: hidden; pointer-events: none; }
.conf { position: absolute; top: -10px; width: 10px; height: 10px; border-radius: 50%; animation: confDrop 3s ease-in forwards; }
@keyframes confDrop { to{transform:translateY(600px) rotate(720deg);opacity:0} }

@media (max-width: 640px) {
  .theme-row { align-items: stretch; }
  .ctrl-select { min-width: 0; width: 100%; }
}
</style>

  
