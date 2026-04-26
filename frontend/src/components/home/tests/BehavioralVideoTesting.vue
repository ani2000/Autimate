<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
    data() {
        return {
            selectedActivity: '',
            videoFile: null,
            videoPreviewUrl: '',
            automaticZoom: true,
            presignedURL: '',
            presignedData: {},
            isLoading: false,
            filename: '',
            buttonText: 'Check Result',
            questionAnswers: null,
            dragOver: false,
            uploadProgress: 0,
        };
    },
    mounted() {
        const questionAnswersQuery = this.$route.query.questionAnswers;
        if (questionAnswersQuery) {
            this.questionAnswers = JSON.parse(questionAnswersQuery);
        }
    },

    methods: {
        async fetchPresignedURL() {
            try {
                const token = Cookies.get('token');
                const response = await axios.get('/api/v1/aex/url/presigned', {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });

                let payload = response.data;
                if (typeof response.data === 'string') {
                    try {
                        payload = JSON.parse(response.data);
                    } catch {
                        payload = {};
                    }
                }

                if (!payload || !payload.preSignedUrl) {
                    this.presignedData = {
                        uuid: `${Date.now()}_pseudo.mp4`,
                        mode: 'pseudo'
                    };
                    this.presignedURL = '';
                    return false;
                }

                this.presignedData = payload;
                this.presignedURL = payload.preSignedUrl;
                return true;
            } catch (error) {
                this.presignedData = {
                    uuid: `${Date.now()}_pseudo.mp4`,
                    mode: 'pseudo'
                };
                this.presignedURL = '';
                return false;
            }
        },

        onFileSelected(event) {
            const file = event.target.files[0];
            if (file) this.setVideoFile(file);
        },

        onDrop(event) {
            this.dragOver = false;
            const file = event.dataTransfer.files[0];
            if (file && file.type.startsWith('video/')) {
                this.setVideoFile(file);
            }
        },

        setVideoFile(file) {
            this.videoFile = file;
            this.filename = file.name;
            if (this.videoPreviewUrl) URL.revokeObjectURL(this.videoPreviewUrl);
            this.videoPreviewUrl = URL.createObjectURL(file);
        },

        sleep(ms) {
            return new Promise(resolve => setTimeout(resolve, ms));
        },

        async uploadFile() {
            const token = Cookies.get('token');

            if (!this.videoFile) {
                alert("Please select a video file before uploading.");
                return;
            }

            const baseAnswers = Array.isArray(this.questionAnswers)
                ? this.questionAnswers
                : Array.from({ length: 14 }, () => 0);

            const convertedAnswers = baseAnswers.map(answer => {
                if (answer === null || answer === undefined || answer === 'undefined') {
                    return 0;
                } else if (answer === "1") {
                    return 1;
                } else if (answer === "0") {
                    return 0;
                } else {
                    const parsed = parseInt(answer, 10);
                    return isNaN(parsed) ? 0 : parsed;
                }
            }).filter((_, index) => index < 14);

            const convertedAnswersAsText = this.convertAnswersToText(convertedAnswers);
            const therapySuggestionString = this.formatQuestionsAndAnswers(convertedAnswersAsText);

            try {
                this.isLoading = true;
                this.uploadProgress = 10;
                this.buttonText = 'Uploading video...';
                const hasCloudUpload = await this.fetchPresignedURL();
                if (!this.presignedData || !this.presignedData.uuid) {
                    this.presignedData = {
                        uuid: `${Date.now()}_pseudo.mp4`,
                        mode: 'pseudo'
                    };
                }

                this.uploadProgress = 30;

                if (hasCloudUpload) {
                    try {
                        const response = await axios.put(this.presignedURL, this.videoFile, {
                            headers: {
                                'Content-Type': this.videoFile.type,
                            },
                        });

                        if (response.status !== 200) {
                            throw new Error('Upload failed: ' + response.statusText);
                        }
                    } catch {
                        this.presignedData = {
                            uuid: `${Date.now()}_pseudo.mp4`,
                            mode: 'pseudo'
                        };
                    }
                }

                this.uploadProgress = 60;
                this.buttonText = 'Analysing behaviour...';

                const requestBody = {
                    "arrq10": convertedAnswers,
                    "video_name": this.presignedData.uuid,
                    "therapy_suggestion_string": therapySuggestionString
                };

                const req_id = (this.presignedData.uuid || `${Date.now()}_pseudo.mp4`).replace(/\.[^/.]+$/, '');

                this.suggestTherapy(therapySuggestionString, req_id);

                const responseInvoke = await axios.post('/api/v1/aex/invoke', requestBody, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });

                this.uploadProgress = 85;
                this.buttonText = 'Generating results...';

                const responseReqId = responseInvoke?.data?.requestID;
                const finalRequestId = responseReqId || req_id;

                this.sleep(10000).then(async () => {
                    this.uploadProgress = 100;
                    this.isLoading = false;
                    this.buttonText = 'Check Result';
                    this.$router.push({
                        path: '/dashboard/aex/r',
                        query: {
                            req_id: finalRequestId
                        }
                    });
                });

            } catch (error) {
                console.error("Error processing video:", error);
                const msg = error?.response?.data || 'Video processing failed. Please try again.';
                alert(typeof msg === 'string' ? msg : 'Video processing failed. Please try again.');
                this.isLoading = false;
                this.uploadProgress = 0;
                this.buttonText = 'Check Result';
            }
        },

        async checkResult() {
            this.uploadFile();
        },

        async suggestTherapy(therapySuggestionString, req_id) {
        },

        convertAnswersToText(answers) {
            const answerMappings = [
                ['Always', 'Usually', 'Sometimes', 'Rarely', 'Never'],
                ['Very easy', 'Quite easy', 'Quite difficult', 'Very difficult', 'Impossible'],
                ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'],
                ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'],
                ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'],
                ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'],
                ['Always', 'Usually', 'Sometimes', 'Rarely', 'Never'],
                ['Very typical', 'Quite typical', 'Slightly unusual', 'Very unusual', 'My child doesn\'t speak'],
                ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'],
                ['Many times a day', 'A few times a day', 'A few times a week', 'Less than once a week', 'Never'],
                ['Yes', 'No'],
            ];

            return answers.map((answer, index) => {
                if (index < 11) {
                    return answerMappings[index][answer] || 'Unknown';
                } else {
                    return answer;
                }
            });
        },

        formatQuestionsAndAnswers(answers) {
            const questions = [
                "Does your child look at you when you call his/her name?",
                "How easy is it for you to get eye contact with your child?",
                "Does your child point to indicate that s/he wants something? (e.g. a toy that is out of reach)",
                "Does your child point to share interest with you? (e.g. pointing at an interesting sight)",
                "Does your child pretend? (e.g. care for dolls, talk on a toy phone)",
                "Does your child follow where you're looking?",
                "When someone else in the family is visibly upset, does your child show signs of wanting to comfort them? (e.g. stroking hair, hugging them)",
                "Would you describe your child's first words as:",
                "Does your child use simple gestures? (e.g. wave goodbye)",
                "Does your child stare at nothing with no apparent purpose?",
                "Does any family member of this child have a history of autism?"
            ];

            const formattedText = questions.map((question, index) => {
                return `${question}\n${answers[index]}\n\n`;
            }).join('');

            return formattedText;
        },
    }
};
</script>

