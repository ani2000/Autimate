<template>
  <section class="doctor-shell">
    <!-- Floating decorations -->
    <span class="float-deco d1">🏥</span>
    <span class="float-deco d2">💊</span>
    <span class="float-deco d3">🩺</span>
    <span class="float-deco d4">❤️</span>
    <span class="float-deco d5">🌟</span>

    <!-- Hero Header -->
    <header class="hero-header">
      <div class="hero-mascot">🧑‍⚕️</div>
      <div class="hero-text">
        <h1>Find a Doctor</h1>
        <p class="hero-sub">Discover caring specialists near you and manage your appointments</p>
      </div>
      <div class="speech-bubble">We'll help you find the best doctor for your child! 💜</div>
    </header>

    <!-- Tab Navigation -->
    <nav class="tab-nav">
      <button :class="['tab-btn', { active: activeTab === 'discover' }]" @click="activeTab = 'discover'">
        <span class="tab-icon">🔍</span> Find Doctors
        <span v-if="doctors.length" class="tab-badge">{{ doctors.length }}</span>
      </button>
      <button :class="['tab-btn', { active: activeTab === 'appointments' }]" @click="activeTab = 'appointments'; fetchAppointmentHistory()">
        <span class="tab-icon">📅</span> My Appointments
        <span v-if="upcomingAppointments.length" class="tab-badge pulse">{{ upcomingAppointments.length }}</span>
      </button>
      <button :class="['tab-btn', { active: activeTab === 'history' }]" @click="activeTab = 'history'; fetchAppointmentHistory()">
        <span class="tab-icon">📋</span> Patient History
        <span v-if="appointmentHistory.length" class="tab-badge">{{ appointmentHistory.length }}</span>
      </button>
    </nav>

    <!-- ==================== DISCOVER TAB ==================== -->
    <div v-if="activeTab === 'discover'" class="tab-content">
      <div class="controls-bar">
        <div class="control-group grow">
          <label>🔍 Search Doctor</label>
          <input v-model="nameQuery" @input="applyFilters" placeholder="Search by doctor name..." class="ctrl-input">
        </div>
        <div class="control-group">
          <label>Sort By</label>
          <select v-model="selectedSorting" @change="reloadDoctors" class="ctrl-select">
            <option value="nearby">📍 Nearest</option>
            <option value="highest-rated">⭐ Highest Rated</option>
          </select>
        </div>
        <div class="control-group">
          <label>Min Rating</label>
          <select v-model.number="minRating" @change="applyFilters" class="ctrl-select">
            <option :value="0">All Ratings</option>
            <option :value="3.5">3.5+ ⭐</option>
            <option :value="4">4.0+ ⭐</option>
            <option :value="4.5">4.5+ ⭐</option>
          </select>
        </div>
        <div class="control-group">
          <label>Distance</label>
          <select v-model.number="maxDistanceKm" @change="applyFilters" class="ctrl-select">
            <option :value="9999">Any Distance</option>
            <option :value="5">Within 5 km</option>
            <option :value="10">Within 10 km</option>
            <option :value="20">Within 20 km</option>
          </select>
        </div>
        <div class="control-group grow">
          <label>Speciality</label>
          <input v-model="specialityQuery" @input="applyFilters" placeholder="e.g. Autism, Speech..." class="ctrl-input">
        </div>
        <button class="ctrl-btn" @click="refreshRealtimeLocation">📡 Use My Location</button>
      </div>

      <p class="loc-status" v-if="locationStatus">{{ locationStatus }}</p>

      <!-- Doctor Cards Grid -->
      <div class="doctor-grid" v-if="filteredDoctors.length">
        <article class="doc-card" v-for="doctor in filteredDoctors" :key="doctor.id">
          <div class="doc-card-top">
            <div class="doc-avatar-wrap">
              <img :src="doctorImageSrc(doctor)" :alt="doctor.name" class="doc-avatar" @error="onDoctorImageError">
              <span class="doc-badge" v-if="Number(doctor.ratings) >= 4.5">🏆</span>
            </div>
            <div class="doc-info">
              <h3 class="doc-name">{{ doctor.name }}</h3>
              <p class="doc-addr">📍 {{ doctor.address || 'Address not available' }}</p>
              <div class="doc-meta-row">
                <span class="star-display">
                  <span v-for="s in 5" :key="s" :class="['star', { filled: s <= Math.round(Number(doctor.ratings || 0)) }]">★</span>
                  <span class="rating-num">{{ ratingText(doctor.ratings) }}</span>
                </span>
                <span class="distance-pill">{{ distanceText(doctor.distanceKm) }}</span>
              </div>
            </div>
          </div>

          <p class="doc-desc">{{ doctor.description || 'Specialist in child development and ASD support' }}</p>

          <div class="doc-details">
            <div class="detail-chip"><span class="chip-label">🎯 Specialities</span> {{ doctor.specialities || 'General ASD' }}</div>
            <div class="detail-chip"><span class="chip-label">🕐 Hours</span> {{ doctor.officeHours || 'N/A' }}</div>
            <div class="detail-chip" v-if="doctor.experienceCount"><span class="chip-label">📅 Experience</span> {{ doctor.experienceCount }}</div>
          </div>

          <div class="doc-actions">
            <button class="btn-book" @click="openBooking(doctor)">📞 Book Visit</button>
            <button class="btn-reviews" @click="openReviews(doctor)">💬 Reviews</button>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">🔍</div>
        <p>No doctors found with the selected filters.</p>
        <button class="btn-action" @click="resetFilters">Reset Filters</button>
      </div>
    </div>

    <!-- ==================== APPOINTMENTS TAB ==================== -->
    <div v-if="activeTab === 'appointments'" class="tab-content">
      <div class="appointments-section" v-if="appointmentHistory.length">
        <!-- Upcoming -->
        <div v-if="upcomingAppointments.length" class="appt-group">
          <h3 class="group-title">📅 Upcoming Appointments</h3>
          <article class="appt-card" v-for="item in upcomingAppointments" :key="item.id">
            <div class="appt-left">
              <div class="appt-date-badge">
                <span class="appt-day">{{ formatDay(item.appointmentAt) }}</span>
                <span class="appt-month">{{ formatMonth(item.appointmentAt) }}</span>
              </div>
            </div>
            <div class="appt-center">
              <h4>{{ item.doctorName }}</h4>
              <p class="appt-time">🕐 {{ formatTime(item.appointmentAt) }}</p>
              <p class="appt-reason" v-if="item.reason">📝 {{ item.reason }}</p>
              <div class="appt-pills">
                <span :class="['status-pill', statusClass(item.status)]">{{ item.status }}</span>
                <span :class="['payment-pill', paymentClass(item.paymentStatus)]">
                  💳 {{ item.paymentStatus || 'PENDING' }}
                </span>
              </div>
            </div>
            <div class="appt-right">
              <button v-if="item.status === 'BOOKED'" class="btn-sm btn-visit" @click="openVisitNotes(item)">✅ Mark Visited</button>
              <button v-if="item.status === 'BOOKED'" class="btn-sm btn-reschedule" @click="openReschedule(item)">🔄 Reschedule</button>
              <button v-if="item.status === 'BOOKED'" class="btn-sm btn-cancel" @click="openCancelConfirm(item)">❌ Cancel</button>
              <button v-if="item.status === 'BOOKED' && (item.paymentStatus === 'PENDING' || !item.paymentStatus)" class="btn-sm btn-pay" @click="openPayment(item)">💳 Pay</button>
            </div>
          </article>
        </div>

        <!-- Past -->
        <div v-if="pastAppointments.length" class="appt-group">
          <h3 class="group-title">📋 Past Visits</h3>
          <article class="appt-card past" v-for="item in pastAppointments" :key="item.id">
            <div class="appt-left">
              <div class="appt-date-badge muted">
                <span class="appt-day">{{ formatDay(item.appointmentAt) }}</span>
                <span class="appt-month">{{ formatMonth(item.appointmentAt) }}</span>
              </div>
            </div>
            <div class="appt-center">
              <h4>{{ item.doctorName }}</h4>
              <p class="appt-time">🕐 {{ formatTime(item.appointmentAt) }}</p>
              <p class="appt-reason" v-if="item.reason">📝 {{ item.reason }}</p>
              <div class="appt-pills">
                <span :class="['status-pill', statusClass(item.status)]">{{ item.status }}</span>
                <span :class="['payment-pill', paymentClass(item.paymentStatus)]">
                  💳 {{ item.paymentStatus || 'PENDING' }}
                </span>
              </div>
              <div class="visit-notes-box" v-if="item.visitNotes">
                <strong>Doctor's Notes:</strong>
                <p>{{ item.visitNotes }}</p>
              </div>
            </div>
            <div class="appt-right">
              <button v-if="item.status === 'VISITED' && !hasRated(item.id)" class="btn-sm btn-rate" @click="openRating(item)">⭐ Rate</button>
              <span v-if="hasRated(item.id)" class="rated-badge">✅ Rated</span>
            </div>
          </article>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">📅</div>
        <p>No appointments yet. Find a doctor and book your first visit!</p>
        <button class="btn-action" @click="activeTab = 'discover'">Find a Doctor</button>
      </div>
    </div>

    <!-- ==================== PATIENT HISTORY TAB ==================== -->
    <div v-if="activeTab === 'history'" class="tab-content">
      <div v-if="appointmentHistory.length" class="history-timeline">
        <h3 class="group-title">📊 Complete Visit Timeline</h3>
        <div class="timeline">
          <div class="timeline-item" v-for="(item, i) in appointmentHistory" :key="item.id">
            <div class="timeline-dot" :class="statusClass(item.status)"></div>
            <div class="timeline-line" v-if="i < appointmentHistory.length - 1"></div>
            <div class="timeline-card">
              <div class="tl-header">
                <strong>{{ item.doctorName }}</strong>
                <span class="tl-date">{{ formatDate(item.appointmentAt) }}</span>
              </div>
              <div class="tl-body">
                <p v-if="item.reason"><span class="tl-label">Reason:</span> {{ item.reason }}</p>
                <p><span class="tl-label">Status:</span>
                  <span :class="['status-pill sm', statusClass(item.status)]">{{ item.status }}</span>
                </p>
                <p v-if="item.visitNotes"><span class="tl-label">Doctor's Notes:</span> {{ item.visitNotes }}</p>
                <p><span class="tl-label">Payment:</span>
                  <span :class="['payment-pill sm', paymentClass(item.paymentStatus)]">
                    {{ item.paymentStatus || 'PENDING' }}
                  </span>
                  <span v-if="item.paymentAmount"> — ৳{{ item.paymentAmount }}</span>
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Summary Stats -->
        <div class="history-stats">
          <div class="stat-card">
            <div class="stat-num">{{ appointmentHistory.length }}</div>
            <div class="stat-label">Total Visits</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ visitedCount }}</div>
            <div class="stat-label">Completed</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ cancelledCount }}</div>
            <div class="stat-label">Cancelled</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ uniqueDoctorCount }}</div>
            <div class="stat-label">Doctors Seen</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">📋</div>
        <p>No visit history yet. Your timeline will appear here after visits.</p>
      </div>
    </div>

    <!-- ==================== BOOKING MODAL ==================== -->
    <div v-if="showBookingModal" class="modal-overlay" @click.self="closeBooking">
      <div class="modal-card booking-modal">
        <button class="modal-close" @click="closeBooking">✕</button>
        <div class="modal-header">
          <div class="modal-icon">📞</div>
          <h3>Book Appointment</h3>
          <p class="modal-sub">with <strong>{{ selectedDoctor?.name }}</strong></p>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>📅 Date & Time</label>
            <input type="datetime-local" v-model="booking.appointmentAt" class="form-input">
          </div>
          <div class="form-group">
            <label>📝 Reason for Visit</label>
            <input type="text" v-model="booking.reason" placeholder="e.g. Weekly follow-up, Initial assessment..." class="form-input">
          </div>
          <div class="form-group">
            <label>💳 Payment Method</label>
            <div class="payment-options">
              <label v-for="m in paymentMethods" :key="m.value" :class="['pay-option', { selected: booking.paymentMethod === m.value }]">
                <input type="radio" :value="m.value" v-model="booking.paymentMethod">
                <span class="pay-icon">{{ m.icon }}</span>
                <span>{{ m.label }}</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>💰 Consultation Fee (৳)</label>
            <input type="number" v-model.number="booking.paymentAmount" placeholder="Enter amount" class="form-input" min="0">
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-confirm" @click="submitBooking">✅ Confirm Booking</button>
          <button class="btn-ghost" @click="closeBooking">Cancel</button>
        </div>
        <p v-if="bookingMessage" class="modal-msg" :class="{ success: bookingSuccess }">{{ bookingMessage }}</p>
      </div>
    </div>

    <!-- ==================== VISIT NOTES MODAL ==================== -->
    <div v-if="showVisitNotesModal" class="modal-overlay" @click.self="closeVisitNotes">
      <div class="modal-card notes-modal">
        <button class="modal-close" @click="closeVisitNotes">✕</button>
        <div class="modal-header">
          <div class="modal-icon">📝</div>
          <h3>Mark as Visited</h3>
          <p class="modal-sub">{{ visitNotesTarget?.doctorName }}</p>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Doctor's Notes (optional)</label>
            <textarea v-model="visitNotesText" rows="4" placeholder="Add any notes from the visit..." class="form-input form-textarea"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-confirm" @click="confirmVisited">✅ Confirm Visited</button>
          <button class="btn-ghost" @click="closeVisitNotes">Cancel</button>
        </div>
      </div>
    </div>

    <!-- ==================== RATING MODAL ==================== -->
    <div v-if="showRatingModal" class="modal-overlay" @click.self="closeRating">
      <div class="modal-card rating-modal">
        <button class="modal-close" @click="closeRating">✕</button>
        <div class="modal-header">
          <div class="modal-icon">⭐</div>
          <h3>Rate Your Visit</h3>
          <p class="modal-sub">{{ ratingTarget?.doctorName }}</p>
        </div>
        <div class="modal-body">
          <div class="star-picker">
            <span v-for="s in 5" :key="s"
              :class="['star-pick', { active: s <= ratingStars }]"
              @click="ratingStars = s"
              @mouseenter="ratingHover = s"
              @mouseleave="ratingHover = 0">
              ★
            </span>
          </div>
          <p class="star-label">{{ starLabels[ratingStars] || 'Tap a star to rate' }}</p>
          <div class="form-group">
            <label>Share your experience (optional)</label>
            <textarea v-model="ratingReview" rows="3" placeholder="How was your experience with the doctor?" class="form-input form-textarea"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-confirm" @click="submitRating" :disabled="ratingStars === 0">Submit Rating</button>
          <button class="btn-ghost" @click="closeRating">Cancel</button>
        </div>
        <p v-if="ratingMessage" class="modal-msg" :class="{ success: ratingSuccess }">{{ ratingMessage }}</p>
      </div>
    </div>

    <!-- ==================== PAYMENT MODAL ==================== -->
    <div v-if="showPaymentModal" class="modal-overlay" @click.self="closePayment">
      <div class="modal-card payment-modal">
        <button class="modal-close" @click="closePayment">✕</button>
        <div class="modal-header">
          <div class="modal-icon">💳</div>
          <h3>Complete Payment</h3>
          <p class="modal-sub">{{ paymentTarget?.doctorName }}</p>
        </div>
        <div class="modal-body">
          <div class="payment-summary">
            <div class="pay-row"><span>Consultation Fee</span><strong>৳{{ paymentTarget?.paymentAmount || 0 }}</strong></div>
            <div class="pay-row"><span>Payment Method</span><strong>{{ paymentTarget?.paymentMethod || 'CASH' }}</strong></div>
          </div>
          <div class="payment-options" style="margin-top: 12px;">
            <label v-for="m in paymentMethods" :key="m.value" :class="['pay-option', { selected: paymentMethodUpdate === m.value }]">
              <input type="radio" :value="m.value" v-model="paymentMethodUpdate">
              <span class="pay-icon">{{ m.icon }}</span>
              <span>{{ m.label }}</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-confirm" @click="confirmPayment">✅ Mark as Paid</button>
          <button class="btn-ghost" @click="closePayment">Cancel</button>
        </div>
      </div>
    </div>

    <!-- ==================== REVIEWS MODAL ==================== -->
    <div v-if="showReviewsModal" class="modal-overlay" @click.self="closeReviews">
      <div class="modal-card reviews-modal">
        <button class="modal-close" @click="closeReviews">✕</button>
        <div class="modal-header">
          <div class="modal-icon">💬</div>
          <h3>Reviews for {{ reviewsDoctor?.name }}</h3>
          <div class="reviews-summary" v-if="reviewsList.length">
            <span class="big-rating">{{ reviewsAvg.toFixed(1) }}</span>
            <span class="star-display">
              <span v-for="s in 5" :key="s" :class="['star', { filled: s <= Math.round(reviewsAvg) }]">★</span>
            </span>
            <span class="review-count">({{ reviewsList.length }} reviews)</span>
          </div>
        </div>
        <div class="modal-body reviews-list" v-if="reviewsList.length">
          <div class="review-item" v-for="rev in reviewsList" :key="rev.id">
            <div class="review-stars">
              <span v-for="s in 5" :key="s" :class="['star sm', { filled: s <= rev.stars }]">★</span>
            </div>
            <p class="review-text" v-if="rev.reviewText">{{ rev.reviewText }}</p>
            <p class="review-date">{{ formatDate(rev.createdAt) }}</p>
          </div>
        </div>
        <div class="modal-body" v-else>
          <p class="empty-reviews">No reviews yet for this doctor.</p>
        </div>
      </div>
    </div>

    <!-- ==================== RESCHEDULE MODAL ==================== -->
    <div v-if="showRescheduleModal" class="modal-overlay" @click.self="closeReschedule">
      <div class="modal-card reschedule-modal">
        <button class="modal-close" @click="closeReschedule">✕</button>
        <div class="modal-header">
          <div class="modal-icon">🔄</div>
          <h3>Reschedule Appointment</h3>
          <p class="modal-sub">{{ rescheduleTarget?.doctorName }}</p>
        </div>
        <div class="modal-body">
          <div class="current-schedule-info">
            <span class="info-label">Current:</span>
            <span>{{ formatDate(rescheduleTarget?.appointmentAt) }} at {{ formatTime(rescheduleTarget?.appointmentAt) }}</span>
          </div>
          <div class="form-group">
            <label>📅 New Date & Time</label>
            <input type="datetime-local" v-model="rescheduleDate" class="form-input">
          </div>
          <div class="form-group">
            <label>📝 Updated Reason (optional)</label>
            <input type="text" v-model="rescheduleReason" :placeholder="rescheduleTarget?.reason || 'Reason for visit'" class="form-input">
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-confirm" @click="submitReschedule" :disabled="!rescheduleDate">🔄 Confirm Reschedule</button>
          <button class="btn-ghost" @click="closeReschedule">Cancel</button>
        </div>
        <p v-if="rescheduleMessage" class="modal-msg" :class="{ success: rescheduleSuccess }">{{ rescheduleMessage }}</p>
      </div>
    </div>

    <!-- ==================== CANCEL CONFIRMATION MODAL ==================== -->
    <div v-if="showCancelConfirm" class="modal-overlay" @click.self="closeCancelConfirm">
      <div class="modal-card cancel-modal">
        <button class="modal-close" @click="closeCancelConfirm">✕</button>
        <div class="modal-header">
          <div class="modal-icon">⚠️</div>
          <h3>Cancel Appointment?</h3>
          <p class="modal-sub">This action cannot be undone</p>
        </div>
        <div class="modal-body">
          <div class="cancel-details">
            <p><strong>Doctor:</strong> {{ cancelTarget?.doctorName }}</p>
            <p><strong>Date:</strong> {{ formatDate(cancelTarget?.appointmentAt) }}</p>
            <p><strong>Time:</strong> {{ formatTime(cancelTarget?.appointmentAt) }}</p>
            <p v-if="cancelTarget?.reason"><strong>Reason:</strong> {{ cancelTarget?.reason }}</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-confirm cancel-confirm-btn" @click="confirmCancel">❌ Yes, Cancel Appointment</button>
          <button class="btn-ghost" @click="closeCancelConfirm">Keep Appointment</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import axios from 'axios';
