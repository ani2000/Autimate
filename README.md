# Autimate

**Disability-tech SaaS for Early Autism Spectrum Disorder (ASD) Screening**

Targeting underserved families in Bangladesh with ML-powered screening and personalized therapy delivery.

🏆 **3rd Place - Hult Prize SUST 2025** (NeuroLearn approach)

---

## Core Mission

Early ASD detection and personalized gamified therapy for children in underserved communities.

---

## Tech Stack

### Frontend
- **Vue.js 3** + **Vite** — Responsive UI
- **Vue Router** — Client-side routing
- **Axios** — API communication

### Backend
- **Spring Boot 3.3.4** (Java 21) — Business logic, API orchestration
- **Spring Security** + **JWT** — Authentication/authorization
- **MongoDB Cloud** — Document storage (profiles, test results, therapy progress)
- **Spring Data MongoDB** — Data persistence layer

### ML/AI Pipeline

#### 1. Activity-Based Video Model (Computer Vision)
- **Architecture:** PyTorch + ONNX Runtime
- **Input:** 16-frame video tensors `(C, T, H, W)` 
- **Activities Analyzed:** Walking, grabbing, repetitive motions
- **Methodology:** Multi-Dataset Supervised Contrastive Learning (Rani & Verma, WACV 2024)
- **Pipeline:** OpenCV frame extraction → normalization → tensor transposition → ONNX inference

#### 2. Q10 Questionnaire Model
- **Architecture:** ONNX classification model
- **Features:** 14 inputs (developmental questions, age, gender, family history)
- **Output:** ASD probability score

#### 3. Drawing Therapy Analysis (Multimodal AI)
- **Model:** Gemini 1.5 Flash
- **Input:** Canvas snapshot composite images
- **Output:** Real-time text feedback on fine motor skills

### Identity & Security
- **NID Verification:** Bangladesh Election Commission (EC) server integration
- **Purpose:** Platform safety, user authenticity guarantee

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend (Vue 3)                    │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ Auth/NID    │  │ ASD Testing  │  │ Therapy/Games    │  │
│  │ Verification│  │ (Video + Q10)│  │ (Drawing, Speech)│  │
│  └─────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓ JWT + REST API
┌─────────────────────────────────────────────────────────────┐
│                  Backend (Spring Boot 3.3.4)                │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │ Auth Service │  │ ASD Screening│  │ Therapy Engine  │  │
│  │ (JWT + NID)  │  │ Orchestration│  │ (Gemini API)    │  │
│  └──────────────┘  └──────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   Data Layer (MongoDB Cloud)                │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │ UserEntity   │  │ ASDExEntity  │  │ TherapyProgress │  │
│  │ (NID, JWT)   │  │ (Scores)     │  │ (Sessions)      │  │
│  └──────────────┘  └──────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  ML Inference (Python + ONNX)               │
│  ┌──────────────────────┐  ┌────────────────────────────┐  │
│  │ video_onnx_proc.py   │  │ q10_manager.py             │  │
│  │ (CV model inference) │  │ (Questionnaire classifier) │  │
│  └──────────────────────┘  └────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## Core Features

### 1. **ASD Screening**

#### Video-Based Behavioral Analysis
- Parents record 16-frame videos per activity prompt
- Model analyzes: walking patterns, object interaction, repetitive behaviors
- Output: Activity-based biomarker classification

#### Q10 Questionnaire
- 14-feature developmental assessment
- Combines behavioral questions + demographic data
- Generates probability score

### 2. **Doctor Discovery**
- Geolocation-based specialist search (IpGeolocation API)
- Distance filtering + rating system
- Appointment booking + tracking

### 3. **Personalized Therapy Engine**

Based on screening results, suggests:

#### Gamified Interventions
- **Sudoku** — Pattern recognition
- **Flashcards** — Memory + association
- **Snake Game** — Motor coordination
- **Tap Game** — Reaction time

#### Therapy Modules
- **Drawing Therapy** — Gemini 1.5 Flash analyzes canvas snapshots, provides fine motor skill feedback
- **Speech Therapy** — Guided vocal exercises with attempt tracking
- **Breathing Exercises** — Anxiety regulation
- **Social Skills Stories** — Scenario-based learning

### 4. **Progress Tracking**
- Weekly test re-administration
- Therapy session history
- Visual improvement charts

### 5. **Video Chat Socialization**
- WebRTC-based peer interaction
- Parental supervision controls

---

## Project Structure