<template>
  <section class="video-shell">
    <div class="deco d1">🎬</div>
    <div class="deco d2">💜</div>
    <div class="deco d3">⭐</div>
    <div class="deco d4">🧸</div>

    <div class="video-card">
      <div class="card-mascot">📹</div>
      <h1>Behavioral Video Analysis</h1>
      <p class="subtitle">Upload a 15-20 second video of your child doing any natural activity.</p>

      <!-- Upload Zone -->
      <div
        class="upload-zone"
        :class="{ 'drag-over': dragOver, 'has-file': videoFile }"
        @dragover.prevent="dragOver = true"
        @dragleave="dragOver = false"
        @drop.prevent="onDrop"
        @click="$refs.fileInput.click()"
      >
        <input ref="fileInput" type="file" @change="onFileSelected" accept="video/*" class="hidden-input" />

        <div v-if="!videoFile" class="upload-placeholder">
          <div class="upload-icon">📁</div>
          <p class="upload-main-text">Drag & drop a video here</p>
          <p class="upload-sub-text">or click to browse files</p>
          <span class="upload-hint">MP4, MOV, AVI • Max 15-20 seconds</span>
        </div>

        <div v-else class="upload-selected">
          <span class="file-icon">🎥</span>
          <span class="file-name">{{ filename }}</span>
          <button class="change-btn" @click.stop="$refs.fileInput.click()">Change</button>
        </div>
      </div>

      <!-- Video Preview -->
      <div v-if="videoPreviewUrl" class="preview-section">
        <div class="preview-header">
          <h3>📺 Preview</h3>
          <label class="zoom-toggle">
            <input type="checkbox" v-model="automaticZoom" />
            <span class="toggle-track"><span class="toggle-thumb"></span></span>
            <span>Auto Zoom</span>
          </label>
        </div>

        <video
          :src="videoPreviewUrl"
          controls
          playsinline
          class="video-preview"
          :class="{ 'video-preview--autozoom': automaticZoom }"
        ></video>
        <p class="preview-hint">Auto Zoom crops the preview to fill the frame. It doesn't change the uploaded video.</p>
      </div>

      <!-- Progress -->
      <div v-if="isLoading" class="upload-progress">
        <div class="progress-bar-wrap">
          <div class="progress-bar-fill" :style="{ width: uploadProgress + '%' }"></div>
        </div>
        <span class="progress-label">{{ buttonText }} ({{ uploadProgress }}%)</span>
      </div>

      <!-- Submit Button -->
      <button class="submit-btn" :class="{ loading: isLoading }" @click="checkResult" :disabled="isLoading">
        <span v-if="!isLoading">🔬 {{ buttonText }}</span>
        <span v-else class="spinner-text">
          <span class="spinner"></span> {{ buttonText }}
        </span>
      </button>
    </div>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&display=swap');