import Cookies from 'js-cookie';

export default {
  data() {
    return {
      activeTab: 'discover',
      doctors: [],
      filteredDoctors: [],
      selectedSorting: 'nearby',
      minRating: 0,
      maxDistanceKm: 9999,
      specialityQuery: '',
      nameQuery: '',
      userLocation: null,
      locationStatus: '',
      locationWatchId: null,

      // Booking
      showBookingModal: false,
      selectedDoctor: null,
      booking: { appointmentAt: '', reason: '', paymentMethod: 'CASH', paymentAmount: 500 },
      bookingMessage: '',
      bookingSuccess: false,

      // Appointments
      appointmentHistory: [],

      // Visit Notes
      showVisitNotesModal: false,
      visitNotesTarget: null,
      visitNotesText: '',

      // Rating
      showRatingModal: false,
      ratingTarget: null,
      ratingStars: 0,
      ratingHover: 0,
      ratingReview: '',
      ratingMessage: '',
      ratingSuccess: false,
      ratedAppointments: new Set(),

      // Payment
      showPaymentModal: false,
      paymentTarget: null,
      paymentMethodUpdate: 'CASH',

      // Reviews
      showReviewsModal: false,
      reviewsDoctor: null,
      reviewsList: [],

      // Reschedule
      showRescheduleModal: false,
      rescheduleTarget: null,
      rescheduleDate: '',
      rescheduleReason: '',
      rescheduleMessage: '',
      rescheduleSuccess: false,

      // Cancel Confirmation
      showCancelConfirm: false,
      cancelTarget: null,

      paymentMethods: [
        { value: 'CASH', label: 'Cash', icon: '💵' },
        { value: 'BKASH', label: 'bKash', icon: '📱' },
        { value: 'NAGAD', label: 'Nagad', icon: '📲' },
        { value: 'CARD', label: 'Card', icon: '💳' },
      ],

      starLabels: {
        1: 'Poor 😔',
        2: 'Fair 😐',
        3: 'Good 🙂',
        4: 'Very Good 😊',
        5: 'Excellent 🤩',
      },
      // Local placeholder pools (use the 21 images in backend static folder)
      femalePlaceholders: Array.from({ length: 10 }, (_, i) => `/doctors/female/${i + 1}.jpeg`),
      malePlaceholders: Array.from({ length: 11 }, (_, i) => `/doctors/male/${i + 1}.jpeg`),
    };
  },
  computed: {
    upcomingAppointments() {
      return this.appointmentHistory.filter(a => a.status === 'BOOKED');
    },
    pastAppointments() {
      return this.appointmentHistory.filter(a => a.status !== 'BOOKED');
    },
    visitedCount() {
      return this.appointmentHistory.filter(a => a.status === 'VISITED').length;
    },
    cancelledCount() {
      return this.appointmentHistory.filter(a => a.status === 'CANCELLED').length;
    },
    uniqueDoctorCount() {
      return new Set(this.appointmentHistory.map(a => a.doctorId)).size;
    },
    reviewsAvg() {
      if (!this.reviewsList.length) return 0;
      return this.reviewsList.reduce((s, r) => s + r.stars, 0) / this.reviewsList.length;
    },
  },
  mounted() {
    // Load doctors list first (show all), then try to enrich with nearby distances
    this.reloadDoctors();
    this.fetchAppointmentHistory();
    this.refreshRealtimeLocation();
    this.startLocationWatcher();
  },
  beforeUnmount() {
    if (this.locationWatchId !== null && navigator.geolocation) {
      navigator.geolocation.clearWatch(this.locationWatchId);
      this.locationWatchId = null;
    }
  },
  methods: {
    getAuthHeaders() {
      return { Authorization: `Bearer ${Cookies.get('token')}` };
    },

    async ensureActiveChildSession() {
      try {
        const res = await axios.get('/api/v1/child/list', { headers: this.getAuthHeaders() });
        const children = Array.isArray(res.data) ? res.data : [];
        if (!children.length) return false;
        const active = children.find(c => c.activeSession);
        if (active?.id) return true;
        await axios.post(`/api/v1/child/toggle_active_session/${children[0].id}`, {}, { headers: this.getAuthHeaders() });
        return true;
      } catch {
        return false;
      }
    },

    doctorImageSrc(doctor) {
      const path = doctor?.image || '';
      // Prefer the image URL stored on the backend if it points to our local doctor images.
      if (path && (path.startsWith('/doctors/') || path.startsWith('http://localhost:8080/doctors/'))) {
        return path.startsWith('/doctors/') ? `http://localhost:8080${path}` : path;
      }

      // Otherwise, fall back to the local placeholder pools (based on gender)
      const gRaw = doctor?.gender || '';
      const g = gRaw.toString().toLowerCase();
      const seed = (doctor?.id || doctor?.name || Math.random().toString()).toString();
      const idx = this.hashToIndex(seed);
      if (g.includes('female')) {
        const file = this.femalePlaceholders[idx % this.femalePlaceholders.length];
        return `http://localhost:8080${file}`;
      }
      if (g.includes('male')) {
        const file = this.malePlaceholders[idx % this.malePlaceholders.length];
        return `http://localhost:8080${file}`;
      }
      const file = this.femalePlaceholders[idx % this.femalePlaceholders.length];
      return `http://localhost:8080${file}`;
    },

    hashToIndex(s) {
      let h = 0;
      for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0;
      return h;
    },
    onDoctorImageError(event) {
      if (!event?.target) return;
      event.target.onerror = null;
      // Fall back to a known existing placeholder image (backend host)
      event.target.src = 'http://localhost:8080/doctors/female/1.jpeg';
    },

    ratingText(value) {
      const num = Number(value || 0);
      return num ? num.toFixed(1) : 'N/A';
    },
    distanceText(value) {
      if (value === null || value === undefined || Number.isNaN(Number(value))) return 'Distance unavailable';
      return `${Number(value).toFixed(1)} km`;
    },
    formatDate(value) {
      if (!value) return '-';
      const d = new Date(value);
      return Number.isNaN(d.getTime()) ? value : d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    },
    formatDay(value) {
      if (!value) return '-';
      return new Date(value).getDate();
    },
    formatMonth(value) {
      if (!value) return '';
      return new Date(value).toLocaleString('en-US', { month: 'short' });
    },
    formatTime(value) {
      if (!value) return '';
      return new Date(value).toLocaleString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
    },
    statusClass(status) {
      const map = { BOOKED: 'pill-booked', VISITED: 'pill-visited', CANCELLED: 'pill-cancelled' };
      return map[status] || '';
    },
    paymentClass(status) {
      const map = { PAID: 'pill-paid', PENDING: 'pill-pending' };
      return map[status] || 'pill-pending';
    },
    hasRated(appointmentId) {
      return this.ratedAppointments.has(appointmentId);
    },

    haversine(lat1, lon1, lat2, lon2) {
      const R = 6371;
      const toRad = deg => (deg * Math.PI) / 180;
      const dLat = toRad(lat2 - lat1);
      const dLon = toRad(lon2 - lon1);
      const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
      return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    },
    toNumber(v) {
      const n = Number(v);
      return Number.isFinite(n) ? n : null;
    },

    resetFilters() {
      this.minRating = 0;
      this.maxDistanceKm = 9999;
      this.specialityQuery = '';
      this.nameQuery = '';
      this.selectedSorting = 'nearby';
      this.reloadDoctors();
    },

    async reloadDoctors() {
      // Always load all doctors first so the UI shows entries immediately.
      await this.fetchDoctorsByRatings();
      // If nearby is selected, attempt to enrich ordering by fetching nearby results in background
      if (this.selectedSorting === 'nearby') {
        try {
          await this.fetchDoctorsNearbyRealtime();
        } catch (e) {
          // ignore, we already loaded full list
        }
      }
      this.applyFilters();
    },

    async refreshRealtimeLocation() {
      if (!navigator.geolocation) {
        this.locationStatus = 'Realtime location is not supported in this browser.';
        return;
      }
      this.locationStatus = 'Detecting your location...';
      navigator.geolocation.getCurrentPosition(
        async (pos) => {
          this.userLocation = { lat: pos.coords.latitude, lon: pos.coords.longitude };
          this.locationStatus = `📍 Location active (${this.userLocation.lat.toFixed(4)}, ${this.userLocation.lon.toFixed(4)})`;
          await this.fetchDoctorsNearbyRealtime();
          this.applyFilters();
        },
        () => {
          this.locationStatus = '⚠️ Could not get location. Using IP-based fallback.';
          this.reloadDoctors();
        },
        { enableHighAccuracy: true, timeout: 9000 }
      );
    },

    startLocationWatcher() {
      if (!navigator.geolocation) return;
      if (this.locationWatchId !== null) return;
      this.locationWatchId = navigator.geolocation.watchPosition(
        (pos) => {
          this.userLocation = { lat: pos.coords.latitude, lon: pos.coords.longitude };
          this.locationStatus = `📍 Live location active (${this.userLocation.lat.toFixed(4)}, ${this.userLocation.lon.toFixed(4)})`;
          this.applyFilters();
        },
        () => {
          this.locationStatus = '⚠️ Live location updates unavailable.';
        },
        { enableHighAccuracy: true, maximumAge: 15000, timeout: 12000 }
      );
    },

    async fetchDoctorsNearbyRealtime() {
      if (!this.userLocation) return;
      try {
        const res = await axios.get('/api/v1/doctor/list/nearby/realtime', {
          params: { lat: this.userLocation.lat, lon: this.userLocation.lon },
          headers: this.getAuthHeaders(),
        });
        this.doctors = Array.isArray(res.data) ? res.data : [];
      } catch {
        this.locationStatus = 'Realtime query failed. Using IP fallback.';
        await this.fetchDoctorsNearby();
      }
    },

    async getUserIpAddress() {
      try {
        const res = await axios.get('https://api.ipify.org?format=json');
        return res.data.ip;
      } catch {
        return '0.0.0.0';
      }
    },

    async fetchDoctorsNearby() {
      const ip = await this.getUserIpAddress();
      try {
        const res = await axios.get('/api/v1/doctor/list/nearby', {
          headers: { ...this.getAuthHeaders(), 'X-IP-Address': ip },
        });
        this.doctors = Array.isArray(res.data) ? res.data : [];
      } catch {
        this.doctors = [];
      }
    },

    async fetchDoctorsByRatings() {
      try {
        const res = await axios.get('/api/v1/doctor/list/ratings', { headers: this.getAuthHeaders() });
        this.doctors = Array.isArray(res.data) ? res.data : [];
      } catch {
        this.doctors = [];
      }
    },

    applyFilters() {
      const speciality = this.specialityQuery.trim().toLowerCase();
      const nameQ = this.nameQuery.trim().toLowerCase();
      const computed = this.doctors.map(d => {
        const c = { ...d };
        if ((c.distanceKm == null) && this.userLocation) {
          const lat = this.toNumber(c.latitude);
          const lon = this.toNumber(c.longitude);
          if (lat !== null && lon !== null) c.distanceKm = this.haversine(this.userLocation.lat, this.userLocation.lon, lat, lon);
        }
        return c;
      });
      this.filteredDoctors = computed.filter(d => {
        const rating = Number(d.ratings || 0);
        const dist = this.toNumber(d.distanceKm);
        const spec = (d.specialities || '').toLowerCase();
        const name = (d.name || '').toLowerCase();
        const withinDistance = this.maxDistanceKm >= 9999
          ? true
          : (dist !== null && dist <= this.maxDistanceKm);
        return rating >= this.minRating
          && withinDistance
          && (!speciality || spec.includes(speciality))
          && (!nameQ || name.includes(nameQ));
      });
      if (this.selectedSorting === 'highest-rated') {
        this.filteredDoctors.sort((a, b) => Number(b.ratings || 0) - Number(a.ratings || 0));
      } else {
        this.filteredDoctors.sort((a, b) => {
          const da = this.toNumber(a.distanceKm), db = this.toNumber(b.distanceKm);
          if (da === null && db === null) return 0;
          if (da === null) return 1;
          if (db === null) return -1;
          return da - db;
        });
      }
    },

    // ---- Booking ----
    openBooking(doctor) {
      this.selectedDoctor = doctor;
      this.booking = { appointmentAt: '', reason: '', paymentMethod: 'CASH', paymentAmount: 500 };
      this.bookingMessage = '';
      this.bookingSuccess = false;
      this.showBookingModal = true;
    },
    closeBooking() {
      this.showBookingModal = false;
      this.selectedDoctor = null;
    },
    async submitBooking() {
      if (!this.selectedDoctor) return;
      if (!this.booking.appointmentAt) {
        this.bookingMessage = 'Please select a date and time';
        this.bookingSuccess = false;
        return;
      }
      try {
        await this.ensureActiveChildSession();
        await axios.post('/api/v1/doctor/appointments/book', {
          doctorId: this.selectedDoctor.id,
          appointmentAt: this.booking.appointmentAt,
          reason: this.booking.reason,
          paymentMethod: this.booking.paymentMethod,
          paymentAmount: this.booking.paymentAmount,
        }, { headers: this.getAuthHeaders() });
        this.bookingMessage = '🎉 Appointment booked successfully!';
        this.bookingSuccess = true;
        this.fetchAppointmentHistory();
        setTimeout(() => this.closeBooking(), 1500);
      } catch (error) {
        const serverMessage = error?.response?.data?.message || error?.response?.data;
        this.bookingMessage = serverMessage
          ? `Failed to book appointment: ${serverMessage}`
          : 'Failed to book appointment. Please try again.';
        this.bookingSuccess = false;
      }
    },

    // ---- Appointments ----
    async fetchAppointmentHistory() {
      try {
        await this.ensureActiveChildSession();
        const res = await axios.get('/api/v1/doctor/appointments/history', { headers: this.getAuthHeaders() });
        this.appointmentHistory = Array.isArray(res.data) ? res.data : [];
        await this.fetchMyRatings();
      } catch {
        this.appointmentHistory = [];
      }
    },

    async fetchMyRatings() {
      try {
        const res = await axios.get('/api/v1/doctor/appointments/ratings/me', { headers: this.getAuthHeaders() });
        const items = Array.isArray(res.data) ? res.data : [];
        this.ratedAppointments = new Set(items.map(r => r.appointmentId).filter(Boolean));
      } catch {
        this.ratedAppointments = new Set();
      }
    },

    // ---- Visit Notes ----
    openVisitNotes(item) {
      this.visitNotesTarget = item;
      this.visitNotesText = '';
      this.showVisitNotesModal = true;
    },
    closeVisitNotes() {
      this.showVisitNotesModal = false;
      this.visitNotesTarget = null;
    },
    async confirmVisited() {
      if (!this.visitNotesTarget) return;
      try {
        await axios.patch(`/api/v1/doctor/appointments/${this.visitNotesTarget.id}/visit`,
          { visitNotes: this.visitNotesText },
          { headers: this.getAuthHeaders() }
        );
        this.fetchAppointmentHistory();
        this.closeVisitNotes();
      } catch { /* silent */ }
    },

    async cancelAppointment(id) {
      try {
        await axios.patch(`/api/v1/doctor/appointments/${id}/cancel`, {}, { headers: this.getAuthHeaders() });
        this.fetchAppointmentHistory();
      } catch { /* silent */ }
    },

    // ---- Cancel Confirmation ----
    openCancelConfirm(item) {
      this.cancelTarget = item;
      this.showCancelConfirm = true;
    },
    closeCancelConfirm() {
      this.showCancelConfirm = false;
      this.cancelTarget = null;
    },
    async confirmCancel() {
      if (!this.cancelTarget) return;
      await this.cancelAppointment(this.cancelTarget.id);
      this.closeCancelConfirm();
    },

    // ---- Reschedule ----
    openReschedule(item) {
      this.rescheduleTarget = item;
      this.rescheduleDate = '';
      this.rescheduleReason = '';
      this.rescheduleMessage = '';
      this.rescheduleSuccess = false;
      this.showRescheduleModal = true;
    },
    closeReschedule() {
      this.showRescheduleModal = false;
      this.rescheduleTarget = null;
    },
    async submitReschedule() {
      if (!this.rescheduleTarget || !this.rescheduleDate) return;
      try {
        await axios.patch(`/api/v1/doctor/appointments/${this.rescheduleTarget.id}/reschedule`, {
          appointmentAt: this.rescheduleDate,
          reason: this.rescheduleReason || this.rescheduleTarget.reason,
        }, { headers: this.getAuthHeaders() });
        this.rescheduleMessage = '✅ Appointment rescheduled!';
        this.rescheduleSuccess = true;
        this.fetchAppointmentHistory();
        setTimeout(() => this.closeReschedule(), 1500);
      } catch {
        this.rescheduleMessage = 'Failed to reschedule appointment.';
        this.rescheduleSuccess = false;
      }
    },

    // ---- Rating ----
    openRating(item) {
      this.ratingTarget = item;
      this.ratingStars = 0;
      this.ratingReview = '';
      this.ratingMessage = '';
      this.ratingSuccess = false;
      this.showRatingModal = true;
    },
    closeRating() {
      this.showRatingModal = false;
      this.ratingTarget = null;
    },
    async submitRating() {
      if (!this.ratingTarget || this.ratingStars === 0) return;
      try {
        await axios.post('/api/v1/doctor/appointments/rate', {
          doctorId: this.ratingTarget.doctorId,
          appointmentId: this.ratingTarget.id,
          stars: this.ratingStars,
          reviewText: this.ratingReview,
        }, { headers: this.getAuthHeaders() });
        this.ratingMessage = '🎉 Thank you for your review!';
        this.ratingSuccess = true;
        this.ratedAppointments.add(this.ratingTarget.id);
        this.reloadDoctors();
        setTimeout(() => this.closeRating(), 1500);
      } catch {
        this.ratingMessage = 'Failed to submit rating.';
        this.ratingSuccess = false;
      }
    },

    // ---- Payment ----
    openPayment(item) {
      this.paymentTarget = item;
      this.paymentMethodUpdate = item.paymentMethod || 'CASH';
      this.showPaymentModal = true;
    },
    closePayment() {
      this.showPaymentModal = false;
      this.paymentTarget = null;
    },
    async confirmPayment() {
      if (!this.paymentTarget) return;
      try {
        await axios.patch(
          `/api/v1/doctor/appointments/${this.paymentTarget.id}/payment`,
          {},
          {
            params: { status: 'PAID', method: this.paymentMethodUpdate || this.paymentTarget.paymentMethod || 'CASH' },
            headers: this.getAuthHeaders(),
          }
        );
        this.fetchAppointmentHistory();
        this.closePayment();
      } catch { /* silent */ }
    },

    // ---- Reviews ----
    async openReviews(doctor) {
      this.reviewsDoctor = doctor;
      this.reviewsList = [];
      this.showReviewsModal = true;
      try {
        const res = await axios.get(`/api/v1/doctor/appointments/ratings/${doctor.id}`, { headers: this.getAuthHeaders() });
        this.reviewsList = Array.isArray(res.data) ? res.data : [];
      } catch {
        this.reviewsList = [];
      }
    },
    closeReviews() {
      this.showReviewsModal = false;
      this.reviewsDoctor = null;
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;500;600;700;800&display=swap');

.doctor-shell {
  position: relative;
  width: 100%;
  min-height: 100vh;
  padding: 24px;
  font-family: 'Syne', sans-serif;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 50%, #f3e8ff 100%);
  overflow: hidden;
}

/* ===== Floating Decorations ===== */
.float-deco {
  position: absolute;
  font-size: 28px;
  opacity: 0.15;
  animation: floatDeco 6s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}
.d1 { top: 5%; left: 3%; animation-delay: 0s; }
.d2 { top: 12%; right: 5%; animation-delay: 1.2s; }
.d3 { bottom: 25%; left: 6%; animation-delay: 2.4s; }
.d4 { bottom: 10%; right: 8%; animation-delay: 3.6s; }
.d5 { top: 50%; right: 2%; animation-delay: 4.8s; }

@keyframes floatDeco {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-18px) rotate(12deg); }
}

/* ===== Hero Header ===== */
.hero-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.hero-mascot {
  font-size: 56px;
  animation: heroWave 2s ease-in-out infinite;
}
@keyframes heroWave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-8deg); }
  75% { transform: rotate(8deg); }
}
.hero-text h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-sub {
  margin: 4px 0 0;
  color: #7c3aed;
  font-size: 0.95rem;
  font-weight: 500;
}
.speech-bubble {
  position: relative;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #c4b5fd;
  border-radius: 20px;
  padding: 10px 18px;
  font-size: 0.88rem;
  font-weight: 600;
  color: #5b21b6;
  margin-left: auto;
  max-width: 300px;
}
.speech-bubble::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
  border: 6px solid transparent;
  border-right-color: #c4b5fd;
}

