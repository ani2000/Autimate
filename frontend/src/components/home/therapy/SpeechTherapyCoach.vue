<template>
  <div class="speech-shell">
    <div class="float-deco d1">🗣️</div>
    <div class="float-deco d2">🎤</div>
    <div class="float-deco d3">⭐</div>
    <div class="float-deco d4">💬</div>

    <header class="sc-header">
      <h1>🎙️ Speech Therapy Coach</h1>
      <p>Practice speaking with fun prompts!</p>
    </header>

    <!-- Category tabs -->
    <div class="cat-tabs">
      <button
        v-for="cat in categories"
        :key="cat.name"
        class="cat-tab"
        :class="{ active: selectedCat === cat.name }"
        @click="selectCat(cat.name)"
      >{{ cat.emoji }} {{ cat.name }}</button>
    </div>

    <!-- Progress -->
    <div class="progress-row">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
      </div>
      <span class="progress-label">{{ index + 1 }} / {{ currentPrompts.length }}</span>
    </div>

    <!-- Prompt card -->
    <div class="prompt-card">
      <div class="prompt-level">{{ currentPrompts[index].level }}</div>
      <div class="prompt-emoji">{{ currentPrompts[index].emoji }}</div>
      <h2 class="prompt-title">{{ currentPrompts[index].title }}</h2>
      <p class="prompt-text">{{ currentPrompts[index].text }}</p>
      <div class="prompt-tip" v-if="currentPrompts[index].tip">
        💡 <em>{{ currentPrompts[index].tip }}</em>
      </div>
    </div>

    <!-- Navigation -->
    <div class="nav-row">
      <button class="btn-nav" :disabled="index === 0" @click="prev">⬅️ Previous</button>
      <button class="btn-speak" @click="speakPrompt">🔊 Listen</button>
      <button class="btn-nav" :disabled="index === currentPrompts.length - 1" @click="next">Next ➡️</button>
    </div>

    <!-- Practice checkmarks -->
    <div class="practice-track">
      <span>Practice tracker:</span>
      <div class="dots">
        <button
          v-for="(p, pi) in currentPrompts"
          :key="pi"
          class="dot"
          :class="{ done: practiced.has(selectedCat + pi), current: pi === index }"
          @click="index = pi"
        >{{ practiced.has(selectedCat + pi) ? '✅' : '⬜' }}</button>
      </div>
    </div>

    <!-- Parent notes -->
    <div class="notes-section">
      <h3>📝 Parent Notes</h3>
      <textarea v-model="notes" rows="3" placeholder="What worked well today?"></textarea>
      <button class="btn-save" @click="save">💾 Save Session Note</button>
    </div>

    <transition name="fade">
      <div class="save-toast" v-if="showToast">✅ Session note saved!</div>
    </transition>
  </div>
</template>

