<template>
  <div class="mem-shell">
    <div class="float-deco d1">🧩</div>
    <div class="float-deco d2">💜</div>
    <div class="float-deco d3">🌟</div>
    <div class="float-deco d4">🎮</div>

    <header class="mem-header">
      <h1>🧠 Memory Match</h1>
      <p>Find all the matching pairs!</p>
    </header>

    <!-- Level & stats -->
    <div class="stats-row">
      <div class="stat-pill level-pill">Level {{ currentLevel }}</div>
      <div class="stat-pill">🔄 Moves: {{ moves }}</div>
      <div class="stat-pill">✅ {{ matchedCount }}/{{ totalPairs }}</div>
      <div class="stat-pill">⭐ {{ score }}</div>
    </div>

    <!-- Game board -->
    <div
      class="board"
      :style="{ gridTemplateColumns: `repeat(${cols}, 1fr)`, maxWidth: cols * 100 + 'px' }"
    >
      <button
        v-for="card in cards"
        :key="card.id"
        :class="['card', { flipped: card.flipped || card.matched, matched: card.matched }]"
        :disabled="card.matched"
        @click="flipCard(card)"
      >
        <div class="card-inner">
          <div class="card-front">
            <span class="q-mark">?</span>
          </div>
          <div class="card-back">
            <span class="card-sym">{{ card.symbol }}</span>
          </div>
        </div>
      </button>
    </div>

    <!-- Level complete overlay -->
    <div class="complete-overlay" v-if="allMatched">
      <div class="confetti-wrap">
        <span v-for="n in 20" :key="n" class="conf" :style="confStyle(n)"></span>
      </div>
      <div class="complete-card">
        <div class="trophy">{{ earnedStars >= 3 ? '🏆' : '⭐' }}</div>
        <h2>Level {{ currentLevel }} Complete!</h2>
        <p>{{ moves }} moves &middot; {{ score }} points</p>
        <div class="stars-row">
          <span v-for="s in 3" :key="s" :class="['star', { earned: s <= earnedStars }]">⭐</span>
        </div>
        <div class="complete-btns">
          <button class="btn-retry" @click="resetLevel">🔄 Retry</button>
          <button v-if="currentLevel < levels.length" class="btn-next" @click="goNextLevel">➡️ Next Level</button>
          <button v-else class="btn-next" @click="currentLevel = 1; resetLevel()">🎉 Play Again!</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const LEVELS = [
  { label: '1', cols: 4, rows: 3, symbols: ['🐶', '🐱', '🐸', '🦁', '🐧', '🐼'] },
  { label: '2', cols: 4, rows: 4, symbols: ['🐶', '🐱', '🐸', '🦁', '🐧', '🐼', '🦊', '🐯'] },
  { label: '3', cols: 5, rows: 4, symbols: ['🚗', '🚀', '✈️', '🛸', '🚂', '🏎️', '🚁', '⛵', '🎠', '🛺'] },
  { label: '4', cols: 6, rows: 4, symbols: ['🍎', '🍌', '🍇', '🍉', '🍕', '🍪', '🧁', '🍩', '🌮', '🍓', '🥕', '🍒'] },
  { label: '5', cols: 6, rows: 5, symbols: ['😊', '😎', '🥳', '😴', '🤩', '😇', '🤗', '🤠', '🥰', '😺', '🦄', '🌈', '🎈', '🎵', '💎'] },
];