.video-shell {
  width: 100%;
  min-height: calc(100vh - 120px);
  padding: 20px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(170deg, #FFF5E4 0%, #E8F4FD 40%, #F0E6FF 100%);
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.deco { position: fixed; font-size: 2rem; opacity: 0.12; animation: floatDeco 6s ease-in-out infinite; pointer-events: none; z-index: 0; }
.d1 { top: 8%; right: 6%; animation-delay: 0s; }
.d2 { top: 45%; right: 10%; animation-delay: 1.5s; }
.d3 { bottom: 12%; left: 5%; animation-delay: 3s; }
.d4 { top: 18%; left: 8%; animation-delay: 4.5s; }
@keyframes floatDeco { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-16px)} }

.video-card {
  width: 100%;
  max-width: 700px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  border: 2px solid rgba(255, 255, 255, 0.7);
  border-radius: 28px;
  box-shadow: 0 20px 50px rgba(100, 50, 200, 0.1);
  padding: 32px 28px;
  position: relative;
  z-index: 1;
  animation: cardPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  text-align: center;
}
@keyframes cardPop {
  from { opacity: 0; transform: scale(0.95) translateY(16px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.card-mascot { font-size: 2.8rem; margin-bottom: 6px; animation: mascotBounce 2s ease-in-out infinite; }
@keyframes mascotBounce { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-6px)} }

.video-card h1 {
  margin: 0;
  color: #1A0B3E;
  font-weight: 800;
  font-size: 1.6rem;
}
.subtitle {
  margin: 6px 0 20px;
  color: #5A4690;
  font-size: 0.95rem;
}

/* Upload Zone */
.upload-zone {
  border: 3px dashed #D4CCFF;
  border-radius: 20px;
  padding: 36px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(240, 230, 255, 0.25);
  position: relative;
}
.upload-zone:hover, .upload-zone.drag-over {
  border-color: #A855F7;
  background: rgba(168, 85, 247, 0.06);
  transform: scale(1.01);
}
.upload-zone.has-file {
  border-color: #22c55e;
  border-style: solid;
  background: rgba(34, 197, 94, 0.04);
  padding: 18px 20px;
}
.hidden-input { display: none; }

.upload-placeholder { text-align: center; }
.upload-icon { font-size: 3rem; margin-bottom: 8px; animation: floatDeco 3s ease-in-out infinite; }
.upload-main-text { font-size: 1.1rem; font-weight: 700; color: #1A0B3E; margin: 0; }
.upload-sub-text { font-size: 0.88rem; color: #5A4690; margin: 4px 0 8px; }
.upload-hint {
  display: inline-block;
  font-size: 0.75rem;
  color: #8B7EC8;
  background: rgba(108, 99, 255, 0.06);
  padding: 4px 14px;
  border-radius: 50px;
}

.upload-selected {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}
.file-icon { font-size: 1.6rem; }
.file-name { font-weight: 600; color: #1A0B3E; font-size: 0.95rem; }
.change-btn {
  border: 1.5px solid #D4CCFF;
  background: #fff;
  color: #6C63FF;
  padding: 5px 14px;
  border-radius: 50px;
  font-weight: 700;
  font-size: 0.8rem;
  cursor: pointer;
  font-family: 'Syne', sans-serif;
}

/* Preview */
.preview-section {
  margin-top: 20px;
  text-align: left;
}
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.preview-header h3 {
  margin: 0;
  color: #1A0B3E;
  font-weight: 700;
  font-size: 1rem;
}

.zoom-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  color: #5A4690;
  user-select: none;
}
.zoom-toggle input { display: none; }
.toggle-track {
  width: 36px; height: 20px;
  background: #D4CCFF;
  border-radius: 10px;
  position: relative;
  transition: background 0.2s;
}
.zoom-toggle input:checked + .toggle-track { background: #A855F7; }
.toggle-thumb {
  position: absolute;
  top: 2px; left: 2px;
  width: 16px; height: 16px;
  background: #fff;
  border-radius: 50%;
  transition: transform 0.2s;
}
.zoom-toggle input:checked + .toggle-track .toggle-thumb { transform: translateX(16px); }

.video-preview {
  width: 100%;
  aspect-ratio: 16 / 9;
  max-height: 55vh;
  background: #000;
  border-radius: 16px;
  object-fit: contain;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}
.video-preview--autozoom { object-fit: cover; }

.preview-hint {
  font-size: 0.75rem;
  color: #8B7EC8;
  margin: 8px 0 0;
  text-align: center;
}

/* Progress */
.upload-progress {
  margin-top: 20px;
}
.progress-bar-wrap {
  height: 10px;
  background: #E8E0FF;
  border-radius: 999px;
  overflow: hidden;
}
.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #6C63FF, #A855F7, #FF85A1);
  border-radius: 999px;
  transition: width 0.5s ease;
}
.progress-label {
  display: block;
  margin-top: 6px;
  font-size: 0.82rem;
  font-weight: 700;
  color: #6C63FF;
}

/* Submit */
.submit-btn {
  margin-top: 20px;
  width: 100%;
  border: 0;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  padding: 16px 28px;
  border-radius: 50px;
  font-weight: 700;
  font-family: 'Syne', sans-serif;
  font-size: 1.1rem;
  cursor: pointer;
  box-shadow: 0 8px 28px rgba(108, 99, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.submit-btn:hover:not(:disabled) {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 12px 36px rgba(108, 99, 255, 0.4);
}
.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}
.submit-btn.loading {
  background: linear-gradient(135deg, #A855F7, #6C63FF);
}

.spinner-text { display: flex; align-items: center; justify-content: center; gap: 10px; }
.spinner {
  width: 20px; height: 20px;
  border: 3px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Responsive */
@media (max-width: 640px) {
  .video-shell { padding: 12px 8px; }
  .video-card { padding: 20px 16px; border-radius: 20px; }
  .video-card h1 { font-size: 1.3rem; }
  .upload-zone { padding: 24px 14px; }
  .submit-btn { font-size: 1rem; padding: 14px 20px; }
}
</style>