<script>
const ALL_PROMPTS = {
  'Naming': [
    { emoji: '🐶', title: 'Name Animals', text: 'Point to and say each animal: Dog, Cat, Bird, Fish, Bunny', level: 'Easy', tip: 'Use toy animals if available!' },
    { emoji: '🍎', title: 'Name Fruits', text: 'Say each fruit: Apple, Banana, Orange, Grape, Mango', level: 'Easy', tip: 'Hold real fruit for multisensory learning' },
    { emoji: '🎨', title: 'Name Colors', text: 'Point to things and say: Red, Blue, Green, Yellow, Purple', level: 'Easy', tip: 'Walk around the room finding each color' },
    { emoji: '👕', title: 'Name Clothes', text: 'Say: Shirt, Pants, Shoes, Hat, Socks', level: 'Medium', tip: 'Touch each item while saying it' },
  ],
  'Requests': [
    { emoji: '💧', title: 'Ask for Things', text: 'Practice: "I want water please" / "Can I have a cookie?"', level: 'Easy', tip: 'Model the sentence first, then let child try' },
    { emoji: '🧩', title: 'Play Requests', text: 'Say: "Can I play?" / "Will you play with me?" / "My turn!"', level: 'Medium', tip: 'Practice during actual playtime!' },
    { emoji: '🆘', title: 'Ask for Help', text: 'Practice: "Help me please" / "I need help with this"', level: 'Medium', tip: 'Set up situations where child needs help' },
    { emoji: '🤔', title: 'Ask Questions', text: 'Practice: "What is that?" / "Where is mommy?" / "Why?"', level: 'Hard', tip: 'Answer enthusiastically to reinforce asking!' },
  ],
  'Social': [
    { emoji: '👋', title: 'Greetings', text: 'Say clearly: "Hello!" / "Hi!" / "Good morning!" / "Bye bye!"', level: 'Easy', tip: 'Wave while saying each greeting' },
    { emoji: '😊', title: 'Feelings', text: 'Name emotions: "I feel happy" / "I am sad" / "I am excited"', level: 'Medium', tip: 'Use mirror to make matching faces' },
    { emoji: '🙏', title: 'Polite Words', text: 'Practice: "Please" / "Thank you" / "Sorry" / "Excuse me"', level: 'Easy', tip: 'Role-play situations needing these words' },
    { emoji: '👫', title: 'Friendly Talk', text: '"My name is..." / "What is your name?" / "Do you like...?"', level: 'Hard', tip: 'Practice with stuffed animals first' },
  ],
  'Sentences': [
    { emoji: '📝', title: 'Two Words', text: 'Combine: "Want milk" / "Big dog" / "More please" / "Go outside"', level: 'Easy', tip: 'Accept any two-word combo as success!' },
    { emoji: '📖', title: 'Three Words', text: 'Build up: "I want milk" / "Big red ball" / "Go to park"', level: 'Medium', tip: 'Expand child\'s two-word attempts to three' },
    { emoji: '🌟', title: 'Full Sentences', text: '"I want to go outside" / "Can we read a book?" / "I like the blue car"', level: 'Hard', tip: 'Don\'t correct grammar — celebrate the attempt!' },
    { emoji: '📚', title: 'Story Telling', text: 'Look at a picture and describe: Who? What? Where?', level: 'Hard', tip: 'Start with "I see..." to help them begin' },
  ],
};