```
autimate/
├── backend/
│   ├── src/main/java/org/ww/wigglew/
│   │   ├── controller/
│   │   │   ├── AuthenticationController.java
│   │   │   ├── aex/AutismExController.java
│   │   │   ├── DoctorController.java
│   │   │   └── SpeechTherapyController.java
│   │   ├── service/
│   │   │   ├── auth/AuthenticationService.java
│   │   │   ├── aex/ASDExServerlessInvokeService.java
│   │   │   ├── GeminiImageUploaderService.java
│   │   │   └── TherapySuggestionAPIService.java
│   │   ├── entity/
│   │   │   ├── auth/UserEntity.java
│   │   │   ├── aex/ASDExEntity.java
│   │   │   └── doctor/DoctorEntity.java
│   │   └── config/
│   │       ├── SecurityConfiguration.java
│   │       └── jwt/JWTAuthenticationFilter.java
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── auth/Authentication.vue
│   │   │   ├── home/
│   │   │   │   ├── tests/
│   │   │   │   │   ├── QuestionnaireTesting.vue
│   │   │   │   │   └── BehavioralVideoTesting.vue
│   │   │   │   ├── therapy/
│   │   │   │   │   ├── DrawingGame.vue
│   │   │   │   │   └── SpeechTherapyCoach.vue
│   │   │   │   └── games/
│   │   │   │       ├── SudokuLiteGame.vue
│   │   │   │       └── FlashcardGame.vue
│   │   └── router.js
│   └── package.json
│
└── ai_fn/
    ├── video_onnx_proc.py      # CV model inference
    ├── q10_manager.py           # Questionnaire model
    ├── video_inference.py       # Video preprocessing pipeline
    └── q10_model.onnx           # Trained ONNX model
```

---

## Setup Instructions

### Prerequisites
- **Java 21**
- **Node.js 18+**
- **Python 3.9+**
- **MongoDB Cloud** account
- **Gemini API** key

### Backend Setup

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

**Environment Variables** (create `application.properties`):
```properties
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster.mongodb.net/autimate
jwt.secret=<your-jwt-secret>
gemini.api.key=<your-gemini-api-key>
ec.nid.verification.url=<bangladesh-ec-server-url>
```

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

### ML Inference Setup

```bash
cd ai_fn
pip install -r requirements.txt  # torch, onnxruntime, opencv-python
python video_inference.py --input <video_path>
```

---

## API Endpoints

### Authentication
- `POST /api/auth/register` — Register with NID verification
- `POST /api/auth/login` — JWT token generation
- `POST /api/auth/verify-nid` — Bangladesh EC NID check

### ASD Screening
- `POST /api/aex/video` — Upload video for CV analysis
- `POST /api/aex/questionnaire` — Submit Q10 responses
- `GET /api/aex/results/{childId}` — Retrieve test results

### Therapy
- `POST /api/therapy/drawing/analyze` — Gemini canvas analysis
- `POST /api/therapy/speech/session` — Start speech therapy session
- `GET /api/therapy/suggestions/{testId}` — Get personalized interventions

### Doctors
- `GET /api/doctors/nearby?lat={lat}&lng={lng}` — Find specialists by location
- `POST /api/doctors/appointment` — Book appointment

---

## Security Features

1. **NID Verification** — All registrations validated against Bangladesh Election Commission servers
2. **JWT Authentication** — Stateless token-based auth (15min access + 7d refresh)
3. **Role-Based Access** — Parent/Admin/Doctor roles
4. **Input Validation** — Jakarta Bean Validation on all endpoints
5. **CORS Configuration** — Restricted origins for production

---

## ML Model Details

### Video Model Training
- **Dataset:** Custom-collected from autism schools (Proyash + others) + neurotypical controls
- **Augmentation:** Temporal jitter, spatial crops, color normalization
- **Loss Function:** Supervised Contrastive Loss (Rani & Verma, 2024)
- **Metrics:** F1-Score, AUC-ROC

### Q10 Model Training
- **Features:** Age, gender, 10 behavioral questions, family history
- **Architecture:** Feedforward neural network → ONNX export
- **Validation:** 5-fold cross-validation

---

## Future Roadmap

- [ ] Bengali language support
- [ ] Offline-first PWA for rural areas
- [ ] Parental education modules
- [ ] Integration with government health databases
- [ ] WhatsApp bot for test reminders
- [ ] Blockchain-based medical record security

---

## Contributing

This is a social-impact project. Contributions welcome:

1. Fork repo
2. Create feature branch (`git checkout -b feature/new-therapy-module`)
3. Commit changes (`git commit -m 'Add emotion recognition game'`)
4. Push branch (`git push origin feature/new-therapy-module`)
5. Open Pull Request

---

## Team

**Co-Founders** — Post-JavaFest 2024

**Recognition** — 3rd Place, Hult Prize SUST 2025

---

## License

MIT License (or specify your choice)

---

## Contact

**Project Lead:** anindyahdec2021@gmail.com  
**GitHub:** [@ani2000](https://github.com/ani2000)

---

## Acknowledgments

- **Rani & Verma (WACV 2024)** — Multi-Dataset Supervised Contrastive Learning methodology
- **Proyash & Partner Schools** — Video dataset contribution
- **Bangladesh Election Commission** — NID verification infrastructure
- **Hult Prize Foundation** — Recognition and mentorship
