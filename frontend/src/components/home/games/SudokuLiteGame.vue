<template>
  <section class="sudoku-shell">
    <div class="deco d1">🧩</div>
    <div class="deco d2">⭐</div>
    <div class="deco d3">🎯</div>

    <header class="head">
      <h1>🧠 Sudoku Lite</h1>
      <p>Fill each row and column with numbers 1-4. No repeats!</p>
    </header>

    <div class="hud">
      <div class="pill">Level {{ level }}</div>
      <div class="pill">Hints {{ hints }}</div>
      <div class="pill">⏱️ {{ elapsed }}s</div>
      <div class="pill">⭐ {{ stars }} stars</div>
    </div>

    <div class="board-wrap">
      <div class="grid">
        <div
          v-for="(cell, idx) in board"
          :key="idx"
          :class="['cell', { locked: isLocked(idx), wrong: wrongCells.has(idx) }]"
        >
          <span v-if="isLocked(idx)">{{ cell }}</span>
          <input
            v-else
            :value="cell"
            maxlength="1"
            inputmode="numeric"
            @input="onInput(idx, $event)"
          >
        </div>
      </div>
    </div>

    <div class="actions">
      <button class="btn ghost" @click="useHint" :disabled="hints <= 0">💡 Hint</button>
      <button class="btn" @click="checkBoard">✅ Check</button>
      <button class="btn ghost" @click="resetPuzzle">🔄 Reset</button>
      <button class="btn next" @click="nextLevel" :disabled="!solved">🚀 Next Level</button>
    </div>

    <p class="msg" :class="msgType" v-if="message">{{ message }}</p>

    <div class="help-card">
      <h4>How to play</h4>
      <ul>
        <li>Each row must contain 1, 2, 3, 4 exactly once.</li>
        <li>Each column must contain 1, 2, 3, 4 exactly once.</li>
        <li>Use hints if stuck. Try to finish with fewer hints for more stars.</li>
      </ul>
    </div>
  </section>
</template>

<script>
const PUZZLES = [
  ['1', '', '', '4', '', '4', '', '', '', '', '4', '', '4', '', '', '1'],
  ['', '2', '', '', '', '', '3', '4', '2', '', '', '', '', '', '1', ''],
  ['3', '', '', '', '', '1', '', '3', '1', '', '2', '', '', '', '', '4'],
  ['', '', '4', '', '', '3', '', '', '', '', '1', '', '', '1', '', ''],
  ['2', '', '', '1', '', '', '4', '', '', '4', '', '', '1', '', '', '2'],
];

const SOLUTIONS = [
  ['1', '3', '2', '4', '2', '4', '1', '3', '3', '1', '4', '2', '4', '2', '3', '1'],
  ['4', '2', '1', '3', '1', '2', '3', '4', '2', '3', '4', '1', '3', '4', '1', '2'],
  ['3', '4', '1', '2', '2', '1', '4', '3', '1', '3', '2', '4', '4', '2', '3', '1'],
  ['1', '3', '4', '2', '2', '3', '1', '4', '4', '2', '1', '3', '3', '1', '2', '4'],
  ['2', '4', '3', '1', '3', '1', '4', '2', '1', '4', '2', '3', '1', '3', '4', '2'],
];