export default {
  name: 'MemoryMatchGame',
  data() {
    return {
      levels: LEVELS,
      currentLevel: 1,
      cards: [],
      opened: [],
      moves: 0,
      score: 0,
      lockBoard: false,
    };
  },
  computed: {
    levelConfig() { return this.levels[this.currentLevel - 1]; },
    cols() { return this.levelConfig.cols; },
    totalPairs() { return this.levelConfig.symbols.length; },
    matchedCount() { return this.cards.filter(c => c.matched).length / 2; },
    allMatched() { return this.cards.length > 0 && this.cards.every(c => c.matched); },
    earnedStars() {
      const perfect = this.totalPairs;
      if (this.moves <= perfect + 2) return 3;
      if (this.moves <= perfect * 2) return 2;
      return 1;
    },
  },
  watch: {
    currentLevel() { this.resetLevel(); },
  },
  mounted() {
    this.resetLevel();
  },
  methods: {
    resetLevel() {
      const syms = this.levelConfig.symbols;
      const deck = [...syms, ...syms]
        .sort(() => Math.random() - 0.5)
        .map((symbol, id) => ({ id, symbol, flipped: false, matched: false }));
      this.cards = deck;
      this.opened = [];
      this.moves = 0;
      this.score = this.currentLevel > 1 ? this.score : 0;
      this.lockBoard = false;
    },
    flipCard(card) {
      if (this.lockBoard || card.flipped || card.matched) return;
      if (this.opened.length === 2) return;
      card.flipped = true;
      this.opened.push(card);

      if (this.opened.length === 2) {
        this.moves++;
        this.lockBoard = true;
        const [a, b] = this.opened;
        if (a.symbol === b.symbol) {
          a.matched = true;
          b.matched = true;
          this.score += 10;
          this.opened = [];
          this.lockBoard = false;
        } else {
          setTimeout(() => {
            a.flipped = false;
            b.flipped = false;
            this.opened = [];
            this.lockBoard = false;
          }, 700);
        }
      }
    },
    goNextLevel() {
      this.currentLevel++;
    },
    confStyle(n) {
      return {
        left: Math.random() * 100 + '%',
        animationDelay: Math.random() * 2 + 's',
        background: `hsl(${(n * 41) % 360}, 80%, 60%)`,
      };
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.mem-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 5s ease-in-out infinite; pointer-events: none; }
.d1 { top: 5%; right: 6%; } .d2 { top: 40%; left: 3%; animation-delay: 1.2s; }
.d3 { bottom: 20%; right: 5%; animation-delay: 2.2s; } .d4 { bottom: 38%; left: 6%; animation-delay: 0.7s; }
@keyframes floatBob { 0%,100%{transform:translateY(0) rotate(0)} 50%{transform:translateY(-14px) rotate(5deg)} }

.mem-header { text-align: center; margin-bottom: 14px; }
.mem-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.mem-header p { margin: 4px 0 0; color: #5A4690; }

/* Stats */
.stats-row { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; margin-bottom: 20px; }
.stat-pill {
  background: rgba(255,255,255,0.8); backdrop-filter: blur(6px);
  border: 2px solid #D4CCFF; border-radius: 50px; padding: 6px 16px;
  font-weight: 700; color: #3D2B7A; font-size: 0.88rem;
}
.level-pill { background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; border-color: transparent; }

/* Board */
.board {
  display: grid; gap: 10px; margin: 0 auto; padding: 10px;
}
.card {
  aspect-ratio: 1; border: none; border-radius: 16px; cursor: pointer;
  perspective: 600px; background: transparent; padding: 0;
}
.card-inner {
  position: relative; width: 100%; height: 100%;
  transition: transform 0.5s cubic-bezier(0.34,1.56,0.64,1);
  transform-style: preserve-3d;
}
.card.flipped .card-inner { transform: rotateY(180deg); }
.card.matched .card-inner { transform: rotateY(180deg); }
.card.matched { animation: matchPop 0.4s ease; }
@keyframes matchPop { 0%{transform:scale(1)} 50%{transform:scale(1.12)} 100%{transform:scale(1)} }

.card-front, .card-back {
  position: absolute; inset: 0; backface-visibility: hidden;
  border-radius: 16px; display: flex; align-items: center; justify-content: center;
}
.card-front {
  background: linear-gradient(135deg, #7C73E6, #A78BFA);
  border: 3px solid #D4CCFF;
}
.q-mark { font-size: 2rem; color: rgba(255,255,255,0.7); font-weight: 800; }
.card-back {
  background: rgba(255,255,255,0.92); backdrop-filter: blur(6px);
  border: 3px solid #D4CCFF; transform: rotateY(180deg);
}
.card-sym { font-size: 2.2rem; }
.card.matched .card-back { border-color: #22c55e; background: rgba(34,197,94,0.08); }

/* Complete */
.complete-overlay {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.4); backdrop-filter: blur(4px); z-index: 10;
}
.complete-card {
  background: rgba(255,255,255,0.95); backdrop-filter: blur(12px);
  border: 2px solid #D4CCFF; border-radius: 28px; padding: 32px 44px;
  text-align: center; z-index: 2; animation: cardIn 0.5s ease;
}
@keyframes cardIn { from{opacity:0;transform:translateY(20px) scale(0.95)} to{opacity:1;transform:translateY(0) scale(1)} }
.trophy { font-size: 3.5rem; margin-bottom: 6px; }
.complete-card h2 { color: #1A0B3E; font-weight: 800; margin: 0 0 6px; }
.complete-card p { color: #5A4690; }
.stars-row { display: flex; justify-content: center; gap: 8px; margin: 12px 0; }
.star { font-size: 2rem; opacity: 0.2; transition: all 0.3s ease; }
.star.earned { opacity: 1; animation: starPop 0.4s ease; }
@keyframes starPop { 0%{transform:scale(0)} 60%{transform:scale(1.3)} 100%{transform:scale(1)} }

.complete-btns { display: flex; gap: 12px; justify-content: center; margin-top: 14px; }
.btn-retry, .btn-next {
  border: none; border-radius: 50px; padding: 11px 24px; font-family: 'Syne', sans-serif;
  font-weight: 700; cursor: pointer; transition: all 0.3s ease;
}
.btn-retry { background: rgba(108,99,255,0.12); color: #6C63FF; }
.btn-next { background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; box-shadow: 0 5px 18px rgba(108,99,255,0.3); }
.btn-next:hover { transform: translateY(-2px); }

/* Confetti */
.confetti-wrap { position: absolute; inset: 0; overflow: hidden; pointer-events: none; }
.conf {
  position: absolute; top: -12px; width: 10px; height: 10px; border-radius: 50%;
  animation: confDrop 3s ease-in forwards;
}
@keyframes confDrop { to{transform:translateY(600px) rotate(720deg); opacity:0} }
</style>