/* ===== Tab Navigation ===== */
.tab-nav {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: 2px solid #c4b5fd;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.7);
  font-family: 'Syne', sans-serif;
  font-weight: 600;
  font-size: 0.9rem;
  color: #7c3aed;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.tab-btn:hover {
  background: rgba(167, 139, 250, 0.15);
  transform: translateY(-2px);
}
.tab-btn.active {
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 16px rgba(108, 99, 255, 0.3);
}
.tab-icon { font-size: 1.1rem; }
.tab-content { position: relative; z-index: 1; }
.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 11px;
  background: #fff;
  color: #7c3aed;
  font-size: 0.75rem;
  font-weight: 800;
  line-height: 1;
}
.tab-btn.active .tab-badge { background: rgba(255,255,255,0.3); color: #fff; }
.tab-badge.pulse { animation: badgePulse 2s ease-in-out infinite; }
@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

/* ===== Controls Bar ===== */
.controls-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: flex-end;
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(196, 181, 253, 0.3);
  margin-bottom: 20px;
}
.control-group { display: flex; flex-direction: column; gap: 4px; }
.control-group.grow { flex: 1; min-width: 180px; }
.control-group label {
  font-size: 0.75rem;
  font-weight: 700;
  color: #7c3aed;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.ctrl-select, .ctrl-input {
  height: 42px;
  border: 2px solid #ddd6fe;
  border-radius: 14px;
  padding: 0 14px;
  font-family: 'Syne', sans-serif;
  font-size: 0.88rem;
  font-weight: 500;
  color: #374151;
  background: #fff;
  outline: none;
  transition: border-color 0.2s;
}
.ctrl-select:focus, .ctrl-input:focus { border-color: #8b5cf6; }
.ctrl-btn {
  height: 42px;
  padding: 0 18px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}
.ctrl-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(108, 99, 255, 0.3); }

