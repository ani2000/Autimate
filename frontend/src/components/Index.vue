<script setup>
</script>

<script>
export default {
  data() {
    return { scrollY: 0, particles: [] };
  },
  mounted() {
    window.addEventListener('scroll', this.onScroll);
    this.particles = Array.from({ length: 18 }, (_, i) => ({
      id: i,
      size: 4 + Math.random() * 10,
      x: Math.random() * 100,
      y: Math.random() * 100,
      dur: 8 + Math.random() * 12,
      delay: Math.random() * 6,
      color: ['#FF6B6B','#A855F7','#4ECDC4','#FFD93D','#6C63FF','#FF85A1'][i % 6]
    }));
    this.observeSections();
  },
  beforeUnmount() {
    window.removeEventListener('scroll', this.onScroll);
  },
  methods: {
    onScroll() { this.scrollY = window.scrollY; },
    observeSections() {
      const obs = new IntersectionObserver((entries) => {
        entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); });
      }, { threshold: 0.12 });
      this.$nextTick(() => {
        this.$el.querySelectorAll('.reveal').forEach(el => obs.observe(el));
      });
    }
  }
};
</script>

<template>
  <div class="landing">
    <!-- Animated particles -->
    <div class="particles" aria-hidden="true">
      <span v-for="p in particles" :key="p.id" class="dot"
        :style="{ width: p.size+'px', height: p.size+'px', left: p.x+'%', top: p.y+'%',
          background: p.color, animationDuration: p.dur+'s', animationDelay: p.delay+'s' }">
      </span>
    </div>

    <!-- Navbar -->
    <nav class="topnav" :class="{ scrolled: scrollY > 40 }">
      <div class="nav-inner">
        <span class="logo">🧩 <b>autimate</b></span>
        <div class="nav-links">
          <router-link to="/auth" class="nav-link">Sign in</router-link>
          <router-link to="/auth" class="nav-cta">Get started</router-link>
        </div>
      </div>
    </nav>

    <!-- Hero -->
    <section class="hero">
      <svg class="blob blob-1" viewBox="0 0 600 600" xmlns="http://www.w3.org/2000/svg"><path fill="#A855F7" fill-opacity=".12" d="M428.6 369.5c-26.7 60.2-106.4 118.5-178.7 112.7C178.7 476.4 114 407 87.2 337.6c-26.9-69.4-16-138.9 23.4-191 39.5-52.2 107.4-87 172-68.2 64.6 18.7 126 91 160.2 157 34.3 66 12.6 74-14.2 134.1z"/></svg>
      <svg class="blob blob-2" viewBox="0 0 600 600" xmlns="http://www.w3.org/2000/svg"><path fill="#6C63FF" fill-opacity=".09" d="M411 344.4c-23.3 65.4-96.7 141.9-169.2 145.5-72.5 3.7-144-65.5-175.4-134.5C35 286.5 43.8 218 86 166.5c42.2-51.6 117.8-86.4 185-74 67.2 12.4 126 72 155 131.1 29.1 59.1 8.3 55.4-15 120.8z"/></svg>

      <div class="hero-grid">
        <div class="hero-text reveal">
          <span class="pill bounce-in">🌟 Early Autism Support Platform</span>
          <h1>Detect early.<br>Support <span class="gradient-text">continuously</span>.</h1>
          <p class="hero-sub">autimate helps families screen children, track progress, connect with doctors, and use engaging games & therapy routines for daily developmental support.</p>

          <div class="hero-btns">
            <router-link to="/auth" class="btn-primary"><span>🚀</span> Get Started Free</router-link>
            <router-link to="/dashboard" class="btn-outline"><span>📋</span> Dashboard</router-link>
          </div>

          <div class="trust-row">
            <div class="trust-avatars">
              <span class="t-avatar" style="background:#FF6B6B">👶</span>
              <span class="t-avatar" style="background:#4ECDC4">👧</span>
              <span class="t-avatar" style="background:#FFD93D">👦</span>
            </div>
            <span class="trust-text">Trusted by <strong>15,000+</strong> families</span>
          </div>
        </div>

        <div class="hero-visual reveal">
          <div class="img-wrapper">
            <img src="../assets/public/t2pubsfgkl--1-removebgpreview-1-1@2x.png" alt="autimate hero" class="hero-img" />
          </div>
        </div>
      </div>
    </section>

    <!-- Quick access chips -->
    <section class="chips-strip reveal">
      <router-link to="/dashboard/aex/q" class="chip"><span>🧪</span> Testing</router-link>
      <router-link to="/dashboard/therapy" class="chip"><span>🎨</span> Therapy</router-link>
      <router-link to="/dashboard/doctors" class="chip"><span>🩺</span> Doctors</router-link>
      <router-link to="/dashboard/game" class="chip"><span>🎮</span> Games</router-link>
      <router-link to="/dashboard/chat" class="chip"><span>📹</span> Video Call</router-link>
      <router-link to="/dashboard/progress" class="chip"><span>📊</span> Progress</router-link>
    </section>

    <!-- Stats -->
    <section class="stats reveal">
      <div class="stat-card" v-for="(s, i) in [{e:'👨‍👩‍👧‍👦',n:'15,000+',l:'Families helped'},{e:'📈',n:'20%',l:'Annual growth'},{e:'⭐',n:'4.5/5',l:'Satisfaction'},{e:'🕐',n:'24/7',l:'Accessible'}]" :key="i" :style="{ animationDelay: (i * 0.08) + 's' }">
        <span class="stat-icon">{{ s.e }}</span>
        <h3>{{ s.n }}</h3>
        <p>{{ s.l }}</p>
      </div>
    </section>

    <!-- Services -->
    <section class="services reveal">
      <h2 class="sec-title">What <span class="gradient-text">autimate</span> offers</h2>
      <div class="svc-grid">
        <article class="svc-card" v-for="(c, i) in [
          {icon:'🧠',accent:'#FF6B6B',title:'Autism Screening',desc:'Questionnaire & behavioral video analysis with per-child result tracking.'},
          {icon:'🎯',accent:'#4ECDC4',title:'Guided Therapy',desc:'Interactive modules for social skills, drawing, breathing, sensory calm & speech.'},
          {icon:'👨‍⚕️',accent:'#45B7D1',title:'Doctor Connect',desc:'Nearby doctor listing, appointments, and community support.'},
          {icon:'🎮',accent:'#A855F7',title:'Learning Games',desc:'Memory match, emotion games, flashcards, musical rhythm & more.'},
          {icon:'🐱',accent:'#FF85A1',title:'Talking Tom',desc:'Speech therapy through fun — Tom repeats your child\'s words in funny voices.'},
          {icon:'📊',accent:'#FFD93D',title:'Progress Tracking',desc:'Visual dashboard for milestones, scores, and therapy completion logs.'}
        ]" :key="i" :style="{ '--accent': c.accent, animationDelay: (i * 0.06) + 's' }">
          <div class="svc-icon">{{ c.icon }}</div>
          <h3>{{ c.title }}</h3>
          <p>{{ c.desc }}</p>
        </article>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta-section reveal">
      <div class="cta-card">
        <span class="cta-emoji">💛</span>
        <h2>Start Your Child's Journey Today</h2>
        <p>Detect early. Support continuously. Empower caregivers with practical, child-friendly tools.</p>
        <router-link to="/auth" class="btn-primary"><span>✨</span> Get Started Now</router-link>
        <span class="brand-mark">autimate</span>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <p>&copy; 2026 autimate — Early Autism Support Platform</p>
    </footer>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

