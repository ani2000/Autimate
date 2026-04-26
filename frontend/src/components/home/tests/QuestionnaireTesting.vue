<template>
  <section class="qna-shell">
    <div class="deco d1">📝</div>
    <div class="deco d2">💜</div>
    <div class="deco d3">⭐</div>
    <div class="deco d4">🧸</div>

    <header class="qna-head">
      <div class="head-mascot">🧩</div>
      <h1>Screening Questionnaire</h1>
      <p>Please answer the following questions about your child to help us understand their needs better.</p>
      <div class="step-dots">
        <div v-for="s in totalSteps" :key="s" class="step-dot" :class="{ active: s === step, done: s < step }">
          <span class="dot-num">{{ s < step ? '✓' : s }}</span>
          <span class="dot-label">{{ s === 1 ? 'Page 1 (5)' : 'Page 2 (6)' }}</span>
        </div>
      </div>

      <div class="progress-wrap">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: `${(step / totalSteps) * 100}%` }"></div>
        </div>
        <span class="progress-text">Page {{ step }} of {{ totalSteps }}</span>
      </div>
    </header>

    <transition name="slide" mode="out-in">
      <div class="qna-body" :key="step">
        <SingleQuestion
          v-for="q in currentQuestions"
          :key="q.index"
          :question="q.question"
          :options="q.options"
          :value="answers[q.index]"
          :index="q.index"
          @update-answer="updateAnswer"
        />
      </div>
    </transition>

    <p v-if="errorMessage" class="error-msg">⚠️ {{ errorMessage }}</p>

    <div class="button-wrapper">
      <button class="action-btn ghost" @click="prevSection" :disabled="step === 1">⬅ Previous</button>
      <button v-if="step < totalSteps" class="action-btn" @click="nextSection">Next ➡</button>
      <button v-else class="action-btn final" @click="proceedToBehavioralTest">🎬 Proceed to Video Test</button>
    </div>
  </section>
</template>

<script>
import SingleQuestion from './SingleQuestion.vue';
import Cookies from 'js-cookie';

