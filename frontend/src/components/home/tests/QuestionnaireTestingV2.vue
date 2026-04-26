<template>
  <div class="questionnaire-page">
    <div class="head">
      <h1>ASD Screening Questionnaire</h1>
      <p>Page {{ step }} of {{ totalSteps }} · 5-6 MCQs per page.</p>
    </div>

    <div class="question-card">
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

    <p v-if="errorMessage" class="err">Please answer all questions on this page first.</p>

    <div class="actions">
      <button class="nav-btn" @click="prevSection" :disabled="step === 1">Previous</button>
      <button v-if="step < totalSteps" class="nav-btn primary" @click="nextSection">Next</button>
      <button v-else class="nav-btn primary" @click="proceedToBehavioralTest">Proceed to Behavioral Test</button>
    </div>
  </div>
</template>

<script>
import SingleQuestion from './SingleQuestion.vue';
import Cookies from 'js-cookie';

export default {
  components: { SingleQuestion },
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
    updateAnswer({ answer, index }) { this.answers[index] = answer; this.errorMessage = ''; },
    hasAnsweredCurrentSection() {
      return this.currentIndexes.every(index => this.answers[index] !== null && this.answers[index] !== undefined);
    },
    nextSection() {
      if (!this.hasAnsweredCurrentSection()) {
        this.errorMessage = 'Please answer all questions in this page.';
        return;
      }
      if (this.step < this.totalSteps) this.step++;
    },
    prevSection() { if (this.step > 1) this.step--; },
    proceedToBehavioralTest() {
      if (!this.hasAnsweredCurrentSection()) {
        this.errorMessage = 'Please answer all remaining questions.';
        return;
      }
      let childAge = Cookies.get('child_age');
      let childGender = Cookies.get('child_gender');
      if (childAge == undefined) childAge = 0;
      this.answers[12] = childAge;
      this.answers[13] = childGender;
      this.answers[11] = this.answers.slice(0, 10).reduce((sum, answer) => sum + (parseInt(answer) || 0), 0);
      this.$router.push({ path: '/dashboard/aex/video', query: { questionAnswers: JSON.stringify(this.answers.filter((_, index) => index < 14)) } });
    },
  },
};
</script>

<style scoped>
.questionnaire-page { width: calc(100vw - 290px); padding: 24px; }
.head h1 { margin: 0; color: #0f172a; }
.head p { margin-top: 6px; color: #64748b; }
.question-card { margin-top: 18px; background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08); }
.err { color: #b91c1c; margin-top: 10px; font-weight: 600; }
.actions { margin-top: 14px; display: flex; justify-content: flex-end; gap: 10px; }
.nav-btn { min-width: 110px; height: 42px; border-radius: 10px; border: 1px solid #cbd5e1; background: #fff; color: #334155; font-weight: 600; }
.nav-btn.primary { border: none; background: #2563eb; color: #fff; }
.nav-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
