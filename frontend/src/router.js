
import { createRouter, createWebHistory } from 'vue-router';

import Index from './components/Index.vue';
// import IndexV2 from './components/IndexV2.vue'; // V2 preserved (inactive)
import Authentication from './components/auth/Authentication.vue';
// import AuthenticationV2 from './components/auth/AuthenticationV2.vue'; // V2 preserved (inactive)
import OTP from './components/auth/OTP.vue';
import PasswordReset from './components/auth/PasswordReset.vue';

import AdminLogin from './components/admin/AdminLogin.vue';
// import AdminLoginV2 from './components/admin/AdminLoginV2.vue'; // V2 preserved (inactive)
import AdminDashboardLayout from './components/admin/AdminDashboardLayout.vue';
// import AdminDashboardLayoutV2 from './components/admin/AdminDashboardLayoutV2.vue'; // V2 preserved (inactive)
import AddDoctor from './components/admin/AddDoctor.vue';
// import AddDoctorV2 from './components/admin/AddDoctorV2.vue'; // V2 preserved (inactive)
import AllDoctors from './components/admin/AllDoctors.vue';
// import AllDoctorsV2 from './components/admin/AllDoctorsV2.vue'; // V2 preserved (inactive)

import DashboardLayout from './components/home/DashboardLayout.vue';
// import DashboardLayoutV2 from './components/home/DashboardLayoutV2.vue'; // V2 preserved (inactive)
import Home from './components/home/Home.vue';
// import HomeV2 from './components/home/HomeV2.vue'; // V2 preserved (inactive)
import QuestionnaireTesting from './components/home/tests/QuestionnaireTesting.vue';
// import QuestionnaireTestingV2 from './components/home/tests/QuestionnaireTestingV2.vue'; // V2 preserved (inactive)
import BehavioralVideoTesting from './components/home/tests/BehavioralVideoTesting.vue';
import GameHome from './components/home/games/GameHome.vue';
import VideoChatHome from './components/home/chat/VideoChatHome.vue';
import DoctorSuggestion from './components/home/doctors/DoctorSuggestion.vue';
// import DoctorSuggestionV2 from './components/home/doctors/DoctorSuggestionV2.vue'; // V2 preserved (inactive)
import AexTestResult from './components/home/tests/AexTestResult.vue';
// import AexTestResultV2 from './components/home/tests/AexTestResultV2.vue'; // V2 preserved (inactive)

import DrawingGame from './components/home/therapy/DrawingGame.vue';

import SocialSkillOptions from './components/home/therapy/SocialSkillOptions.vue';
import TherapyHome from './components/home/therapy/TherapyHome.vue';
import SocialSkillStoryView from './components/home/therapy/SocialSkillStoryView.vue';
import TapGame from './components/home/games/TapGame.vue';
import SnakeGame from './components/home/games/SnakeGame.vue';
import MemoryMatchGame from './components/home/games/MemoryMatchGame.vue';
import EmotionMatchGame from './components/home/games/EmotionMatchGame.vue';
import FlashcardGame from './components/home/games/FlashcardGame.vue';
import SudokuLiteGame from './components/home/games/SudokuLiteGame.vue';
import AlphabetFind from './components/home/therapy/funalphabet/AlphabetFind.vue';
import AlphabetLearn from './components/home/therapy/funalphabet/AlphabetLearn.vue';
import FunAlphabetMainMenu from './components/home/therapy/funalphabet/FunAlphabetMainMenu.vue';
import AddNewChildProfile from './components/home/AddNewChildProfile.vue';
// import AddNewChildProfileV2 from './components/home/AddNewChildProfileV2.vue'; // V2 preserved (inactive)
import BreathingExercise from './components/home/therapy/BreathingExercise.vue';
import SensoryBreakGuide from './components/home/therapy/SensoryBreakGuide.vue';
import SpeechTherapyCoach from './components/home/therapy/SpeechTherapyCoach.vue';
import RoutinePlanner from './components/home/therapy/RoutinePlanner.vue';
import ColorTherapy from './components/home/therapy/ColorTherapy.vue';
import MusicalRhythmGame from './components/home/games/MusicalRhythmGame.vue';
import ProgressTracker from './components/home/tests/ProgressTracker.vue';
import TalkingTom from './components/home/therapy/TalkingTom.vue';

