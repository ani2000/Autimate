<template>
  <section class="draw-shell">
    <div class="deco d1">🎨</div>
    <div class="deco d2">⭐</div>
    <div class="deco d3">🖌️</div>

    <header class="head">
      <h1>🎨 Drawing Therapy</h1>
      <p>Copy the picture and let the app give fun feedback!</p>
    </header>

    <div class="layout">
      <aside class="reference-card">
        <h3>Reference</h3>
        <img :src="`/drawing_images_references/${imageName}`" alt="Reference image" class="ref-image">
        <button class="btn" @click="shuffleImage">🖼️ New Image</button>
      </aside>

      <main class="canvas-card">
        <canvas
          ref="canvas"
          width="700"
          height="520"
          @mousedown="startDrawing"
          @mouseup="stopDrawing"
          @mouseleave="stopDrawing"
          @mousemove="draw"
          @touchstart.prevent="startDrawing"
          @touchmove.prevent="draw"
          @touchend.prevent="stopDrawing"
          @touchcancel.prevent="stopDrawing"
        ></canvas>

        <div class="tools">
          <button :class="['tool-btn', {active: activeTool === 'pen'}]" @click="setPen">✏️ Pen</button>
          <button :class="['tool-btn', {active: activeTool === 'brush'}]" @click="setBrush">🖌️ Brush</button>
          <button :class="['tool-btn', {active: activeTool === 'eraser'}]" @click="setEraser">🧽 Eraser</button>
          <button class="tool-btn ghost" @click="clearCanvas">🧹 Clear</button>

          <div class="tool-inline">
            <label>Color</label>
            <input type="color" v-model="color">
          </div>

          <div class="tool-inline">
            <label>Size {{ lineWidth }}</label>
            <input type="range" min="2" max="28" v-model.number="lineWidth">
          </div>
        </div>
      </main>
    </div>

    <p class="msg" v-if="statusMsg">{{ statusMsg }}</p>
  </section>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  name: 'DrawingCanvas',
  data() {
    return {
      isDrawing: false,
      context: null,
      lineWidth: 4,
      color: '#6C63FF',
      activeTool: 'pen',
      imageName: '',
      imageList: [
        '8115ab50-6f99-409a-ae47-d730b9c68ced.jpeg',
        '5d4a4260-1456-4a9e-aa05-0c8daaa21b6a.jpeg',
        '9457e4dd-7550-4c90-98a1-f521f88051f9.jpeg',
        'b5ed1fb5-b8eb-40db-b8f4-d30e6023c4e6.jpeg',
      ],
      statusMsg: '',
    };
  },
  mounted() {
    const canvas = this.$refs.canvas;
    this.context = canvas.getContext('2d');
    this.context.fillStyle = '#ffffff';
    this.context.fillRect(0, 0, canvas.width, canvas.height);
    this.context.lineCap = 'round';
    this.context.lineJoin = 'round';
    this.shuffleImage();
  },
  methods: {
    shuffleImage() {
      const randomIndex = Math.floor(Math.random() * this.imageList.length);
      this.imageName = this.imageList[randomIndex];
      this.statusMsg = 'New picture loaded! Copy it on the canvas.';
    },
    getCanvasPoint(event) {
      const canvas = this.$refs.canvas;
      const rect = canvas.getBoundingClientRect();
      const point = event.touches?.[0] || event.changedTouches?.[0] || event;
      const x = ((point.clientX - rect.left) * canvas.width) / rect.width;
      const y = ((point.clientY - rect.top) * canvas.height) / rect.height;
      return { x, y };
    },
    startDrawing(event) {
      this.isDrawing = true;
      const { x, y } = this.getCanvasPoint(event);
      this.context.beginPath();
      this.context.moveTo(x, y);
    },
    async stopDrawing() {
      if (!this.isDrawing) return;
      this.isDrawing = false;
      this.context.closePath();
      await this.compareWithReference();
    },
    draw(event) {
      if (!this.isDrawing) return;
      const { x, y } = this.getCanvasPoint(event);
      this.context.lineWidth = this.lineWidth;
      this.context.strokeStyle = this.color;
      this.context.lineTo(x, y);
      this.context.stroke();
    },
    setPen() {
      this.activeTool = 'pen';
      if (this.color === '#FFFFFF') this.color = '#6C63FF';
      this.lineWidth = Math.max(2, this.lineWidth);
    },
    setBrush() {
      this.activeTool = 'brush';
      this.lineWidth = Math.max(10, this.lineWidth);
    },
    setEraser() {
      this.activeTool = 'eraser';
      this.color = '#FFFFFF';
      this.lineWidth = Math.max(14, this.lineWidth);
    },
    clearCanvas() {
      const canvas = this.$refs.canvas;
      this.context.fillStyle = '#FFFFFF';
      this.context.fillRect(0, 0, canvas.width, canvas.height);
      this.statusMsg = 'Canvas cleared!';
    },
    async compareWithReference() {
      this.statusMsg = 'Checking drawing...';
      const tempCanvas = document.createElement('canvas');
      const tempContext = tempCanvas.getContext('2d');

      const refImage = new Image();
      refImage.crossOrigin = 'Anonymous';
      refImage.src = `/drawing_images_references/${this.imageName}`;

      await new Promise((resolve) => {
        refImage.onload = async () => {
          tempCanvas.width = Math.max(700, refImage.width);
          tempCanvas.height = refImage.height + 520;
          tempContext.drawImage(refImage, 0, 0);
          tempContext.drawImage(this.$refs.canvas, 0, refImage.height);
          const combinedImage = tempCanvas.toDataURL('image/png');

          const token = Cookies.get('token');
          try {
            const response = await axios.post(
              '/api/v1/drawing/compare_drawn_and_reference',
              { image: combinedImage },
              { headers: { Authorization: `Bearer ${token}` } }
            );

            const feedback = response?.data || 'Great effort! Keep practicing!';
            this.statusMsg = `Feedback: ${feedback}`;
            this.speakFeedback(feedback);
          } catch (error) {
            this.statusMsg = 'Could not fetch feedback right now.';
            console.error('Drawing compare error:', error);
          }
          resolve();
        };
      });
    },
    speakFeedback(text) {
      if (!text || !window.speechSynthesis) return;
      speechSynthesis.cancel();
      const utter = new SpeechSynthesisUtterance(text);
      utter.rate = 0.95;
      utter.pitch = 1.1;
      speechSynthesis.speak(utter);
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.draw-shell {
  position: relative;
  width: calc(100vw - 300px);
  min-height: calc(100vh - 120px);
  padding: 24px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
}
.deco { position: absolute; font-size: 2rem; opacity: .12; animation: float 6s ease-in-out infinite; }
.d1 { top: 6%; right: 7%; } .d2 { top: 35%; left: 4%; animation-delay: 1.2s; } .d3 { bottom: 12%; right: 4%; animation-delay: 2.6s; }
@keyframes float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.head { text-align: center; margin-bottom: 14px; }
.head h1 { margin: 0; color: #1A0B3E; }
.head p { margin: 6px 0 0; color: #5A4690; }

.layout { display: grid; grid-template-columns: 280px 1fr; gap: 14px; align-items: start; }
.reference-card, .canvas-card {
  background: rgba(255,255,255,0.9);
  border: 2px solid #D4CCFF;
  border-radius: 16px;
  padding: 12px;
}
.reference-card h3 { margin: 0 0 8px; color: #1A0B3E; }
.ref-image { width: 100%; border-radius: 12px; border: 2px solid #E8E0FF; }
.btn {
  margin-top: 10px;
  border: none; border-radius: 999px; padding: 10px 14px; cursor: pointer;
  background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; font-weight: 700;
}

canvas {
  width: 100%;
  max-width: 100%;
  border-radius: 12px;
  border: 2px solid #E8E0FF;
  background: #fff;
}

.tools { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; align-items: center; }
.tool-btn {
  border: none; border-radius: 999px; padding: 9px 12px; cursor: pointer; font-weight: 700;
  background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff;
}
.tool-btn.active { box-shadow: 0 0 0 3px rgba(108,99,255,.25); }
.tool-btn.ghost { background: rgba(255,255,255,.95); border: 2px solid #D4CCFF; color: #5A4690; }
.tool-inline { display: flex; align-items: center; gap: 6px; background: rgba(255,255,255,.9); border: 2px solid #E8E0FF; border-radius: 999px; padding: 6px 10px; }
.tool-inline label { font-size: .86rem; color: #5A4690; font-weight: 700; }

.msg { margin-top: 12px; text-align: center; color: #3D2B7A; font-weight: 700; }

@media (max-width: 980px) {
  .layout { grid-template-columns: 1fr; }
}
</style>