export default {
  name: 'SudokuLiteGame',
  data() {
    return {
      level: 1,
      board: [],
      initial: [],
      solved: false,
      hints: 3,
      stars: 0,
      wrongCells: new Set(),
      message: '',
      msgType: '',
      elapsed: 0,
      timer: null,
    };
  },
  mounted() {
    this.loadLevel(1);
  },
  beforeUnmount() {
    clearInterval(this.timer);
  },
  methods: {
    loadLevel(lvl) {
      const i = (lvl - 1) % PUZZLES.length;
      this.level = lvl;
      this.initial = [...PUZZLES[i]];
      this.board = [...PUZZLES[i]];
      this.solved = false;
      this.hints = Math.max(1, 4 - Math.floor((lvl - 1) / 2));
      this.wrongCells = new Set();
      this.message = '';
      this.msgType = '';
      this.elapsed = 0;
      clearInterval(this.timer);
      this.timer = setInterval(() => { this.elapsed += 1; }, 1000);
    },
    isLocked(idx) {
      return this.initial[idx] !== '';
    },
    onInput(idx, e) {
      const v = String(e.target.value || '').replace(/[^1-4]/g, '').slice(0, 1);
      this.board[idx] = v;
      this.wrongCells.delete(idx);
    },
    checkBoard() {
      this.wrongCells = new Set();
      const rowsValid = [0, 1, 2, 3].every(r => {
        const row = this.board.slice(r * 4, r * 4 + 4);
        return this.isUnitValid(row);
      });
      const colsValid = [0, 1, 2, 3].every(c => {
        const col = [this.board[c], this.board[c + 4], this.board[c + 8], this.board[c + 12]];
        return this.isUnitValid(col);
      });

      if (!rowsValid || !colsValid) {
        // highlight wrong user-entered cells based on solution
        const solution = this.solutionForLevel();
        this.board.forEach((cell, idx) => {
          if (!this.isLocked(idx) && cell && solution[idx] !== cell) this.wrongCells.add(idx);
        });
        this.message = 'Not solved yet. Keep going!';
        this.msgType = 'fail';
        return;
      }

      this.solved = true;
      clearInterval(this.timer);
      this.updateStars();
      this.message = 'Awesome! Puzzle solved! 🎉';
      this.msgType = 'ok';
    },
    isUnitValid(unit) {
      return unit.length === 4 && !unit.includes('') && new Set(unit).size === 4;
    },
    solutionForLevel() {
      const i = (this.level - 1) % SOLUTIONS.length;
      return SOLUTIONS[i];
    },
    useHint() {
      if (this.hints <= 0 || this.solved) return;
      const solution = this.solutionForLevel();
      const candidates = this.board
        .map((v, idx) => ({ v, idx }))
        .filter(x => !this.isLocked(x.idx) && this.board[x.idx] !== solution[x.idx]);
      if (!candidates.length) return;
      const pick = candidates[Math.floor(Math.random() * candidates.length)].idx;
      this.board[pick] = solution[pick];
      this.hints -= 1;
      this.wrongCells.delete(pick);
      this.message = 'Hint used! 💡';
      this.msgType = 'info';
    },
    resetPuzzle() {
      this.board = [...this.initial];
      this.solved = false;
      this.wrongCells = new Set();
      this.message = '';
      this.msgType = '';
      this.elapsed = 0;
    },
    nextLevel() {
      if (!this.solved) return;
      this.loadLevel(this.level + 1);
    },
    updateStars() {
      const timeStar = this.elapsed <= 60 ? 1 : 0;
      const hintStar = this.hints >= 2 ? 2 : this.hints >= 1 ? 1 : 0;
      this.stars = Math.min(3, timeStar + hintStar);
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.sudoku-shell {
  position: relative;
  width: calc(100vw - 300px);
  min-height: calc(100vh - 120px);
  padding: 24px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  overflow: hidden;
}
.deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: float 6s ease-in-out infinite; }
.d1 { top: 6%; right: 8%; } .d2 { top: 30%; left: 4%; animation-delay: 1.5s; } .d3 { bottom: 16%; right: 6%; animation-delay: 3s; }
@keyframes float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.head { text-align: center; margin-bottom: 14px; }
.head h1 { margin: 0; color: #1A0B3E; font-weight: 800; }
.head p { margin: 6px 0 0; color: #5A4690; }

.hud { display: flex; justify-content: center; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; }
.pill { background: rgba(255,255,255,0.85); border: 2px solid #D4CCFF; border-radius: 50px; padding: 6px 14px; font-weight: 700; color: #3D2B7A; }

.board-wrap { display: flex; justify-content: center; margin: 14px 0; }
.grid { display: grid; grid-template-columns: repeat(4, 62px); gap: 8px; }
.cell {
  width: 62px; height: 62px; border-radius: 12px;
  border: 2px solid #D4CCFF; background: rgba(255,255,255,0.9);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.25rem; font-weight: 800; color: #1A0B3E;
}
.cell.locked { background: rgba(108,99,255,0.12); border-color: #A855F7; }
.cell.wrong { border-color: #EF4444; background: rgba(239,68,68,0.08); }
.cell input {
  width: 100%; height: 100%; border: none; outline: none; text-align: center;
  font-size: 1.2rem; font-weight: 800; color: #1A0B3E; background: transparent;
}

.actions { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; margin-top: 8px; }
.btn {
  border: none; border-radius: 50px; padding: 12px 20px; font-family: 'Syne', sans-serif;
  font-weight: 700; cursor: pointer; background: linear-gradient(135deg,#6C63FF,#A855F7); color:#fff;
}
.btn.ghost { background: rgba(255,255,255,0.9); color: #5A4690; border: 2px solid #D4CCFF; }
.btn.next { background: linear-gradient(135deg,#22c55e,#16a34a); }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

.msg { text-align: center; margin-top: 10px; font-weight: 700; }
.msg.ok { color: #16a34a; }
.msg.fail { color: #b91c1c; }
.msg.info { color: #6C63FF; }

.help-card { margin: 16px auto 0; max-width: 700px; background: rgba(255,255,255,0.85); border: 2px solid #E8E0FF; border-radius: 16px; padding: 14px; }
.help-card h4 { margin: 0 0 8px; color: #1A0B3E; }
.help-card ul { margin: 0; padding-left: 18px; color: #5A4690; line-height: 1.6; }
</style>