export default {
  components: {
    SingleQuestion,
  },
  data() {
    return {
      step: 1,
      totalSteps: 2,
      answers: Array(14).fill(null),
      errorMessage: '',
      questions: [
        { index: 0, question: 'Does your child look at you when you call his/her name?', options: ['Always', 'Usually', 'Sometimes', 'Rarely', 'Never'] },
        { index: 1, question: 'How easy is it for you to get eye contact with your child?', options: ['Very easy', 'Quite easy', 'Quite difficult', 'Very difficult', 'Impossible'] },
        { index: 2, question: 'Does your child point to indicate that s/he wants something? (e.g. a toy that is out of reach)', options: ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'] },
        { index: 3, question: 'Does your child point to share interest with you? (e.g. pointing at an interesting sight)', options: ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'] },
        { index: 4, question: 'Does your child pretend? (e.g. care for dolls, talk on a toy phone)', options: ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'] },
        { index: 5, question: "Does your child follow where you're looking?", options: ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'] },
        { index: 6, question: 'When someone else in the family is visibly upset, does your child show signs of wanting to comfort them? (e.g. stroking hair, hugging them)', options: ['Always', 'Usually', 'Sometimes', 'Rarely', 'Never'] },
        { index: 7, question: "Would you describe your child's first words as:", options: ['Very typical', 'Quite typical', 'Slightly unusual', 'Very unusual', "My child doesn't speak"] },
        { index: 8, question: 'Does your child use simple gestures? (e.g. wave goodbye)', options: ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'] },
        { index: 9, question: 'Does your child stare at nothing with no apparent purpose?', options: ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'] },
        { index: 10, question: 'Does any family member of this child have a history of autism?', options: ['Yes', 'No'] },
      ],
    };
  },
  computed: {
    currentIndexes() {
      return this.step === 1 ? [0, 1, 2, 3, 4] : [5, 6, 7, 8, 9, 10];
    },
    currentQuestions() {
      return this.questions.filter(q => this.currentIndexes.includes(q.index));
    },
  },
  methods: {
    hasAnsweredCurrentSection() {
      return this.currentIndexes.every(index => this.answers[index] !== null && this.answers[index] !== undefined);
    },
    updateAnswer({ answer, index }) {
      this.answers[index] = answer;
      this.errorMessage = '';
    },
    nextSection() {
      if (!this.hasAnsweredCurrentSection()) {
        this.errorMessage = 'Please answer all questions in this section before moving forward.';
        return;
      }
      if (this.step < this.totalSteps) {
        this.step++;
      }
    },
    prevSection() {
      this.errorMessage = '';
      if (this.step > 1) this.step--;
    },
    proceedToBehavioralTest() {
      if (!this.hasAnsweredCurrentSection()) {
        this.errorMessage = 'Please answer all remaining questions first.';
        return;
      }

      let childAge = Cookies.get('child_age');
      let childGender = Cookies.get('child_gender');

      if (childAge === undefined) {
        childAge = 0;
      }

      this.answers[12] = childAge;
      this.answers[13] = childGender;

      this.answers[11] = this.answers.slice(0, 10).reduce((sum, answer) => {
        const value = parseInt(answer, 10) || 0;
        return sum + value;
      }, 0);

      this.$router.push({
        path: '/dashboard/aex/video',
        query: {
          questionAnswers: JSON.stringify(this.answers.filter((_, index) => index < 14)),
        },
      });
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.qna-shell {
  width: 100%;
  max-width: 960px;
  margin: 16px auto;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  border: 2px solid rgba(255, 255, 255, 0.7);
  border-radius: 28px;
  box-shadow: 0 20px 50px rgba(100, 50, 200, 0.1);
  padding: 28px 24px;
  font-family: 'Syne', sans-serif;
  position: relative;
  overflow: hidden;
  animation: cardPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes cardPop {
  from { opacity: 0; transform: scale(0.95) translateY(16px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

/* Floating decorations */
.deco { position: absolute; font-size: 1.6rem; opacity: 0.12; animation: floatDeco 6s ease-in-out infinite; pointer-events: none; z-index: 0; }
.d1 { top: 8%; right: 5%; animation-delay: 0s; }
.d2 { top: 50%; right: 8%; animation-delay: 1.5s; }
.d3 { bottom: 10%; left: 4%; animation-delay: 3s; }
.d4 { top: 15%; left: 6%; animation-delay: 4.5s; }
@keyframes floatDeco { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-14px)} }

.qna-head { position: relative; z-index: 1; text-align: center; }
.head-mascot { font-size: 2.5rem; margin-bottom: 4px; animation: mascotBounce 2s ease-in-out infinite; }
@keyframes mascotBounce { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-6px)} }

.qna-head h1 {
  margin: 0;
  color: #1A0B3E;
  font-weight: 800;
  font-size: 1.5rem;
}
.qna-head p {
  margin: 6px 0 16px;
  color: #5A4690;
}

/* Step dots */
.step-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.step-dot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  opacity: 0.45;
  transition: all 0.3s ease;
}
.step-dot.active {
  opacity: 1;
  transform: scale(1.1);
}
.step-dot.done { opacity: 0.8; }
.dot-num {
  width: 34px; height: 34px;
  display: grid; place-items: center;
  border-radius: 50%;
  font-weight: 800;
  font-size: 0.8rem;
  background: #E8E0FF;
  color: #6C63FF;
  transition: all 0.3s ease;
}
.step-dot.active .dot-num {
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  box-shadow: 0 4px 14px rgba(108, 99, 255, 0.35);
}
.step-dot.done .dot-num {
  background: #22c55e;
  color: #fff;
}
.dot-label {
  font-size: 0.68rem;
  font-weight: 600;
  color: #5A4690;
}

.progress-wrap { margin-bottom: 8px; }
.progress-bar {
  height: 10px;
  background: #E8E0FF;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6C63FF, #A855F7, #FF85A1);
  transition: width 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  border-radius: 999px;
}
.progress-text {
  display: block;
  font-size: 0.78rem;
  font-weight: 700;
  color: #6C63FF;
  margin-top: 6px;
}

/* Slide transition */
.slide-enter-active, .slide-leave-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.slide-enter-from { opacity: 0; transform: translateX(30px); }
.slide-leave-to { opacity: 0; transform: translateX(-30px); }

.qna-body {
  margin-top: 10px;
  position: relative;
  z-index: 1;
}

.error-msg {
  color: #b91c1c;
  margin: 10px 0 0;
  font-weight: 600;
  text-align: center;
  padding: 10px 16px;
  border-radius: 14px;
  background: rgba(185, 28, 28, 0.06);
  border: 1.5px solid rgba(185, 28, 28, 0.15);
  position: relative;
  z-index: 1;
}

.button-wrapper {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.action-btn {
  border: 0;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  padding: 13px 28px;
  border-radius: 50px;
  font-weight: 700;
  font-family: 'Syne', sans-serif;
  font-size: 0.95rem;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(108, 99, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.action-btn:hover {
  transform: translateY(-3px) scale(1.03);
  box-shadow: 0 10px 28px rgba(108, 99, 255, 0.4);
}
.action-btn.final {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  box-shadow: 0 6px 20px rgba(34, 197, 94, 0.3);
}
.action-btn.final:hover {
  box-shadow: 0 10px 28px rgba(34, 197, 94, 0.4);
}

.action-btn.ghost {
  background: rgba(255, 255, 255, 0.8);
  color: #6C63FF;
  border: 2px solid #D4CCFF;
  box-shadow: 0 4px 14px rgba(108, 99, 255, 0.1);
}
.action-btn.ghost:hover {
  border-color: #A855F7;
  background: #fff;
}
.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 900px) {
  .qna-shell { margin: 12px 10px; padding: 20px 16px; }
}
@media (max-width: 640px) {
  .qna-shell { border-radius: 20px; padding: 16px 12px; }
  .qna-head h1 { font-size: 1.2rem; }
  .step-dots { gap: 4px; }
  .dot-label { display: none; }
  .button-wrapper { justify-content: center; }
  .action-btn { padding: 11px 20px; font-size: 0.9rem; }
}
</style>
