<template>
  <section class="page">
    <div class="head">
      <h1>{{ isEdit ? 'Update Doctor' : 'Add Doctor' }}</h1>
      <router-link to="/admin/doctors" class="link">Back to list</router-link>
    </div>

    <form class="form" @submit.prevent="submitDoctor">
      <input v-model="doctorData.name" type="text" placeholder="Name" required>
      <input v-model="doctorData.phone" type="text" placeholder="Phone" required>

      <textarea v-model="doctorData.description" rows="3" placeholder="Description" required></textarea>

      <select v-model="doctorData.experienceCount" required>
        <option disabled value="">Experience</option>
        <option value="0-5">0-5 years</option>
        <option value="5-8">5-8 years</option>
        <option value="8-13">8-13 years</option>
        <option value="13-17">13-17 years</option>
        <option value="18-25">18-25 years</option>
        <option value="25+">25+ years</option>
      </select>

      <select v-model="doctorData.gender" required>
        <option disabled value="">Gender</option>
        <option value="Male">Male</option>
        <option value="Female">Female</option>
      </select>

      <input v-model="doctorData.specialities" type="text" placeholder="Specialities" required>
      <input v-model="doctorData.officeHours" type="text" placeholder="Office Hours" required>
      <input v-model="doctorData.address" type="text" placeholder="Address" required>

      <label class="file-label">Doctor image (required for new doctor)
        <input type="file" @change="handleImageUpload" :required="!isEdit">
      </label>

      <button class="btn" type="submit">{{ isEdit ? 'Update doctor' : 'Create doctor' }}</button>
    </form>

    <p v-if="message" class="message">{{ message }}</p>
  </section>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  name: 'AddDoctorV2',
  data() {
    return {
      doctorData: {
        id: null,
        phone: '',
        name: '',
        description: '',
        experienceCount: '',
        gender: '',
        specialities: '',
        image: '',
        officeHours: '',
        website: '',
        longitude: '',
        latitude: '',
        address: '',
        ratings: '',
      },
      message: '',
      isEdit: false,
    };
  },
  mounted() {
    this.loadDoctorData();
  },
  methods: {
    loadDoctorData() {
      const doctorQuery = this.$route.query.doctorData;
      if (doctorQuery) {
        this.doctorData = JSON.parse(doctorQuery);
        this.isEdit = true;
      }
    },
    handleImageUpload(event) {
      this.doctorData.image = event.target.files[0];
    },
    async submitDoctor() {
      this.message = '';
      const formData = new FormData();
      const doctorDataCopy = { ...this.doctorData };
      const isUpdating = this.isEdit;

      if (!isUpdating) {
        delete doctorDataCopy.image;
      }

      formData.append('doctorData', new Blob([JSON.stringify(doctorDataCopy)], { type: 'application/json' }));

      if (this.doctorData.image && !isUpdating) {
        formData.append('image', this.doctorData.image);
      }

      try {
        const token = Cookies.get('admin_token');
        let response;

        if (isUpdating) {
          response = await axios.put(`http://localhost:8080/api/v1/doctor/update/${this.doctorData.id}`, formData, {
            headers: { Authorization: `Bearer ${token}` },
          });
        } else {
          response = await axios.post('http://localhost:8080/api/v1/doctor/create', formData, {
            headers: { Authorization: `Bearer ${token}` },
          });
        }

        if (response.data.responseStat) {
          this.$router.push('/admin/doctors');
        } else {
          this.message = response.data.responseMessage || 'Request failed';
        }
      } catch (error) {
        if (error.response && error.response.data && error.response.data.responseMessage) {
          this.message = `Error: ${error.response.data.responseMessage}`;
        } else {
          this.message = `Error: ${error.message}`;
        }
      }
    },
  },
};
</script>

<style scoped>
.page { max-width: 880px; display: grid; gap: 12px; }
.head { display: flex; align-items: center; justify-content: space-between; }
h1 { margin: 0; color: #0f172a; }
.link { color: #1e40af; text-decoration: none; }
.form { background: #fff; border-radius: 12px; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08); padding: 16px; display: grid; gap: 10px; }
input, textarea, select { border: 1px solid #cbd5e1; border-radius: 10px; padding: 10px 12px; color: #0f172a; }
.file-label { font-size: 0.9rem; color: #334155; display: grid; gap: 8px; }
.btn { height: 42px; border: 0; border-radius: 10px; background: #0f172a; color: #fff; font-weight: 700; }
.message { color: #b91c1c; margin: 0; }
</style>
