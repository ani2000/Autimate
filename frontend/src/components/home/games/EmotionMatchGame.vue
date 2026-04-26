<template>
  <div class="emo-shell">
    <div class="float-deco d1">😊</div>
    <div class="float-deco d2">💜</div>
    <div class="float-deco d3">🌟</div>
    <div class="float-deco d4">🎭</div>

    <header class="emo-header">
      <h1>🎭 Emotion Match</h1>
      <p>How would you feel? Pick the right emotion!</p>
    </header>

    <!-- Level / progress bar -->
    <div class="progress-bar-wrapper">
      <div class="level-badge">Level {{ currentLevel }}</div>
      <div class="progress-track">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>
      <div class="score-badge">⭐ {{ score }}</div>
    </div>

    <!-- Question card -->
    <div class="q-card" v-if="currentQuestion && !showResult" :key="questionKey">
      <div class="q-number">{{ index + 1 }} / {{ levelQuestions.length }}</div>
      <div class="q-scene">{{ currentQuestion.scene }}</div>
      <h3 class="q-text">{{ currentQuestion.text }}</h3>
      <div class="options">
        <button
          v-for="opt in currentQuestion.options"
          :key="opt.label"
          :class="['opt-btn', { selected: selectedOption === opt.label, correct: feedback === 'correct' && selectedOption === opt.label, wrong: feedback === 'wrong' && selectedOption === opt.label }]"
          @click="answer(opt.label)"
          :disabled="!!feedback"
        >
          <span class="opt-emoji">{{ opt.emoji }}</span>
          <span class="opt-label">{{ opt.label }}</span>
        </button>
      </div>
      <div class="feedback-area" v-if="feedback">
        <div :class="['feedback-msg', feedback]">
          <span v-if="feedback === 'correct'">🎉 Awesome! That's right!</span>
          <span v-else>😅 Not quite! It was <strong>{{ currentQuestion.correct }}</strong></span>
        </div>
      </div>
    </div>

    <!-- Level complete -->
    <div class="level-complete" v-if="showResult">
      <div class="confetti" v-if="levelScore >= levelQuestions.length * 0.6">
        <span v-for="n in 24" :key="n" class="conf" :style="confettiStyle(n)"></span>
      </div>
      <div class="result-card">
        <div class="result-emoji">{{ levelScore >= levelQuestions.length * 0.8 ? '🏆' : levelScore >= levelQuestions.length * 0.5 ? '⭐' : '💪' }}</div>
        <h2>Level {{ currentLevel }} Complete!</h2>
        <p class="result-score">You got <strong>{{ levelScore }}/{{ levelQuestions.length }}</strong> correct!</p>
        <div class="stars-row">
          <span v-for="s in 3" :key="s" :class="['star', { earned: s <= earnedStars }]">⭐</span>
        </div>
        <div class="result-btns">
          <button class="btn-retry" @click="retryLevel">🔄 Try Again</button>
          <button v-if="currentLevel < totalLevels" class="btn-next" @click="nextLevel">➡️ Next Level</button>
          <button v-else class="btn-next" @click="retryLevel">🎉 Play Again!</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const ALL_QUESTIONS = [
  // Level 1 — Easy (basic emotions)
  { level: 1, text: 'A friend gives you a birthday present!', scene: '🎁', options: [{ emoji: '😊', label: 'Happy' }, { emoji: '😢', label: 'Sad' }, { emoji: '😡', label: 'Angry' }], correct: 'Happy' },
  { level: 1, text: 'You drop your ice cream on the ground.', scene: '🍦', options: [{ emoji: '😢', label: 'Sad' }, { emoji: '😊', label: 'Happy' }, { emoji: '😴', label: 'Sleepy' }], correct: 'Sad' },
  { level: 1, text: 'A big dog suddenly barks at you!', scene: '🐕', options: [{ emoji: '😨', label: 'Scared' }, { emoji: '😊', label: 'Happy' }, { emoji: '😡', label: 'Angry' }], correct: 'Scared' },
  { level: 1, text: 'Someone takes your toy without asking.', scene: '😤', options: [{ emoji: '😡', label: 'Angry' }, { emoji: '😊', label: 'Happy' }, { emoji: '😨', label: 'Scared' }], correct: 'Angry' },
  { level: 1, text: 'You get a gold star from your teacher!', scene: '🌟', options: [{ emoji: '🥰', label: 'Proud' }, { emoji: '😢', label: 'Sad' }, { emoji: '😨', label: 'Scared' }], correct: 'Proud' },

  // Level 2 — Medium
  { level: 2, text: 'Your best friend is moving to another city.', scene: '🏘️', options: [{ emoji: '😢', label: 'Sad' }, { emoji: '😡', label: 'Angry' }, { emoji: '😴', label: 'Bored' }], correct: 'Sad' },
  { level: 2, text: 'You are about to ride a roller coaster!', scene: '🎢', options: [{ emoji: '😆', label: 'Excited' }, { emoji: '😢', label: 'Sad' }, { emoji: '😡', label: 'Angry' }], correct: 'Excited' },
  { level: 2, text: 'You finished a really hard puzzle all by yourself!', scene: '🧩', options: [{ emoji: '🥰', label: 'Proud' }, { emoji: '😨', label: 'Scared' }, { emoji: '😴', label: 'Bored' }], correct: 'Proud' },
  { level: 2, text: 'Your mom reads you a story and you start yawning.', scene: '📖', options: [{ emoji: '😴', label: 'Sleepy' }, { emoji: '😆', label: 'Excited' }, { emoji: '😡', label: 'Angry' }], correct: 'Sleepy' },
  { level: 2, text: 'You see a spider crawling on your bed!', scene: '🕷️', options: [{ emoji: '😨', label: 'Scared' }, { emoji: '😊', label: 'Happy' }, { emoji: '😴', label: 'Sleepy' }], correct: 'Scared' },
  { level: 2, text: 'There is nothing to do and it is raining outside.', scene: '🌧️', options: [{ emoji: '😴', label: 'Bored' }, { emoji: '😆', label: 'Excited' }, { emoji: '🥰', label: 'Proud' }], correct: 'Bored' },

  // Level 3 — Harder (nuanced)
  { level: 3, text: 'You practiced piano every day and finally played the whole song!', scene: '🎹', options: [{ emoji: '🥰', label: 'Proud' }, { emoji: '😨', label: 'Nervous' }, { emoji: '😢', label: 'Sad' }], correct: 'Proud' },
  { level: 3, text: 'Tomorrow is your first day at a new school.', scene: '🏫', options: [{ emoji: '😨', label: 'Nervous' }, { emoji: '😡', label: 'Angry' }, { emoji: '😴', label: 'Sleepy' }], correct: 'Nervous' },
  { level: 3, text: 'You see two friends whispering and looking at you.', scene: '🤫', options: [{ emoji: '😟', label: 'Worried' }, { emoji: '😊', label: 'Happy' }, { emoji: '😆', label: 'Excited' }], correct: 'Worried' },
  { level: 3, text: 'Your grandma surprises you with cookies she baked!', scene: '🍪', options: [{ emoji: '🤗', label: 'Grateful' }, { emoji: '😡', label: 'Angry' }, { emoji: '😨', label: 'Scared' }], correct: 'Grateful' },
  { level: 3, text: 'You accidentally broke your sister\'s drawing.', scene: '🖼️', options: [{ emoji: '😞', label: 'Guilty' }, { emoji: '😊', label: 'Happy' }, { emoji: '😴', label: 'Bored' }], correct: 'Guilty' },
  { level: 3, text: 'You helped a little kid who fell down at the park.', scene: '🤝', options: [{ emoji: '🤗', label: 'Kind' }, { emoji: '😢', label: 'Sad' }, { emoji: '😨', label: 'Nervous' }], correct: 'Kind' },
  { level: 3, text: 'Your friend shared their lunch with you when you forgot yours.', scene: '🥪', options: [{ emoji: '🤗', label: 'Grateful' }, { emoji: '😡', label: 'Angry' }, { emoji: '😴', label: 'Bored' }], correct: 'Grateful' },

  // Level 4 — Expert
  { level: 4, text: 'You want to join a game but the others say the team is full.', scene: '⚽', options: [{ emoji: '😞', label: 'Left Out' }, { emoji: '😊', label: 'Happy' }, { emoji: '😆', label: 'Excited' }], correct: 'Left Out' },
  { level: 4, text: 'Your friend won the race and you came second.', scene: '🏃', options: [{ emoji: '😤', label: 'Jealous' }, { emoji: '😨', label: 'Scared' }, { emoji: '😴', label: 'Sleepy' }], correct: 'Jealous' },
  { level: 4, text: 'You worked hard on a painting but your teacher didn\'t notice.', scene: '🎨', options: [{ emoji: '😞', label: 'Disappointed' }, { emoji: '😆', label: 'Excited' }, { emoji: '😡', label: 'Angry' }], correct: 'Disappointed' },
  { level: 4, text: 'A new kid joins your class and looks shy.', scene: '🧒', options: [{ emoji: '🤗', label: 'Sympathetic' }, { emoji: '😡', label: 'Angry' }, { emoji: '😴', label: 'Bored' }], correct: 'Sympathetic' },
  { level: 4, text: 'You told a joke and everyone laughed really hard!', scene: '😂', options: [{ emoji: '😆', label: 'Amused' }, { emoji: '😢', label: 'Sad' }, { emoji: '😨', label: 'Nervous' }], correct: 'Amused' },
  { level: 4, text: 'Your teacher asks you to read aloud in front of the class.', scene: '📚', options: [{ emoji: '😨', label: 'Nervous' }, { emoji: '😊', label: 'Happy' }, { emoji: '😴', label: 'Sleepy' }], correct: 'Nervous' },
];

