<script>
import Sidebar from './Sidebar.vue';

import Modal from './Modal.vue';
import SingleQuestion from './tests/SingleQuestion.vue';

export default {
  components: {
    Sidebar,
    Modal,
    SingleQuestion,
  },
  data() {
    return {
      isModalVisible: false,
      isTestingModel: true,
      sidebarOpen: false,
    };
  },
  methods: {
    showModal() {
      this.isModalVisible = true;
      this.isTestingModel = false;
    },
    showAutismTestingModal() {
      this.isModalVisible = true;
      this.isTestingModel = true;
    }
  },
  watch: {
    $route() {
      this.sidebarOpen = false;
    }
  },
};

</script>

<template>
  <div class="dashboard-wrapper">
    <!-- Mobile top bar -->
    <div class="mobile-topbar">
      <button class="hamburger" @click="sidebarOpen = !sidebarOpen" aria-label="Toggle menu">
        <span :class="['bar', sidebarOpen && 'open']"></span>
      </button>
      <span class="topbar-brand">autimate</span>
    </div>

    <!-- Overlay for mobile -->
    <div v-if="sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false"></div>

    <Sidebar :class="['sidebar-slot', sidebarOpen && 'sidebar-visible']" />

    <main class="content-area">
      <router-view></router-view>
      <Modal v-if="isModalVisible" @close="isModalVisible = false" :isTestingModel="isTestingModel" />
    </main>
  </div>
</template>

<style lang="css" scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.dashboard-wrapper {
  display: flex;
  min-height: 100dvh;
  background: #f2f7fb;
  font-family: 'Syne', sans-serif;
}

.content-area {
  flex: 1;
  margin-left: var(--sidebar-w, 260px);
  min-height: 100dvh;
  overflow-y: auto;
  overflow-x: hidden;
  transition: margin-left 0.3s ease;
}

/* ── Mobile top bar ── */
.mobile-topbar {
  display: none;
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 64px;
  padding-top: env(safe-area-inset-top, 0px);
  background: linear-gradient(135deg, #20557d, #3d2d84);
  box-shadow: 0 10px 24px rgba(17, 38, 64, 0.24);
  z-index: 200;
  align-items: center;
  padding-left: 16px;
  padding-right: 16px;
  gap: 14px;
}
.topbar-brand {
  font-weight: 800;
  font-size: 1.1rem;
  letter-spacing: 0.04em;
  background: linear-gradient(135deg, #f8ffba, #c8f7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hamburger {
  width: 40px; height: 40px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
}
.bar, .bar::before, .bar::after {
  display: block;
  width: 22px; height: 2.5px;
  background: #ecf6ff;
  border-radius: 4px;
  transition: all 0.3s ease;
}
.bar::before, .bar::after {
  content: '';
  position: absolute;
}
.bar::before { transform: translateY(-7px); }
.bar::after { transform: translateY(7px); }
.bar.open { background: transparent; }
.bar.open::before { transform: rotate(45deg); }
.bar.open::after { transform: rotate(-45deg); }

.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(8, 14, 30, 0.44);
  backdrop-filter: blur(2px);
  z-index: 180;
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .mobile-topbar { display: flex; }
  .sidebar-overlay { display: block; }

  .sidebar-slot {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 190;
    height: 100dvh;
  }

  .content-area {
    margin-left: 0;
    padding-top: calc(64px + env(safe-area-inset-top, 0px));
  }

  /* Normalize legacy child pages that still use desktop fixed widths. */
  .content-area > * {
    width: 100% !important;
    max-width: 100% !important;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }

  .content-area > *[style] {
    width: 100% !important;
    max-width: 100% !important;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  .sidebar-slot {
    transform: translateX(-100%);
    transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .sidebar-slot.sidebar-visible {
    transform: translateX(0);
  }
}
</style>