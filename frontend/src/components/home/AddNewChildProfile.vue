<template>
    <div class="newch-page">
        <section class="creator-shell">
            <article class="profile-creator-card">
                <p class="eyebrow">New Child Profile</p>
                <h2 class="child-info-title">Child Information</h2>
                <p class="helper-text">Add basic information to personalize therapy and learning activities.</p>

                <form id="childForm" class="child-form" @submit.prevent="submitChildForm">
                    <label for="name">Full Name</label>
                    <input
                        id="name"
                        type="text"
                        class="input-field"
                        v-model="newChild.name"
                        placeholder="e.g. Anindya Rahman"
                        autocomplete="name"
                        required
                    >

                    <label for="gender">Gender</label>
                    <select id="gender" class="input-field" v-model="newChild.gender" required>
                        <option value="" disabled>Select Gender</option>
                        <option value="female">Female</option>
                        <option value="male">Male</option>
                    </select>

                    <label for="dob">Date of Birth</label>
                    <input
                        id="dob"
                        type="date"
                        class="input-field"
                        v-model="newChild.dob"
                        :max="today"
                        required
                    >

                    <button type="submit" class="submit-btn">+ Add Child</button>
                </form>
            </article>
        </section>
    </div>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

const API_BASE = (import.meta.env.VITE_API_BASE || '').replace(/\/$/, '');

export default {
    data() {
        return {
            testResults: [], // Will hold the fetched test results
            children: null,  // Will hold the fetched child profiles
            newChild: { name: '', dob: '', gender: '' }, // New child profile data
            selectedFile: null, // File for upload
            activeChildId: null, // Hold the selected active child ID
            today: new Date().toISOString().split('T')[0],
        };
    },
    methods: {
        //fetch all profile to check if profile of this child already added or not.
        async fetchChildren() {
            try {
                const token = Cookies.get('token');
                const response = await axios.get(`${API_BASE}/api/v1/child/list`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                this.children = response.data || []; // Ensure empty array if null
                this.activateChildSession();
            } catch (error) {
                console.error("Error fetching child profiles:", error);
            }
        },

        // async submitChildForm() {
        //     try {
        //         const formData = new FormData();
        //         formData.append("name", this.newChild.name);
        //         formData.append("dob", this.newChild.dob);
        //         formData.append("gender", this.newChild.gender);

        //         console.log("newChild: ", this.newChild);

        //         // formData.append("file", this.selectedFile); // Append file

        //         console.log("formdata: -__---");
        //         console.log(formData);

        //         const token = Cookies.get('token');
        //         const response = await axios.post(`http://localhost:8080/api/v1/child/add`, formData, {
        //             headers: {
        //                 Authorization: `Bearer ${token}`,
        //             }
        //         });

        //         alert("Child profile created successfully!");
        //         this.fetchChildren(); // Refresh child profiles after creation
        //     } catch (error) {
        //         console.error("Error creating child profile:", error);
        //         alert("Failed to create child profile.");
        //     }
        // },

        async submitChildForm() {
            try {
                const token = Cookies.get('token');
                if (!token) {
                    alert('Session expired. Please log in again.');
                    this.$router.push('/auth');
                    return;
                }

                // Prepare the child data as an object, not FormData
                const childData = {
                    name: this.newChild.name.trim(),
                    dob: this.newChild.dob,
                    gender: this.newChild.gender
                };


                console.log(childData);

                // Convert the object to JSON and send it with a POST request
                await axios.post(`${API_BASE}/api/v1/child/add`, childData, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        'Content-Type': 'application/json' // Ensure content type is set to JSON
                    }
                });

                alert("Child profile created successfully!");
                this.$router.push('/dashboard/home');
            } catch (error) {
                console.error("Error creating child profile:", error);
                const message = error?.response?.data?.message || 'Failed to create child profile.';
                alert(message);
            }
        },

        handleFileUpload(event) {
            this.selectedFile = event.target.files[0]; // Store the selected file
        },

        formatDate(dateString) {
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', {
                day: '2-digit',
                month: 'short',
                year: 'numeric',
            });
        },
    },


    mounted() {

    },
};
</script>

<style scoped>
.newch-page {
    min-height: 100dvh;
    width: min(1120px, calc(100vw - 320px));
    margin: 0 auto;
    padding: 24px 18px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.creator-shell {
    width: 100%;
    display: flex;
    justify-content: center;
}

.profile-creator-card {
    width: min(560px, 100%);
    background: linear-gradient(160deg, #ffffff 0%, #f4f8ff 100%);
    border: 1px solid rgba(45, 64, 105, 0.12);
    border-radius: 22px;
    padding: 24px;
    box-shadow: 0 18px 35px rgba(25, 44, 77, 0.14);
}

.eyebrow {
    margin: 0;
    color: #0f6b74;
    font-weight: 800;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    font-size: 0.72rem;
}

.child-info-title {
    margin: 6px 0 2px;
    font-size: clamp(1.4rem, 2.8vw, 2rem);
    color: #16213e;
}

.helper-text {
    margin: 0 0 16px;
    color: #4f647f;
    font-weight: 600;
}

.child-form {
    display: grid;
    gap: 8px;
}

.child-form label {
    margin-top: 6px;
    color: #2b3f5f;
    font-weight: 700;
    font-size: 0.92rem;
}

.input-field {
    width: 100%;
    min-height: 48px;
    border: 1px solid #c7d9ea;
    border-radius: 14px;
    background: #fff;
    color: #12223c;
    padding: 0 14px;
    font-size: 1rem;
}

.input-field:focus {
    outline: 2px solid #9ee8ef;
    border-color: #43b0bc;
}

.submit-btn {
    margin-top: 14px;
    width: 100%;
    min-height: 50px;
    border: 0;
    border-radius: 999px;
    background: linear-gradient(120deg, #0f7a85, #2a9cb2);
    color: #fff;
    font-size: 1rem;
    font-weight: 800;
    letter-spacing: 0.02em;
}

.submit-btn:hover {
    filter: brightness(0.96);
}

@media (max-width: 980px) {
    .newch-page {
        width: 100%;
        padding: 18px 12px;
        min-height: auto;
    }
}

@media (max-width: 600px) {
    .newch-page {
        padding: 12px 8px;
    }

    .profile-creator-card {
        border-radius: 16px;
        padding: 14px;
    }

    .helper-text {
        font-size: 0.93rem;
    }

    .input-field,
    .submit-btn {
        font-size: 16px;
    }
}
</style>