<template>
  <div class="sso-shell">
    <div class="float-deco d1">📖</div>
    <div class="float-deco d2">🤝</div>
    <div class="float-deco d3">💫</div>
    <div class="float-deco d4">🌟</div>

    <header class="sso-header">
      <h1>📚 Social Skills Stories</h1>
      <p>Pick a story and learn together!</p>
    </header>

    <div class="stories-grid">
      <button
        v-for="story in stories"
        :key="story.assetName"
        class="story-card"
        @click="navigateToStory(story.assetName)"
      >
        <span class="story-emoji">{{ story.emoji }}</span>
        <span class="story-title">{{ story.title }}</span>
        <span class="story-arrow">→</span>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      stories: [
        { title: "Let's Make Friends", assetName: 'asset1', emoji: '👫' },
        { title: 'I Can Calm Myself', assetName: 'asset2', emoji: '🧘' },
        { title: 'Classroom Manners', assetName: 'asset3', emoji: '🏫' },
        { title: 'Good Listeners', assetName: 'asset4', emoji: '👂' },
        { title: 'Respectful Behaviors', assetName: 'asset5', emoji: '🙏' },
        { title: 'Good Manners', assetName: 'asset6', emoji: '😊' },
        { title: "What's My Hobby?", assetName: 'asset7', emoji: '🎯' },
      ],
    };
  },
  methods: {
    navigateToStory(assetName) {
      this.$router.push({ name: 'SocialSkillStoryView', params: { assetName } });
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.sso-shell {
  position: relative; width: calc(100vw - 300px); min-height: calc(100vh - 120px);
  padding: 24px; font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #EDE9FE 0%, #E0F2FE 50%, #FFF7ED 100%);
  display: flex; flex-direction: column; align-items: center; overflow: hidden;
}
.float-deco { position: absolute; font-size: 2rem; opacity: 0.12; animation: floatBob 6s ease-in-out infinite; pointer-events: none; }
.d1 { top: 6%; right: 7%; } .d2 { bottom: 12%; left: 5%; animation-delay: 1.5s; }
.d3 { top: 25%; left: 4%; animation-delay: 3s; } .d4 { bottom: 25%; right: 5%; animation-delay: 0.8s; }
@keyframes floatBob { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.sso-header { text-align: center; margin-bottom: 24px; }
.sso-header h1 { margin: 0; font-size: 1.8rem; font-weight: 800; color: #1A0B3E; }
.sso-header p { margin: 4px 0 0; color: #5A4690; }

.stories-grid { display: flex; flex-direction: column; gap: 10px; width: 100%; max-width: 420px; }
.story-card {
  display: flex; align-items: center; gap: 12px; padding: 16px 20px;
  background: rgba(255,255,255,0.85); border: 2px solid #E8E0FF; border-radius: 16px;
  backdrop-filter: blur(6px); cursor: pointer; transition: all 0.3s; text-align: left;
  font-family: 'Syne', sans-serif;
}
.story-card:hover { transform: translateY(-3px); border-color: #A855F7; box-shadow: 0 6px 20px rgba(108,99,255,0.12); }
.story-emoji { font-size: 1.8rem; }
.story-title { flex: 1; font-weight: 700; font-size: 1rem; color: #1A0B3E; }
.story-arrow { font-size: 1.2rem; color: #A855F7; font-weight: 800; }
</style>