const routes = [
  // Public Routes
  { path: '/', component: Index },
  { path: '/auth', component: Authentication },
  { path: '/auth/otp', component: OTP, props: true },
  { path: '/auth/password/reset', component: PasswordReset, props: true },

  // Normal User Dashboard Routes
  {
    path: '/dashboard',
    component: DashboardLayout, // Common layout for dashboard
    meta: { requiresAuth: true, role: 'user' },
    children: [
      { path: '', redirect: '/dashboard/home' }, // Default child route
      { path: 'home', component: Home },
      { path: 'newchprofile', component: AddNewChildProfile },

      { path: 'aex/q', component: QuestionnaireTesting },
      { path: 'aex/video', component: BehavioralVideoTesting, props: true },
      { path: 'aex/r', component: AexTestResult, props: true },
      { path: 'therapy', component: TherapyHome },
      { path: 'game', component: GameHome },
      { path: 'doctors', component: DoctorSuggestion },
      { path: 'chat', component: VideoChatHome },

      { path: 'game/tapgame', component: TapGame },
      { path: 'game/snake', component: SnakeGame },
      { path: 'game/memory-match', component: MemoryMatchGame },
      { path: 'game/emotion-match', component: EmotionMatchGame },
      { path: 'game/flashcard', component: FlashcardGame },
      { path: 'game/flashcards', component: FlashcardGame },
      { path: 'game/sudoku-lite', component: SudokuLiteGame },
      { path: 'game/musical-rhythm', component: MusicalRhythmGame },


      { path: 'therapy/socialskill', component: SocialSkillOptions },
      {
        path: 'therapy/story/:assetName',
        name: 'SocialSkillStoryView',
        component: SocialSkillStoryView,
        props: true,
      },
      { path: 'therapy/drawing', component: DrawingGame },
      { path: 'therapy/funalphabet/learn', component:  AlphabetLearn},
      { path: 'therapy/funalphabet/find', component: AlphabetFind },
      { path: 'therapy/funalp', component: FunAlphabetMainMenu },
      { path: 'therapy/breathing', component: BreathingExercise },
      { path: 'therapy/sensory-break', component: SensoryBreakGuide },
      { path: 'therapy/speech-coach', component: SpeechTherapyCoach },
      { path: 'therapy/routine-planner', component: RoutinePlanner },
      { path: 'therapy/color-therapy', component: ColorTherapy },
      { path: 'therapy/talking-tom', component: TalkingTom },

      { path: 'progress', component: ProgressTracker },
    ],
  },

  // Admin Routes
  { path: '/admin/login', component: AdminLogin },
  {
    path: '/admin',
    component: AdminDashboardLayout, // Common layout for admin dashboard
    meta: { requiresAuth: true, role: 'admin' }, // Protect with admin role
    children: [
      { path: '', redirect: '/admin/home' }, // Default child route
      { path: 'home', component: AllDoctors },
      { path: 'doctors', component: AllDoctors },
      { path: 'doctors/add', component: AddDoctor },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}

router.beforeEach((to, from, next) => {
  // Check if the route requires authentication
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const authRecord = to.matched.find(record => record.meta.requiresAuth);
    const requiredRole = authRecord.meta.role;

    if (requiredRole === 'user') {
      const userToken = getCookie('token');
      const adminToken = getCookie('admin_token');
      if (userToken) {
        next();
      } else if (adminToken) {
        // Admin logged in but accessing user area — redirect to dashboard home
        next({ path: '/admin/home' });
      } else {
        next({ path: '/auth', query: { redirect: to.fullPath } });
      }
    } else if (requiredRole === 'admin') {
      const adminToken = getCookie('admin_token');
      const userToken = getCookie('token');
      if (adminToken) {
        next();
      } else if (userToken) {
        // Regular user trying to access admin area — send to user dashboard
        next({ path: '/dashboard/home' });
      } else {
        next({ path: '/admin/login', query: { redirect: to.fullPath } });
      }
    } else { // If role is not defined, deny access
      next({ path: '/' }); // Redirect to home or an error page
    }
  } else {
    next(); // Route does not require authentication, proceed as normal
  }
});

export default router;
