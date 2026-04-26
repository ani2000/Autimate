<script>
export default {
    name: 'Modal',
    props: {
        isTestingModel: {
            type: Boolean,
            default: false,
        },
        isTestResultModel: {
            type: Boolean,
            default: false,
        },
    },
    data() {
        return {
            showForm: false, // Controls form visibility
        };
    },
    methods: {
        toggleFormVisibility() {
            this.showForm = !this.showForm; // Toggles the form's visibility
        },
    },
};
</script>


<template>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <div class="modal">
        <div class="modal-content">
            <button class="close-btn" @click="$emit('close')">&times;</button>
            <div class="space_maker"></div>



            <!-- Create new profile -->
            <div class="space_maker"></div>


            <!-- <section v-if="isTestingModel" class="autism_test_management">
                <div class="select_asd_test">
                    <h4 style="font-family:'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">Select preferred test</h4>
                    <br>

                    <router-link to="/dashboard/test-video/">
                        <div @click="$emit('close')" class="asd_test_sel_card">
                            <div class="test_redirect_card">
                                <img src="@/assets/public/child_walking.png" class="tc_card_bg">
                            </div>
                            <div class="tc_overlay"> </div>
                            <div class="tcname_cover">
                                <div class="test_name">Behavioral Video Analysis</div>
                            </div>
                        </div>
                    </router-link>

                    <br>
                    <router-link to="/dashboard/test-q/">
                        <div @click="$emit('close')" class="asd_test_sel_card">
                            <div class="test_redirect_card">
                                <img src="@/assets/public/baby_thinking.jpg" class="tc_card_bg">
                            </div>
                            <div class="tc_overlay"></div>
                            <div class="tcname_cover">
                                <div class="test_name">Behavioral Questionnaire</div>
                            </div>
                        </div>
                    </router-link>

                    <br>
                    <p style="font-size: 14px; color: burlywood;">You can do any test any number of times.</p>
                    <br><br>
                </div>
            </section>

            <section v-if="isTestResultModel" class="test_result_profile">

            </section> -->


            


            <slot></slot>
        </div>
    </div>
</template>


<style scoped>
.modal {
    display: flex;
    justify-content: center;
    align-items: center;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
    background-color: white;
    padding: 20px;
    border-radius: 10px;
    width: 400px;
    position: relative;
}

.close-btn {
    position: absolute;
    top: 10px;
    right: 10px;
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
}

.close-btn:hover {
    color: red;
}

.space_maker {
    width: 2px;
    height: 50px;
}

.new_profile_form {
    margin-top: 20px;
}

.select_asd_test {
    width: 100%;
    height: 530px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.asd_test_sel_card {
    width: 300px;
    height: 200px;
    background-color: white;
    position: relative;
    border-radius: 15px;
}


.tc_card_bg {
    width: 300px;
    height: 200px;
    z-index: 0;
    border-radius: 15px;

}

.tc_overlay {
    width: 300px;
    height: 200px;
    background-color: white;
    position: absolute;
    z-index: 1;
    margin-top: -200px;
    opacity: 0.4;
    border-radius: 15px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.tcname_cover{
    width: 300px;
    height: 200px;
    background-color: transparent;
    position: absolute;
    z-index: 1;
    margin-top: -200px;
    border-radius: 15px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.test_name {
    font-size: 22px;
    width: 280px;
    min-height: 60px;
    display: flex;
    background-color: rgb(237, 232, 232);
    color: black;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    position: fixed;
    border-radius: 20px;
}
</style>