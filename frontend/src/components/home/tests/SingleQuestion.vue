<template>
  <div class="sq-card" :class="{ 'sq-answered': selectedOption !== null }">
    <div class="sq-question-row">
      <span class="sq-num">Q{{ index + 1 }}</span>
      <p class="sq-text">{{ question }}</p>
    </div>
    <div class="sq-options">
      <label
        v-for="(option, oi) in options"
        :key="oi"
        class="sq-option"
        :class="{ selected: selectedOption === (oi + 1).toString() }"
      >
        <input
          type="radio"
          :value="(oi + 1).toString()"
          v-model="selectedOption"
          @change="emitAnswer"
          class="sq-radio"
        />
        <span class="sq-emoji">{{ optionEmojis[oi] || '🔘' }}</span>
        <span class="sq-label">{{ option }}</span>
      </label>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    question: { type: String, required: true },
    options: { type: Array, required: true },
    value: { type: String, default: null },
    index: { type: Number, required: true },
  },
  data() {
    return {
      selectedOption: this.value,
      optionEmojis: ['😊', '🙂', '😐', '😟', '😢'],
    };
  },
  methods: {
    emitAnswer() {
      this.$emit('update-answer', { answer: this.selectedOption, index: this.index });
    },
  },
  watch: {
    value(newValue) {
      this.selectedOption = newValue;
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.sq-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(212, 204, 255, 0.5);
  border-radius: 20px;
  padding: 18px 20px;
  margin: 12px 0;
  font-family: 'Syne', sans-serif;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: sqPop 0.4s ease-out;
}
@keyframes sqPop {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.sq-card.sq-answered {
  border-color: #A855F7;
  box-shadow: 0 6px 24px rgba(168, 85, 247, 0.12);
}

.sq-question-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}
.sq-num {
  flex-shrink: 0;
  width: 40px; height: 40px;
  display: grid; place-items: center;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-weight: 800;
  font-size: 0.82rem;
  border-radius: 50%;
}
.sq-text {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 600;
  color: #1A0B3E;
  line-height: 1.5;
  padding-top: 6px;
}

.sq-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.sq-option {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(240, 230, 255, 0.5);
  border: 2px solid transparent;
  border-radius: 14px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
  flex: 1 1 180px;
  min-width: 160px;
}
.sq-option:hover {
  background: rgba(168, 85, 247, 0.08);
  border-color: #D4CCFF;
  transform: translateY(-2px);
}
.sq-option.selected {
  background: linear-gradient(135deg, rgba(108, 99, 255, 0.1), rgba(168, 85, 247, 0.12));
  border-color: #A855F7;
  box-shadow: 0 4px 16px rgba(168, 85, 247, 0.15);
  transform: scale(1.02);
}

.sq-radio {
  display: none;
}
.sq-emoji {
  font-size: 1.3rem;
  transition: transform 0.25s ease;
}
.sq-option.selected .sq-emoji {
  transform: scale(1.2);
  animation: emojiBounce 0.4s ease;
}
@keyframes emojiBounce {
  0% { transform: scale(1); }
  50% { transform: scale(1.35); }
  100% { transform: scale(1.2); }
}
.sq-label {
  font-size: 0.92rem;
  font-weight: 600;
  color: #3D2B7A;
}

@media (max-width: 640px) {
  .sq-options { flex-direction: column; }
  .sq-option { flex: 1 1 100%; min-width: 0; }
  .sq-text { font-size: 0.95rem; }
}
</style>