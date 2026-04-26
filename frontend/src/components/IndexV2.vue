<template>
  <div class="landing">
    <header class="hero">
      <div class="hero-content">
        <p class="badge">autimate • Early-stage ASD Support Platform</p>
        <h1>Early autism screening, safer onboarding, and personalized care guidance.</h1>
        <p class="subtext">
          autimate helps parents run structured screening, review results quickly, connect with nearby specialists,
          and start therapy-focused activities in one place.
        </p>

        <div class="hero-actions">
          <router-link to="/auth" class="btn primary">Get Started</router-link>
          <router-link to="/dashboard" class="btn ghost">Open Dashboard</router-link>
        </div>
      </div>

      <div class="hero-card">
        <h3>Core Workflow</h3>
        <ol>
          <li>Register/Login and verify parent identity (real or pseudo mode)</li>
          <li>Add child profile and answer screening questionnaire</li>
          <li>Upload behavioral video and generate result</li>
          <li>Review doctors, therapies, and social engagement options</li>
        </ol>
      </div>
    </header>

    <section class="features">
      <article class="feature">
        <h3>Autism Screening</h3>
        <p>Questionnaire + behavioral video pipeline with saved test history for each child profile.</p>
      </article>
      <article class="feature">
        <h3>Personalized Therapy</h3>
        <p>Therapy and game suggestions based on screening outcomes and developmental needs.</p>
      </article>
      <article class="feature">
        <h3>Doctor Discovery</h3>
        <p>Nearby or highest-rated specialists with fallback sample doctor data for continuity.</p>
      </article>
      <article class="feature">
        <h3>Safe Socialization</h3>
        <p>Moderated video chat and guided activities to support communication and confidence.</p>
      </article>
    </section>

    <section class="motto">
      <h2>Main Motto</h2>
      <p>
        Detect early. Support continuously. Empower families with practical digital tools while preserving
        child safety and responsible verification.
      </p>
    </section>
  </div>
</template>

<script setup>
</script>

<style scoped>
.landing { min-height: 100vh; padding: 24px; background: #f8fafc; color: #0f172a; }
.hero { max-width: 1180px; margin: 0 auto; background: linear-gradient(135deg, #dbeafe, #eef2ff); border-radius: 20px; padding: 36px; display: grid; grid-template-columns: 1.35fr 1fr; gap: 24px; }
.badge { display: inline-block; background: #1e40af; color: #fff; font-size: 0.82rem; padding: 6px 10px; border-radius: 999px; margin-bottom: 12px; }
.hero-content h1 { font-size: clamp(1.9rem, 2.8vw, 2.9rem); line-height: 1.2; margin-bottom: 12px; }
.subtext { color: #334155; line-height: 1.65; max-width: 720px; }
.hero-actions { margin-top: 20px; display: flex; gap: 12px; }
.btn { text-decoration: none; border-radius: 10px; padding: 11px 18px; font-weight: 700; }
.btn.primary { background: #1d4ed8; color: #fff; }
.btn.ghost { border: 1px solid #1d4ed8; color: #1d4ed8; }
.hero-card { background: #fff; border-radius: 14px; padding: 20px; box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12); }
.hero-card h3 { margin-bottom: 10px; }
.hero-card ol { margin: 0; padding-left: 18px; line-height: 1.8; color: #334155; }
.features { max-width: 1180px; margin: 24px auto 0; display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.feature { background: #fff; border-radius: 12px; padding: 18px; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08); }
.feature h3 { font-size: 1.05rem; margin-bottom: 8px; }
.feature p { color: #475569; line-height: 1.55; font-size: 0.93rem; }
.motto { max-width: 1180px; margin: 20px auto 0; background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08); }
.motto h2 { margin-bottom: 8px; }
.motto p { color: #475569; line-height: 1.7; }
@media (max-width: 980px) { .hero { grid-template-columns: 1fr; } .features { grid-template-columns: 1fr 1fr; } }
@media (max-width: 640px) { .features { grid-template-columns: 1fr; } .hero-actions { flex-direction: column; } }
</style>
