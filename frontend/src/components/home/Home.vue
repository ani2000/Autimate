<template>
  <div class="dashboard-container">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
      integrity="sha512-iecdLmaskl7CVJkEZSMUkrQ6usknAUUSomyajfeBMAe6irNflcrYhM+LRxZcCAUL8q+KL5xQs5M7rW6ZWF00+w=="
      crossorigin="anonymous" referrerpolicy="no-referrer" />

    <!-- Welcome Header -->
    <div class="welcome-header" v-if="children && children.length > 0">
      <div class="header-content">
        <div class="header-left">
          <h1 class="welcome-title">Welcome Back! 👋</h1>
          <p class="welcome-subtitle">Here's what's happening with your child's progress today</p>
        </div>
        <div class="header-right">
          <div class="date-display">
            <i class="fas fa-calendar"></i>
            <span>{{ getCurrentDate() }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Child Profile Section -->
    <div class="child-section" v-if="children && children.length > 0">
      <div class="child-header">
        <h2 class="section-title">Child Profile</h2>
        <button class="btn-primary-small" @click="addNewProfile">
          <i class="fas fa-plus"></i> Add Profile
        </button>
      </div>

      <!-- Active Child Profile Card -->
      <div v-if="activeChild" class="active-child-card">
        <div class="child-avatar-wrapper">
          <div class="child-avatar">
            <i class="fas" :class="activeChild.gender === 0 ? 'fa-child' : 'fa-child'"></i>
          </div>
        </div>
        <div class="child-info">
          <h3 class="child-name">{{ activeChild.name }}</h3>
          <div class="child-details">
            <span class="detail-item">
              <i class="fas fa-birthday-cake"></i>
              <span>{{ calculateAgeFromDob(activeChild.dob) }} years old</span>
            </span>
            <span class="detail-item">
              <i class="fas" :class="activeChild.gender === 0 ? 'fa-venus' : 'fa-mars'"></i>
              <span>{{ activeChild.gender === 0 ? 'Female' : 'Male' }}</span>
            </span>
          </div>
        </div>
        <div class="child-selector-dropdown">
          <select v-model="activeChildId" @change="handleSelect" class="select-profile">
            <option v-for="child in children" :key="child.id" :value="child.id">
              {{ child.name }}
            </option>
          </select>
        </div>
      </div>

      <!-- Other Profiles Quick Access -->
      <div v-if="children.length > 1" class="other-profiles">
        <div class="profiles-scroll">
          <div v-for="child in children" :key="child.id" 
            v-show="child.id !== activeChildId"
            @click="handleChildSelect(child)"
            class="profile-button">
            <div class="profile-avatar-mini">
              <i class="fas fa-child"></i>
            </div>
            <span>{{ child.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Statistics Cards Section -->
    <div class="stats-section" v-if="children && children.length > 0 && testResults.length > 0">
      <h2 class="section-title">Progress Overview</h2>
      <div class="stats-grid">
        <div class="stat-card stat-card-1">
          <div class="stat-icon">
            <i class="fas fa-chart-line"></i>
          </div>
          <div class="stat-content">
            <h4>Total Tests</h4>
            <p class="stat-value">{{ testResults.length }}</p>
          </div>
        </div>

        <div class="stat-card stat-card-2">
          <div class="stat-icon">
            <i class="fas fa-calendar-check"></i>
          </div>
          <div class="stat-content">
            <h4>Last Test</h4>
            <p class="stat-value">{{ testResults.length > 0 ? formatDate(testResults[0].testDate) : 'N/A' }}</p>
          </div>
        </div>

        <div class="stat-card stat-card-3">
          <div class="stat-icon">
            <i class="fas fa-target"></i>
          </div>
          <div class="stat-content">
            <h4>Avg Confidence</h4>
            <p class="stat-value">{{ getAverageConfidence() }}%</p>
          </div>
        </div>

        <div class="stat-card stat-card-4">
          <div class="stat-icon">
            <i class="fas fa-trophy"></i>
          </div>
          <div class="stat-content">
            <h4>Latest Status</h4>
            <p class="stat-value" :class="testResults[0]?.asdStatus === '1' ? 'status-control' : 'status-positive'">
              {{ getResultDescription(testResults[0]?.asdStatus) }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Test Results Section -->
    <div class="test-results-section" v-if="children && children.length > 0 && testResults.length > 0">
      <div class="results-header">
        <h2 class="section-title">Recent Test Results</h2>
        <a href="#" class="view-all-link">View All Tests →</a>
      </div>

      <div class="results-container">
        <div v-for="(test, index) in testResults.slice(0, 5)" :key="test.id" class="result-card">
          <div class="result-card-header">
            <div class="result-number">
              <span class="badge">Test #{{ index + 1 }}</span>
            </div>
            <span class="result-date">{{ formatDate(test.testDate) }}</span>
          </div>

          <div class="result-card-body">
            <div class="result-metric">
              <span class="metric-label">Q10 Score</span>
              <div class="metric-value-box">
                <span class="metric-value metric-q10">{{ Number(test.q10 * 100).toFixed(1) }}%</span>
              </div>
            </div>

            <div class="result-metric">
              <span class="metric-label">Confidence</span>
              <div class="confidence-bar">
                <div class="confidence-fill" :style="{ width: (test.confidence * 100) + '%' }"></div>
              </div>
              <span class="confidence-text">{{ (test.confidence * 100).toFixed(0) }}%</span>
            </div>

            <div class="result-metric">
              <span class="metric-label">Result</span>
              <span class="result-badge" :class="test.asdStatus === '1' ? 'badge-control' : 'badge-positive'">
                {{ getResultDescription(test.asdStatus) }}
              </span>
            </div>
          </div>

          <div class="result-card-footer" v-if="test.therapies || test.games">
            <div class="footer-item" v-if="test.therapies">
              <i class="fas fa-brain"></i>
              <span>{{ test.therapies }}</span>
            </div>
            <div class="footer-item" v-if="test.games">
              <i class="fas fa-gamepad"></i>
              <span>{{ test.games }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions Section -->
    <div class="quick-actions-section" v-if="children && children.length > 0">
      <h2 class="section-title">Quick Actions</h2>
      <div class="actions-grid">
        <router-link to="/dashboard/aex/q" class="action-card action-test">
          <div class="action-icon">
            <i class="fas fa-clipboard-check"></i>
          </div>
          <h4>Take Test</h4>
          <p>Run Q10 Assessment</p>
        </router-link>

        <router-link to="/dashboard/therapy" class="action-card action-therapy">
          <div class="action-icon">
            <i class="fas fa-spa"></i>
          </div>
          <h4>Therapy</h4>
          <p>Start Therapeutic Activities</p>
        </router-link>

        <router-link to="/dashboard/game" class="action-card action-game">
          <div class="action-icon">
            <i class="fas fa-puzzle-piece"></i>
          </div>
          <h4>Games</h4>
          <p>Play Learning Games</p>
        </router-link>

        <router-link to="/dashboard/doctors" class="action-card action-doctors">
          <div class="action-icon">
            <i class="fas fa-stethoscope"></i>
          </div>
          <h4>Find Doctor</h4>
          <p>Connect with Specialists</p>
        </router-link>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <i class="fas fa-inbox"></i>
      </div>
      <h2>No child profiles found</h2>
      <p>Create a new profile to get started</p>
      <button class="btn-primary" @click="addNewProfile">Create Your First Profile</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  data() {
    return {
      testResults: [],
      children: null,
      activeChildId: null,
      activeChild: null,
    };
  },
  methods: {
    getCurrentDate() {
      const options = { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' };
      return new Date().toLocaleDateString('en-US', options);
    },

    calculateAgeFromDob(dobValue) {
      if (!dobValue) return 0;
      const dob = new Date(dobValue);
      if (Number.isNaN(dob.getTime())) return 0;
      const now = new Date();
      let age = now.getFullYear() - dob.getFullYear();
      const monthDiff = now.getMonth() - dob.getMonth();
      if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < dob.getDate())) {
        age -= 1;
      }
      return Math.max(0, age);
    },

    setChildContextCookies(child) {
      if (!child) return;
      const age = this.calculateAgeFromDob(child.dob);
      const ageGroup = age <= 3 ? '0-3' : '3-15';
      const gender = child.gender === 0 ? 'female' : 'male';

      Cookies.set('child_id', child.id || '');
      Cookies.set('child_name', child.name || '');
      Cookies.set('child_age', String(age));
      Cookies.set('child_gender', gender);
      Cookies.set('child_age_group', ageGroup);
    },

    async fetchTestResults() {
      try {
        const token = Cookies.get('token');
        const response = await axios.get(`http://localhost:8080/api/v1/aex/lists`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.testResults = response.data.map(test => ({
          id: test.id,
          testDate: test.testDate,
          asdStatus: test.vid_res,
          confidence: test.vid_confid,
          q10: test.q10,
          therapies: test.suggested_therapies,
          games: test.suggested_games
        }));
      } catch (error) {
        console.error("Error fetching test results:", error);
      }
    },

    async fetchChildren() {
      try {
        const token = Cookies.get('token');
        const response = await axios.get(`http://localhost:8080/api/v1/child/list`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.children = response.data || [];

        if (this.children.length === 0) {
          this.$router.push('/dashboard/newchprofile');
        } else {
          await this.activateChildSession();
          await this.fetchTestResults();
        }
      } catch (error) {
        console.error("Error fetching child profiles:", error);
      }
    },

    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    },

    getResultDescription(asdStatus) {
      return asdStatus === "1" ? "Control (no autism)" : "Autism Positive";
    },

    getAverageConfidence() {
      if (this.testResults.length === 0) return 0;
      const sum = this.testResults.reduce((acc, test) => acc + test.confidence, 0);
      return ((sum / this.testResults.length) * 100).toFixed(0);
    },

    async activateChildSession() {
      const activeChild = this.children.find(child => child.activeSession);

      if (activeChild) {
        this.activeChild = activeChild;
        this.setChildContextCookies(activeChild);
        const savedChildId = Cookies.get('child_id');
        this.activeChildId = savedChildId || (this.children.length ? this.children[0].id : null);
      } else if (this.children.length) {
        const first = this.children[0];
        try {
          const token = Cookies.get('token');
          await axios.post(`http://localhost:8080/api/v1/child/toggle_active_session/${first.id}`, {}, {
            headers: { Authorization: `Bearer ${token}` }
          });
        } catch (error) {
          console.error('Error auto-activating first child:', error);
        }
        this.activeChild = first;
        this.setChildContextCookies(first);
        this.activeChildId = first.id;
      }
    },

    addNewProfile() {
      this.$router.push('/dashboard/newchprofile');
    },

    handleChildSelect(child) {
      this.activeChildId = child.id;
      this.handleSelect({ target: { value: child.id } });
    },

    handleSelect(event) {
      const selectedId = event.target.value;
      if (selectedId === "newchprofile") {
        this.$router.push('newchprofile');
      } else {
        const selectedChild = this.children.find(child => child.id === selectedId);
        if (selectedChild) {
          this.setChildContextCookies(selectedChild);
          this.switchProfile(selectedChild);
        }
      }
    },

    async switchProfile(child) {
      try {
        const token = Cookies.get('token');
        await axios.post(`http://localhost:8080/api/v1/child/toggle_active_session/${child.id}`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.setChildContextCookies(child);
        window.location.reload();
      } catch (error) {
        console.error("Error switching profile:", error);
      }
    },
  },

  watch: {
    activeChild(newVal) {
      if (newVal) {
        this.activeChildId = newVal.id;
      }
    }
  },

  mounted() {
    this.fetchChildren();
  },
};
</script>

<style scoped>
/* Main Container */
.dashboard-container {
  min-height: 100dvh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 20px;
  max-width: 1400px;
  margin: 0 auto;
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Welcome Header */
.welcome-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 40px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.2);
  animation: slideDown 0.6s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left h1.welcome-title {
  color: white;
  font-size: 2.5em;
  font-weight: 700;
  margin: 0;
  margin-bottom: 8px;
}

.header-left p.welcome-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1em;
  margin: 0;
  font-weight: 500;
}

.header-right .date-display {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 10px 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1em;
  font-weight: 500;
}

.date-display i {
  font-size: 1.2em;
}

/* Section Title */
.section-title {
  font-size: 1.8em;
  font-weight: 700;
  color: #1a0b3e;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Child Profile Section */
.child-section {
  margin-bottom: 40px;
}

.child-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.btn-primary-small {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn-primary-small:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

/* Active Child Card */
.active-child-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 30px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  animation: slideUp 0.5s ease-out 0.1s both;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.child-avatar-wrapper {
  flex-shrink: 0;
}

.child-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 2.5em;
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.child-info {
  flex: 1;
}

.child-name {
  font-size: 1.5em;
  font-weight: 700;
  color: #1a0b3e;
  margin: 0 0 12px 0;
}

.child-details {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-weight: 500;
}

.detail-item i {
  color: #667eea;
  font-size: 1.2em;
}

.child-selector-dropdown {
  flex-shrink: 0;
}

.select-profile {
  padding: 12px 15px;
  border: 2px solid #667eea;
  border-radius: 8px;
  font-size: 1em;
  cursor: pointer;
  background-color: white;
  color: #1a0b3e;
  transition: all 0.3s ease;
}

.select-profile:hover {
  background-color: #f5f7fa;
}

.select-profile:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

/* Other Profiles */
.other-profiles {
  margin-bottom: 20px;
}

.profiles-scroll {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 15px;
}

.profile-button {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 15px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #1a0b3e;
}

.profile-button:hover {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  transform: translateY(-4px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.2);
}

.profile-avatar-mini {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5em;
}

/* Statistics Section */
.stats-section {
  margin-bottom: 40px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  animation: slideUp 0.5s ease-out backwards;
}

.stat-card:nth-child(1) { animation-delay: 0.1s; }
.stat-card:nth-child(2) { animation-delay: 0.2s; }
.stat-card:nth-child(3) { animation-delay: 0.3s; }
.stat-card:nth-child(4) { animation-delay: 0.4s; }

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8em;
  flex-shrink: 0;
}

.stat-card-1 .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card-2 .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-card-3 .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-card-4 .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.stat-content h4 {
  font-size: 0.9em;
  color: #999;
  margin: 0 0 8px 0;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 1.8em;
  font-weight: 700;
  color: #1a0b3e;
  margin: 0;
}

.status-control {
  color: #43e97b;
}

.status-positive {
  color: #f5576c;
}

/* Test Results Section */
.test-results-section {
  margin-bottom: 40px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 10px;
}

.view-all-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.view-all-link:hover {
  color: #764ba2;
  transform: translateX(5px);
  display: inline-block;
}

.results-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.result-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border-left: 4px solid #667eea;
  animation: slideUp 0.5s ease-out backwards;
}

.result-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

.result-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8em;
  font-weight: 600;
}

.result-date {
  color: #999;
  font-size: 0.9em;
  font-weight: 500;
}

.result-card-body {
  margin-bottom: 20px;
}

.result-metric {
  margin-bottom: 16px;
}

.result-metric:last-child {
  margin-bottom: 0;
}

.metric-label {
  display: block;
  color: #999;
  font-size: 0.85em;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.metric-value-box {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 12px 16px;
  border-radius: 8px;
}

.metric-value {
  font-size: 1.4em;
  font-weight: 700;
  color: #667eea;
}

.metric-q10 {
  color: #f5576c;
}

.confidence-bar {
  background: #f0f0f0;
  height: 8px;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 8px;
}

.confidence-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  transition: width 0.5s ease;
}

.confidence-text {
  color: #666;
  font-size: 0.9em;
  font-weight: 600;
}

.result-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.9em;
  font-weight: 600;
}

.badge-control {
  background: #e8f5e9;
  color: #43e97b;
}

.badge-positive {
  background: #ffebee;
  color: #f5576c;
}

.result-card-footer {
  display: flex;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 0.9em;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.footer-item i {
  color: #667eea;
  font-size: 1.1em;
}

/* Quick Actions Section */
.quick-actions-section {
  margin-bottom: 40px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 26px 18px;
  text-decoration: none;
  text-align: center;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border-top: 4px solid transparent;
  animation: slideUp 0.5s ease-out backwards;
}

.action-card:nth-child(1) { animation-delay: 0.1s; border-top-color: #667eea; }
.action-card:nth-child(2) { animation-delay: 0.2s; border-top-color: #f093fb; }
.action-card:nth-child(3) { animation-delay: 0.3s; border-top-color: #4facfe; }
.action-card:nth-child(4) { animation-delay: 0.4s; border-top-color: #43e97b; }

.action-card:hover {
  transform: translateY(-12px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

.action-icon {
  font-size: 3em;
  margin-bottom: 16px;
  display: block;
}

.action-test .action-icon { color: #667eea; }
.action-therapy .action-icon { color: #f093fb; }
.action-game .action-icon { color: #4facfe; }
.action-doctors .action-icon { color: #43e97b; }

.action-card h4 {
  font-size: 1.3em;
  color: #1a0b3e;
  margin: 0 0 8px 0;
  font-weight: 700;
}

.action-card p {
  color: #999;
  font-size: 0.9em;
  margin: 0;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 4em;
  color: #ccc;
  margin-bottom: 20px;
}

.empty-state h2 {
  color: #1a0b3e;
  font-size: 1.8em;
  margin: 0 0 10px 0;
}

.empty-state p {
  color: #999;
  font-size: 1.1em;
  margin: 0 0 30px 0;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 1em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
}

/* Responsive Design */
@media (max-width: 900px) {
  .dashboard-container {
    padding: 18px 14px;
  }

  .welcome-header {
    padding: 30px 20px;
  }

  .header-left h1.welcome-title {
    font-size: 2em;
  }

  .active-child-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    padding: 20px 16px;
  }

  .child-details {
    justify-content: center;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }
}

@media (max-width: 600px) {
  .dashboard-container {
    padding: 14px 10px;
  }

  .welcome-header {
    padding: 18px 14px;
    border-radius: 14px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-left h1.welcome-title {
    font-size: 1.5em;
  }

  .header-left p.welcome-subtitle {
    font-size: 0.95em;
  }

  .section-title {
    font-size: 1.4em;
    margin-bottom: 16px;
  }

  .child-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .btn-primary-small {
    justify-content: center;
    width: 100%;
  }

  .profile-button {
    padding: 12px 10px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 18px 14px;
    border-radius: 14px;
  }

  .actions-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .results-container {
    grid-template-columns: 1fr;
  }

  .result-card {
    padding: 16px 12px;
    border-radius: 14px;
  }

  .result-card-header {
    align-items: flex-start;
    gap: 8px;
    flex-direction: column;
  }

  .result-card-footer {
    flex-direction: column;
    gap: 10px;
  }
}
</style>