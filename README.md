# 🎓 SkillBridge

**SkillBridge** is a peer-to-peer learning marketplace that connects students who want to learn with other students who can teach.   

Our mission is to make learning **affordable, flexible, and community-driven** by leveraging the skills already available within the student community.  

Many students struggle to afford professional tutoring, while others want to earn money or gain experience by teaching what they know.   

With **SkillBridge**:
- Learners get affordable help in both academics and hobbies.   
- Tutors earn extra income and experience by sharing their skills.  
- The student community becomes a self-sustaining support network.  

---

## 🎯 Target Users
- **Ava**:  a first-year student looking for affordable math tutoring.   
- **Liam**: a third-year student who wants to earn money by offering piano lessons.  

---

## 🚀 Features

### Core Features
- 🔐 **Secure Authentication**:  Sign-up & login via email or university SSO with **Firebase Authentication**  
- 👩‍🏫 **Dual Role System**: Users can be Learners, Tutors, or Both  
- 📍 **Location-Based Search**: Find nearby tutors using **GPS** with map integration  
- 💾 **Offline Mode**: Access profiles, saved tutors, and booked lessons without internet  

### Listings & Discovery
- 📝 **Listing Management**: Create and manage both tutor proposals and student requests
- 🔍 **Smart Search**: Filter and discover tutors by subject, location, and availability
- ⭐ **Profile Ratings**: View tutor ratings and read student reviews with comments

### Booking System
- 📅 **Flexible Scheduling**: Book sessions with preferred tutors
- ⚠️ **Duplicate Booking Prevention**: Smart guards against double-booking
- 🔄 **Booking States**: Track pending, confirmed, completed, and cancelled bookings
- ✅ **Completion Confirmation**: Both parties can mark sessions as complete

### Payment Integration
- 💳 **Payment Tracking**: Monitor payment status (Pending, Paid, Confirmed)
- 💰 **Role-Based Payment Flow**: Different views for payers (students) and receivers (tutors)
- 🔒 **Payment Verification**: Completion restrictions until payment confirmation
- ⚡ **Request vs Proposal Support**: Handles reversed roles for student requests

### Communication
- 💬 **Real-Time Messaging**: Chat with tutors and students
- ⏰ **Message Timestamps**: See when messages were sent with human-readable formats
- 🔔 **Unread Message Tracking**: Never miss an important conversation
- 👤 **Conversation Management**: Organized chat list with participant names

### Profile & Account
- 📊 **Rich Profiles**: Display skills, subjects, availability, and ratings
- 🗑️ **Account Deletion**: Users can permanently delete their accounts with confirmation
- 🔐 **Multi-Account Support**: Switch between accounts with logout functionality
- 🔄 **Profile Navigation**: Seamlessly view other users' profiles from listings and bookings

---

## 🏗️ Tech Stack
- **Frontend**: Android mobile app built with **Kotlin** (99.8%) and **JavaScript** (0.2%)
- **UI Framework**: Jetpack Compose for modern declarative UI
- **Backend**: Google Firebase ecosystem: 
  - **Cloud Firestore**: Real-time database with offline persistence
  - **Firebase Authentication**: Secure user authentication
  - **Cloud Functions**: Serverless backend logic
- **Device Features**: 
  - GPS/location services for tutor discovery
  - Local caching for offline support
  - Push notifications for messages and bookings

---

## 📡 Offline Mode
- ✅ **Available Offline**: Profile data, saved tutors, booked lessons, cached messages
- 🔄 **Requires Online**:  New tutor listings, live chat, payment updates, updated ratings

---

## 🔒 Security & Data Protection
- Accounts managed with **Firebase Authentication**  
- Role-based permissions (Learner / Tutor / Both)  
- Data stored securely in **Cloud Firestore** with strict security rules
- Production-ready Firestore rules for access control
- Payment verification before booking completion

---

## 🎨 Design (Figma)  
We use **Figma** to create mockups and track design work.   

- 🔗 [SkillBridge Mockup on Figma](https://www.figma.com/design/KLu1v4Q1ahcIgpufrbxQCV/SkillBridge-mockup?node-id=0-1&t=MaZllQ2pNaWYwCoW-1)  
- ✅ All team members have **edit access**  
- 👩‍💻 **Dev Mode** is enabled for developers to inspect styles and assets  
- 🌍 File is set to **public view** for course staff access

---

## 📱 App Status

### ✅ Implemented Features
- User authentication (email & SSO)
- Profile creation and management
- Listing creation (proposals and requests)
- Location-based tutor search
- Booking system with state management
- Real-time messaging with timestamps
- Payment tracking and verification
- Rating system with comments
- Account deletion and logout
- Offline data persistence
- Duplicate booking prevention
- Profile navigation across the app

## 📄 License
This project is part of a university course at EPFL for Software Enterprise. 
