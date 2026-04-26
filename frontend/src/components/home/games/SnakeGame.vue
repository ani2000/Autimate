<template>
  <section class="snake-shell">
    <div class="deco d1">🐍</div>
    <div class="deco d2">🍎</div>
    <div class="deco d3">⭐</div>

    <header class="head">
      <h1>🐍 Snake Adventure</h1>
      <p>Eat apples, grow longer, and avoid crashing!</p>
    </header>

    <div class="hud">
      <div class="pill">Score {{ score }}</div>
      <div class="pill">Best {{ bestScore }}</div>
      <div class="pill">Speed {{ speedLabel }}</div>
    </div>

    <div class="controls" v-if="!inGame">
      <button
        v-for="d in difficulties"
        :key="d.name"
        :class="['diff-btn', { active: difficulty === d.name }]"
        @click="difficulty = d.name"
      >{{ d.emoji }} {{ d.name }}</button>
    </div>

    <div class="board-wrap">
      <canvas ref="gameCanvas" :width="boardSize" :height="boardSize"></canvas>
      <div v-if="!inGame" class="overlay">
        <div class="card">
          <div class="emoji">🐍</div>
          <h3>{{ gameOver ? 'Game Over!' : 'Ready?' }}</h3>
          <p v-if="gameOver">Score {{ score }} · Best {{ bestScore }}</p>
          <button class="btn-start" @click="startGame">{{ gameOver ? '🔄 Play Again' : '▶️ Start Game' }}</button>
        </div>
      </div>
    </div>

    <div class="touch-controls" aria-label="Snake mobile controls">
      <button class="ctrl up" @click="setDirection('up')">▲</button>
      <div class="lr-row">
        <button class="ctrl" @click="setDirection('left')">◀</button>
        <button class="ctrl" @click="setDirection('right')">▶</button>
      </div>
      <button class="ctrl down" @click="setDirection('down')">▼</button>
    </div>

    <p class="hint">Use Arrow keys ⬅️➡️⬆️⬇️ to move</p>
  </section>
</template>

<script>
const DIFFICULTIES = [
  { name: 'Easy', emoji: '🐢', interval: 160 },
  { name: 'Normal', emoji: '🐇', interval: 120 },
  { name: 'Hard', emoji: '🔥', interval: 90 },
];

