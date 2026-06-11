# Lecturer Workload Distribution (Credify)

**Credify** is a comprehensive Android application designed to streamline the management of lecturer workloads, course assignments, and academic data. Built with a robust backend powered by Supabase, it provides a seamless experience for Administrators, Coordinators, and Lecturers to monitor and distribute teaching loads efficiently.

## 👥 Contributors (Group 16)
- **Ilyas Akbar Khan**
- **Ahmad Khaizuran**
- **Alyssa Natalya**
- **Hui Lee**

## ✨ Features
- **Role-Based Access:** Distinct interfaces and permissions for Admins, Coordinators, and Lecturers.
- **Workload Management:** Automated calculation of BTSA and credit hours.
- **Course & Section Tracking:** Manage subjects, sections, and student counts.
- **Real-time Synchronization:** Powered by Supabase for instant data updates.
- **Authentication:** Secure login and password recovery system.
- **Edge Functions:** Server-side logic for managing user profiles and database triggers.

## 🛠 Tech Stack
- **Language:** Java & Kotlin
- **UI:** Android XML & Jetpack Compose (BOM 2024.10.01)
- **Backend:** Supabase (Auth, Postgrest, Storage, Functions)
- **Networking:** Ktor & OkHttp

---

## 📥 How to Install (from GitHub Releases)

Follow these steps to install the app on your Android device without using Android Studio:

### 1. Enable "Unknown Sources"
Since this app is not on the Google Play Store, you must allow your device to install apps from external sources:
1. Open **Settings** on your Android device.
2. Go to **Apps** > **Special app access** > **Install unknown apps**.
3. Select the browser you use (e.g., Chrome) and toggle on **Allow from this source**.

### 2. Download the App
1. Go to the [Releases](https://github.com/credify16/LecturerWorkloadDistributer/releases) page of this repository.
2. Find the latest version and under **Assets**, click on the `.apk` file (e.g., `app-release.apk`) to download it.

### 3. Install the APK
1. Once the download is complete, open your **Downloads** folder or click the download notification.
2. Tap the `.apk` file.
3. A prompt will ask if you want to install the application. Click **Install**.
4. Once installed, tap **Open** to launch Credify.

---

## 🔐 Test Credentials
Once you have run the database setup script, you can use the following account to log in as an Admin:

- **Email:** `admin@credify.com`
- **Password:** `credify16`

---

## 🚀 Development Setup (For Developers)
If you wish to run the project locally using Android Studio:
1. Clone the repository:
   ```bash
   git clone https://github.com/credify16/LecturerWorkloadDistributer.git
   ```
### 2. Database Setup (Supabase)
To replicate the database structure:
1. Go to your **Supabase Dashboard** and create a new project.
2. Navigate to the **SQL Editor**.
3. Copy the contents of [`database/setup.sql`](database/setup.sql) from this repository and run it.
4. (Optional) If using Edge Functions, navigate to the `supabase/` directory and run `supabase functions deploy`.

### 3. Create a `local.properties` file
Create a `local.properties` file in the root directory and add your credentials:
   ```properties
   supabase.url="YOUR_SUPABASE_PROJECT_URL"
   supabase.key="YOUR_SUPABASE_ANON_KEY"
   ```
4. Sync the project with Gradle and run it on an emulator or physical device.
