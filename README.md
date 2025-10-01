# 🎓 SkillSwap

**SkillSwap** is a peer-to-peer learning marketplace that connects students who want to learn with other students who can teach.  

Our mission is to make learning **affordable, flexible, and community-driven** by leveraging the skills already available within the student community.  

Many students struggle to afford professional tutoring, while others want to earn money or gain experience by teaching what they know.  

With **SkillSwap**:
- Learners get affordable help in both academics and hobbies.  
- Tutors earn extra income and experience by sharing their skills.  
- The student community becomes a self-sustaining support network.  

---

## 🎯 Target Users
- **Ava**: a first-year student looking for affordable math tutoring.  
- **Liam**: a third-year student who wants to earn money by offering piano lessons.  

---

## 🚀 Features
- 🔐 Secure sign-up & login via email or university SSO with **Firebase Authentication**  
- 👩‍🏫 Role-based profiles: **Learner, Tutor, or Both**  
- 📍 Location-based search using **GPS** to find and sort nearby tutors on a map  
- 📝 Booking system for lessons and scheduling  
- ⭐ Ratings and reviews for tutors  
- 💾 **Offline mode**: access to profiles, saved tutors, and booked lessons without internet  

---

## 🏗️ Tech Stack
- **Frontend**: Mobile app (React Native or Flutter)  
- **Backend**: Google Firebase (Cloud Firestore, Authentication, Cloud Functions)  
- **Device Features**: GPS/location services, local caching for offline support  

---

## 📡 Offline Mode
- ✅ Available offline: profile, saved tutors, booked lessons  
- 🔄 Online required: new tutor listings, updated ratings, personalized recommendations


  

## 🔒 Security
- Accounts managed with **Firebase Authentication**  
- Role-based permissions (Learner / Tutor)  
- Data stored securely in **Cloud Firestore** with strict access rules  
