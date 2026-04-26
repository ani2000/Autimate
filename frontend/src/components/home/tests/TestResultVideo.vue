<script>
import '../../../css/global.css';
import '../../../css/page6.css';

import axios from 'axios';
import Cookies from 'js-cookie';

export default {
    data() {
        return {
            childId: null,
            childName: null
        };
    },
    mounted() {
        this.childId = Cookies.get('childId');
        this.childName = Cookies.get('child_name');
    },
}

</script>



<template>
    <div class="tres_wrapper" style="width: calc(100vw - 400px); display: flex; align-items: center; margin-left: 100px; justify-content: center;">
        <div class="icn-bxsstar3"></div>
        <div class="score-details" style="margin-top: 60px; margin-right: -40px;">
            <div class="full-name-score">
                <h3 class="full-names-score" style="font-size: 25px;font-family: 'Poppins'; text-decoration: none; color: rgb(50, 50, 230);">{{ childName }}’s 
                    <font color="gray" style="font-family: 'Poppins';">Behavioral Analysis Score</font></h3>
            </div>
            <div class="progress-bars-wrapper" style="margin-top: 50px;">
                <div class="progress-bars">
                    <div class="progress-bar">
                        <img class="subtract-icon" loading="lazy" alt="" src="@/assets/public/subtract.svg" />

                        <img class="progress-bar-child" alt="" src="@/assets/public/ellipse-211.svg" />

                        <img class="progress-bar-item" alt="" src="@/assets/public/ellipse-212.svg" />
                    </div>
                    <b class="link">66%</b>
                </div>
            </div>

            <div class="advice">
                <div class="adviceneed-improvement-wrapper" style="margin-top: 50px;">
                    <div class="adviceneed-improvement" style="font-family: 'Inter'">
                        Needs Improvement
                    </div>
                </div>
                <div class="click-here-to-container" style="font-size: 20px;">
                   <router-link to="/dashboard/doctors">
                    Check nearby doctors.
                   </router-link>
                </div>
            </div>
        </div>
        <div class="chart">
            <div class="chart-container">
                <div class="chart-area">
                    <!-- <div class="chart-elements">
                        <div class="chart-bars">
                            <img class="icon" loading="lazy" alt="" src="@/assets/public/-0117.svg" />

                            <img class="icon1" alt="" src="@/assets/public/-0213.svg" />

                            <img class="icon2" alt="" src="@/assets/public/-0313.svg" />
                        </div>
                        <div class="chart-label">
                            <div class="chart-legend">
                                <div class="legend-title">
                                    <p class="mcq">MCQ</p>
                                    <p class="mcq">BV1</p>
                                </div>
                                <div class="legend-description">
                                    <p class="mcq">224</p>
                                    <p class="mcq">245</p>
                                </div>
                            </div>
                        </div>
                    </div> -->
                </div>
                <div class="pagination">
                    <div class="pagination-controls">
                        <img class="up-kontur-34-icon" loading="lazy" alt="" src="@/assets/public/up--kontur34.svg" />

                        <img class="up-kontur-34-icon" loading="lazy" alt="" src="@/assets/public/up--kontur35.svg" />

                        <img class="up-kontur-34-icon" loading="lazy" alt="" src="@/assets/public/down--kontur7.svg" />
                    </div>
                </div>
                <!-- 
                <div class="details">

                    <div class="user">
                        <div class="paragraph-sec">100</div>
                        <div class="user-name">
                            <div class="row">50</div>
                        </div>
                        <div class="colmd">0</div>
                    </div>
                    <div class="quiz">
                        <div class="quiz-content">
                            <img class="testimonials-card-icon" loading="lazy" alt=""
                                src="@/assets/public/vector-153.svg" />

                            <div class="quiz-content-inner">
                                <div class="frame-parent11">
                                    <div class="frame-parent12">
                                        <div class="icn-bxsstar-wrapper">
                                            <div class="icn-bxsstar4"></div>
                                        </div>
                                        <div class="mcq1">MCQ</div>
                                    </div>
                                    <div class="frame-parent13">
                                        <div class="icn-bxsstar-container">
                                            <div class="icn-bxsstar4"></div>
                                        </div>
                                        <div class="bv11">BV1</div>
                                    </div>
                                   
                                </div>
                            </div>
                        </div>
                    </div>

                </div> -->
            </div>
        </div>
        

        <div class="sugg_therapies">
            <h4 style="font-size: 20px; color: blueviolet;">Suggested Therapies & Games</h4>
            <br>
            <ul style="font-size: 17px; margin-left: 20px; color: orangered;">
                <li>Memory therapy</li>
                <li>Speech therapy</li>
            </ul>
        </div>

    </div>
</template>


<style>
.sugg_therapies{
    position: absolute;
    right: 0;
    bottom: 0;
    margin-right: 120px;
    font-family: 'Poppins';
    margin-bottom: 50vh;
}

</style>