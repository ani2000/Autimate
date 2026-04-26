# Autimate - Complete Setup Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Backend Setup](#backend-setup)
3. [Frontend Setup](#frontend-setup)
4. [ML Inference Setup](#ml-inference-setup)
5. [Database Configuration](#database-configuration)
6. [External API Configuration](#external-api-configuration)
7. [Running the Full Stack](#running-the-full-stack)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- **Java Development Kit (JDK) 21** — [Download](https://adoptium.net/)
- **Node.js 18+** and npm — [Download](https://nodejs.org/)
- **Python 3.9+** — [Download](https://www.python.org/)
- **Git** — [Download](https://git-scm.com/)
- **Maven 3.8+** — Included via Maven Wrapper (./mvnw)

### Required Accounts
- **MongoDB Atlas** — [Sign up](https://www.mongodb.com/cloud/atlas)
- **Google Cloud** — For Gemini API access
- **Bangladesh EC API** — For NID verification (if available)
- **AWS S3** — For file storage (optional)

### Verify Installations

```bash
java -version        # Should show Java 21
node -v              # Should show v18+
npm -v               # Should show v8+
python --version     # Should show Python 3.9+
git --version        # Should show git 2.x+
```

---

## Backend Setup

### 1. Navigate to Backend Directory

```bash
cd backend
```

### 2. Create Configuration File

Create `src/main/resources/application.properties`:

```properties
# ========================================
# SERVER CONFIGURATION
# ========================================
server.port=8080
spring.application.name=autimate-backend

# ========================================
# MONGODB CONFIGURATION
# ========================================
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/autimate?retryWrites=true&w=majority
spring.data.mongodb.database=autimate

# ========================================
# JWT CONFIGURATION
# ========================================
jwt.secret=your-super-secret-jwt-key-min-256-bits-change-this-in-production
jwt.expiration=900000
jwt.refresh.expiration=604800000

# ========================================
# GEMINI API CONFIGURATION
# ========================================
gemini.api.key=your-gemini-api-key-here
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent

# ========================================
# NID VERIFICATION (Bangladesh EC)
# ========================================
ec.nid.verification.url=https://ec-server-endpoint-here
ec.nid.api.key=your-ec-api-key

# ========================================
# AWS S3 CONFIGURATION (Optional)
# ========================================
aws.s3.bucket.name=autimate-storage
aws.s3.region=us-east-1
aws.access.key.id=your-aws-access-key
aws.secret.access.key=your-aws-secret-key

# ========================================
# CORS CONFIGURATION
# ========================================
cors.allowed.origins=http://localhost:5173,http://localhost:3000
cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed.headers=*

# ========================================
# FILE UPLOAD
# ========================================
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# ========================================
# LOGGING
# ========================================
logging.level.org.ww.wigglew=DEBUG
logging.level.org.springframework.security=DEBUG
```

### 3. Install Dependencies & Build

```bash
# Windows
mvnw.cmd clean install

# Unix/Mac/Linux
./mvnw clean install
```

### 4. Run Backend

```bash
# Windows
mvnw.cmd spring-boot:run

# Unix/Mac/Linux
./mvnw spring-boot:run
```

Backend should start on **http://localhost:8080**

---

## Frontend Setup

### 1. Navigate to Frontend Directory

```bash
cd frontend
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Create Environment Configuration

Create `.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GEMINI_API_KEY=your-gemini-api-key
```

### 4. Run Development Server

```bash
npm run dev
```

Frontend should start on **http://localhost:5173**

### 5. Build for Production

```bash
npm run build
```

Production build will be in `dist/` directory.

---

## ML Inference Setup

### 1. Navigate to AI Functions Directory

```bash
cd ai_fn
```

### 2. Create Virtual Environment (Recommended)

```bash
# Create venv
python -m venv venv

# Activate venv
# Windows
venv\Scripts\activate

# Unix/Mac/Linux
source venv/bin/activate
```

### 3. Create Requirements File

Create `requirements.txt`:

```txt
torch==2.1.0
onnxruntime==1.16.0
opencv-python==4.8.1.78
numpy==1.24.3
pymongo==4.5.0
python-dotenv==1.0.0
fastapi==0.104.1
uvicorn[standard]==0.24.0
```

### 4. Install Dependencies

```bash
pip install -r requirements.txt
```

### 5. Create Configuration File

Create `config.py`:

```python
import os
from dotenv import load_dotenv

load_dotenv()

# MongoDB Configuration
MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017/")
DB_NAME = os.getenv("DB_NAME", "autimate")

# Model Paths
VIDEO_MODEL_PATH = "./video_model.onnx"
Q10_MODEL_PATH = "./q10_model.onnx"

# Inference Configuration
VIDEO_FRAME_COUNT = 16
VIDEO_INPUT_SIZE = (224, 224)
BATCH_SIZE = 1
```

### 6. Test Video Inference

```bash
python video_inference.py --input test_video.mp4
```

### 7. Test Q10 Model

```bash
python q10_manager.py --test
```

---

## Database Configuration

### 1. Create MongoDB Atlas Cluster

1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create a new cluster (Free tier available)
3. Create database user with read/write permissions
4. Whitelist IP address (or use 0.0.0.0/0 for development)
5. Get connection string

### 2. Initialize Collections

Collections will be auto-created by Spring Data MongoDB:

- `users` — User profiles with NID verification
- `asd_ex_entities` — ASD test results
- `question_exam_entities` — Q10 questionnaire responses
- `doctor_entities` — Registered specialists
- `therapy_suggestions` — Personalized interventions
- `speech_sessions` — Speech therapy tracking
- `doctor_appointments` — Appointment bookings

### 3. Seed Initial Data (Optional)

Create `backend/src/main/resources/data/doctors.json`:

```json
[
  {
    "name": "Dr. Rahman Ahmed",
    "specialization": "Pediatric Neurologist",
    "phone": "+880-1700-000000",
    "email": "dr.rahman@example.com",
    "location": {
      "address": "Dhaka Medical College Hospital",
      "latitude": 23.7282,
      "longitude": 90.3947
    },
    "rating": 4.8
  }
]
```

---

## External API Configuration

### 1. Gemini API Setup

1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create API key
3. Add to `application.properties`: `gemini.api.key=YOUR_KEY`

### 2. Bangladesh EC NID Verification

Contact Bangladesh Election Commission for API access:
- Endpoint URL
- API credentials
- Request/response format documentation

### 3. AWS S3 Setup (Optional)

```bash
# Install AWS CLI
pip install awscli

# Configure credentials
aws configure
```

Enter:
- AWS Access Key ID
- AWS Secret Access Key
- Default region (e.g., `us-east-1`)
- Output format: `json`

---

## Running the Full Stack

### Option 1: Manual Start (3 terminals)

**Terminal 1 - Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

**Terminal 3 - ML Service (if separate):**
```bash
cd ai_fn
source venv/bin/activate
uvicorn do:app --reload --port 8001
```

### Option 2: Using Startup Script (Windows)

Run `START_PROJECT.bat` from project root:

```batch
@echo off
start cmd /k "cd backend && mvnw.cmd spring-boot:run"
timeout /t 5
start cmd /k "cd frontend && npm run dev"
timeout /t 5
start cmd /k "ngrok http 5173"
```

### Option 3: Docker Compose (Future)

```yaml
# docker-compose.yml (example)
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      MONGO_URI: ${MONGO_URI}
      
  frontend:
    build: ./frontend
    ports:
      - "5173:5173"
    depends_on:
      - backend
```

---

## Troubleshooting

### Backend Issues

**Issue:** `Port 8080 already in use`

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <process_id> /F

# Unix/Mac/Linux
lsof -ti:8080 | xargs kill -9
```

**Issue:** MongoDB connection failed

- Check connection string in `application.properties`
- Verify IP whitelist in MongoDB Atlas
- Test connection: `mongosh "your-connection-string"`

**Issue:** JWT token invalid

- Ensure `jwt.secret` is at least 256 bits
- Verify clock synchronization between client/server

### Frontend Issues

**Issue:** CORS errors

Update backend `CustomCorsFilter.java`:

```java
response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
```

**Issue:** API calls failing

- Check `VITE_API_BASE_URL` in `.env.local`
- Verify backend is running on correct port
- Check browser console for errors

### ML Inference Issues

**Issue:** ONNX model not found

```bash
# Verify model file exists
ls -la ai_fn/q10_model.onnx

# Check file permissions
chmod 644 ai_fn/q10_model.onnx
```

**Issue:** Torch/CUDA compatibility

```bash
# Force CPU-only inference
pip install torch --index-url https://download.pytorch.org/whl/cpu
```

**Issue:** Video processing slow

- Reduce `VIDEO_FRAME_COUNT` in `config.py`
- Use GPU acceleration if available
- Resize videos before processing

---

## Next Steps

1. **Security Hardening:**
   - Change all default secrets
   - Enable HTTPS in production
   - Implement rate limiting
   - Add input sanitization

2. **Performance Optimization:**
   - Enable MongoDB indexing
   - Implement Redis caching
   - Use CDN for static assets
   - Optimize video compression

3. **Deployment:**
   - Deploy backend to AWS/GCP/Heroku
   - Deploy frontend to Vercel/Netlify
   - Set up CI/CD pipeline
   - Configure production database

4. **Monitoring:**
   - Add application logging (ELK stack)
   - Set up error tracking (Sentry)
   - Monitor API performance (New Relic)

---

## Support

**Issues:** [GitHub Issues](https://github.com/ani2000/autimate/issues)  
**Email:** anindyahdec2021@gmail.com  
**Documentation:** [Wiki](https://github.com/ani2000/autimate/wiki)