export default {
  name: 'EmotionMatchGame',
  data() {
    return {
      currentLevel: 1,
      totalLevels: 4,
      index: 0,
      score: 0,
      levelScore: 0,
      selectedOption: null,
      feedback: null,
      showResult: false,
      questionKey: 0,
    };
  },
  computed: {
    levelQuestions() {
      return ALL_QUESTIONS.filter(q => q.level === this.currentLevel);
    },
    currentQuestion() {
      return this.levelQuestions[this.index] || null;
    },
    progressPercent() {
      if (!this.levelQuestions.length) return 0;
      return ((this.index) / this.levelQuestions.length) * 100;
    },
    earnedStars() {
      const pct = this.levelScore / this.levelQuestions.length;
      if (pct >= 0.9) return 3;
      if (pct >= 0.6) return 2;
      if (pct >= 0.3) return 1;
      return 0;
    },
  },
  methods: {
    answer(option) {
      if (this.feedback || !this.currentQuestion) return;
      this.selectedOption = option;
      const isCorrect = option === this.currentQuestion.correct;
      this.feedback = isCorrect ? 'correct' : 'wrong';
      if (isCorrect) {
        this.score++;
        this.levelScore++;
      }
      setTimeout(() => {
        this.feedback = null;
        this.selectedOption = null;
        if (this.index < this.levelQuestions.length - 1) {
          this.index++;
          this.questionKey++;
        } else {
          this.showResult = true;
        }
      }, 1200);
    },
    nextLevel() {
      this.currentLevel++;
      this.index = 0;
      this.levelScore = 0;
      this.showResult = false;
      this.questionKey++;
    },
    retryLevel() {
      this.index = 0;
      this.levelScore = 0;
      this.showResult = false;
      this.questionKey++;
    },
    confettiStyle(n) {
      const hue = (n * 47) % 360;
      return {
        left: Math.random() * 100 + '%',
        animationDelay: Math.random() * 2 + 's',
        background: `hsl(${hue}, 80%, 60%)`,
      };
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.emo-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 5s ease-in-out infinite; pointer-events: none; }
.d1 { top: 6%; right: 8%; } .d2 { top: 40%; left: 3%; animation-delay: 1s; }
.d3 { bottom: 15%; right: 5%; animation-delay: 2s; } .d4 { bottom: 30%; left: 6%; animation-delay: 0.5s; }
@keyframes floatBob { 0%,100%{transform:translateY(0) rotate(0)} 50%{transform:translateY(-14px) rotate(5deg)} }

.emo-header { text-align: center; margin-bottom: 16px; }
.emo-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.emo-header p { margin: 6px 0 0; color: #5A4690; font-size: 1rem; }

/* Progress bar */
.progress-bar-wrapper { display: flex; align-items: center; gap: 12px; max-width: 500px; margin: 0 auto 20px; }
.level-badge { background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; font-weight: 700; border-radius: 50px; padding: 6px 16px; white-space: nowrap; font-size: 0.85rem; }
.score-badge { font-weight: 700; color: #6C63FF; font-size: 1rem; white-space: nowrap; }
.progress-track { flex: 1; height: 12px; background: rgba(108,99,255,0.15); border-radius: 12px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #6C63FF, #A855F7); border-radius: 12px; transition: width 0.5s ease; }

/* Question card */
.q-card {
  max-width: 550px; margin: 0 auto 20px; padding: 28px;
  background: rgba(255,255,255,0.88); backdrop-filter: blur(10px);
  border: 2px solid #D4CCFF; border-radius: 24px;
  text-align: center; animation: cardIn 0.4s ease;
}
@keyframes cardIn { from{opacity:0;transform:translateY(20px) scale(0.95)} to{opacity:1;transform:translateY(0) scale(1)} }
.q-number { font-size: 0.8rem; color: #9B8CC4; font-weight: 600; margin-bottom: 8px; }
.q-scene { font-size: 3rem; margin-bottom: 10px; }
.q-text { color: #1A0B3E; font-weight: 700; font-size: 1.15rem; margin: 0 0 20px; }

.options { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; }
.opt-btn {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  border: 3px solid #E0D8FF; border-radius: 20px; padding: 16px 22px;
  background: rgba(255,255,255,0.9); cursor: pointer; font-family: 'Syne', sans-serif;
  transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); min-width: 100px;
}
.opt-btn:hover:not(:disabled) { transform: scale(1.08); border-color: #A855F7; box-shadow: 0 6px 20px rgba(108,99,255,0.2); }
.opt-emoji { font-size: 2.2rem; }
.opt-label { font-weight: 700; color: #3D2B7A; font-size: 0.95rem; }
.opt-btn.correct { border-color: #22c55e; background: rgba(34,197,94,0.15); animation: popCorrect 0.4s ease; }
.opt-btn.wrong { border-color: #ef4444; background: rgba(239,68,68,0.1); animation: shake 0.4s ease; }
.opt-btn.selected { transform: scale(1.05); }
@keyframes popCorrect { 0%{transform:scale(1)} 50%{transform:scale(1.15)} 100%{transform:scale(1.05)} }
@keyframes shake { 0%,100%{transform:translateX(0)} 25%{transform:translateX(-6px)} 75%{transform:translateX(6px)} }

.feedback-area { margin-top: 16px; }
.feedback-msg { padding: 10px 18px; border-radius: 16px; font-weight: 700; font-size: 1rem; animation: cardIn 0.3s ease; }
.feedback-msg.correct { background: rgba(34,197,94,0.12); color: #16a34a; }
.feedback-msg.wrong { background: rgba(239,68,68,0.08); color: #dc2626; }

/* Level complete */
.level-complete { position: relative; display: flex; justify-content: center; padding-top: 40px; }
.result-card {
  background: rgba(255,255,255,0.92); backdrop-filter: blur(12px);
  border: 2px solid #D4CCFF; border-radius: 28px; padding: 36px 44px;
  text-align: center; z-index: 2; animation: cardIn 0.5s ease;
}
.result-emoji { font-size: 4rem; margin-bottom: 10px; }
.result-card h2 { color: #1A0B3E; font-weight: 800; margin: 0 0 8px; }
.result-score { color: #5A4690; font-size: 1.1rem; }
.result-score strong { color: #6C63FF; }
.stars-row { margin: 14px 0; display: flex; justify-content: center; gap: 8px; }
.star { font-size: 2rem; opacity: 0.25; transition: all 0.3s ease; }
.star.earned { opacity: 1; animation: starPop 0.4s ease; }
@keyframes starPop { 0%{transform:scale(0)} 60%{transform:scale(1.3)} 100%{transform:scale(1)} }

.result-btns { display: flex; gap: 12px; justify-content: center; margin-top: 16px; }
.btn-retry, .btn-next {
  border: none; border-radius: 50px; padding: 12px 24px; font-family: 'Syne', sans-serif;
  font-weight: 700; font-size: 1rem; cursor: pointer; transition: all 0.3s ease;
}
.btn-retry { background: rgba(108,99,255,0.12); color: #6C63FF; }
.btn-retry:hover { background: rgba(108,99,255,0.2); }
.btn-next { background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; box-shadow: 0 6px 20px rgba(108,99,255,0.3); }
.btn-next:hover { transform: translateY(-2px); box-shadow: 0 10px 30px rgba(108,99,255,0.4); }

/* Confetti */
.confetti { position: absolute; inset: 0; overflow: hidden; pointer-events: none; }
.conf {
  position: absolute; top: -10px; width: 10px; height: 10px; border-radius: 50%;
  animation: confFall 3s ease-in forwards;
}
@keyframes confFall { to{transform:translateY(500px) rotate(720deg); opacity:0} }
</style>