.loc-status {
  margin: -10px 0 16px;
  font-size: 0.85rem;
  color: #7c3aed;
  font-weight: 500;
}

/* ===== Doctor Cards Grid ===== */
.doctor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
}
.doc-card {
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(196, 181, 253, 0.25);
  border-radius: 20px;
  padding: 20px;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.doc-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(108, 99, 255, 0.15);
  border-color: #a78bfa;
}
.doc-card-top { display: flex; gap: 14px; align-items: flex-start; }
.doc-avatar-wrap { position: relative; flex-shrink: 0; }
.doc-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #ddd6fe;
}
.doc-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  font-size: 18px;
}
.doc-info { flex: 1; min-width: 0; }
.doc-name {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e1b4b;
}
.doc-addr {
  margin: 3px 0 0;
  font-size: 0.82rem;
  color: #6d28d9;
  font-weight: 500;
}
.doc-meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
  flex-wrap: wrap;
}
.star-display { display: flex; align-items: center; gap: 1px; }
.star { font-size: 0.9rem; color: #d4d4d8; }
.star.filled { color: #f59e0b; }
.star.sm { font-size: 0.75rem; }
.rating-num { font-size: 0.82rem; font-weight: 700; color: #92400e; margin-left: 4px; }
.distance-pill {
  font-size: 0.75rem;
  font-weight: 600;
  color: #6d28d9;
  background: #ede9fe;
  padding: 2px 10px;
  border-radius: 10px;
}
.doc-desc {
  margin: 12px 0 10px;
  font-size: 0.88rem;
  color: #4b5563;
  line-height: 1.5;
}
.doc-details { display: flex; flex-direction: column; gap: 5px; margin-bottom: 14px; }
.detail-chip {
  font-size: 0.82rem;
  color: #374151;
  background: #f5f3ff;
  padding: 6px 12px;
  border-radius: 10px;
}
.chip-label { font-weight: 700; margin-right: 6px; }
.doc-actions { display: flex; gap: 8px; }
.btn-book {
  flex: 1;
  height: 42px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.88rem;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-book:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(108, 99, 255, 0.35); }
.btn-reviews {
  height: 42px;
  padding: 0 18px;
  border: 2px solid #ddd6fe;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.8);
  color: #7c3aed;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-reviews:hover { border-color: #a78bfa; background: #f5f3ff; }

/* ===== Empty State ===== */
.empty-state {
  text-align: center;
  padding: 48px 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  border-radius: 24px;
  border: 2px dashed #c4b5fd;
}
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-state p { color: #6b7280; font-weight: 500; margin: 0 0 16px; }
.btn-action {
  padding: 10px 24px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  cursor: pointer;
}

/* ===== Appointments ===== */
.appt-group { margin-bottom: 24px; }
.group-title {
  margin: 0 0 14px;
  font-size: 1.1rem;
  font-weight: 700;
  color: #5b21b6;
}
.appt-card {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 16px;
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  border-radius: 18px;
  border: 1px solid rgba(196, 181, 253, 0.25);
  transition: all 0.3s;
}
.appt-card:hover { box-shadow: 0 8px 24px rgba(108, 99, 255, 0.1); }
.appt-card.past { opacity: 0.85; }
.appt-left { flex-shrink: 0; }
.appt-date-badge {
  width: 56px;
  height: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  border-radius: 14px;
  color: #fff;
}
.appt-date-badge.muted { background: linear-gradient(135deg, #9ca3af, #6b7280); }
.appt-day { font-size: 1.4rem; font-weight: 800; line-height: 1; }
.appt-month { font-size: 0.7rem; font-weight: 600; text-transform: uppercase; }
.appt-center { flex: 1; min-width: 0; }
.appt-center h4 { margin: 0 0 4px; font-size: 1rem; font-weight: 700; color: #1e1b4b; }
.appt-time, .appt-reason { margin: 2px 0; font-size: 0.85rem; color: #6b7280; }
.appt-pills { display: flex; gap: 6px; margin-top: 8px; flex-wrap: wrap; }
.status-pill, .payment-pill {
  font-size: 0.72rem;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 10px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.status-pill.sm, .payment-pill.sm { font-size: 0.68rem; padding: 2px 8px; }
.pill-booked { background: #dbeafe; color: #1e40af; }
.pill-visited { background: #d1fae5; color: #065f46; }
.pill-cancelled { background: #fee2e2; color: #991b1b; }
.pill-paid { background: #d1fae5; color: #065f46; }
.pill-pending { background: #fef3c7; color: #92400e; }

.appt-right { display: flex; flex-direction: column; gap: 6px; }
.btn-sm {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: 10px;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.78rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}
.btn-visit { background: #d1fae5; color: #065f46; }
.btn-visit:hover { background: #a7f3d0; }
.btn-cancel { background: #fee2e2; color: #991b1b; }
.btn-cancel:hover { background: #fecaca; }
.btn-pay { background: #dbeafe; color: #1e40af; }
.btn-pay:hover { background: #bfdbfe; }
.btn-rate { background: #fef3c7; color: #92400e; }
.btn-rate:hover { background: #fde68a; }
.btn-reschedule { background: #e0e7ff; color: #3730a3; }
.btn-reschedule:hover { background: #c7d2fe; }
.rated-badge { font-size: 0.82rem; font-weight: 700; color: #059669; }

.visit-notes-box {
  margin-top: 8px;
  padding: 10px;
  background: #f5f3ff;
  border-radius: 10px;
  border-left: 3px solid #8b5cf6;
  font-size: 0.85rem;
  color: #374151;
}
.visit-notes-box strong { color: #5b21b6; }
.visit-notes-box p { margin: 4px 0 0; }

/* ===== Patient History Timeline ===== */
.history-timeline { position: relative; }
.timeline { position: relative; padding-left: 28px; }
.timeline-item { position: relative; margin-bottom: 16px; }
.timeline-dot {
  position: absolute;
  left: -28px;
  top: 16px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 3px solid #c4b5fd;
  background: #fff;
  z-index: 1;
}
.timeline-dot.pill-visited { border-color: #059669; background: #d1fae5; }
.timeline-dot.pill-cancelled { border-color: #dc2626; background: #fee2e2; }
.timeline-dot.pill-booked { border-color: #3b82f6; background: #dbeafe; }
.timeline-line {
  position: absolute;
  left: -22px;
  top: 30px;
  width: 2px;
  height: calc(100% + 2px);
  background: #ddd6fe;
}
.timeline-card {
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(8px);
  border-radius: 16px;
  border: 1px solid rgba(196, 181, 253, 0.25);
  padding: 14px 18px;
}
.tl-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.tl-header strong { color: #1e1b4b; font-size: 0.95rem; }
.tl-date { font-size: 0.8rem; color: #9ca3af; font-weight: 500; }
.tl-body p { margin: 4px 0; font-size: 0.85rem; color: #4b5563; }
.tl-label { font-weight: 700; color: #7c3aed; margin-right: 4px; }

.history-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  margin-top: 24px;
}
.stat-card {
  text-align: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  border-radius: 18px;
  border: 1px solid rgba(196, 181, 253, 0.25);
}
.stat-num {
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.stat-label { font-size: 0.82rem; font-weight: 600; color: #7c3aed; margin-top: 4px; }

/* ===== Modals ===== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal-card {
  position: relative;
  width: 100%;
  max-width: 480px;
  max-height: 85vh;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(14px);
  border-radius: 24px;
  border: 1px solid rgba(196, 181, 253, 0.3);
  box-shadow: 0 20px 60px rgba(108, 99, 255, 0.2);
  padding: 0;
  animation: modalIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes modalIn {
  from { transform: scale(0.9) translateY(20px); opacity: 0; }
  to { transform: scale(1) translateY(0); opacity: 1; }
}
.modal-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: #f5f3ff;
  color: #7c3aed;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 2;
}
.modal-close:hover { background: #ede9fe; }
.modal-header {
  padding: 24px 24px 16px;
  text-align: center;
}
.modal-icon { font-size: 40px; margin-bottom: 8px; }
.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 800;
  color: #1e1b4b;
}
.modal-sub { margin: 4px 0 0; color: #7c3aed; font-size: 0.9rem; font-weight: 500; }
.modal-body { padding: 0 24px 16px; }
.modal-footer {
  padding: 16px 24px 24px;
  display: flex;
  gap: 10px;
}
.modal-msg {
  text-align: center;
  padding: 8px 24px 16px;
  font-size: 0.88rem;
  font-weight: 600;
  color: #dc2626;
}
.modal-msg.success { color: #059669; }

.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 0.82rem; font-weight: 700; color: #5b21b6; margin-bottom: 6px; }
.form-input {
  width: 100%;
  height: 44px;
  border: 2px solid #ddd6fe;
  border-radius: 14px;
  padding: 0 14px;
  font-family: 'Syne', sans-serif;
  font-size: 0.9rem;
  color: #374151;
  background: #fff;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}
.form-input:focus { border-color: #8b5cf6; }
.form-textarea {
  height: auto;
  padding: 10px 14px;
  resize: vertical;
}

.payment-options { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.pay-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border: 2px solid #ddd6fe;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.88rem;
  font-weight: 600;
  color: #374151;
}
.pay-option:hover { border-color: #a78bfa; }
.pay-option.selected { border-color: #7c3aed; background: #f5f3ff; color: #5b21b6; }
.pay-option input[type="radio"] { display: none; }
.pay-icon { font-size: 1.2rem; }

.btn-confirm {
  flex: 1;
  height: 46px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #6C63FF, #A855F7);
  color: #fff;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.92rem;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-confirm:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(108, 99, 255, 0.35); }
.btn-confirm:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
.btn-ghost {
  height: 46px;
  padding: 0 24px;
  border: 2px solid #ddd6fe;
  border-radius: 50px;
  background: #fff;
  color: #7c3aed;
  font-family: 'Syne', sans-serif;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-ghost:hover { border-color: #a78bfa; background: #f5f3ff; }

/* ===== Star Picker ===== */
.star-picker {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-bottom: 8px;
}
.star-pick {
  font-size: 2.2rem;
  color: #d4d4d8;
  cursor: pointer;
  transition: all 0.2s;
}
.star-pick:hover, .star-pick.active { color: #f59e0b; transform: scale(1.15); }
.star-label {
  text-align: center;
  font-size: 0.9rem;
  font-weight: 600;
  color: #92400e;
  margin-bottom: 14px;
}

/* ===== Reviews Modal ===== */
.reviews-modal { max-width: 520px; }
.reviews-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  justify-content: center;
}
.big-rating {
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.review-count { font-size: 0.85rem; color: #9ca3af; font-weight: 500; }
.reviews-list { max-height: 350px; overflow-y: auto; }
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}
.review-item:last-child { border-bottom: none; }
.review-stars { margin-bottom: 4px; }
.review-text { margin: 4px 0; font-size: 0.88rem; color: #374151; line-height: 1.5; }
.review-date { font-size: 0.78rem; color: #9ca3af; }
.empty-reviews { text-align: center; color: #9ca3af; padding: 24px; }

.payment-summary {
  background: #f5f3ff;
  border-radius: 14px;
  padding: 14px;
}
.pay-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 0.9rem;
  color: #374151;
}
.pay-row strong { color: #5b21b6; }

/* ===== Reschedule & Cancel Confirm ===== */
.current-schedule-info {
  background: #fef3c7;
  border-radius: 12px;
  padding: 10px 14px;
  margin-bottom: 14px;
  font-size: 0.88rem;
  color: #92400e;
}
.info-label { font-weight: 700; margin-right: 6px; }
.cancel-details {
  background: #fef2f2;
  border-radius: 14px;
  padding: 16px;
  border-left: 4px solid #ef4444;
}
.cancel-details p { margin: 6px 0; font-size: 0.9rem; color: #374151; }
.cancel-details strong { color: #991b1b; }
.cancel-confirm-btn { background: linear-gradient(135deg, #ef4444, #dc2626) !important; }
.cancel-confirm-btn:hover { box-shadow: 0 6px 20px rgba(239, 68, 68, 0.35) !important; }

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .doctor-shell { padding: 16px; }
  .hero-header { flex-direction: column; text-align: center; }
  .speech-bubble { margin-left: 0; margin-top: 8px; }
  .speech-bubble::before { display: none; }
  .doctor-grid { grid-template-columns: 1fr; }
  .appt-card { flex-direction: column; }
  .appt-right { flex-direction: row; flex-wrap: wrap; }
  .controls-bar { flex-direction: column; }
  .control-group.grow { min-width: unset; }
  .tab-btn { padding: 8px 14px; font-size: 0.82rem; }
  .payment-options { grid-template-columns: 1fr; }
}

@media (max-width: 480px) {
  .hero-text h1 { font-size: 1.5rem; }
  .hero-mascot { font-size: 42px; }
  .doc-card-top { flex-direction: column; align-items: center; text-align: center; }
  .doc-meta-row { justify-content: center; }
  .history-stats { grid-template-columns: repeat(2, 1fr); }
  .controls-bar { padding: 12px; gap: 10px; }
  .ctrl-btn { width: 100%; }
  .appt-right { width: 100%; }
  .btn-sm {
    white-space: normal;
    height: auto;
    min-height: 34px;
    padding: 6px 10px;
  }
}

@media (max-width: 375px) {
  .doctor-shell { padding: 10px 8px; }
  .hero-text h1 { font-size: 1.25rem; }
  .hero-subtitle { font-size: 0.82rem; }

  .tabs-wrap {
    gap: 6px;
    padding: 8px;
  }
  .tab-btn {
    flex: 1 1 100%;
    justify-content: center;
    padding: 8px 10px;
    font-size: 0.78rem;
  }

  .controls-bar { padding: 10px; }
  .ctrl-select,
  .ctrl-input,
  .ctrl-btn { height: 40px; font-size: 0.82rem; }

  .doc-card,
  .appt-card,
  .history-card,
  .payment-card,
  .booking-card { padding: 12px; border-radius: 14px; }

  .appt-center h4 { font-size: 0.9rem; }
  .appt-time,
  .appt-reason { font-size: 0.8rem; }
}
</style>
