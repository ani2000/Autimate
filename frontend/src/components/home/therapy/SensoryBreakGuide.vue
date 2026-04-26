<template>
  <div class="sensory-shell">
    <div class="float-deco d1">🧸</div>
    <div class="float-deco d2">🫧</div>
    <div class="float-deco d3">🌿</div>
    <div class="float-deco d4">💜</div>

    <header class="sg-header">
      <h1>🧘 Sensory Break Guide</h1>
      <p>Quick calming activities when feeling overwhelmed</p>
    </header>

    <!-- Activity cards -->
    <div class="activities-grid">
      <div
        v-for="(act, i) in activities"
        :key="i"
        class="act-card"
        :class="{ active: activeIndex === i }"
        @click="selectActivity(i)"
      >
        <div class="act-emoji">{{ act.emoji }}</div>
        <h3>{{ act.title }}</h3>
        <p class="act-dur">{{ act.duration }}</p>
      </div>
    </div>

    <!-- Active activity detail -->
    <transition name="slide">
      <div class="detail-panel" v-if="activeIndex !== null">
        <div class="detail-card">
          <div class="detail-top">
            <span class="detail-emoji">{{ activities[activeIndex].emoji }}</span>
            <div>
              <h2>{{ activities[activeIndex].title }}</h2>
              <span class="detail-dur">{{ activities[activeIndex].duration }}</span>
            </div>
          </div>
          <ol class="steps-list">
            <li v-for="(step, si) in activities[activeIndex].steps" :key="si">
              <span class="step-num">{{ si + 1 }}</span>
              <span>{{ step }}</span>
            </li>
          </ol>

          <!-- Timer -->
          <div class="timer-section" v-if="activities[activeIndex].timerSec">
            <div class="timer-circle" :class="{ ticking: timerRunning }">
              <span>{{ formatTimer(timerLeft) }}</span>
            </div>
            <div class="timer-btns">
              <button v-if="!timerRunning" class="btn-timer" @click="startTimer">▶️ Start Timer</button>
              <button v-else class="btn-timer stop" @click="stopTimer">⏸️ Stop</button>
            </div>
          </div>

          <button class="btn-done" @click="markDone">✅ I Feel Better!</button>
        </div>
      </div>
    </transition>

    <!-- Journal -->
    <div class="journal-section">
      <h3>📒 My Calm Journal</h3>
      <textarea v-model="note" placeholder="What helped me feel calm today?" rows="3"></textarea>
      <div class="journal-row">
        <div class="mood-pick">
          <span>How I feel now:</span>
          <button v-for="m in moods" :key="m.emoji" class="mood-btn" :class="{ picked: mood === m.emoji }" @click="mood = m.emoji">{{ m.emoji }}</button>
        </div>
        <button class="btn-save" @click="saveNote">💾 Save</button>
      </div>
    </div>

    <transition name="fade">
      <div class="save-toast" v-if="showToast">{{ toastMsg }}</div>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'SensoryBreakGuide',
  data() {
    return {
      activeIndex: null,
      timerLeft: 0,
      timerRunning: false,
      timerInterval: null,
      note: localStorage.getItem('sensory_break_note') || '',
      mood: '',
      showToast: false,
      toastMsg: '',
      moods: [
        { emoji: '😊' }, { emoji: '😌' }, { emoji: '😐' }, { emoji: '😢' }, { emoji: '😤' },
      ],
      activities: [
        {
          emoji: '🤗', title: 'Deep Pressure Hug', duration: '2-3 min', timerSec: 120,
          steps: ['Find a soft blanket or stuffed animal', 'Give yourself a big, tight hug', 'Squeeze gently for 10 seconds', 'Release slowly and breathe', 'Repeat 3-5 times'],
        },
        {
          emoji: '🫧', title: 'Bubble Breathing', duration: '3-4 min', timerSec: 180,
          steps: ['Sit comfortably and relax your shoulders', 'Breathe in slowly through your nose (4 sec)', 'Imagine blowing a big bubble', 'Breathe out slowly through your mouth (6 sec)', 'Watch your imaginary bubble float away!'],
        },
        {
          emoji: '🧊', title: 'Ice Cube Hold', duration: '1-2 min', timerSec: 60,
          steps: ['Get a small ice cube', 'Hold it gently in your hand', 'Focus on how the cold feels', 'Notice it slowly melting', 'Dry your hands when done'],
        },
        {
          emoji: '🎵', title: 'Quiet Corner', duration: '3-5 min', timerSec: 180,
          steps: ['Find a quiet, dim spot', 'Bring a soft toy or blanket', 'Sit or lie down comfortably', 'Close your eyes if you want', 'Listen to soft music or silence'],
        },
        {
          emoji: '🚶', title: 'Calm Walk', duration: '2-3 min', timerSec: 120,
          steps: ['Stand up slowly', 'Take 5 slow steps forward', 'Feel your feet on the ground', 'Take 5 slow steps back', 'Take a deep breath after'],
        },
        {
          emoji: '🧸', title: 'Stuffed Friend Talk', duration: '2-4 min', timerSec: 0,
          steps: ['Pick your favourite stuffed animal', 'Tell them how you feel', 'Give them a hug', 'Ask them for advice (pretend!)', 'Say "thank you" to your friend'],
        },
      ],
    };
  },
  methods: {
    selectActivity(i) { this.activeIndex = this.activeIndex === i ? null : i; this.stopTimer(); },
    startTimer() {
      this.timerLeft = this.activities[this.activeIndex].timerSec;
      this.timerRunning = true;
      this.timerInterval = setInterval(() => {
        if (this.timerLeft > 0) { this.timerLeft--; }
        else { this.stopTimer(); this.toast('⏰ Time is up! Great job!'); }
      }, 1000);
    },
    stopTimer() { clearInterval(this.timerInterval); this.timerRunning = false; },
    formatTimer(s) { return `${Math.floor(s/60)}:${(s%60).toString().padStart(2,'0')}`; },
    markDone() { this.toast('🌟 You did amazing!'); this.activeIndex = null; this.stopTimer(); },
    saveNote() {
      localStorage.setItem('sensory_break_note', this.note || '');
      localStorage.setItem('sensory_break_mood', this.mood);
      this.toast('✅ Journal saved!');
    },
    toast(msg) { this.toastMsg = msg; this.showToast = true; setTimeout(() => { this.showToast = false; }, 2200); },
  },
  beforeUnmount() { clearInterval(this.timerInterval); },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.sensory-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #F0FDF4 0%, #EDE9FE 50%, #FFF7ED 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 6s ease-in-out infinite; pointer-events: none; }
