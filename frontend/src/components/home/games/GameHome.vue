<template>
  <section class="game-shell">
    <!-- Floating decorations -->
    <div class="float-deco fd1">🎈</div>
    <div class="float-deco fd2">🌟</div>
    <div class="float-deco fd3">🦋</div>
    <div class="float-deco fd4">🎪</div>
    <div class="float-deco fd5">🌈</div>

    <header>
      <div class="hero-mascot">🤹</div>
      <h1>🎮 Game Zone!</h1>
      <div class="speech-bubble">
        <p>Pick a game and let's have some fun together! 🎉</p>
        <div class="bubble-tail"></div>
      </div>
      <div class="age-badge">
        <span>{{ ageGroupLabel === 'Not selected' ? '🔓' : '🎯' }}</span>
        Access Group: <strong>{{ ageGroupLabel }}</strong>
      </div>
    </header>

    <div class="featured-strip">
      <div class="featured-text">
        <strong>Today&apos;s Challenge:</strong> {{ dailyChallenge }}
      </div>
      <button class="shuffle-btn" @click="shuffleChallenge">🎲 New Challenge</button>
    </div>

    <div class="filter-row">
      <button
        v-for="cat in categories"
        :key="cat"
        class="filter-chip"
        :class="{ active: activeCategory === cat }"
        @click="activeCategory = cat"
      >{{ cat }}</button>
    </div>

    <div class="game-grid">
      <router-link to="/dashboard/game/snake" class="game-card" style="--accent:#FF6B6B;--accent-light:#FFE0E0">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🐍</div>
        <h3>Snake Game</h3>
        <p>Guide the hungry snake to eat yummy fruits! Be quick! 🍎</p>
        <span class="badge">3-15 Years</span>
        <div class="difficulty"><span class="diff-dot active"></span><span class="diff-dot active"></span><span class="diff-dot"></span></div>
      </router-link>

      <router-link to="/dashboard/game/tapgame" class="game-card" style="--accent:#4ECDC4;--accent-light:#D8F5F2">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">👆</div>
        <h3>Tap the Ball</h3>
        <p>Pop the bouncy balls before they fly away! So fun! 🎈</p>
        <span class="badge">0-3 Years</span>
        <div class="difficulty"><span class="diff-dot active"></span><span class="diff-dot"></span><span class="diff-dot"></span></div>
      </router-link>

      <router-link
        to="/dashboard/game/memory-match"
        class="game-card"
        style="--accent:#45B7D1;--accent-light:#D4F0F8"
        :class="{ locked: ageGroupCode === '0-3' }"
        @click="guardRoute($event, ageGroupCode === '0-3', 'Memory Match is available for 3-15 years group')"
      >
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🃏</div>
        <h3>Memory Match</h3>
        <p>Flip cards and find matching pairs! Can you remember? 🧠</p>
        <span class="badge">{{ ageGroupCode === '0-3' ? '🔒 Locked' : '3-15 Years' }}</span>
        <div class="difficulty"><span class="diff-dot active"></span><span class="diff-dot active"></span><span class="diff-dot"></span></div>
      </router-link>

      <router-link
        to="/dashboard/game/emotion-match"
        class="game-card"
        style="--accent:#FFD93D;--accent-light:#FFF6D4"
        :class="{ locked: ageGroupCode === '0-3' }"
        @click="guardRoute($event, ageGroupCode === '0-3', 'Emotion Match is available for 3-15 years group')"
      >
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">😊</div>
        <h3>Emotion Match</h3>
        <p>How does the bunny feel? Match the faces to find out! 🐰</p>
        <span class="badge">{{ ageGroupCode === '0-3' ? '🔒 Locked' : '3-15 Years' }}</span>
        <div class="difficulty"><span class="diff-dot active"></span><span class="diff-dot active"></span><span class="diff-dot"></span></div>
      </router-link>

      <router-link
        to="/dashboard/game/flashcard"
        class="game-card"
        style="--accent:#A855F7;--accent-light:#F0E6FF"
        :class="{ locked: ageGroupCode === '0-3' }"
        @click="guardRoute($event, ageGroupCode === '0-3', 'Flashcard Quest is available for 3-15 years group')"
      >
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🎴</div>
        <h3>Flashcard Quest</h3>
        <p>Match words to pictures and learn new things! 📚</p>
        <span class="badge">{{ ageGroupCode === '0-3' ? '🔒 Locked' : '3-15 Years' }}</span>
        <div class="difficulty"><span class="diff-dot active"></span><span class="diff-dot active"></span><span class="diff-dot active"></span></div>
      </router-link>

      <router-link
        to="/dashboard/game/sudoku-lite"
        class="game-card"
        style="--accent:#6C63FF;--accent-light:#E0DDFF"
        :class="{ locked: ageGroupCode !== '3-15' }"
        @click="guardRoute($event, ageGroupCode !== '3-15', 'Sudoku Lite is available for 3-15 years group')"
      >
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🧩</div>
        <h3>Sudoku Lite</h3>
        <p>Fill in the grid puzzle — every number counts! 🔢</p>
        <span class="badge">{{ ageGroupCode === '3-15' ? '3-15 Years' : '🔒 Locked' }}</span>
        <div class="difficulty"><span class="diff-dot active"></span><span class="diff-dot active"></span><span class="diff-dot active"></span></div>
      </router-link>

      <router-link to="/dashboard/game/musical-rhythm" class="game-card" style="--accent:#FF85A1;--accent-light:#FFE0E8">
        <div class="card-sparkle">✨</div>
        <div class="card-emoji">🎵</div>
        <h3>Musical Rhythm</h3>
        <p>Listen to the melody and repeat the colors! 🎶🌈</p>
        <span class="badge">0-15 Years</span>
        <div class="difficulty"><span class="diff-dot active"></span><span class="diff-dot"></span><span class="diff-dot"></span></div>
      </router-link>
    </div>

    <p v-if="infoMessage" class="info-msg">💡 {{ infoMessage }}</p>
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
      categories: ['All', 'Quick', 'Brain', 'Creative'],
      activeCategory: 'All',
      dailyChallenges: [
        'Play 3 rounds of Musical Rhythm without mistakes',
        'Score 20+ in Tap the Ball',
        'Complete one Memory Match board',
        'Practice 10 moves in Snake Game',
      ],
      dailyChallenge: 'Play 3 rounds of Musical Rhythm without mistakes',
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
    shuffleChallenge() {
      const prev = this.dailyChallenge;
      let next = prev;
      if (this.dailyChallenges.length === 1) return;
      while (next === prev) {
        next = this.dailyChallenges[Math.floor(Math.random() * this.dailyChallenges.length)];
      }
      this.dailyChallenge = next;
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.game-shell {
  width: 100%;
  min-height: calc(100vh - 120px);
  padding: 20px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

/* Floating decorations */
.float-deco { position: fixed; font-size: 2rem; opacity: 0.12; pointer-events: none; animation: floatDeco 6s ease-in-out infinite; z-index: 0; }
.fd1 { top: 5%; right: 8%; }
.fd2 { top: 30%; left: 5%; animation-delay: 1s; }
.fd3 { bottom: 20%; right: 4%; animation-delay: 2s; }
.fd4 { bottom: 8%; left: 10%; animation-delay: 3s; }
.fd5 { top: 50%; right: 15%; animation-delay: 4s; }
@keyframes floatDeco { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-16px)} }

