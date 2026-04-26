package org.ww.wigglew.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.ww.wigglew.entity.auth.AccessRole;
import org.ww.wigglew.entity.auth.Child;
import org.ww.wigglew.entity.auth.PhoneNumberVerificationStatus;
import org.ww.wigglew.entity.auth.UserEntity;
import org.ww.wigglew.entity.aex.ASDExEntity;
import org.ww.wigglew.entity.doctor.DoctorAppointmentEntity;
import org.ww.wigglew.entity.doctor.DoctorEntity;
import org.ww.wigglew.entity.doctor.DoctorRatingEntity;
import org.ww.wigglew.repo.DoctorAppointmentRepository;
import org.ww.wigglew.repo.DoctorRatingRepository;
import org.ww.wigglew.repo.DoctorsRepository;
import org.ww.wigglew.repo.UserRepository;
import org.ww.wigglew.repo.aex.ASDExRepository;
import org.ww.wigglew.service.DoctorImageService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorsRepository doctorsRepository;
    private final DoctorImageService doctorImageService;
    private final DoctorAppointmentRepository appointmentRepository;
    private final DoctorRatingRepository ratingRepository;
    private final ASDExRepository asdExRepository;

    @Value("${app.seed.clean-start:false}")
    private boolean cleanStart;

    @Override
    public void run(String... args) {
        if (cleanStart) {
            wipeSeedCollections();
        }
        initializeDefaultUsers();
        initializeDemoDoctors();
        initializeDemoAppointmentsAndRatings();
        initializeDemoAssessmentHistory();
    }

    private void wipeSeedCollections() {
        logger.warn("Seed clean-start enabled. Clearing Mongo collections before seeding.");
        ratingRepository.deleteAll();
        appointmentRepository.deleteAll();
        asdExRepository.deleteAll();
        doctorsRepository.deleteAll();
        userRepository.deleteAll();
        logger.warn("Seed clean-start completed.");
    }

    // ───────────────── USERS ─────────────────

    private void initializeDefaultUsers() {
        upsertUser("+8801643806870", "admin", "Admin User", AccessRole.ADMIN, null);
        upsertUser("01643806870", "user", "Regular User", AccessRole.USER, null);

        // Demo parent with pre-created children
        List<Child> demoChildren = new ArrayList<>();
        demoChildren.add(Child.builder()
                .id("child-demo-1")
                .name("Aryan")
                .gender(1)
                .dob(LocalDate.of(2021, 3, 15))
                .activeSession(true)
                .build());
        demoChildren.add(Child.builder()
                .id("child-demo-2")
                .name("Anika")
                .gender(0)
                .dob(LocalDate.of(2019, 8, 22))
                .activeSession(false)
                .build());

        upsertUser("+8801234567890", "123456", "Demo Parent", AccessRole.USER, demoChildren);

        List<Child> demoChildren2 = new ArrayList<>();
        demoChildren2.add(Child.builder()
            .id("child-demo-3")
            .name("Nabila")
            .gender(0)
            .dob(LocalDate.of(2018, 1, 11))
            .activeSession(false)
            .build());
        demoChildren2.add(Child.builder()
            .id("child-demo-4")
            .name("Rayan")
            .gender(1)
            .dob(LocalDate.of(2020, 7, 9))
            .activeSession(false)
            .build());

        upsertUser("+8801987654321", "123456", "Demo Parent 2", AccessRole.USER, demoChildren2);
        upsertUser("+8801911223344", "123456", "Demo Parent 3", AccessRole.USER, new ArrayList<>());

        logger.info("Default users initialized successfully.");
    }

    private void upsertUser(String phone, String password, String fullName, AccessRole role, List<Child> children) {
        var existing = userRepository.findByPhone(phone);
        if (existing.isPresent()) {
            UserEntity user = existing.get();
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(role);
            user.setVerificationStatus(PhoneNumberVerificationStatus.VERIFIED);
            if (children != null && (user.getChildren() == null || user.getChildren().isEmpty())) {
                user.setChildren(children);
            }
            userRepository.save(user);
            logger.info("Updated {} user: {} / {}", role, phone, password);
        } else {
            UserEntity user = UserEntity.builder()
                    .phone(phone)
                    .password(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .role(role)
                    .verificationStatus(PhoneNumberVerificationStatus.VERIFIED)
                    .children(children != null ? children : new ArrayList<>())
                    .build();
            userRepository.save(user);
            logger.info("Created {} user: {} / {}", role, phone, password);
        }
    }

    // ───────────────── DOCTORS ─────────────────

    private void initializeDemoDoctors() {
        List<DoctorEntity> doctors = List.of(
            createDoctor("Dr. Nusrat Jahan", "+8801711111111", "Female",
                "Pediatric neurologist specializing in autism spectrum disorder and developmental delays. 12+ years of experience with children ages 0-15.",
                "12 years", "Autism Specialist, Pediatric Neurology", 4.8,
                "Sun-Thu 9:00 AM - 5:00 PM", null, getRandomImageForGender("Female"),
                "23.7808", "90.4126", "House 12, Road 5, Dhanmondi, Dhaka"),

                createDoctor("Dr. Tanim Rahman", "+8801722222222", "Male",
                "Child psychiatrist with expertise in behavioral therapy and ASD management. Known for patient-centered approach.",
                "8 years", "Child Psychiatry, Behavioral Therapy", 4.6,
                    "Sat-Wed 10:00 AM - 6:00 PM", null, getRandomImageForGender("Male"),
                "23.7461", "90.3742", "Mirpur DOHS, Dhaka"),

                createDoctor("Dr. Marium Akter", "+8801733333333", "Female",
                "Developmental pediatrician focused on early intervention for ASD. Specializes in speech and occupational therapy referrals.",
                "15 years", "Developmental Pediatrics, Early Intervention", 4.9,
                    "Sun-Thu 8:00 AM - 3:00 PM", null, getRandomImageForGender("Female"),
                "23.8103", "90.4125", "Uttara Sector 4, Dhaka"),

                createDoctor("Dr. Rafiq Hasan", "+8801744444444", "Male",
                "Clinical psychologist specializing in applied behavior analysis (ABA) therapy for children with ASD.",
                "10 years", "Clinical Psychology, ABA Therapy", 4.5,
                    "Sat-Thu 11:00 AM - 7:00 PM", null, getRandomImageForGender("Male"),
                "23.7509", "90.3937", "Green Road, Dhaka"),

                createDoctor("Dr. Sadia Islam", "+8801755555555", "Female",
                "Pediatric occupational therapist helping children with sensory processing disorders and fine motor skill development.",
                "7 years", "Occupational Therapy, Sensory Integration", 4.7,
                    "Sun-Wed 9:00 AM - 4:00 PM", null, getRandomImageForGender("Female"),
                "22.3569", "91.7832", "Agrabad, Chattogram"),

                createDoctor("Dr. Farhana Karim", "+8801766666666", "Female",
                "Speech-language pathologist focusing on articulation, expressive language, and ASD communication challenges.",
                "11 years", "Speech Therapy, Communication Disorders", 4.8,
                    "Sun-Thu 9:30 AM - 5:30 PM", null, getRandomImageForGender("Female"),
                "23.7332", "90.3994", "Shantinagar, Dhaka"),

                createDoctor("Dr. Mahfuz Alom", "+8801777777777", "Male",
                "Child development specialist with experience in parent coaching and school-based intervention planning.",
                "9 years", "Child Development, Parent Coaching", 4.4,
                    "Sat-Thu 10:00 AM - 6:00 PM", null, getRandomImageForGender("Male"),
                "24.3636", "88.6241", "Rajshahi Medical Area, Rajshahi"),

            createDoctor("Dr. Iffat Ara", "+8801788888888", "Female",
                "Pediatric neurologist in Sylhet providing ASD screening and neuro-development follow-up care.",
                "13 years", "Pediatric Neurology, ASD Screening", 4.7,
                "Sun-Thu 8:30 AM - 2:30 PM", null, getRandomImageForGender("Female"),
                "24.8949", "91.8687", "Zindabazar, Sylhet"),

            createDoctor("Dr. Rezaul Kabir", "+8801799999999", "Male",
                "Behavior analyst specializing in social skills groups, classroom adaptation, and positive reinforcement routines.",
                "6 years", "Behavior Analysis, Social Skills", 4.3,
                "Sun-Wed 1:00 PM - 8:00 PM", null, getRandomImageForGender("Male"),
                "22.8456", "89.5403", "Sonadanga, Khulna"),

            createDoctor("Dr. Tamanna Sultana", "+8801810101010", "Female",
                "Multidisciplinary pediatric consultant with focus on autism, ADHD overlap, and school-readiness plans.",
                "14 years", "Autism, ADHD, School Readiness", 4.9,
                "Sun-Thu 9:00 AM - 4:00 PM", null, getRandomImageForGender("Female"),
                "23.7718", "90.4256", "Banani, Dhaka"),

            createDoctor("Dr. Shifat Sultana", "+8801811111111", "Female",
                "Child chest and breathing care specialist with supportive communication for parents.",
                "3+ years", "Pediatric Respiratory Care", 4.6,
                "Sat-Thu 11:00 AM - 7:00 PM", "https://www.doctorbangladesh.com/dr-shifat-sultana/", getRandomImageForGender("Female"),
                "23.4607", "91.1809", "Kandirpar, Cumilla"),

            createDoctor("Dr. Mohammad Ashraf-Us-Zaman Mahmud", "+8801812121212", "Male",
                "Oncology specialist with family counselling for long-term pediatric care planning.",
                "15+ years", "Clinical Oncology, Family Counselling", 4.8,
                "Thu-Fri 5:00 PM - 10:00 PM", "https://www.doctorbangladesh.com/dr-mohammad-ashraf-us-zaman-mahmud/", getRandomImageForGender("Male"),
                "23.1707", "89.2137", "Jessore Sadar, Jessore"),

            createDoctor("Major (Dr.) Jonayed Ahmed Talukder", "+8801813131313", "Male",
                "ENT specialist for child speech-hearing pathway and developmental communication support.",
                "12+ years", "ENT, Hearing-Speech Support", 4.4,
                "Sat-Thu 4:00 PM - 8:00 PM", "https://www.doctorbangladesh.com/major-dr-jonayed-ahmed-talukder/", getRandomImageForGender("Male"),
                "25.7439", "89.2752", "Jail Road, Rangpur"),

            createDoctor("Dr. Abida Hossain", "+8801814141414", "Female",
                "Pediatric-friendly surgical consultant with strong parent communication and care guidance.",
                "10+ years", "Pediatric Surgery Support", 4.7,
                "Sun-Tue-Thu 6:00 PM - 9:00 PM", "https://www.doctorbangladesh.com/dr-abida_hossain/", getRandomImageForGender("Female"),
                "22.3569", "91.7832", "Panchlaish, Chattogram"),

            createDoctor("Dr. Khaleda Akter Lucky", "+8801815151515", "Female",
                "Women and family care specialist supporting maternal-child wellbeing.",
                "14+ years", "Gynecology, Family Care", 4.5,
                "Tue-Fri 3:30 PM - 8:00 PM", "https://www.doctorbangladesh.com/dr-khaleda-akter-lucky/", getRandomImageForGender("Female"),
                "23.4607", "91.1809", "Laksam Road, Cumilla"),

            createDoctor("Dr. Luies Sourav Sarker", "+8801816161616", "Male",
                "Medicine and diabetic care specialist for family follow-up and nutrition routines.",
                "8+ years", "Medicine, Diabetology", 4.3,
                "Sat-Thu 5:00 PM - 9:00 PM", "https://www.doctorbangladesh.com/dr-luies-sourav-sarker/", getRandomImageForGender("Male"),
                "22.8456", "89.5403", "Khanjahan Ali Road, Khulna"),

            createDoctor("Dr. Balaram Paul", "+8801817171717", "Male",
                "General and laparoscopic surgeon supporting referral-based pediatric pathways.",
                "14+ years", "General Surgery", 4.6,
                "Sat-Thu 4:00 PM - 7:00 PM", "https://www.doctorbangladesh.com/dr-balaram-paul/", getRandomImageForGender("Male"),
                "22.3586", "91.8320", "OR Nizam Road, Chattogram"),

            createDoctor("Dr. Saymun Jesmin", "+8801818181818", "Female",
                "Skin and allergy specialist for sensory-related irritation and child skin management.",
                "17+ years", "Dermatology, Allergy Care", 4.8,
                "Wed-Fri 9:00 AM - 12:00 PM", "https://www.doctorbangladesh.com/dr-saymun-jesmin/", getRandomImageForGender("Female"),
                "23.7386", "90.3733", "Zigatola, Dhaka"),

            createDoctor("Dr. Mohammad Khaza Ahamed (Russel)", "+8801819191919", "Male",
                "Orthopedic consultant for developmental mobility planning and rehab referrals.",
                "20+ years", "Orthopedics, Rehabilitation Referral", 4.4,
                "Tue-Fri 3:00 PM - 5:00 PM", "https://www.doctorbangladesh.com/dr-mohammad-khaza-ahamed-russel/", "https://i.pravatar.cc/220?img=67",
                "23.4607", "91.1809", "Race Course, Cumilla")
        );

        int created = 0;
        int updated = 0;
        for (DoctorEntity incoming : doctors) {
            Optional<DoctorEntity> existing = doctorsRepository.findByName(incoming.getName());
            if (existing.isPresent()) {
                DoctorEntity d = existing.get();
                d.setPhone(incoming.getPhone());
                d.setGender(incoming.getGender());
                d.setDescription(incoming.getDescription());
                d.setExperienceCount(incoming.getExperienceCount());
                d.setSpecialities(incoming.getSpecialities());
                d.setRatings(incoming.getRatings());
                d.setOfficeHours(incoming.getOfficeHours());
                d.setWebsite(incoming.getWebsite());
                d.setImage(incoming.getImage());
                d.setLatitude(incoming.getLatitude());
                d.setLongitude(incoming.getLongitude());
                d.setAddress(incoming.getAddress());
                doctorsRepository.save(d);
                updated++;
            } else {
                doctorsRepository.save(incoming);
                created++;
            }
        }

        logger.info("Doctor seed completed. Created: {}, Updated: {}", created, updated);
    }

    private DoctorEntity createDoctor(String name, String phone, String gender, String description,
                                       String experience, String specialities, double ratings,
                                       String hours, String website, String image,
                                       String lat, String lon, String address) {
        DoctorEntity d = new DoctorEntity();
        d.setName(name);
        d.setPhone(phone);
        d.setGender(gender);
        d.setDescription(description);
        d.setExperienceCount(experience);
        d.setSpecialities(specialities);
        d.setRatings(ratings);
        d.setOfficeHours(hours);
        d.setWebsite(website);
        d.setImage(doctorImageService.getImageForGender(gender, name));
        d.setLatitude(lat);
        d.setLongitude(lon);
        d.setAddress(address);
        return d;
    }

    /**
     * Backwards-compatible helper used by the existing seeding logic.
     */
    private String getRandomImageForGender(String gender) {
        return doctorImageService.getImageForGender(gender, gender != null ? gender : "");
    }

    // ───────────────── APPOINTMENTS & RATINGS ─────────────────

    private void initializeDemoAppointmentsAndRatings() {
        List<DoctorEntity> doctors = doctorsRepository.findAll();
        if (doctors.isEmpty()) {
            logger.info("No doctors found, skipping appointment seeding.");
            return;
        }

        Map<String, DoctorEntity> doctorByName = new HashMap<>();
        for (DoctorEntity d : doctors) {
            doctorByName.put(d.getName(), d);
        }

        Set<String> existingKeys = new HashSet<>();
        for (DoctorAppointmentEntity ex : appointmentRepository.findAll()) {
            existingKeys.add(buildApptKey(ex.getChildId(), ex.getDoctorId(), ex.getReason(), ex.getAppointmentAt()));
        }

        LocalDateTime now = LocalDateTime.now();

        List<DoctorAppointmentEntity> seeds = List.of(
            buildAppointment("child-demo-1", doctorByName.get("Dr. Nusrat Jahan"), now.minusDays(30),
                "Initial ASD screening consultation", "VISITED",
                "Child shows signs of mild ASD. Recommended Q10 questionnaire and behavioral video assessment.", "BKASH", "PAID", 1500.0),
            buildAppointment("child-demo-1", doctorByName.get("Dr. Tanim Rahman"), now.minusDays(21),
                "Behavioral support planning", "VISITED",
                "ABA-based weekly routine and parent-led reinforcement plan shared.", "CASH", "PAID", 1200.0),
            buildAppointment("child-demo-1", doctorByName.get("Dr. Marium Akter"), now.minusDays(15),
                "Speech and communication baseline", "VISITED",
                "Speech delay observed. Weekly language stimulation therapy advised.", "NAGAD", "PAID", 2000.0),
            buildAppointment("child-demo-1", doctorByName.get("Dr. Farhana Karim"), now.minusDays(9),
                "Articulation follow-up", "VISITED",
                "Consonant articulation improved. Continue home practice with picture cards.", "CARD", "PAID", 1800.0),
            buildAppointment("child-demo-1", doctorByName.get("Dr. Tamanna Sultana"), now.minusDays(4),
                "School readiness and behavior review", "VISITED",
                "Attention improving. Suggested visual daily planner and social stories.", "BKASH", "PAID", 2200.0),
            buildAppointment("child-demo-1", doctorByName.get("Dr. Nusrat Jahan"), now.plusDays(6),
                "Monthly check-up and progress review", "BOOKED", "", "CARD", "PENDING", 1500.0),
            buildAppointment("child-demo-1", doctorByName.get("Dr. Rafiq Hasan"), now.plusDays(11),
                "ABA review session", "BOOKED", "", "BKASH", "PENDING", 1300.0),
            buildAppointment("child-demo-1", doctorByName.get("Dr. Rezaul Kabir"), now.minusDays(2),
                "Social skills group orientation", "CANCELLED", "", "CASH", "PENDING", 0.0),

            buildAppointment("child-demo-2", doctorByName.get("Dr. Marium Akter"), now.minusDays(12),
                "Developmental milestone check", "VISITED",
                "All milestones generally on track. Encourage peer interaction weekly.", "BKASH", "PAID", 1800.0),
            buildAppointment("child-demo-2", doctorByName.get("Dr. Sadia Islam"), now.minusDays(8),
                "Sensory integration review", "VISITED",
                "Sensory seeking behavior reduced. Continue deep pressure activities.", "NAGAD", "PAID", 1600.0),
            buildAppointment("child-demo-2", doctorByName.get("Dr. Iffat Ara"), now.plusDays(5),
                "Neurology follow-up", "BOOKED", "", "CARD", "PENDING", 2100.0),
            buildAppointment("child-demo-2", doctorByName.get("Dr. Mahfuz Alom"), now.plusDays(14),
                "Parent coaching session", "BOOKED", "", "CASH", "PENDING", 1000.0),

            buildAppointment("child-demo-3", doctorByName.get("Dr. Saymun Jesmin"), now.minusDays(40),
                "First speech-language intake", "VISITED",
                "Delayed expressive language identified. Parent training plan created.", "BKASH", "PAID", 1700.0),
            buildAppointment("child-demo-3", doctorByName.get("Dr. Balaram Paul"), now.minusDays(28),
                "School readiness assessment", "VISITED",
                "Suggested classroom visual schedule and weekly social goals.", "CARD", "PAID", 2100.0),
            buildAppointment("child-demo-3", doctorByName.get("Dr. Nusrat Jahan"), now.minusDays(16),
                "Emotion regulation coaching", "VISITED",
                "Breathing and color-based calming activities were effective.", "NAGAD", "PAID", 1300.0),
            buildAppointment("child-demo-3", doctorByName.get("Dr. Sadia Islam"), now.plusDays(4),
                "Sensory routine review", "BOOKED", "", "CASH", "PENDING", 1500.0),
            buildAppointment("child-demo-3", doctorByName.get("Dr. Mohammad Ashraf-Us-Zaman Mahmud"), now.plusDays(12),
                "Behavior check-in", "BOOKED", "", "BKASH", "PENDING", 1600.0),

            buildAppointment("child-demo-4", doctorByName.get("Dr. Shifat Sultana"), now.minusDays(22),
                "Toddler ASD early signs consultation", "VISITED",
                "Recommended language stimulation games and predictable routine blocks.", "BKASH", "PAID", 1800.0),
            buildAppointment("child-demo-4", doctorByName.get("Dr. Tanim Rahman"), now.minusDays(12),
                "Play-based speech practice", "VISITED",
                "Improved turn-taking and imitation in guided play sessions.", "CASH", "PAID", 1200.0),
            buildAppointment("child-demo-4", doctorByName.get("Dr. Iffat Ara"), now.minusDays(5),
                "Neurology follow-up", "VISITED",
                "No alarming changes; continue current therapy schedule.", "CARD", "PAID", 2300.0),
            buildAppointment("child-demo-4", doctorByName.get("Major (Dr.) Jonayed Ahmed Talukder"), now.plusDays(8),
                "Structured play session", "BOOKED", "", "NAGAD", "PENDING", 1400.0),
            buildAppointment("child-demo-4", doctorByName.get("Dr. Tamanna Sultana"), now.plusDays(18),
                "Comprehensive follow-up", "BOOKED", "", "CARD", "PENDING", 2200.0),
            buildAppointment("child-demo-4", doctorByName.get("Dr. Rezaul Kabir"), now.minusDays(1),
                "Social group intro", "CANCELLED", "", "CASH", "PENDING", 0.0)
        );

        List<DoctorAppointmentEntity> insertedAppointments = new ArrayList<>();
        for (DoctorAppointmentEntity seed : seeds) {
            if (seed.getDoctorId() == null) continue;
            String key = buildApptKey(seed.getChildId(), seed.getDoctorId(), seed.getReason(), seed.getAppointmentAt());
            if (!existingKeys.contains(key)) {
                insertedAppointments.add(appointmentRepository.save(seed));
                existingKeys.add(key);
            }
        }
        logger.info("Appointment seed completed. Inserted: {}", insertedAppointments.size());

        List<DoctorAppointmentEntity> allAppointments = appointmentRepository.findAll();
        Map<String, DoctorAppointmentEntity> apptIndex = new HashMap<>();
        for (DoctorAppointmentEntity appt : allAppointments) {
            apptIndex.put(buildRatingKey(appt.getChildId(), appt.getDoctorId(), appt.getReason()), appt);
        }

        List<DoctorRatingEntity> ratingSeeds = new ArrayList<>();
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-1", doctorByName.get("Dr. Nusrat Jahan"), "Initial ASD screening consultation", 5,
            "Excellent doctor! Very patient with children and explained everything clearly.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-1", doctorByName.get("Dr. Tanim Rahman"), "Behavioral support planning", 4,
            "Great session. Practical tips for daily routines helped us a lot.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-1", doctorByName.get("Dr. Marium Akter"), "Speech and communication baseline", 5,
            "Outstanding support. Child felt very comfortable.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-1", doctorByName.get("Dr. Farhana Karim"), "Articulation follow-up", 5,
            "Clear speech exercises and useful home recommendations.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-2", doctorByName.get("Dr. Sadia Islam"), "Sensory integration review", 4,
            "Very detailed explanation of sensory activities for home.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-3", doctorByName.get("Dr. Saymun Jesmin"), "First speech-language intake", 5,
            "Wonderful communication and child-friendly approach.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-3", doctorByName.get("Dr. Balaram Paul"), "School readiness assessment", 4,
            "Practical school plan with clear weekly goals.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-3", doctorByName.get("Dr. Nusrat Jahan"), "Emotion regulation coaching", 5,
            "Helpful calming strategies and emotional support.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-4", doctorByName.get("Dr. Shifat Sultana"), "Toddler ASD early signs consultation", 5,
            "Extremely patient and supportive for parents.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-4", doctorByName.get("Dr. Tanim Rahman"), "Play-based speech practice", 4,
            "Play activities were easy to continue at home.");
        addRatingIfPossible(ratingSeeds, apptIndex,
            "child-demo-4", doctorByName.get("Dr. Iffat Ara"), "Neurology follow-up", 5,
            "Clear diagnosis discussion and practical recommendations.");

        int insertedRatings = 0;
        for (DoctorRatingEntity seed : ratingSeeds) {
            if (ratingRepository.findByAppointmentIdAndChildId(seed.getAppointmentId(), seed.getChildId()).isEmpty()) {
                ratingRepository.save(seed);
                insertedRatings++;
            }
        }

        assignRandomRatingsToAllDoctors();
        logger.info("Rating seed completed. Inserted: {}", insertedRatings);
    }

    private void assignRandomRatingsToAllDoctors() {
        List<DoctorEntity> doctors = doctorsRepository.findAll();
        int updated = 0;
        for (DoctorEntity doctor : doctors) {
            double hashBase = Math.abs((doctor.getName() + doctor.getPhone()).hashCode() % 120) / 100.0;
            double rating = Math.min(4.95, Math.max(3.7, 3.7 + hashBase));
            doctor.setRatings(Math.round(rating * 10.0) / 10.0);
            doctorsRepository.save(doctor);
            updated++;
        }
        logger.info("Assigned deterministic random ratings for {} doctors", updated);
    }

    private String buildApptKey(String childId, String doctorId, String reason, LocalDateTime at) {
        return String.join("|",
            childId == null ? "" : childId,
            doctorId == null ? "" : doctorId,
            reason == null ? "" : reason,
            at == null ? "" : at.withSecond(0).withNano(0).toString());
    }

    private String buildRatingKey(String childId, String doctorId, String reason) {
        return String.join("|",
            childId == null ? "" : childId,
            doctorId == null ? "" : doctorId,
            reason == null ? "" : reason);
    }

    private void addRatingIfPossible(List<DoctorRatingEntity> target,
                                     Map<String, DoctorAppointmentEntity> apptIndex,
                                     String childId,
                                     DoctorEntity doctor,
                                     String reason,
                                     int stars,
                                     String review) {
        if (doctor == null) return;
        DoctorAppointmentEntity appt = apptIndex.get(buildRatingKey(childId, doctor.getId(), reason));
        if (appt == null) return;
        target.add(buildRating(childId, doctor.getId(), appt.getId(), stars, review));
    }

    private void initializeDemoAssessmentHistory() {
        if (asdExRepository.count() >= 30) {
            logger.info("ASD assessment history already populated, skipping extra seeds.");
            return;
        }

        List<ASDExEntity> existing = asdExRepository.findAll();
        Set<String> requestIds = new HashSet<>();
        for (ASDExEntity row : existing) {
            if (row.getRequestID() != null) requestIds.add(row.getRequestID());
        }

        LocalDateTime base = LocalDateTime.now().minusMonths(4);
        List<ASDExEntity> rows = new ArrayList<>();

        rows.add(buildAssessment("child-demo-1", "demo-c1-001", base.plusDays(2), "0.62", "0", "0.71"));
        rows.add(buildAssessment("child-demo-1", "demo-c1-002", base.plusDays(18), "0.58", "0", "0.66"));
        rows.add(buildAssessment("child-demo-1", "demo-c1-003", base.plusDays(34), "0.53", "0", "0.61"));
        rows.add(buildAssessment("child-demo-1", "demo-c1-004", base.plusDays(50), "0.49", "1", "0.56"));
        rows.add(buildAssessment("child-demo-1", "demo-c1-005", base.plusDays(68), "0.46", "1", "0.51"));
        rows.add(buildAssessment("child-demo-1", "demo-c1-006", base.plusDays(86), "0.42", "1", "0.47"));

        rows.add(buildAssessment("child-demo-2", "demo-c2-001", base.plusDays(5), "0.44", "1", "0.42"));
        rows.add(buildAssessment("child-demo-2", "demo-c2-002", base.plusDays(26), "0.41", "1", "0.39"));
        rows.add(buildAssessment("child-demo-2", "demo-c2-003", base.plusDays(47), "0.39", "1", "0.36"));
        rows.add(buildAssessment("child-demo-2", "demo-c2-004", base.plusDays(70), "0.35", "1", "0.31"));
        rows.add(buildAssessment("child-demo-2", "demo-c2-005", base.plusDays(93), "0.33", "1", "0.28"));

        rows.add(buildAssessment("child-demo-3", "demo-c3-001", base.plusDays(3), "0.67", "0", "0.74"));
        rows.add(buildAssessment("child-demo-3", "demo-c3-002", base.plusDays(21), "0.61", "0", "0.69"));
        rows.add(buildAssessment("child-demo-3", "demo-c3-003", base.plusDays(42), "0.56", "0", "0.63"));
        rows.add(buildAssessment("child-demo-3", "demo-c3-004", base.plusDays(64), "0.50", "1", "0.57"));
        rows.add(buildAssessment("child-demo-3", "demo-c3-005", base.plusDays(85), "0.47", "1", "0.52"));
        rows.add(buildAssessment("child-demo-3", "demo-c3-006", base.plusDays(106), "0.44", "1", "0.48"));

        rows.add(buildAssessment("child-demo-4", "demo-c4-001", base.plusDays(8), "0.59", "0", "0.68"));
        rows.add(buildAssessment("child-demo-4", "demo-c4-002", base.plusDays(27), "0.55", "0", "0.63"));
        rows.add(buildAssessment("child-demo-4", "demo-c4-003", base.plusDays(46), "0.52", "1", "0.58"));
        rows.add(buildAssessment("child-demo-4", "demo-c4-004", base.plusDays(67), "0.48", "1", "0.53"));
        rows.add(buildAssessment("child-demo-4", "demo-c4-005", base.plusDays(88), "0.45", "1", "0.49"));
        rows.add(buildAssessment("child-demo-4", "demo-c4-006", base.plusDays(109), "0.41", "1", "0.44"));

        int inserted = 0;
        for (ASDExEntity row : rows) {
            if (!requestIds.contains(row.getRequestID())) {
                asdExRepository.save(row);
                inserted++;
            }
        }

        logger.info("ASD result-history seed completed. Inserted: {}", inserted);
    }

    private ASDExEntity buildAssessment(String childId, String requestId, LocalDateTime at,
                                        String q10, String vidRes, String vidConf) {
        ASDExEntity e = new ASDExEntity();
        e.setUsername(childId);
        e.setRequestID(requestId);
        e.setTestDate(at);
        e.setQ10(q10);
        e.setVid_res(vidRes);
        e.setVid_confid(vidConf);
        e.setSuggested_therapies("Speech Therapy, Social Skill Training, Parent Coaching, Sensory Break Routine");
        e.setSuggested_games("Musical Rhythm, Emotion Match, Memory Match, Drawing Therapy");
        return e;
    }

    private DoctorAppointmentEntity buildAppointment(String childId, DoctorEntity doctor,
                                                      LocalDateTime appointmentAt, String reason,
                                                      String status, String visitNotes,
                                                      String paymentMethod, String paymentStatus, Double paymentAmount) {
        DoctorAppointmentEntity a = new DoctorAppointmentEntity();
        a.setChildId(childId);
        if (doctor == null) {
            logger.warn("Skipping appointment seed because doctor is missing for reason: {}", reason);
            return a;
        }
        a.setDoctorId(doctor.getId());
        a.setDoctorName(doctor.getName());
        a.setAppointmentAt(appointmentAt);
        a.setReason(reason);
        a.setStatus(status);
        a.setVisitNotes(visitNotes);
        a.setPaymentMethod(paymentMethod);
        a.setPaymentStatus(paymentStatus);
        a.setPaymentAmount(paymentAmount);
        a.setCreatedAt(appointmentAt.minusDays(2));
        a.setUpdatedAt(appointmentAt);
        return a;
    }

    private DoctorRatingEntity buildRating(String childId, String doctorId, String appointmentId,
                                            int stars, String review) {
        DoctorRatingEntity r = new DoctorRatingEntity();
        r.setChildId(childId);
        r.setDoctorId(doctorId);
        r.setAppointmentId(appointmentId);
        r.setStars(stars);
        r.setReviewText(review);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }
}