export default {
  name: 'SnakeGame',
  data() {
    return {
      boardSize: 520,
      cell: 20,
      snake: [{ x: 8, y: 8 }, { x: 7, y: 8 }, { x: 6, y: 8 }],
      dir: { x: 1, y: 0 },
      nextDir: { x: 1, y: 0 },
      apple: { x: 14, y: 10 },
      score: 0,
      bestScore: Number(localStorage.getItem('snake_best') || 0),
      inGame: false,
      gameOver: false,
      timer: null,
      difficulty: 'Normal',
      difficulties: DIFFICULTIES,
    };
  },
  computed: {
    cols() { return this.boardSize / this.cell; },
    speedLabel() { return this.difficulty; },
    intervalMs() {
      return DIFFICULTIES.find(d => d.name === this.difficulty)?.interval || 120;
    },
  },
  mounted() {
    window.addEventListener('keydown', this.handleKey);
    this.draw();
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.handleKey);
    clearInterval(this.timer);
  },
  methods: {
    startGame() {
      this.snake = [{ x: 8, y: 8 }, { x: 7, y: 8 }, { x: 6, y: 8 }];
      this.dir = { x: 1, y: 0 };
      this.nextDir = { x: 1, y: 0 };
      this.score = 0;
      this.gameOver = false;
      this.inGame = true;
      this.placeApple();
      clearInterval(this.timer);
      this.timer = setInterval(this.tick, this.intervalMs);
      this.draw();
    },
    handleKey(e) {
      const k = e.key;
      if (k === 'ArrowLeft' && this.dir.x !== 1) this.nextDir = { x: -1, y: 0 };
      if (k === 'ArrowRight' && this.dir.x !== -1) this.nextDir = { x: 1, y: 0 };
      if (k === 'ArrowUp' && this.dir.y !== 1) this.nextDir = { x: 0, y: -1 };
      if (k === 'ArrowDown' && this.dir.y !== -1) this.nextDir = { x: 0, y: 1 };
    },
    setDirection(dir) {
      if (dir === 'left' && this.dir.x !== 1) this.nextDir = { x: -1, y: 0 };
      if (dir === 'right' && this.dir.x !== -1) this.nextDir = { x: 1, y: 0 };
      if (dir === 'up' && this.dir.y !== 1) this.nextDir = { x: 0, y: -1 };
      if (dir === 'down' && this.dir.y !== -1) this.nextDir = { x: 0, y: 1 };
    },
    tick() {
      this.dir = { ...this.nextDir };
      const head = this.snake[0];
      const newHead = { x: head.x + this.dir.x, y: head.y + this.dir.y };

      if (newHead.x < 0 || newHead.y < 0 || newHead.x >= this.cols || newHead.y >= this.cols) {
        this.finishGame();
        return;
      }

      if (this.snake.some(p => p.x === newHead.x && p.y === newHead.y)) {
        this.finishGame();
        return;
      }

      this.snake.unshift(newHead);

      if (newHead.x === this.apple.x && newHead.y === this.apple.y) {
        this.score += 10;
        this.placeApple();
      } else {
        this.snake.pop();
      }

      this.draw();
    },
    finishGame() {
      this.inGame = false;
      this.gameOver = true;
      clearInterval(this.timer);
      if (this.score > this.bestScore) {
        this.bestScore = this.score;
        localStorage.setItem('snake_best', String(this.bestScore));
      }
      this.draw();
    },
    placeApple() {
      let x = 0;
      let y = 0;
      do {
        x = Math.floor(Math.random() * this.cols);
        y = Math.floor(Math.random() * this.cols);
      } while (this.snake.some(p => p.x === x && p.y === y));
      this.apple = { x, y };
    },
    drawGrid(ctx) {
      ctx.strokeStyle = 'rgba(255,255,255,0.08)';
      for (let i = 0; i <= this.cols; i++) {
        ctx.beginPath();
        ctx.moveTo(i * this.cell, 0);
        ctx.lineTo(i * this.cell, this.boardSize);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(0, i * this.cell);
        ctx.lineTo(this.boardSize, i * this.cell);
        ctx.stroke();
      }
    },
    draw() {
      const c = this.$refs.gameCanvas;
      if (!c) return;
      const ctx = c.getContext('2d');
      ctx.fillStyle = '#111827';
      ctx.fillRect(0, 0, this.boardSize, this.boardSize);
      this.drawGrid(ctx);

      // Apple
      ctx.fillStyle = '#ef4444';
      ctx.beginPath();
      ctx.arc(this.apple.x * this.cell + this.cell / 2, this.apple.y * this.cell + this.cell / 2, this.cell * 0.38, 0, Math.PI * 2);
      ctx.fill();

      // Snake
      this.snake.forEach((p, i) => {
        ctx.fillStyle = i === 0 ? '#22c55e' : '#86efac';
        const pad = 2;
        ctx.fillRect(p.x * this.cell + pad, p.y * this.cell + pad, this.cell - pad * 2, this.cell - pad * 2);
      });
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.snake-shell {
  position: relative;
  width: calc(100vw - 300px);
  min-height: calc(100vh - 120px);
  padding: 24px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
}
.deco { position: absolute; font-size: 2rem; opacity: .12; animation: bob 6s ease-in-out infinite; }
.d1 { top: 6%; right: 8%; } .d2 { top: 30%; left: 5%; animation-delay: 1.5s; } .d3 { bottom: 10%; right: 6%; animation-delay: 3s; }
@keyframes bob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.head { text-align: center; margin-bottom: 12px; }
.head h1 { margin: 0; color: #1A0B3E; font-weight: 800; }
.head p { margin: 6px 0 0; color: #5A4690; }

.hud { display: flex; justify-content: center; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
.pill { background: rgba(255,255,255,0.85); border: 2px solid #D4CCFF; border-radius: 50px; padding: 6px 14px; font-weight: 700; color: #3D2B7A; }

.controls { display: flex; justify-content: center; gap: 8px; margin-bottom: 10px; }
.diff-btn {
  border: 2px solid #D4CCFF; border-radius: 999px; padding: 8px 14px;
  background: rgba(255,255,255,0.85); color: #5A4690; font-weight: 700; cursor: pointer;
}
.diff-btn.active { background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; border-color: transparent; }

.board-wrap { position: relative; display: flex; justify-content: center; }
canvas {
  width: min(520px, 92vw);
  height: auto;
  border: 2px solid #D4CCFF;
  border-radius: 16px;
  box-shadow: 0 8px 26px rgba(108,99,255,0.18);
}
.overlay {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  background: rgba(17,24,39,0.35); border-radius: 16px;
}
.card {
  background: rgba(255,255,255,0.95); border: 2px solid #D4CCFF; border-radius: 20px; padding: 20px 24px;
  text-align: center;
}
.emoji { font-size: 2.3rem; }
.card h3 { margin: 6px 0; color: #1A0B3E; }
.card p { margin: 0 0 10px; color: #5A4690; }
.btn-start {
  border: none; border-radius: 999px; padding: 12px 18px; font-family: 'Syne', sans-serif;
  font-weight: 800; cursor: pointer; background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff;
}
.touch-controls {
  display: none;
  grid-template-columns: 1fr;
  justify-items: center;
  gap: 8px;
  margin: 14px auto 6px;
}
.lr-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.ctrl {
  min-width: 58px;
  min-height: 48px;
  border: 2px solid #D4CCFF;
  border-radius: 14px;
  background: rgba(255,255,255,0.92);
  color: #3D2B7A;
  font-weight: 800;
}
.hint { text-align: center; margin-top: 10px; color: #5A4690; font-weight: 600; }

@media (max-width: 900px) {
  .touch-controls { display: grid; }
}
</style>
