<template>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.0.0/css/font-awesome.min.css"
        integrity="sha512-FEQLazq9ecqLN5T6wWq26hCZf7kPqUbFC9vsHNbXMJtSZZWAcbJspT+/NEAQkBfFReZ8r9QlA9JHaAuo28MTJA=="
        crossorigin="anonymous" referrerpolicy="no-referrer" />

        <h1 style="position: fixed; margin-top: 25px; margin-left: 6px;">All Doctors</h1>

    <div class="all_doc_view">


        <div class="doctor_card" v-for="doctor in doctors" :key="doctor.id">
            <!-- <div class="item-6" style="position: relative; height: 330px;" :style="{borderRadius: 0}">
                <img class="image-icon3" loading="lazy" :style="{borderRadius: 0}"
                    :src="doctor.image ? 'http://localhost:8080' + doctor.image : './public/image-3@2x.png'"
                    alt="Doctor Image" />

                <div class="profile-info">
                    <b class="text">{{ doctor.name }}</b>
                    <div class="pricing">{{ doctor.experienceCount || 'N/A' }}</div>
                    <div class="location-on-black-24dp-2-parent" style="margin-top: -10px;">
                        <div class="doane-street-fremont-ca-94538-wrapper">
                            <div class="doane-street-fremont"
                                style="position: absolute; left: 0; margin-left: 10px; margin-top: -2px;">
                                <i class="fa fa-map-marker" aria-hidden="true"></i>
                                {{ doctor.address || 'Address not available' }}
                            </div>
                        </div>
                    </div>
                    <div style="width: 1px; height: 30px;"></div>
                    <div class="control_buttons">
                        <button class="edit_doc" @click="editDoctor(doctor)"><i class="fa fa-pencil"
                                aria-hidden="true"></i> Edit</button>
                        <button class="delete_doc" @click="deleteDoctor(doctor.id)"><i class="fa fa-trash-o"
                                aria-hidden="true"></i> Delete</button>
                    </div>
                </div>
            </div> -->


            <div style="display: flex;">
                <div>
                    <h1 :style="{ fontSize: '22px', fontWeight: '600', color: 'black' }" class="text">{{ doctor.name }}
                    </h1> <br>

                    <p style="margin-top: 10px; width: 270px;">{{ doctor.description ? doctor.description.slice(0, 120) + (doctor.description.length > 120 ? '...' : '') : 'No description' }}</p><br>

                    <p>{{ doctor.experienceCount || 'N/A' }} years of Experience</p>
                </div>

                <img class="doc_image" loading="lazy"
                    :style="{ borderRadius: 0, width: '110px', height: '110px', borderRadius: '110px', position: 'absolute', top: 0, right: 0, marginRight: '15px', marginTop: '10px' }"
                    :src="doctor.image ? 'http://localhost:8080' + doctor.image : './public/image-3@2x.png'"
                    alt="Doctor Image" />

                    <div class="control_buttons">
                        <button class="edit_doc" @click="editDoctor(doctor)"><i class="fa fa-pencil"
                                aria-hidden="true"></i> Edit</button>
                        <button class="delete_doc" @click="deleteDoctor(doctor.id)"><i class="fa fa-trash-o"
                                aria-hidden="true"></i> Delete</button>
                    </div>
            </div>

        </div>

    </div>

</template>

<script>
import '../../css/global.css';
import '../../css/page7.css';

import axios from 'axios';
import Cookies from 'js-cookie';


export default {
    data() {
        return {
            doctors: [] // This will hold the doctor data
        };
    },
    mounted() {
        this.fetchDoctors();
    },
    methods: {
        async fetchDoctors() {
            const token = Cookies.get('admin_token');

            const ipAddress = await this.getUserIpAddress(); // Get user's IP address

            try {
                const response = await axios.get('http://localhost:8080/api/v1/doctor/list/nearby', {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'X-IP-Address': ipAddress
                    }
                });
                this.doctors = response.data; // Assuming the response is in the expected format
            } catch (error) {
                console.error('Error fetching doctors:', error);
            }
        },
        async getUserIpAddress() {
            try {
                const response = await axios.get('https://api.ipify.org?format=json');
                return response.data.ip; // Returns the user's IP address
            } catch (error) {
                console.error('Error fetching IP address:', error);
                return '0.0.0.0'; // Default IP if unable to fetch
            }
        },
        editDoctor(doctor) {
            // Redirect to edit page and pass doctor data as query params
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
                    })
                }
            });
        },
        async deleteDoctor(doctorId) {
            const token = Cookies.get('admin_token'); // Fetch the admin token from cookies
            if (!token) {
                console.error('Access Denied: No admin token found.');
                return; // Optionally, you can show a message to the user here
            }

            try {
                const response = await axios.delete(`http://localhost:8080/api/v1/doctor/delete/${doctorId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                console.log('Doctor deleted successfully:', response.data);
                this.fetchDoctors(); // Refresh the list of doctors
            } catch (error) {
                console.error('Error deleting doctor:', error);
            }
        },
    }
};
</script>



<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap');

.all_doc_view {
    width: 100vw;
    min-height: calc(98vh-100px);
    display: flex;
    justify-content: flex-start;
    align-items: flex-start;
    margin-top: 100px;
}

.doctor_card {
    color: black;
    font-family: "Inter", serif;
    margin: 7px;
    width: 400px;
    height: 220px;
    background-color: white;
    padding: 10px;
    position: relative;
    border-radius: 10px;
}


.control_buttons {
    width: 100%;
    height: 30px;
    position: absolute;
    bottom: 0;
    left: 0;

    /* border-bottom-left-radius: 15px; */
    /* border-bottom-right-radius: 15px; */
}

.edit_doc {
    width: 50%;
    background-color: black;
    border-radius: 0;
    /* border-bottom-left-radius: 15px; */
    height: 30px;
    font-size: 13px;
    color: white;
    opacity: 0.8;
}

.delete_doc {
    width: 50%;
    background-color: rgb(144, 44, 44);
    border-radius: 0;
    /* border-bottom-right-radius: 15px; */
    height: 30px;
    font-size: 13px;
    color: white;
}
</style>