/* ============ BASE ============ */
.landing {
  min-height: 100vh;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 35%, #F0E6FF 70%, #FFF0F5 100%);
  color: #1A0B3E;
  font-family: 'Syne', sans-serif;
  overflow-x: hidden;
  position: relative;
}

/* ============ PARTICLES ============ */
.particles { position: fixed; inset: 0; pointer-events: none; z-index: 0; overflow: hidden; }
.dot {
  position: absolute;
  border-radius: 50%;
  opacity: 0.18;
  animation: drift linear infinite;
}
@keyframes drift {
  0%   { transform: translateY(0) scale(1); opacity: 0.18; }
  50%  { transform: translateY(-40vh) scale(1.3); opacity: 0.35; }
  100% { transform: translateY(-80vh) scale(0.8); opacity: 0; }
}

/* ============ NAV ============ */
.topnav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 50;
  transition: all 0.3s ease;
  padding: 14px 0;
}
.topnav.scrolled {
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(14px);
  box-shadow: 0 2px 20px rgba(100,50,200,0.08);
  padding: 10px 0;
}
.nav-inner {
  max-width: 1200px; margin: 0 auto;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px;
}
.logo { font-size: 1.3rem; color: #1A0B3E; }
.logo b { font-weight: 800; background: linear-gradient(135deg,#6C63FF,#A855F7); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.nav-links { display: flex; gap: 12px; align-items: center; }
.nav-link {
  text-decoration: none; color: #5A4690; font-weight: 600; font-size: 0.92rem;
  padding: 8px 16px; border-radius: 50px; transition: all 0.2s;
}
.nav-link:hover { background: rgba(108,99,255,0.08); }
.nav-cta {
  text-decoration: none; color: #fff; font-weight: 700; font-size: 0.92rem;
  background: linear-gradient(135deg,#6C63FF,#A855F7);
  padding: 10px 22px; border-radius: 50px;
  box-shadow: 0 6px 20px rgba(108,99,255,0.3);
  transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1);
}
.nav-cta:hover { transform: translateY(-2px) scale(1.04); box-shadow: 0 10px 28px rgba(108,99,255,0.4); }

/* ============ HERO ============ */
.hero {
  position: relative; max-width: 1200px; margin: 0 auto;
  padding: 120px 24px 40px; z-index: 1;
}
.blob { position: absolute; width: 500px; height: 500px; z-index: 0; }
.blob-1 { top: -60px; right: -80px; animation: blobFloat 12s ease-in-out infinite; }
.blob-2 { bottom: -100px; left: -120px; animation: blobFloat 14s ease-in-out infinite reverse; }
@keyframes blobFloat {
  0%,100% { transform: translate(0,0) rotate(0deg); }
  33% { transform: translate(30px, -20px) rotate(8deg); }
  66% { transform: translate(-20px, 15px) rotate(-5deg); }
}

.hero-grid {
  display: grid; grid-template-columns: 1.2fr 1fr; gap: 40px;
  align-items: center; position: relative; z-index: 2;
}

.pill {
  display: inline-flex; align-items: center; gap: 8px;
  background: linear-gradient(135deg,#6C63FF,#A855F7);
  color: #fff; border-radius: 50px; padding: 8px 20px;
  font-size: 0.85rem; font-weight: 700; letter-spacing: 0.3px;
  animation: bounceIn 0.6s cubic-bezier(0.34,1.56,0.64,1);
}
@keyframes bounceIn { from { opacity:0; transform: scale(0.7) translateY(16px); } to { opacity:1; transform: scale(1) translateY(0); } }

.hero-text h1 {
  margin: 18px 0 14px; font-size: clamp(2rem, 4.5vw, 3.4rem);
  font-weight: 800; line-height: 1.12;
}
.gradient-text {
  background: linear-gradient(135deg,#FF6B6B,#FFD93D,#A855F7);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text; background-size: 200% 200%;
  animation: gradientShift 4s ease infinite;
}
@keyframes gradientShift { 0%{background-position:0% 50%} 50%{background-position:100% 50%} 100%{background-position:0% 50%} }

.hero-sub {
  color: #4A3875; font-size: 1.05rem; line-height: 1.75; margin: 0 0 22px;
  max-width: 520px;
}

.hero-btns { display: flex; gap: 14px; flex-wrap: wrap; }
.btn-primary, .btn-outline {
  text-decoration: none; padding: 14px 28px; border-radius: 50px;
  font-weight: 700; font-family: 'Syne', sans-serif; font-size: 1rem;
  display: inline-flex; align-items: center; gap: 8px;
  transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); cursor: pointer;
}
.btn-primary {
  background: linear-gradient(135deg,#6C63FF,#A855F7); color: #fff;
  box-shadow: 0 8px 28px rgba(108,99,255,0.35); border: none;
}
.btn-primary:hover { transform: translateY(-4px) scale(1.04); box-shadow: 0 14px 36px rgba(108,99,255,0.45); }
.btn-outline {
  background: rgba(255,255,255,0.8); color: #6C63FF;
  border: 2px solid #D4CCFF; backdrop-filter: blur(6px);
}
.btn-outline:hover { transform: translateY(-4px); border-color: #A855F7; background: #fff; }

.trust-row { display: flex; align-items: center; gap: 12px; margin-top: 22px; }
.trust-avatars { display: flex; }
.t-avatar {
  width: 34px; height: 34px; border-radius: 50%; display: grid; place-items: center;
  font-size: 1rem; border: 2px solid #fff; margin-left: -8px;
}
.t-avatar:first-child { margin-left: 0; }
.trust-text { color: #5A4690; font-size: 0.88rem; }

/* Hero visual */
.hero-visual { display: flex; justify-content: center; align-items: center; position: relative; }
.img-wrapper {
  position: relative;
  animation: heroFloat 3.5s ease-in-out infinite;
}
.img-wrapper::before {
  content: ''; position: absolute; inset: -20%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(168,85,247,0.15), rgba(108,99,255,0.06), transparent);
  animation: blobPulse 4s ease-in-out infinite;
}
@keyframes blobPulse { 0%,100%{transform:scale(1);opacity:0.7} 50%{transform:scale(1.1);opacity:1} }
@keyframes heroFloat { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-12px)} }

.hero-img {
  width: min(100%, 420px); max-height: 380px; object-fit: contain;
  filter: drop-shadow(0 20px 40px rgba(100,50,200,0.25));
  position: relative; z-index: 1;
}

/* ============ CHIPS ============ */
.chips-strip {
  max-width: 1200px; margin: 0 auto; padding: 0 24px;
  display: flex; flex-wrap: wrap; gap: 10px; justify-content: center;
  position: relative; z-index: 1;
}
.chip {
  text-decoration: none; color: #4A3875; font-weight: 600;
  background: rgba(255,255,255,0.75); backdrop-filter: blur(6px);
  padding: 10px 18px; border-radius: 50px;
  border: 1.5px solid rgba(108,99,255,0.12); font-size: 0.9rem;
  display: inline-flex; align-items: center; gap: 6px;
  transition: all 0.25s cubic-bezier(0.34,1.56,0.64,1);
}
.chip:hover { background: rgba(108,99,255,0.1); border-color: #A855F7; transform: translateY(-3px) scale(1.05); }

/* ============ STATS ============ */
.stats {
  max-width: 1200px; margin: 36px auto 0; padding: 0 24px;
  display: grid; grid-template-columns: repeat(4,1fr); gap: 16px;
  position: relative; z-index: 1;
}
.stat-card {
  background: rgba(255,255,255,0.85); backdrop-filter: blur(10px);
  border-radius: 24px; padding: 24px 18px; text-align: center;
  border: 2px solid rgba(255,255,255,0.7);
  box-shadow: 0 10px 30px rgba(100,50,200,0.07);
  transition: transform 0.35s cubic-bezier(0.34,1.56,0.64,1);
}
.stat-card:hover { transform: translateY(-8px) scale(1.03); }
.stat-icon { font-size: 2rem; display: block; margin-bottom: 6px; }
.stat-card h3 { margin: 0; color: #6C63FF; font-size: 1.7rem; font-weight: 800; }
.stat-card p { margin: 6px 0 0; color: #5A4690; font-size: 0.9rem; }

/* ============ SERVICES ============ */
.services {
  max-width: 1200px; margin: 44px auto 0; padding: 0 24px;
  position: relative; z-index: 1;
}
.sec-title { text-align: center; font-size: 1.9rem; font-weight: 800; margin: 0 0 24px; }
.svc-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 18px; }

.svc-card {
  background: rgba(255,255,255,0.88); backdrop-filter: blur(8px);
  border-radius: 24px; padding: 28px 22px; text-align: center;
  border: 2px solid rgba(255,255,255,0.7);
  box-shadow: 0 12px 30px rgba(100,50,200,0.07);
  transition: all 0.35s cubic-bezier(0.34,1.56,0.64,1);
  position: relative; overflow: hidden;
}
.svc-card::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 5px;
  background: var(--accent); border-radius: 24px 24px 0 0;
}
.svc-card:hover { transform: translateY(-8px) scale(1.02); box-shadow: 0 20px 45px rgba(100,50,200,0.15); border-color: var(--accent); }
.svc-icon { font-size: 2.6rem; margin-bottom: 10px; }
.svc-card h3 { margin: 0 0 8px; font-weight: 700; font-size: 1.15rem; }
.svc-card p { margin: 0; color: #5A4690; line-height: 1.65; font-size: 0.92rem; }

/* ============ CTA ============ */
.cta-section { max-width: 1200px; margin: 44px auto 24px; padding: 0 24px; position: relative; z-index: 1; }
.cta-card {
  background: linear-gradient(135deg, rgba(108,99,255,0.08), rgba(168,85,247,0.1));
  backdrop-filter: blur(10px); border-radius: 28px; padding: 40px 32px;
  text-align: center; border: 2px solid rgba(255,255,255,0.6);
  box-shadow: 0 14px 36px rgba(100,50,200,0.08);
}
.cta-emoji { font-size: 2.4rem; display: block; margin-bottom: 8px; animation: bounce 2s ease-in-out infinite; }
@keyframes bounce { 0%,100%{transform:translateY(0) scale(1)} 50%{transform:translateY(-8px) scale(1.12)} }
.cta-card h2 { margin: 0 0 8px; font-weight: 800; font-size: 1.6rem; }
.cta-card p { margin: 0 0 20px; color: #5A4690; font-size: 1.02rem; }
.brand-mark {
  display: block; margin-top: 16px; font-weight: 800; font-size: 1rem;
  background: linear-gradient(135deg,#6C63FF,#A855F7);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}

/* ============ FOOTER ============ */
.footer { text-align: center; padding: 20px; color: #5A4690; font-size: 0.85rem; position: relative; z-index: 1; }

/* ============ SCROLL REVEAL ============ */
.reveal {
  opacity: 0; transform: translateY(32px);
  transition: opacity 0.7s ease, transform 0.7s cubic-bezier(0.34,1.56,0.64,1);
}
.reveal.visible { opacity: 1; transform: translateY(0); }

/* ============ RESPONSIVE ============ */
@media (max-width: 1024px) {
  .hero-grid { grid-template-columns: 1fr; text-align: center; }
  .hero-sub { max-width: 100%; margin-left: auto; margin-right: auto; }
  .hero-btns { justify-content: center; }
  .trust-row { justify-content: center; }
  .hero-visual { margin-top: 24px; }
  .svc-grid { grid-template-columns: repeat(2, 1fr); }
  .stats { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .hero { padding: 100px 16px 28px; }
  .hero-text h1 { font-size: 1.8rem; }
  .stats { grid-template-columns: 1fr 1fr; gap: 10px; }
  .svc-grid { grid-template-columns: 1fr; }
  .nav-link { display: none; }
  .blob { display: none; }
  .btn-primary, .btn-outline { width: 100%; justify-content: center; }
  .hero-btns { flex-direction: column; }
  .cta-card { padding: 28px 18px; }
}
@media (max-width: 400px) {
  .stats { grid-template-columns: 1fr; }
  .chips-strip { gap: 8px; }
  .chip { font-size: 0.82rem; padding: 8px 14px; }
}
</style>