/* Header with mascot */
header { text-align: center; position: relative; z-index: 1; margin-bottom: 8px; }
.hero-mascot { font-size: 3.5rem; animation: heroJuggle 2s ease-in-out infinite; }
@keyframes heroJuggle { 0%,100%{transform:rotate(0deg) scale(1)} 25%{transform:rotate(-8deg) scale(1.05)} 75%{transform:rotate(8deg) scale(1.05)} }

header h1 {
  margin: 4px 0 0;
  color: #1A0B3E;
  font-weight: 800;
  font-size: 1.7rem;
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
  max-width: 400px;
  box-shadow: 0 4px 16px rgba(100,50,200,0.06);
}
.speech-bubble p { margin: 0; color: #5A4690; font-size: 0.92rem; }
.bubble-tail {
  position: absolute; top: -10px; left: 50%; transform: translateX(-50%);
  width: 0; height: 0;
  border-left: 10px solid transparent; border-right: 10px solid transparent;
  border-bottom: 10px solid #D4CCFF;
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

.featured-strip {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  background: rgba(255, 255, 255, 0.86);
  border: 2px solid #d4ccff;
  border-radius: 16px;
  padding: 10px 14px;
  margin-bottom: 10px;
}
.featured-text {
  color: #3d2b7a;
  font-size: 0.9rem;
  font-weight: 600;
}
.shuffle-btn {
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  padding: 8px 14px;
  cursor: pointer;
}

.filter-row {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.filter-chip {
  border: 2px solid #d4ccff;
  background: rgba(255,255,255,0.85);
  color: #5a4690;
  border-radius: 999px;
  padding: 6px 12px;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  cursor: pointer;
}
.filter-chip.active {
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  border-color: transparent;
}

.game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  position: relative;
  z-index: 1;
}

.game-card {
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
.game-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 5px;
  background: var(--accent);
}

.game-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 18px 40px rgba(100, 50, 200, 0.15);
  border-color: var(--accent);
}
.game-card:hover .card-sparkle { opacity: 1; }

.game-card.locked {
  opacity: 0.55;
  filter: grayscale(0.4);
}
.game-card.locked:hover {
  transform: none;
  box-shadow: 0 10px 30px rgba(100, 50, 200, 0.08);
}

/* Sparkle on hover */
.card-sparkle {
  position: absolute; top: 10px; right: 14px; font-size: 1.2rem;
  opacity: 0; transition: opacity 0.3s; animation: sparkleRotate 2s linear infinite;
}
@keyframes sparkleRotate { 0%{transform:rotate(0deg) scale(1)} 50%{transform:rotate(180deg) scale(1.3)} 100%{transform:rotate(360deg) scale(1)} }

.card-emoji {
  font-size: 2.6rem;
  margin-bottom: 8px;
  animation: emojiFloat 3s ease-in-out infinite;
}
@keyframes emojiFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.game-card h3 {
  margin: 0;
  color: #1A0B3E;
  font-weight: 700;
  font-size: 1.1rem;
}

.game-card p {
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

/* Difficulty indicator */
.difficulty { display: flex; gap: 4px; margin-top: 8px; }
.diff-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #E0DDFF; transition: background 0.3s;
}
.diff-dot.active { background: var(--accent); }

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
  .game-shell { padding: 16px 12px; }
  .game-grid { grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; }
}
@media (max-width: 480px) {
  .game-shell { padding: 12px 8px; }
  header h1 { font-size: 1.3rem; }
  .hero-mascot { font-size: 2.5rem; }
  .game-grid { grid-template-columns: 1fr; }
  .game-card { padding: 18px; }
}
</style>
