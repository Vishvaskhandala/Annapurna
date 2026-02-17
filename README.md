# 🪔 Annapurna – Food Waste Reduction App

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
  <img src="https://img.shields.io/badge/Backend-Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white"/>
  <img src="https://img.shields.io/badge/Notifications-Firebase%20FCM-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
</p>

<p align="center">
  <b>अन्न दान • महा दान</b><br/>
  <i>"He who feeds a hungry soul, feeds God himself."</i>
</p>

---

**Annapurna** is a production-grade Android application that bridges the gap between food surplus and food scarcity — connecting **Anna Daatas (donors)** with **Seva Grahis (recipients & NGOs)** through real-time food sharing.

> Built to fight hunger. Designed with compassion. Engineered for scale.

---

## ✨ Features

### 🔐 Authentication & Session
- Secure email/password login & registration
- Role-based users: **Anna Daata** (Donor) / **Seva Grahi** (Recipient / NGO)
- Persistent session management via Supabase Auth
- Auto session restore on app restart

### 🍱 Food Donation & Claim
- Donors post surplus food with image, quantity, pickup time, location & description
- Recipients browse available food in real-time
- One-tap claim with pickup coordination
- Food status tracking: Available → Claimed → Fulfilled

### 🔔 Real-Time Push Notifications
- **New food posted** → instant notification to all nearby recipients
- **Food claimed** → instant notification to the donor
- Built with **Firebase Cloud Messaging (FCM v1 HTTP API)**
- Notifications triggered via **Supabase Edge Functions** (server key never exposed in app)
- FCM token stored per user in Supabase, refreshed automatically on login
- Foreground + background + killed-state notification handling

### 📊 Activity & Impact Tracking
- Total donated meals counter
- Received meals counter
- Completed food sharing actions
- Personal journey dashboard with stats

### 🧭 Clean Modern UI
- Built with **Jetpack Compose + Material 3**
- Saffron-inspired warm humanitarian color theme
- Smooth animations and state-based navigation
- Role-specific home screens

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose (Material 3) |
| Architecture | MVVM (ViewModel + StateFlow) |
| Backend & DB | Supabase (PostgreSQL + PostgREST) |
| Authentication | Supabase Auth |
| Push Notifications | Firebase Cloud Messaging (FCM v1) |
| Notification Trigger | Supabase Edge Functions (Deno/TypeScript) |
| Image Storage | Supabase Storage |
| Image Loading | Coil |
| Navigation | Navigation Compose |

---

## 🔔 Notification Architecture

```
User Action (Post Food / Claim Food)
         ↓
  Android App (FoodRepository)
         ↓
  Supabase Edge Function (bright-task)
         ↓
  FCM HTTP v1 API (Google)
         ↓
  Target Device (FirebaseService.kt)
         ↓
  showNotification() → System Tray
```

- Server key is stored as a **Supabase Secret** — never shipped in the APK
- Edge Function fetches FCM tokens from Supabase DB and fans out notifications
- Handles both `notification` payload (background) and `data` payload (foreground)

---

## 🔒 Security & Scalability

- **Supabase Row Level Security (RLS)** — users can only read/write their own data
- **FCM server key stored in Supabase Secrets** — not in app source code
- **MVVM architecture** — clean separation of UI, business logic, and data
- **Session management** — `loadFromStorage()` ensures no false logouts; `signOut()` invalidates server session

---

## 📁 Project Structure

```
app/
├── data/
│   ├── model/          # FoodPost, User, UserStats
│   ├── remote/         # SupabaseClient, FirebaseService, EdgeNotificationRepository
│   └── repository/     # AuthRepository, FoodRepository
├── ui/
│   ├── screen/         # All Composable screens
│   ├── component/      # Reusable UI components
│   ├── navigation/     # NavGraph
│   └── theme/          # Colors, Typography
└── viewmodel/          # AuthViewModel, FoodViewModel
```

---

## 📱 Screens

| Screen | Description |
|---|---|
| Splash | Animated intro with session check |
| Login / Register | Auth with role selection |
| UserType | Anna Daata or Seva Grahi selection |
| Home (Donor) | View & manage posted food |
| Home (Recipient) | Browse & claim available food |
| Post Food | Image + details form to share food |
| My Activity | Personal donation/claim history |
| Profile | User stats, contact info, logout |
| Map | 📍 Coming Soon |

---

## ⚙️ Setup & Installation

1. Clone the repository
   ```bash
   git clone https://github.com/vishvaskhandala/Annapurna.git
   ```

2. Open in **Android Studio Hedgehog** or later

3. Add your **Supabase credentials** in `SupabaseClientProvider.kt`
   ```kotlin
   private const val SUPABASE_URL = "your-supabase-url"
   private const val SUPABASE_KEY = "your-anon-key"
   ```

4. Add your **Firebase `google-services.json`** to `/app` directory

5. Add your **Firebase Service Account JSON** as a Supabase Secret named `FIREBASE_SERVICE_ACCOUNT`

6. Deploy the Edge Function
   ```bash
   supabase functions deploy bright-task
   ```

7. Run the app on a physical device (Android 7.0+)

---

## 🗃️ Database Schema (Supabase)

```sql
users        → user_id, name, email, phone, user_type, fcm_token, food_donated, food_received
food_posts   → food_id, donor_id, food_name, quantity, description, location, pickup_time, image_url, status, claimed_by
claims       → claim_id, food_id, recipient_id, claimed_at
```

RLS policies ensure users access only their permitted data.

---

## 🎯 Vision & Social Impact

Every meal shared through Annapurna directly contributes to:

- 🌾 **Reducing food waste** from households, restaurants, and events
- 🤲 **Fighting hunger** in local communities
- 🏢 **Empowering NGOs** with a direct food supply pipeline
- 🌍 Working toward **UN SDG Goal 2: Zero Hunger**

---

## 🚀 Roadmap

- [x] Authentication with persistent sessions
- [x] Food posting with image upload
- [x] Food claiming with status tracking
- [x] Push notifications (FCM + Edge Functions)
- [x] Activity dashboard
- [ ] 📍 Real-time map with nearby food pins
- [ ] 💬 In-app donor ↔ recipient chat
- [ ] ⭐ Trust & rating system
- [ ] 🌍 Multi-language support (Hindi, Marathi, Gujarati)
- [ ] 📦 NGO bulk food request system

---

## 🧑‍💻 Developer

**Vishvas Khandala**
Android Developer · Kotlin · Jetpack Compose · MVVM · Supabase

- 🐙 GitHub: [github.com/vishvaskhandala](https://github.com/vishvaskhandala)
- 💼 LinkedIn: [linkedin.com/in/vishvaskhandala](https://linkedin.com/in/vishvaskhandala)
- 📧 Email: vishvaskhandala@gmail.com

---

## 📜 License

This project is built for **educational and social impact purposes**.
Free to use, fork, and improve for non-commercial humanitarian projects.

---

<p align="center">Made with ❤️ and 🪔 to feed the world — one meal at a time.</p>