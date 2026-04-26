<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
    name: 'DoctorForm',
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
                ratings: ''
            },
            message: " ",
            isEdit: false // Track if it's an edit operation
        };
    },
    mounted() {
        this.loadDoctorData();
    },
    methods: {
        loadDoctorData() {
            // Check if there is doctor data passed via query parameters
            const doctorQuery = this.$route.query.doctorData;
            if (doctorQuery) {
                this.doctorData = JSON.parse(doctorQuery);
                this.isEdit = true; // Set to true since we're editing
            }
        },
        handleImageUpload(event) {
            this.doctorData.image = event.target.files[0]; // Store the file
        },
        async submitDoctor(e) {
            e.preventDefault();
            let formData = new FormData();

            // Prepare doctor data without the image for submission
            let doctorDataCopy = { ...this.doctorData };
            const isUpdating = this.isEdit;

            if (!isUpdating) {
                delete doctorDataCopy.image; // Exclude image for new doctors
            }

            // Append doctorData as JSON blob
            formData.append('doctorData', new Blob([JSON.stringify(doctorDataCopy)], { type: 'application/json' }));

            // Append the image if it is being uploaded
            if (this.doctorData.image && !isUpdating) { // Ensure image is present only for create
                formData.append('image', this.doctorData.image);
            }

            try {
                const token = Cookies.get('admin_token');
                let response;

                if (isUpdating) {
                    // Update doctor request
                    response = await axios.put(`http://localhost:8080/api/v1/doctor/update/${this.doctorData.id}`, formData, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                        }
                    });
                } else {
                    // Create doctor request
                    response = await axios.post('http://localhost:8080/api/v1/doctor/create', formData, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                        }
                    });
                }

                console.log(response.data)

                if (response.data.responseStat) {
                    this.message = isUpdating ? "Doctor updated successfully!" : "Doctor created successfully!";
                    this.resetForm();

                    //move to doctors.
                    this.$router.push({
                        path: '/admin/doctors'
                    })
                } else {
                    this.message = response.data.responseMessage;
                }
            } catch (error) {
                // Enhanced error message handling
                if (error.response && error.response.data && error.response.data.responseMessage) {
                    this.message = "Error: " + error.response.data.responseMessage;
                } else {
                    this.message = "Error. " + error.message;
                }
            }
        },
        resetForm() {
            this.doctorData = {
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
                ratings: ''
            };
        }
    }
};
</script>


<template>
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link href="//fonts.googleapis.com/css2?family=Kumbh+Sans:wght@300;400;700&display=swap" rel="stylesheet">
    <div class="doctorform" style="width: calc(100vw - 300px);">
        <div class="doc_add_custom_form" style=" margin-top: -10px;">
            <h2>Add New Doctor</h2>
            <form action="#" method="post" style="margin-left: 10px">

                <br>

                <div class="row_div_sep">
                    <input v-model="doctorData.name" type="text" placeholder="Name" required>
                    <input style="color: black;" v-model="doctorData.phone" type="text" placeholder="Phone" required>
                </div>


                <div class="row_div_sep">
                    <textarea rows="2" cols="90" v-model="doctorData.description" placeholder="Description"
                        required></textarea>
                </div>


                <div class="row_div_sep">
                    <select style="color: black;" v-model="doctorData.experienceCount" required>
                        <option value="" disabled selected>Experience</option>
                        <option value="0-5">0-5 years</option>
                        <option value="5-8">5-8 years</option>
                        <option value="8-13">8-13 years</option>
                        <option value="13-17">13-17 years</option>
                        <option value="18-25">18-25 years</option>
                        <option value="25+">25+ years</option>
                    </select>

                    <select style="color: black;" v-model="doctorData.gender" required>
                        <option value="" disabled selected>Gender</option>
                        <option value="Male">Male</option>
                        <option value="Female">Female</option>
                    </select>

                    <div style="display: flex; flex-direction: column;">
                        <p style="margin-left: 15px; font-size: 13px; margin-top: 10px; color: blue;">Doctor's Picture
                        </p>
                        <input style="color: black; border: 0; " type="file" @change="handleImageUpload" required>
                    </div>

                </div>

                <div class="row_div_sep">
                    <input style="color: black; min-width: 820px;" v-model="doctorData.specialities" type="text"
                        placeholder="Specialities" required>
                </div>

                <div class="row_div_sep">
                    <input style="color: black;" v-model="doctorData.officeHours" type="text" placeholder="Office Hours"
                        required>
                    <input style="color: black;" v-model="doctorData.address" type="text" placeholder="Address"
                        required>
                </div>

                <button class="submit_button" @click="submitDoctor">{{ isEdit ? 'Update Doctor' :
                    'Create Doctor' }}</button>
            </form>



            <div style="width: 10px; height: 20px;"></div>
            <div class="message_show_alert_view">
                <p style="font-weight: 500; color: red; font-size: 18px;" v-if="message">{{ message }}</p>
            </div>
        </div>
    </div>
</template>


<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap');

select {
    appearance: none;
    /* Removes default styling */
    -webkit-appearance: none;
    /* Safari */
    -moz-appearance: none;
    /* Firefox */
    background-color: white;
    color: black;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
}

.doc_add_custom_form {
    position: absolute;
    margin-top: 100px;
    margin-left: 30px;
    padding-top: 100px;
    width: 98vw;
    height: calc(98vh-100px);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    font-family: "Inter", serif;
}

.doc_add_custom_form input {
    font-size: 15px;
}

.row_div_sep {
    display: flex;
    margin-left: -20px;
    max-width: calc(99vw - 400px);
}

.row_div_sep input {
    max-width: 400px;
}

.row_div_sep select {
    min-width: 200px;
    padding-left: 20px;
    padding-right: 20px;
}

.row_div_sep textarea {
    width: 820px;
    padding-left: 20px;
    padding-top: 10px;
    min-height: 100px;
}

.row_div_sep input,
.row_div_sep textarea,
.row_div_sep select {
    color: black;
    background-color: transparent;
    margin: 10px;
    border-bottom: 0.5px solid rgb(212, 208, 208);
    height: 55px;
    font-weight: 300;
    border-left: 0;
    border-top: 0;
    border-right: 0;
}

.input_group_right_sided {
    margin-left: 15px;
}

input {
    font-size: 12px;
}

.submit_button {
    background-color: black;
    margin-top: 40px;
    margin-left: -10px;
    width: 200px;
    height: 45px;
    border-radius: 7px;
}
</style>