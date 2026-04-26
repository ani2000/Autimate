<template>
  <section class="page">
    <div class="head">
      <h1>All Doctors</h1>
      <router-link to="/admin/doctors/add" class="add-btn">Add doctor</router-link>
    </div>

    <div v-if="doctors.length === 0" class="empty">No doctors found.</div>

    <div v-else class="grid">
      <article class="card" v-for="doctor in doctors" :key="doctor.id">
        <img
          class="avatar"
          :src="doctor.image ? 'http://localhost:8080' + doctor.image : '/image-3@2x.png'"
          alt="Doctor image"
        >

        <div class="body">
          <h3>{{ doctor.name }}</h3>
          <p class="meta">{{ doctor.experienceCount || 'N/A' }} years • {{ doctor.gender || 'N/A' }}</p>
          <p class="desc">{{ shortDescription(doctor.description) }}</p>
          <p class="meta">{{ doctor.address || 'Address not available' }}</p>
        </div>

        <div class="actions">
          <button class="btn edit" @click="editDoctor(doctor)">Edit</button>
          <button class="btn delete" @click="deleteDoctor(doctor.id)">Delete</button>
        </div>
      </article>
    </div>
  </section>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  name: 'AllDoctorsV2',
  data() {
    return {
      doctors: [],
    };
  },
  mounted() {
    this.fetchDoctors();
  },
  methods: {
    shortDescription(text) {
      if (!text) return 'No description';
      return text.length > 130 ? `${text.slice(0, 130)}...` : text;
    },
    async getUserIpAddress() {
      try {
        const response = await axios.get('https://api.ipify.org?format=json');
        return response.data.ip;
      } catch {
        return '0.0.0.0';
      }
    },
    async fetchDoctors() {
      const token = Cookies.get('admin_token');
      const ipAddress = await this.getUserIpAddress();
      try {
        const response = await axios.get('http://localhost:8080/api/v1/doctor/list/nearby', {
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
    editDoctor(doctor) {
      this.$router.push({
        path: '/admin/doctors/add',
        query: {
          doctorData: JSON.stringify({
            id: doctor.id,
            phone: doctor.phone,
            name: doctor.name,
            description: doctor.description,
            experienceCount: doctor.experienceCount,
            gender: doctor.gender,
            specialities: doctor.specialities,
            officeHours: doctor.officeHours,
            website: doctor.website,
            longitude: doctor.longitude,
            latitude: doctor.latitude,
            address: doctor.address,
            ratings: doctor.ratings,
          }),
        },
      });
    },
    async deleteDoctor(doctorId) {
      const token = Cookies.get('admin_token');
      if (!token) return;
      try {
        await axios.delete(`http://localhost:8080/api/v1/doctor/delete/${doctorId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.fetchDoctors();
      } catch {
      }
    },
  },
};
</script>

<style scoped>
.page { display: grid; gap: 14px; }
.head { display: flex; align-items: center; justify-content: space-between; }
h1 { margin: 0; color: #0f172a; }
.add-btn { text-decoration: none; background: #1d4ed8; color: #fff; padding: 9px 12px; border-radius: 10px; font-weight: 700; }
.empty { padding: 18px; border-radius: 12px; background: #fff; color: #64748b; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 12px; }
.card { background: #fff; border-radius: 12px; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08); padding: 12px; display: grid; grid-template-columns: 72px 1fr; gap: 10px; }
.avatar { width: 72px; height: 72px; border-radius: 999px; object-fit: cover; }
.body h3 { margin: 0; color: #0f172a; }
.meta { margin: 6px 0 0; color: #64748b; font-size: 0.86rem; }
.desc { margin: 8px 0 0; color: #334155; font-size: 0.92rem; }
.actions { grid-column: 1 / -1; display: flex; gap: 8px; margin-top: 8px; }
.btn { height: 36px; border: 0; border-radius: 9px; font-weight: 700; flex: 1; }
.btn.edit { background: #0f172a; color: #fff; }
.btn.delete { background: #b91c1c; color: #fff; }
</style>
