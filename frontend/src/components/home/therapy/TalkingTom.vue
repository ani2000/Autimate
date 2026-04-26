<template>
  <section class="mimic-shell">
    <header class="hero" @click="reactToTouch">
      <div class="character" :class="{ speaking: isSpeaking, blinking: isBlinking, happy: reaction === 'happy' }">
        <div class="head">
          <div class="eyes"><span></span><span></span></div>
          <div class="mouth"></div>
        </div>
      </div>
      <div>
        <h1>Speech Therapy Talking Character</h1>
        <p>Simple repetition practice for words, sentences, and animal sounds.</p>
      </div>
    </header>

    <div class="card">
      <h2>1) Child Profile</h2>
      <div class="grid-3">
        <input v-model.trim="profile.name" type="text" placeholder="Child Name" />
        <input v-model.number="profile.age" type="number" min="1" max="18" placeholder="Age" />
        <select v-model="profile.therapyLevel">
          <option>Beginner</option>
          <option>Intermediate</option>
          <option>Advanced</option>
        </select>
      </div>
      <div class="row">
        <button class="btn primary" @click="createChildProfile">Create Child Profile</button>
        <span v-if="profile.childId" class="chip">childId: {{ profile.childId }}</span>
      </div>
    </div>

    <div class="card">
      <h2>2) Session</h2>
      <div class="row">
        <button class="btn primary" @click="startSession" :disabled="!profile.childId || !!session.sessionId">Start Session</button>
        <button class="btn" @click="endSession" :disabled="!session.sessionId">End Session</button>
        <span class="chip" v-if="session.sessionId">Session: {{ session.sessionId.slice(0, 8) }}</span>
        <span class="chip">Duration: {{ formatDuration(session.duration) }}</span>
      </div>
    </div>

    <div class="card">
      <h2>3) Animal Sound Module</h2>
      <div class="animal-grid">
        <button
          v-for="animal in animals"
          :key="animal.name"
          class="btn animal"
          :class="{ active: selectedAnimal && selectedAnimal.name === animal.name }"
          @click="playAnimalSound(animal)"
        >
          {{ animal.name }}
        </button>
      </div>
      <p class="muted">Tap an animal to play target sound, then press Record Voice and imitate.</p>
    </div>

    <div class="card">
      <h2>4) Word Practice + Voice Generator</h2>
      <div class="grid-3">
        <select v-model="exerciseType">
          <option>Sound</option>
          <option>Word</option>
          <option>Sentence</option>
        </select>
        <select v-model="selectedVoice">
          <option v-for="v in voiceTypes" :key="v" :value="v">{{ v }}</option>
        </select>
        <input v-model.trim="textInput" type="text" placeholder="Enter word or sentence" />
      </div>

      <div class="slider-grid">
        <label>Pitch {{ pitch.toFixed(1) }}<input type="range" min="0.1" max="2" step="0.1" v-model.number="pitch" /></label>
        <label>Speed {{ speed.toFixed(1) }}<input type="range" min="0.5" max="2" step="0.1" v-model.number="speed" /></label>
        <label>Volume {{ volume.toFixed(1) }}<input type="range" min="0" max="1" step="0.1" v-model.number="volume" /></label>
      </div>

      <div class="row">
        <button class="btn primary" @click="playAudio">Speak</button>
        <button class="btn" @click="stopAudio">Stop</button>
        <button class="btn" @click="replayAudio" :disabled="!lastSpeech.text">Replay</button>
        <button class="btn" @click="recordVoice" :disabled="isRecording">{{ isRecording ? 'Recording...' : 'Record Voice' }}</button>
      </div>

      <div class="feedback" v-if="transcript || feedbackMessage">
        <p><strong>Transcript:</strong> {{ transcript || '-' }}</p>
        <p><strong>Feedback:</strong> {{ feedbackMessage || '-' }}</p>
      </div>
    </div>

    <div class="card">
      <h2>5) Progress</h2>
      <div class="metrics">
        <div class="metric"><span>Attempts</span><strong>{{ progress.attempts }}</strong></div>
        <div class="metric"><span>Correct</span><strong>{{ progress.correct }}</strong></div>
        <div class="metric"><span>Accuracy</span><strong>{{ progress.accuracy.toFixed(1) }}%</strong></div>
        <div class="metric"><span>Session Time</span><strong>{{ formatDuration(progress.sessionDuration) }}</strong></div>
      </div>
      <button class="btn" @click="loadProgress" :disabled="!profile.childId">Refresh Progress</button>
    </div>

    <p class="status" :class="statusType" v-if="statusMessage">{{ statusMessage }}</p>
  </section>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  name: 'TalkingTom',
  data() {
    return {
      profile: {
        childId: '',
        name: '',
        age: null,
        therapyLevel: 'Beginner',
      },
      session: {
        sessionId: '',
        startTime: null,
        duration: 0,
        timer: null,
      },
      animals: [],
      selectedAnimal: null,
      expectedTarget: '',
      exerciseType: 'Word',
      textInput: '',
      voiceTypes: ['MALE', 'FEMALE', 'CHILD', 'ROBOT', 'DOG', 'CAT', 'DUCK', 'LION'],
      selectedVoice: 'FEMALE',
      pitch: 1.0,
      speed: 1.0,
      volume: 1.0,
      transcript: '',
      feedbackMessage: '',
      isRecording: false,
      recognition: null,
      isSpeaking: false,
      isBlinking: false,
      reaction: 'idle',
      lastSpeech: {
        text: '',
        voiceType: 'FEMALE',
      },
      progress: {
        attempts: 0,
        correct: 0,
        accuracy: 0,
        sessionDuration: 0,
      },
      statusMessage: '',
      statusType: 'info',
    };
  },
  mounted() {
    this.loadAnimals();
    this.setupSpeechRecognition();
    this.startBlinkLoop();
  },
  beforeUnmount() {
    if (this.session.timer) clearInterval(this.session.timer);
    if (window.speechSynthesis) window.speechSynthesis.cancel();
  },
  methods: {
    authHeaders() {
      const token = Cookies.get('token');
      return token ? { Authorization: `Bearer ${token}` } : {};
    },
    showStatus(message, type = 'info') {
      this.statusMessage = message;
      this.statusType = type;
    },
    async createChildProfile() {
      if (!this.profile.name || !this.profile.age) {
        this.showStatus('Enter child name and age first.', 'warn');
        return;
      }
      try {
        const { data } = await axios.post('/create-child', {
          name: this.profile.name,
          age: this.profile.age,
          therapyLevel: this.profile.therapyLevel,
        }, { headers: this.authHeaders() });

        this.profile.childId = data.childId;
        this.showStatus('Child profile created.', 'ok');
        await this.loadProgress();
      } catch (error) {
        this.showStatus('Failed to create profile.', 'error');
      }
    },
    async loadAnimals() {
      try {
        const { data } = await axios.get('/animals', { headers: this.authHeaders() });
        this.animals = Array.isArray(data) ? data : [];
      } catch (error) {
        // Offline fallback
        this.animals = [
          { name: 'Dog', soundText: 'woof woof' },
          { name: 'Cat', soundText: 'meow meow' },
          { name: 'Cow', soundText: 'moo moo' },
          { name: 'Lion', soundText: 'roar' },
          { name: 'Sheep', soundText: 'baa baa' },
          { name: 'Duck', soundText: 'quack quack' },
          { name: 'Horse', soundText: 'neigh' },
          { name: 'Elephant', soundText: 'pawoo' },
        ];
      }
    },
    async startSession() {
      if (!this.profile.childId) {
        this.showStatus('Create child profile first.', 'warn');
        return;
      }
      try {
        const { data } = await axios.post('/start-session', {
          childId: this.profile.childId,
        }, { headers: this.authHeaders() });

        this.session.sessionId = data.sessionId;
        this.session.startTime = Date.now();
        this.session.duration = 0;
        this.session.timer = setInterval(() => {
          this.session.duration += 1;
          this.progress.sessionDuration = this.session.duration;
        }, 1000);
        this.showStatus('Session started.', 'ok');
      } catch (error) {
        this.showStatus('Failed to start session.', 'error');
      }
    },
    async endSession() {
      if (!this.session.sessionId) return;
      try {
        await axios.post('/end-session', {
          sessionId: this.session.sessionId,
          duration: this.session.duration,
        }, { headers: this.authHeaders() });
      } catch (error) {
        // keep local session end even if offline
      }
      if (this.session.timer) clearInterval(this.session.timer);
      this.session.timer = null;
      this.session.sessionId = '';
      this.showStatus('Session ended.', 'ok');
      await this.loadProgress();
    },
    playAnimation(state) {
      if (state === 'speak') this.isSpeaking = true;
      if (state === 'idle') this.isSpeaking = false;
      if (state === 'happy') {
        this.reaction = 'happy';
        setTimeout(() => { this.reaction = 'idle'; }, 700);
      }
    },
    reactToTouch() {
      this.playAnimation('happy');
    },
    voicePreset(voiceType) {
      const map = {
        MALE: { pitch: 0.8, speed: 0.95 },
        FEMALE: { pitch: 1.2, speed: 1.0 },
        CHILD: { pitch: 1.6, speed: 1.12 },
        ROBOT: { pitch: 0.6, speed: 0.9 },
        DOG: { pitch: 1.7, speed: 1.18 },
        CAT: { pitch: 1.8, speed: 1.2 },
        DUCK: { pitch: 1.9, speed: 1.25 },
        LION: { pitch: 0.5, speed: 0.8 },
      };
      return map[voiceType] || map.FEMALE;
    },
    transformTextForVoice(text, voiceType) {
      const clean = (text || '').trim();
      if (!clean) return '';
      if (voiceType === 'ROBOT') return clean.split('').join(' - ');
      if (voiceType === 'DOG') return `${clean}, woof!`;
      if (voiceType === 'CAT') return `${clean}, meow!`;
      if (voiceType === 'DUCK') return `${clean}, quack!`;
      if (voiceType === 'LION') return `${clean}... Roooar!`;
      return clean;
    },
    async generateSpeech(text, voiceType) {
      const normalizedText = (text || '').trim();
      if (!normalizedText) {
        this.showStatus('Enter text first.', 'warn');
        return;
      }

      this.lastSpeech = { text: normalizedText, voiceType };

      try {
        await axios.post('/api/speak', {
          text: normalizedText,
          voiceType,
          pitch: this.pitch,
          speed: this.speed,
          volume: this.volume,
        }, { headers: this.authHeaders() });
      } catch (error) {
        // Offline fallback still continues with browser TTS.
      }

      this.speakText(normalizedText, voiceType);
    },
    speakText(text, voiceType) {
      if (!window.speechSynthesis) {
        this.showStatus('Speech synthesis not supported in this browser.', 'error');
        return;
      }
      const preset = this.voicePreset(voiceType);
      const utterance = new SpeechSynthesisUtterance(this.transformTextForVoice(text, voiceType));
      utterance.pitch = Math.max(0.1, Math.min(2, preset.pitch * this.pitch));
      utterance.rate = Math.max(0.5, Math.min(2, preset.speed * this.speed));
      utterance.volume = Math.max(0, Math.min(1, this.volume));

      this.playAnimation('speak');
      utterance.onend = () => this.playAnimation('idle');
      utterance.onerror = () => {
        this.playAnimation('idle');
        this.showStatus('Failed to play generated speech.', 'error');
      };

      window.speechSynthesis.cancel();
      window.speechSynthesis.speak(utterance);
    },
    playAudio() {
      const source = this.textInput || this.expectedTarget;
      this.generateSpeech(source, this.selectedVoice);
    },
    stopAudio() {
      if (window.speechSynthesis) window.speechSynthesis.cancel();
      this.playAnimation('idle');
    },
    replayAudio() {
      if (!this.lastSpeech.text) return;
      this.generateSpeech(this.lastSpeech.text, this.lastSpeech.voiceType);
    },
    playAnimalSound(animal) {
      this.selectedAnimal = animal;
      this.expectedTarget = animal.soundText;
      this.exerciseType = 'Sound';
      const animalVoice = (animal.name || '').toUpperCase();
      this.generateSpeech(animal.soundText, this.voiceTypes.includes(animalVoice) ? animalVoice : 'CHILD');
    },
    setupSpeechRecognition() {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      if (!SpeechRecognition) {
        this.showStatus('Speech recognition unavailable. You can still use voice generation.', 'warn');
        return;
      }
      this.recognition = new SpeechRecognition();
      this.recognition.lang = 'en-US';
      this.recognition.interimResults = false;
      this.recognition.maxAlternatives = 1;

      this.recognition.onresult = (event) => {
        const spoken = event.results?.[0]?.[0]?.transcript || '';
        this.transcript = spoken;
        this.isRecording = false;
        this.analyzeVoice();
      };
      this.recognition.onerror = () => {
        this.isRecording = false;
        this.showStatus('Voice capture failed. Try again in a quieter environment.', 'error');
      };
      this.recognition.onend = () => {
        this.isRecording = false;
      };
    },
    recordVoice() {
      if (!this.recognition) {
        this.showStatus('Speech recognition is not supported on this browser.', 'error');
        return;
      }
      this.transcript = '';
      this.feedbackMessage = '';
      this.isRecording = true;
      this.recognition.start();
    },
    normalize(str) {
      return (str || '').toLowerCase().replace(/[^a-z0-9 ]/g, ' ').replace(/\s+/g, ' ').trim();
    },
    levenshtein(a, b) {
      const dp = Array.from({ length: a.length + 1 }, () => Array(b.length + 1).fill(0));
      for (let i = 0; i <= a.length; i++) dp[i][0] = i;
      for (let j = 0; j <= b.length; j++) dp[0][j] = j;
      for (let i = 1; i <= a.length; i++) {
        for (let j = 1; j <= b.length; j++) {
          const cost = a[i - 1] === b[j - 1] ? 0 : 1;
          dp[i][j] = Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost);
        }
      }
      return dp[a.length][b.length];
    },
    scoreSimilarity(target, transcript) {
      const t = this.normalize(target);
      const s = this.normalize(transcript);
      if (!t && !s) return 1;
      if (!t || !s) return 0;
      const distance = this.levenshtein(t, s);
      return Math.max(0, 1 - distance / Math.max(t.length, s.length));
    },
    async analyzeVoice() {
      const targetText = this.expectedTarget || this.textInput;
      if (!targetText) {
        this.feedbackMessage = 'No target text selected.';
        return;
      }
      const similarity = this.scoreSimilarity(targetText, this.transcript);
      const correct = similarity >= 0.7;

      this.feedbackMessage = correct
        ? `Great job! Similarity ${Math.round(similarity * 100)}%`
        : `Try again. Similarity ${Math.round(similarity * 100)}%`;

      this.progress.attempts += 1;
      if (correct) this.progress.correct += 1;
      this.progress.accuracy = this.progress.attempts
        ? (this.progress.correct / this.progress.attempts) * 100
        : 0;

      if (correct) this.playAnimation('happy');

      try {
        await axios.post('/record-voice', {
          sessionId: this.session.sessionId || null,
          childId: this.profile.childId || null,
          targetText,
          voiceType: this.selectedVoice,
          transcript: this.transcript,
          similarity,
          correct,
        }, { headers: this.authHeaders() });
      } catch (error) {
        // Keep local progress for offline support.
      }
    },
    async loadProgress() {
      if (!this.profile.childId) return;
      try {
        const { data } = await axios.get('/progress', {
          params: { childId: this.profile.childId },
          headers: this.authHeaders(),
        });
        this.progress.attempts = data.attempts || 0;
        this.progress.correct = data.correct || 0;
        this.progress.accuracy = data.accuracy || 0;
        this.progress.sessionDuration = data.sessionDuration || this.session.duration;
      } catch (error) {
        // keep local values when offline
      }
    },
    startBlinkLoop() {
      setInterval(() => {
        this.isBlinking = true;
        setTimeout(() => { this.isBlinking = false; }, 130);
      }, 3500);
    },
    formatDuration(seconds) {
      const value = Number(seconds || 0);
      const m = Math.floor(value / 60);
      const s = value % 60;
      return `${m}:${String(s).padStart(2, '0')}`;
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.mimic-shell {
  width: 100%;
  min-height: calc(100vh - 120px);
  padding: 16px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #f5fbff 0%, #eef6ff 50%, #f7f0ff 100%);
  color: #1a0b3e;
}

.hero {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
  padding: 12px;
  border: 2px solid #dbe7ff;
  border-radius: 16px;
  background: #ffffff;
}
.hero h1 {
  margin: 0;
  font-size: 1.3rem;
}
.hero p {
  margin: 4px 0 0;
  color: #4d5b7c;
}

.character {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: #cfe6ff;
  display: grid;
  place-items: center;
  border: 2px solid #9ac7ff;
}
.head {
  width: 48px;
}
.eyes {
  display: flex;
  justify-content: space-between;
}
.eyes span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #103b6d;
}
.mouth {
  width: 22px;
  height: 5px;
  background: #103b6d;
  border-radius: 8px;
  margin: 8px auto 0;
}
.character.speaking .mouth {
  height: 12px;
}
.character.blinking .eyes span {
  height: 2px;
}
.character.happy {
  transform: scale(1.06);
}

