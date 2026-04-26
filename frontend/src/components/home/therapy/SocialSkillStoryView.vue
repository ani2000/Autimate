<template>
  <div class="story-shell">
    <div class="float-deco d1">📖</div>
    <div class="float-deco d2">⭐</div>

    <header class="sv-header">
      <h1>{{ storyTitle }}</h1>
      <span class="page-badge">Page {{ currentIndex + 1 }} / {{ images.length }}</span>
    </header>

    <div class="image-stage">
      <img :src="`/story/${assetName}/${images[currentIndex]}`" alt="Story Image" class="story-img" />
    </div>

    <div class="nav-row">
      <button class="btn-nav" @click="prevImage" :disabled="currentIndex === 0">⬅️ Previous</button>
      <button class="btn-end" @click="goBack">🏠 End Story</button>
      <button class="btn-nav" @click="nextImage" :disabled="currentIndex === images.length - 1">Next ➡️</button>
    </div>

    <!-- Progress dots -->
    <div class="progress-dots">
      <span v-for="(img, i) in images" :key="i" class="pdot" :class="{ active: i === currentIndex, visited: i <= currentIndex }"></span>
    </div>
  </div>
</template>

<script>
export default {
  props: ['assetName'],
  data() {
    return { images: [], currentIndex: 0, storyTitle: '' };
  },
  created() { this.loadStory(); },
  methods: {
    loadStory() {
      const counts = { asset1: 5, asset2: 4, asset3: 8, asset4: 5, asset5: 4, asset6: 6, asset7: 5 };
      const titles = {
        asset1: "Let's Make Friends", asset2: 'I Can Calm Myself', asset3: 'Classroom Manners',
        asset4: 'Good Listeners', asset5: 'Respectful Behaviors', asset6: 'Good Manners', asset7: "What's My Hobby?",
      };
      this.storyTitle = titles[this.assetName] || 'Story';
      const c = counts[this.assetName] || 0;
      this.images = Array.from({ length: c }, (_, i) => `${i}.png`);
    },
    prevImage() { if (this.currentIndex > 0) this.currentIndex--; },
    nextImage() { if (this.currentIndex < this.images.length - 1) this.currentIndex++; },
    goBack() { this.$router.push('/dashboard/therapy/socialskill'); },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.story-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #EDE9FE 0%, #FFF7ED 50%, #E0F2FE 100%);
  display: flex; flex-direction: column; align-items: center; overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 6s ease-in-out infinite; pointer-events: none; }
.d1 { top: 5%; left: 5%; } .d2 { bottom: 10%; right: 6%; animation-delay: 2s; }
@keyframes floatBob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.sv-header { text-align: center; margin-bottom: 14px; }
.sv-header h1 { margin: 0; font-size: 1.6rem; font-weight: 800; color: #1A0B3E; }
.page-badge {
  display: inline-block; margin-top: 6px; padding: 3px 14px; border-radius: 50px;
  background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff; font-size: 0.78rem; font-weight: 700;
}

.image-stage {
  background: rgba(255,255,255,0.9); border: 2px solid #D4CCFF; border-radius: 20px;
  padding: 12px; margin-bottom: 14px; box-shadow: 0 6px 24px rgba(108,99,255,0.08);
}
.story-img { max-width: 100%; max-height: 400px; border-radius: 14px; display: block; margin: 0 auto; }

.nav-row { display: flex; gap: 12px; margin-bottom: 12px; }
.btn-nav, .btn-end {
  border: none; border-radius: 50px; padding: 12px 24px; font-family: 'Syne', sans-serif;
  font-weight: 700; cursor: pointer; transition: all 0.25s;
}
.btn-nav { background: rgba(255,255,255,0.85); border: 2px solid #D4CCFF; color: #5A4690; }
.btn-nav:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-nav:hover:not(:disabled) { border-color: #6C63FF; transform: translateY(-2px); }
.btn-end { background: linear-gradient(135deg,#FF6B6B,#FF4466); color: #fff; }

.progress-dots { display: flex; gap: 6px; }
.pdot { width: 10px; height: 10px; border-radius: 50%; background: #D4CCFF; transition: all 0.3s; }
.pdot.visited { background: #A855F7; }
.pdot.active { background: #6C63FF; transform: scale(1.3); }
</style>
  