export default {
  name: 'SpeechTherapyCoach',
  data() {
    return {
      selectedCat: 'Naming',
      categories: [
        { name: 'Naming', emoji: '🏷️' },
        { name: 'Requests', emoji: '🙋' },
        { name: 'Social', emoji: '👫' },
        { name: 'Sentences', emoji: '📝' },
      ],
      index: 0,
      practiced: new Set(JSON.parse(localStorage.getItem('speech_practiced') || '[]')),
      notes: localStorage.getItem('speech_therapy_note') || '',
      showToast: false,
    };
  },
  computed: {
    currentPrompts() { return ALL_PROMPTS[this.selectedCat]; },
    progressPct() { return ((this.index + 1) / this.currentPrompts.length) * 100; },
  },
  methods: {
    selectCat(name) { this.selectedCat = name; this.index = 0; },
    next() {
      this.practiced.add(this.selectedCat + this.index);
      this.savePracticed();
      if (this.index < this.currentPrompts.length - 1) this.index++;
    },
    prev() { if (this.index > 0) this.index--; },
    speakPrompt() {
      const u = new SpeechSynthesisUtterance(this.currentPrompts[this.index].text);
      u.rate = 0.85; u.pitch = 1.1;
      speechSynthesis.cancel();
      speechSynthesis.speak(u);
    },
    save() {
      localStorage.setItem('speech_therapy_note', this.notes || '');
      this.showToast = true;
      setTimeout(() => { this.showToast = false; }, 2000);
    },
    savePracticed() {
      localStorage.setItem('speech_practiced', JSON.stringify([...this.practiced]));
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.speech-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #EDE9FE 0%, #FFF1E6 50%, #E0F2FE 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 6s ease-in-out infinite; pointer-events: none; }
.d1 { top: 8%; right: 6%; } .d2 { bottom: 18%; left: 5%; animation-delay: 1.5s; }
.d3 { top: 28%; left: 4%; animation-delay: 3s; } .d4 { bottom: 28%; right: 5%; animation-delay: 0.8s; }
@keyframes floatBob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.sc-header { text-align: center; margin-bottom: 16px; }
.sc-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.sc-header p { margin: 4px 0 0; color: #5A4690; }

/* Category tabs */
.cat-tabs { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; margin-bottom: 14px; }
.cat-tab {
  border: 2px solid #D4CCFF; border-radius: 50px; padding: 8px 18px;
  background: rgba(255,255,255,0.8); font-family: 'Syne', sans-serif;
  font-weight: 600; color: #5A4690; cursor: pointer; transition: all 0.25s;
}
.cat-tab.active { background: linear-gradient(135deg,#6C63FF,#A855F7); color:#fff; border-color: transparent; }

/* Progress */
.progress-row { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.progress-bar { flex: 1; height: 8px; background: rgba(255,255,255,0.6); border-radius: 50px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg,#6C63FF,#A855F7); border-radius: 50px; transition: width 0.4s ease; }
.progress-label { font-weight: 700; color: #5A4690; font-size: 0.85rem; }

/* Prompt card */
.prompt-card {
  background: rgba(255,255,255,0.9); border: 2px solid #D4CCFF; border-radius: 22px;
  padding: 24px; text-align: center; backdrop-filter: blur(10px);
  box-shadow: 0 6px 24px rgba(108,99,255,0.08); margin-bottom: 16px;
}
.prompt-level {
  display: inline-block; padding: 3px 12px; border-radius: 50px; font-size: 0.72rem;
  font-weight: 700; background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; margin-bottom: 8px;
}
.prompt-emoji { font-size: 3rem; margin-bottom: 6px; }
.prompt-title { margin: 0; font-size: 1.3rem; font-weight: 800; color: #1A0B3E; }
.prompt-text { margin: 10px 0; font-size: 1.05rem; color: #3D2B7A; line-height: 1.5; }
.prompt-tip {
  margin-top: 10px; padding: 8px 16px; background: rgba(237,233,254,0.6);
  border-radius: 12px; font-size: 0.82rem; color: #5A4690;
}

/* Navigation */
.nav-row { display: flex; gap: 10px; justify-content: center; margin-bottom: 14px; }
.btn-nav, .btn-speak {
  border: none; border-radius: 50px; padding: 12px 24px; font-family: 'Syne', sans-serif;
  font-weight: 700; cursor: pointer; transition: all 0.25s;
}
.btn-nav { background: rgba(255,255,255,0.85); border: 2px solid #D4CCFF; color: #5A4690; }
.btn-nav:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-nav:hover:not(:disabled) { border-color: #6C63FF; }
.btn-speak { background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; box-shadow: 0 4px 14px rgba(108,99,255,0.2); }
.btn-speak:hover { transform: translateY(-2px); }

/* Practice tracker */
.practice-track { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; font-size: 0.85rem; color: #5A4690; font-weight: 600; }
.dots { display: flex; gap: 4px; flex-wrap: wrap; }
.dot {
  width: 30px; height: 30px; border-radius: 8px; border: 2px solid #E8E0FF;
  background: rgba(255,255,255,0.7); font-size: 0.85rem; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.dot.current { border-color: #6C63FF; box-shadow: 0 0 8px rgba(108,99,255,0.3); }
.dot.done { background: rgba(134,239,172,0.3); border-color: #86EFAC; }

/* Notes */
.notes-section {
  background: rgba(255,255,255,0.85); border: 2px solid #E8E0FF; border-radius: 18px;
  padding: 16px 20px; backdrop-filter: blur(6px);
}
.notes-section h3 { margin: 0 0 8px; color: #1A0B3E; font-weight: 800; }
.notes-section textarea {
  width: 100%; border: 2px solid #D4CCFF; border-radius: 12px; padding: 10px;
  font-family: 'Syne', sans-serif; font-size: 0.9rem; resize: vertical; outline: none;
}
.notes-section textarea:focus { border-color: #6C63FF; }
.btn-save {
  margin-top: 8px; border: none; border-radius: 50px; padding: 10px 24px;
  font-family: 'Syne', sans-serif; font-weight: 700; background: linear-gradient(135deg,#6C63FF,#A855F7);
  color: #fff; cursor: pointer;
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
