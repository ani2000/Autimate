<template>
  <div class="color-shell">
    <div class="float-deco d1">🎨</div>
    <div class="float-deco d2">🌈</div>
    <div class="float-deco d3">✨</div>
    <div class="float-deco d4">🖌️</div>

    <header class="ct-header">
      <h1>🎨 Color Therapy</h1>
      <p>Paint, explore colors, and feel calm!</p>
    </header>

    <!-- Palette -->
    <div class="palette">
      <button
        v-for="c in palette"
        :key="c"
        class="swatch"
        :style="{ background: c }"
        :class="{ active: selectedColor === c }"
        @click="selectedColor = c"
      ></button>
    </div>

    <!-- Canvas -->
    <div class="canvas-area">
      <canvas
        ref="canvas"
        width="520"
        height="360"
        @click="paintAt"
        @mousemove="paintDrag"
        @mousedown="startDrag"
        @mouseup="stopDrag"
        @touchstart.prevent="startDrag"
        @touchmove.prevent="paintDrag"
        @touchend.prevent="stopDrag"
        @touchcancel.prevent="stopDrag"
      ></canvas>
    </div>

    <!-- Tools -->
    <div class="tools-row">
      <div class="tool-group">
        <label>🖌️ Size: <strong>{{ brushSize }}</strong></label>
        <input type="range" min="6" max="60" v-model.number="brushSize">
      </div>
      <button class="tool-btn" @click="clearCanvas">🧹 Clear</button>
      <button class="tool-btn accent" @click="fillRandom">🎲 Random Fill</button>
    </div>

    <!-- Tips -->
    <div class="tips-card">
      <h4>💡 Tips for Parents</h4>
      <ul>
        <li>Ask your child to pick their favourite color first.</li>
        <li>Try painting simple shapes together — circles, stars, houses.</li>
        <li>Use Random Fill to introduce new color discussions.</li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      palette: ['#ef4444', '#f59e0b', '#22c55e', '#3b82f6', '#8b5cf6', '#ec4899', '#06b6d4', '#0f172a', '#f8fafc'],
      selectedColor: '#3b82f6',
      brushSize: 20,
      dragging: false,
    };
  },
  mounted() { this.clearCanvas(); },
  methods: {
    getCtx() { return this.$refs.canvas?.getContext('2d'); },
    getCanvasPoint(e) {
      const canvas = this.$refs.canvas;
      const rect = canvas.getBoundingClientRect();
      const point = e.touches?.[0] || e.changedTouches?.[0] || e;
      const x = ((point.clientX - rect.left) * canvas.width) / rect.width;
      const y = ((point.clientY - rect.top) * canvas.height) / rect.height;
      return { x, y };
    },
    clearCanvas() { const ctx = this.getCtx(); if (!ctx) return; ctx.fillStyle = '#f8fafc'; ctx.fillRect(0, 0, 520, 360); },
    fillRandom() {
      const ctx = this.getCtx(); if (!ctx) return;
      for (let i = 0; i < 40; i++) {
        ctx.beginPath();
        ctx.fillStyle = this.palette[Math.floor(Math.random() * this.palette.length)];
        ctx.arc(Math.random() * 520, Math.random() * 360, 10 + Math.random() * 35, 0, Math.PI * 2);
        ctx.fill();
      }
    },
    paintAt(e) { const { x, y } = this.getCanvasPoint(e); this.dot(x, y); },
    startDrag(e) { this.dragging = true; this.paintAt(e); },
    stopDrag() { this.dragging = false; },
    paintDrag(e) { if (!this.dragging) return; const { x, y } = this.getCanvasPoint(e); this.dot(x, y); },
    dot(x, y) { const ctx = this.getCtx(); if (!ctx) return; ctx.beginPath(); ctx.fillStyle = this.selectedColor; ctx.arc(x, y, this.brushSize / 2, 0, Math.PI * 2); ctx.fill(); },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.color-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF7ED 0%, #EDE9FE 50%, #ECFDF5 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 6s ease-in-out infinite; pointer-events: none; }
.d1 { top: 6%; right: 6%; } .d2 { bottom: 12%; left: 5%; animation-delay: 2s; }
.d3 { top: 30%; left: 3%; animation-delay: 1.2s; } .d4 { bottom: 30%; right: 4%; animation-delay: 3s; }
@keyframes floatBob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.ct-header { text-align: center; margin-bottom: 14px; }
.ct-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.ct-header p { margin: 4px 0 0; color: #5A4690; }

.palette { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; margin-bottom: 12px; }
.swatch {
  width: 40px; height: 40px; border-radius: 50%; border: 3px solid transparent;
  cursor: pointer; transition: all 0.2s; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.swatch.active { border-color: #1A0B3E; transform: scale(1.2); box-shadow: 0 4px 14px rgba(0,0,0,0.15); }

.canvas-area {
  display: flex; justify-content: center; margin-bottom: 12px;
}
.canvas-area canvas {
  width: min(520px, 92vw);
  height: auto;
  border: 2px solid #D4CCFF; border-radius: 16px; background: #fff; cursor: crosshair;
  box-shadow: 0 4px 20px rgba(108,99,255,0.08);
}

.tools-row { display: flex; gap: 10px; align-items: center; justify-content: center; flex-wrap: wrap; margin-bottom: 14px; }
.tool-group { display: flex; align-items: center; gap: 8px; }
.tool-group label { font-weight: 600; color: #5A4690; font-size: 0.88rem; }
.tool-group input[type=range] { width: 110px; accent-color: #6C63FF; }
.tool-btn {
  border: none; border-radius: 50px; padding: 10px 20px; font-family: 'Syne', sans-serif;
  font-weight: 700; background: rgba(255,255,255,0.85); border: 2px solid #D4CCFF;
  color: #5A4690; cursor: pointer; transition: all 0.25s;
}
.tool-btn:hover { border-color: #6C63FF; }
.tool-btn.accent { background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; border-color: transparent; }

.tips-card {
  background: rgba(255,255,255,0.85); border: 2px solid #E8E0FF; border-radius: 16px;
  padding: 14px 18px; backdrop-filter: blur(6px); max-width: 520px; margin: 0 auto;
}
.tips-card h4 { margin: 0 0 8px; color: #1A0B3E; font-weight: 800; }
.tips-card ul { margin: 0; padding-left: 18px; color: #5A4690; line-height: 1.7; font-size: 0.9rem; }
</style>
