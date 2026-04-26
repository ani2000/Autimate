<template>
  <section class="doctor-page">
    <div class="top-bar">
      <h2>Doctor Suggestions</h2>
      <select v-model="selectedSorting" @change="onSortingChange">
        <option value="nearby">Nearest</option>
        <option value="highest-rated">Highest Rated</option>
      </select>
    </div>

    <p class="mode-note">Uses real API data when available, with backend fallback sample doctors if needed.</p>

    <div v-if="doctors.length === 0" class="empty">No doctors available right now.</div>

    <div v-else class="doctor-grid">
      <article class="doctor-card" v-for="doctor in doctors" :key="doctor.id">
        <img class="img" :src="doctor.image ? 'http://localhost:8080' + doctor.image : '/image-3@2x.png'" alt="Doctor">
        <div>
          <h3>{{ doctor.name }}</h3>
          <p class="meta">{{ doctor.experienceCount || '_' }} years • {{ doctor.specialities || 'General ASD Support' }}</p>
          <p class="desc">{{ doctor.description || 'No description available' }}</p>
          <p class="addr">{{ doctor.address || 'Address unavailable' }}</p>
          <p class="phone">{{ doctor.phone || 'Phone unavailable' }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  name: 'DoctorSuggestionV2',
  data() {
    return {
      doctors: [],
      selectedSorting: 'nearby',
    };
  },
  mounted() {
    this.fetchDoctors(this.selectedSorting);
  },
  methods: {
    async getUserIpAddress() {
      try {
        const response = await axios.get('https://api.ipify.org?format=json');
        return response.data.ip;
      } catch {
        return '0.0.0.0';
      }
    },
    async fetchDoctors(sorting) {
      const token = Cookies.get('token');
      const ipAddress = await this.getUserIpAddress();
      const endpoint = sorting === 'highest-rated'
        ? 'http://localhost:8080/api/v1/doctor/list/ratings'
        : 'http://localhost:8080/api/v1/doctor/list/nearby';

      try {
        const response = await axios.get(endpoint, {
          headers: {
            Authorization: `Bearer ${token}`,
            'X-IP-Address': ipAddress,
          },
        });
        this.doctors = response.data || [];
      } catch {
        this.doctors = [];
      }
    },
    onSortingChange() {
      this.fetchDoctors(this.selectedSorting);
    },
  },
};
</script>

<style scoped>
.doctor-page { display: grid; gap: 10px; padding: 10px; }
.top-bar { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
h2 { margin: 0; color: #0f172a; }
select { height: 40px; min-width: 160px; border-radius: 10px; border: 1px solid #cbd5e1; padding: 0 10px; }
.mode-note { margin: 0; color: #64748b; font-size: 0.9rem; }
.empty { background: #fff; border-radius: 12px; padding: 16px; color: #64748b; }
.doctor-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 12px; }
.doctor-card { background: #fff; border-radius: 12px; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08); padding: 12px; display: grid; grid-template-columns: 70px 1fr; gap: 10px; }
.img { width: 70px; height: 70px; border-radius: 999px; object-fit: cover; }
h3 { margin: 0; color: #0f172a; }
.meta, .addr, .phone { margin: 6px 0 0; color: #64748b; font-size: 0.86rem; }
.desc { margin: 8px 0 0; color: #334155; font-size: 0.92rem; }
</style>