.d1 { top: 6%; right: 7%; } .d2 { bottom: 15%; left: 5%; animation-delay: 2s; }
.d3 { top: 30%; left: 3%; animation-delay: 1s; } .d4 { bottom: 25%; right: 4%; animation-delay: 3s; }
@keyframes floatBob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.sg-header { text-align: center; margin-bottom: 18px; }
.sg-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.sg-header p { margin: 4px 0 0; color: #5A4690; }

/* Activity grid */
.activities-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; margin-bottom: 16px; }
.act-card {
  background: rgba(255,255,255,0.85); border: 2px solid #E8E0FF; border-radius: 16px;
  padding: 14px; text-align: center; cursor: pointer; transition: all 0.3s;
  backdrop-filter: blur(6px);
}
.act-card:hover { transform: translateY(-3px); border-color: #A855F7; }
.act-card.active { border-color: #6C63FF; background: rgba(108,99,255,0.08); box-shadow: 0 4px 18px rgba(108,99,255,0.15); }
.act-emoji { font-size: 2.2rem; margin-bottom: 4px; }
.act-card h3 { margin: 0; font-size: 0.85rem; font-weight: 700; color: #1A0B3E; }
.act-dur { margin: 4px 0 0; font-size: 0.72rem; color: #8B7EC8; font-weight: 600; }

/* Detail panel */
.detail-card {
  background: rgba(255,255,255,0.9); border: 2px solid #D4CCFF; border-radius: 20px;
  padding: 20px 24px; backdrop-filter: blur(10px); margin-bottom: 18px;
}
.detail-top { display: flex; gap: 12px; align-items: center; margin-bottom: 14px; }
.detail-emoji { font-size: 2.5rem; }
.detail-top h2 { margin: 0; font-size: 1.2rem; font-weight: 800; color: #1A0B3E; }
.detail-dur { font-size: 0.8rem; color: #8B7EC8; font-weight: 600; }

.steps-list { padding: 0; margin: 0; list-style: none; display: flex; flex-direction: column; gap: 8px; }
.steps-list li {
  display: flex; gap: 10px; align-items: start; padding: 8px 12px;
  background: rgba(237,233,254,0.5); border-radius: 10px; font-size: 0.88rem; color: #3D2B7A;
}
.step-num {
  min-width: 24px; height: 24px; background: linear-gradient(135deg,#6C63FF,#A855F7);
  color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 0.75rem; font-weight: 800; flex-shrink: 0;
}

/* Timer */
.timer-section { display: flex; align-items: center; gap: 14px; margin-top: 14px; }
.timer-circle {
  width: 70px; height: 70px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.8); border: 3px solid #D4CCFF; font-weight: 800; font-size: 1.1rem; color: #3D2B7A;
}
.timer-circle.ticking { border-color: #6C63FF; animation: pulse 1s ease-in-out infinite; }
@keyframes pulse { 0%,100%{transform:scale(1)} 50%{transform:scale(1.05)} }
.btn-timer {
  border: none; border-radius: 50px; padding: 10px 20px; font-family: 'Syne', sans-serif;
  font-weight: 700; background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; cursor: pointer;
}
.btn-timer.stop { background: linear-gradient(135deg,#FF6B6B,#FF4466); }

.btn-done {
  display: block; margin: 16px auto 0; border: none; border-radius: 50px; padding: 12px 28px;
  font-family: 'Syne', sans-serif; font-weight: 700; font-size: 1rem;
  background: linear-gradient(135deg,#22c55e,#16a34a); color: #fff; cursor: pointer;
  box-shadow: 0 4px 14px rgba(34,197,94,0.2); transition: all 0.25s;
}
.btn-done:hover { transform: translateY(-2px); }

.slide-enter-active, .slide-leave-active { transition: all 0.35s ease; }
.slide-enter-from { opacity: 0; transform: translateY(16px); }
.slide-leave-to { opacity: 0; transform: translateY(-16px); }

/* Journal */
.journal-section {
  background: rgba(255,255,255,0.85); border: 2px solid #E8E0FF; border-radius: 18px;
  padding: 18px 20px; backdrop-filter: blur(6px);
}
.journal-section h3 { margin: 0 0 10px; color: #1A0B3E; font-weight: 800; }
.journal-section textarea {
  width: 100%; border: 2px solid #D4CCFF; border-radius: 12px; padding: 10px;
  font-family: 'Syne', sans-serif; font-size: 0.9rem; resize: vertical; outline: none;
}
.journal-section textarea:focus { border-color: #6C63FF; }
.journal-row { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; flex-wrap: wrap; gap: 8px; }
.mood-pick { display: flex; gap: 4px; align-items: center; font-size: 0.85rem; color: #5A4690; font-weight: 600; }
.mood-btn {
  background: rgba(255,255,255,0.6); border: 2px solid #E8E0FF; border-radius: 50%;
  width: 36px; height: 36px; font-size: 1.2rem; cursor: pointer; transition: all 0.2s;
}
.mood-btn.picked { border-color: #6C63FF; transform: scale(1.2); background: rgba(108,99,255,0.1); }
.btn-save {
  border: none; border-radius: 50px; padding: 10px 22px; font-family: 'Syne', sans-serif;
  font-weight: 700; background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; cursor: pointer;
}

/* Toast */
.save-toast {
  position: fixed; bottom: 30px; left: 50%; transform: translateX(-50%);
  background: linear-gradient(135deg,#22c55e,#16a34a); color: #fff;
  padding: 12px 28px; border-radius: 50px; font-weight: 700; font-family: 'Syne', sans-serif;
  box-shadow: 0 6px 20px rgba(34,197,94,0.3); z-index: 999;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.4s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
