<template>
  <div class="flash-shell">
    <div class="float-deco d1">📚</div>
    <div class="float-deco d2">💡</div>
    <div class="float-deco d3">⭐</div>
    <div class="float-deco d4">🧠</div>

    <header class="flash-header">
      <h1>🃏 Flashcard Quest</h1>
      <p>Flip cards to learn, then test your memory!</p>
    </header>

    <!-- Mode toggle -->
    <div class="mode-toggle">
      <button :class="['mode-btn', { active: mode === 'learn' }]" @click="mode = 'learn'">📖 Learn</button>
      <button :class="['mode-btn', { active: mode === 'quiz' }]" @click="startQuiz">🧪 Quiz Me!</button>
    </div>

    <!-- Category selector -->
    <div class="cat-row">
      <button
        v-for="cat in categories"
        :key="cat.name"
        :class="['cat-chip', { active: activeCat === cat.name }]"
        @click="activeCat = cat.name"
      >{{ cat.emoji }} {{ cat.name }}</button>
    </div>

    <!-- Learn Mode -->
    <div v-if="mode === 'learn'" class="learn-grid">
      <div
        v-for="(card, i) in activeCards"
        :key="card.word + i"
        :class="['flip-card', { flipped: card.flipped }]"
        @click="card.flipped = !card.flipped"
      >
        <div class="flip-inner">
          <div class="flip-front">
            <span class="card-emoji">{{ card.emoji }}</span>
            <span class="card-word">{{ card.word }}</span>
            <span class="tap-hint">Tap to flip!</span>
          </div>
          <div class="flip-back">
            <span class="card-meaning">{{ card.meaning }}</span>
            <span class="card-example" v-if="card.example">"{{ card.example }}"</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Quiz Mode -->
    <div v-if="mode === 'quiz' && quizQuestion" class="quiz-area">
      <div class="quiz-progress">
        <span>Question {{ quizIndex + 1 }} / {{ quizCards.length }}</span>
        <span>⭐ {{ quizScore }}</span>
      </div>
      <div class="quiz-card" :key="quizIndex">
        <div class="quiz-emoji">{{ quizQuestion.emoji }}</div>
        <h3 class="quiz-prompt">What does <strong>{{ quizQuestion.word }}</strong> mean?</h3>
        <div class="quiz-options">
          <button
            v-for="opt in quizQuestion.choices"
            :key="opt"
            :class="['quiz-opt', { correct: quizFeedback === 'correct' && opt === quizQuestion.meaning, wrong: quizFeedback === 'wrong' && opt === quizSelected }]"
            @click="answerQuiz(opt)"
            :disabled="!!quizFeedback"
          >{{ opt }}</button>
        </div>
        <div class="quiz-feedback" v-if="quizFeedback">
          <span v-if="quizFeedback === 'correct'" class="fb-correct">🎉 Correct!</span>
          <span v-else class="fb-wrong">😅 The answer is: {{ quizQuestion.meaning }}</span>
        </div>
      </div>
    </div>

    <!-- Quiz Done -->
    <div v-if="mode === 'quiz' && !quizQuestion && quizCards.length" class="quiz-done">
      <div class="done-card">
        <div class="done-emoji">{{ quizScore >= quizCards.length * 0.8 ? '🏆' : '⭐' }}</div>
        <h2>Quiz Complete!</h2>
        <p>You got <strong>{{ quizScore }}/{{ quizCards.length }}</strong> right!</p>
        <div class="done-btns">
          <button class="btn-learn" @click="mode = 'learn'">📖 Review Cards</button>
          <button class="btn-retry" @click="startQuiz">🔄 Try Again</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FlashcardGame',
  data() {
    return {
      mode: 'learn',
      activeCat: 'Emotions',
      categories: [
        {
          name: 'Emotions', emoji: '😊',
          cards: [
            { word: 'Happy', emoji: '😊', meaning: 'Feeling good and smiling', example: 'I am happy when I play!' },
            { word: 'Sad', emoji: '😢', meaning: 'Feeling low or upset', example: 'I feel sad when it rains.' },
            { word: 'Angry', emoji: '😡', meaning: 'Feeling mad inside', example: 'I get angry when someone is mean.' },
            { word: 'Scared', emoji: '😨', meaning: 'Feeling afraid of something', example: 'Loud noises make me scared.' },
            { word: 'Excited', emoji: '🤩', meaning: 'Feeling very eager and happy', example: 'I am excited for my birthday!' },
            { word: 'Calm', emoji: '😌', meaning: 'Feeling peaceful and relaxed', example: 'Deep breaths make me calm.' },
            { word: 'Proud', emoji: '🥰', meaning: 'Feeling good about what you did', example: 'I feel proud when I help others.' },
            { word: 'Shy', emoji: '🫣', meaning: 'Feeling nervous around people', example: 'I feel shy meeting new friends.' },
          ],
        },
        {
          name: 'Manners', emoji: '🤝',
          cards: [
            { word: 'Please', emoji: '🙏', meaning: 'A polite way to ask for something', example: 'Can I have water, please?' },
            { word: 'Thank You', emoji: '💐', meaning: 'Showing you are grateful', example: 'Thank you for the gift!' },
            { word: 'Sorry', emoji: '😔', meaning: 'Saying you feel bad about something', example: 'Sorry I bumped into you.' },
            { word: 'Share', emoji: '🤲', meaning: 'Letting others have a turn', example: 'Let\'s share the crayons!' },
            { word: 'Listen', emoji: '👂', meaning: 'Paying attention to someone talking', example: 'Listen when the teacher talks.' },
            { word: 'Wait', emoji: '⏳', meaning: 'Being patient for your turn', example: 'Wait in line for the slide.' },
          ],
        },
        {
          name: 'Actions', emoji: '🏃',
          cards: [
            { word: 'Run', emoji: '🏃', meaning: 'Moving your legs very fast', example: 'I run in the park!' },
            { word: 'Jump', emoji: '🦘', meaning: 'Pushing off the ground with your feet', example: 'Jump over the puddle!' },
            { word: 'Clap', emoji: '👏', meaning: 'Hitting your hands together', example: 'Clap for the show!' },
            { word: 'Wave', emoji: '👋', meaning: 'Moving your hand to say hi or bye', example: 'Wave goodbye to grandma!' },
            { word: 'Hug', emoji: '🤗', meaning: 'Wrapping your arms around someone', example: 'I love a warm hug.' },
            { word: 'Dance', emoji: '💃', meaning: 'Moving your body to music', example: 'Dance to the music!' },
          ],
        },
        {
          name: 'Nature', emoji: '🌿',
          cards: [
            { word: 'Sun', emoji: '☀️', meaning: 'The bright star that gives us light', example: 'The sun is warm today.' },
            { word: 'Rain', emoji: '🌧️', meaning: 'Water falling from clouds', example: 'Rain makes flowers grow.' },
            { word: 'Tree', emoji: '🌳', meaning: 'A tall plant with leaves', example: 'Birds sit in the tree.' },
            { word: 'Flower', emoji: '🌸', meaning: 'The colorful part of a plant', example: 'This flower smells nice!' },
            { word: 'Moon', emoji: '🌙', meaning: 'The light we see at night', example: 'The moon is so bright!' },
            { word: 'Star', emoji: '⭐', meaning: 'Tiny lights in the night sky', example: 'I count the stars at night.' },
          ],
        },
      ],
      // quiz
      quizCards: [],
      quizIndex: 0,
      quizScore: 0,
      quizFeedback: null,
      quizSelected: null,
    };
  },
  computed: {
    activeCards() {
      const cat = this.categories.find(c => c.name === this.activeCat);
      return cat ? cat.cards.map(c => ({ ...c, flipped: false })) : [];
    },
    quizQuestion() {
      return this.quizCards[this.quizIndex] || null;
    },
  },
  methods: {
    startQuiz() {
      const cat = this.categories.find(c => c.name === this.activeCat);
      if (!cat) return;
      const allMeanings = cat.cards.map(c => c.meaning);
      this.quizCards = cat.cards.map(card => {
        const wrong = allMeanings.filter(m => m !== card.meaning).sort(() => Math.random() - 0.5).slice(0, 2);
        const choices = [card.meaning, ...wrong].sort(() => Math.random() - 0.5);
        return { ...card, choices };
      }).sort(() => Math.random() - 0.5);
      this.quizIndex = 0;
      this.quizScore = 0;
      this.quizFeedback = null;
      this.quizSelected = null;
      this.mode = 'quiz';
    },
    answerQuiz(opt) {
      if (this.quizFeedback) return;
      this.quizSelected = opt;
      const isCorrect = opt === this.quizQuestion.meaning;
      this.quizFeedback = isCorrect ? 'correct' : 'wrong';
      if (isCorrect) this.quizScore++;
      setTimeout(() => {
        this.quizFeedback = null;
        this.quizSelected = null;
        this.quizIndex++;
      }, 1200);
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.flash-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 5s ease-in-out infinite; pointer-events: none; }
.d1 { top: 5%; right: 7%; } .d2 { top: 35%; left: 4%; animation-delay: 1.2s; }
.d3 { bottom: 18%; right: 6%; animation-delay: 2s; } .d4 { bottom: 35%; left: 5%; animation-delay: 0.8s; }
@keyframes floatBob { 0%,100%{transform:translateY(0) rotate(0)} 50%{transform:translateY(-14px) rotate(5deg)} }

.flash-header { text-align: center; margin-bottom: 16px; }
.flash-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.flash-header p { margin: 6px 0 0; color: #5A4690; }

/* Mode toggle */
.mode-toggle { display: flex; gap: 10px; justify-content: center; margin-bottom: 14px; }
.mode-btn {
  border: 2px solid #D4CCFF; border-radius: 50px; padding: 10px 22px;
  background: rgba(255,255,255,0.8); font-family: 'Syne', sans-serif;
  font-weight: 700; color: #5A4690; cursor: pointer; font-size: 1rem;
  transition: all 0.25s ease;
}
.mode-btn.active { background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; border-color: transparent; box-shadow: 0 5px 18px rgba(108,99,255,0.3); }

/* Categories */
.cat-row { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; margin-bottom: 20px; }
.cat-chip {
  border: 2px solid #E0D8FF; border-radius: 50px; padding: 6px 14px;
  background: rgba(255,255,255,0.7); font-family: 'Syne', sans-serif;
  font-weight: 600; color: #5A4690; cursor: pointer; font-size: 0.85rem;
  transition: all 0.2s ease;
}
.cat-chip.active { background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; border-color: transparent; }

/* Flip cards */
.learn-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; max-width: 800px; margin: 0 auto; }
.flip-card { height: 200px; perspective: 800px; cursor: pointer; }
.flip-inner {
  position: relative; width: 100%; height: 100%;
  transition: transform 0.6s cubic-bezier(0.34,1.56,0.64,1);
  transform-style: preserve-3d;
}
.flip-card.flipped .flip-inner { transform: rotateY(180deg); }
.flip-front, .flip-back {
  position: absolute; inset: 0; backface-visibility: hidden;
  border-radius: 20px; display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 16px; text-align: center;
}
.flip-front {
  background: rgba(255,255,255,0.9); backdrop-filter: blur(8px);
  border: 2px solid #D4CCFF;
}
.flip-back {
  background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff;
  border: 2px solid #A855F7; transform: rotateY(180deg);
}
.card-emoji { font-size: 2.5rem; margin-bottom: 6px; }
.card-word { font-weight: 800; color: #1A0B3E; font-size: 1.2rem; }
.tap-hint { font-size: 0.7rem; color: #9B8CC4; margin-top: 8px; }
.card-meaning { font-weight: 700; font-size: 1rem; margin-bottom: 8px; }
.card-example { font-size: 0.8rem; opacity: 0.85; font-style: italic; }

/* Quiz mode */
.quiz-area { max-width: 500px; margin: 0 auto; }
.quiz-progress { display: flex; justify-content: space-between; margin-bottom: 14px; font-weight: 700; color: #5A4690; }
.quiz-card {
  background: rgba(255,255,255,0.9); backdrop-filter: blur(10px);
  border: 2px solid #D4CCFF; border-radius: 24px; padding: 28px;
  text-align: center; animation: cardIn 0.4s ease;
}
@keyframes cardIn { from{opacity:0;transform:translateY(16px) scale(0.96)} to{opacity:1;transform:translateY(0) scale(1)} }
.quiz-emoji { font-size: 3rem; margin-bottom: 10px; }
.quiz-prompt { color: #1A0B3E; font-weight: 700; margin: 0 0 18px; }
.quiz-prompt strong { color: #6C63FF; }

.quiz-options { display: flex; flex-direction: column; gap: 10px; }
.quiz-opt {
  border: 2px solid #E0D8FF; border-radius: 16px; padding: 14px 18px;
  background: rgba(255,255,255,0.85); font-family: 'Syne', sans-serif;
  font-weight: 600; color: #3D2B7A; cursor: pointer; text-align: left;
  transition: all 0.25s ease;
}
.quiz-opt:hover:not(:disabled) { border-color: #A855F7; transform: translateX(4px); }
.quiz-opt.correct { border-color: #22c55e; background: rgba(34,197,94,0.15); }
.quiz-opt.wrong { border-color: #ef4444; background: rgba(239,68,68,0.1); }

.quiz-feedback { margin-top: 14px; font-weight: 700; }
.fb-correct { color: #16a34a; }
.fb-wrong { color: #dc2626; }

/* Quiz done */
.quiz-done { display: flex; justify-content: center; padding-top: 30px; }
.done-card {
  background: rgba(255,255,255,0.92); backdrop-filter: blur(10px);
  border: 2px solid #D4CCFF; border-radius: 24px; padding: 32px 40px;
  text-align: center; animation: cardIn 0.5s ease;
}
.done-emoji { font-size: 3.5rem; }
.done-card h2 { color: #1A0B3E; font-weight: 800; margin: 8px 0; }
.done-card p { color: #5A4690; }
.done-card strong { color: #6C63FF; }
.done-btns { display: flex; gap: 10px; justify-content: center; margin-top: 16px; }
.btn-learn, .btn-retry {
  border: none; border-radius: 50px; padding: 10px 22px; font-family: 'Syne', sans-serif;
  font-weight: 700; cursor: pointer; transition: all 0.3s ease;
}
.btn-learn { background: rgba(108,99,255,0.12); color: #6C63FF; }
.btn-retry { background: linear-gradient(135deg, #6C63FF, #A855F7); color: #fff; box-shadow: 0 5px 18px rgba(108,99,255,0.3); }
</style>