.card {
  background: #fff;
  border: 2px solid #e5edff;
  border-radius: 14px;
  padding: 12px;
  margin-bottom: 12px;
}
.card h2 {
  margin: 0 0 10px;
  font-size: 1rem;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.slider-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin: 10px 0;
}
.slider-grid label {
  font-size: 0.84rem;
  font-weight: 700;
  color: #394d75;
}
.slider-grid input {
  width: 100%;
}

input,
select {
  height: 44px;
  border: 2px solid #d9e4ff;
  border-radius: 12px;
  padding: 0 12px;
  font-family: inherit;
}

.row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  margin-top: 10px;
}

.btn {
  min-height: 44px;
  border-radius: 12px;
  border: 2px solid #c8d8ff;
  background: #fff;
  color: #1a3468;
  font-weight: 700;
  padding: 0 14px;
}
.btn.primary {
  border-color: transparent;
  color: #fff;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
}
.btn.animal.active {
  border-color: #2563eb;
  background: #e7f0ff;
}

.animal-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.chip {
  background: #edf2ff;
  color: #23437a;
  border-radius: 999px;
  padding: 6px 10px;
  font-weight: 700;
  font-size: 0.8rem;
}

.feedback {
  margin-top: 10px;
  background: #f6f9ff;
  border: 1px solid #d8e6ff;
  border-radius: 12px;
  padding: 10px;
}
.feedback p {
  margin: 4px 0;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 10px;
}
.metric {
  background: #f4f7ff;
  border: 1px solid #dbe6ff;
  border-radius: 12px;
  padding: 10px;
  text-align: center;
}
.metric span {
  display: block;
  color: #5c6f98;
  font-size: 0.78rem;
  margin-bottom: 4px;
}
.metric strong {
  font-size: 1.05rem;
}

.status {
  margin-top: 6px;
  padding: 10px;
  border-radius: 10px;
  font-weight: 700;
}
.status.info { background: #e8f1ff; color: #21426e; }
.status.ok { background: #e8f9ef; color: #11653b; }
.status.warn { background: #fff7e8; color: #7a4e09; }
.status.error { background: #ffecec; color: #922020; }

.muted {
  color: #5f6f92;
  margin: 8px 0 0;
}

@media (max-width: 900px) {
  .grid-3,
  .slider-grid,
  .metrics {
    grid-template-columns: 1fr 1fr;
  }
  .animal-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 520px) {
  .mimic-shell {
    padding: 10px 8px;
  }
  .hero {
    align-items: flex-start;
  }
  .grid-3,
  .slider-grid,
  .metrics,
  .animal-grid {
    grid-template-columns: 1fr;
  }
  .btn {
    width: 100%;
  }
}
</style>
