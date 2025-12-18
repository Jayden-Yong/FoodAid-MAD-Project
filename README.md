# FoodAid - Food Donation & Community Application

**FoodAid** is an Android application designed to reduce food waste and help those in need by connecting food donors with individuals or organizations seeking food assistance. It features a community platform, real-time donation tracking, and a user-friendly interface for managing food distributions.

## 📱 Key Features

*   **Food Donation Platform**: Easily list food items for donation with details like expiry date, quantity, and images.
*   **Interactive Map**: View available donations on a map and find nearby food banks or donors.
*   **Community Hub**: Share updates and stories with the community.
*   **Impact Tracking**: Track your contributions and view your impact on the community with visual statistics.
*   **Gamification**: Earn badges and recognition based on your donation activity.
*   **Notifications**: Stay updated with real-time alerts for donation statuses and claim requests.
*   **Secure Authentication**: Robust email/password login and registration system powered by Firebase Authentication.

## 🛠 Tech Stack

*   **Language**: Java
*   **Platform**: Android (Native)
*   **Backend**: Firebase (Firestore, Authentication, Cloud Storage, Cloud Functions)
*   **Architecture**: MVVM (Model-View-ViewModel) pattern
*   **Testing**: Android Instrumentation Tests (supports Firebase Emulator)

## 📁 Project Structure

*   `app/src/main/java/com/example/foodaid_mad_project/`
    *   `AuthFragments/`: Login, Registration, and User authentication logic.
    *   `CommunityFragments/`: Community post feed and post creation logic.
    *   `DonateFragments/`: Screens for listing new donations and uploading images.
    *   `HomeFragments/`: Main dashboard, Map view integration, and Item detail views.
    *   `ImpactFragments/`: User impact statistics and contribution tracking.
    *   `ProfileFragments/`: User profile management and settings.
    *   `Model/`: Data models (e.g., `User`, `FoodItem`, `CommunityPost`, `Notification`, `Badge`).
    *   `Utils/`: Helper classes and utility functions.

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Ladybug or newer recommended)
*   JDK 17+
*   Firebase Project configuration

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/Jayden-Yong/FoodAid-MAD-Project.git
    ```
2.  Open the project in **Android Studio**.
3.  **Important**: You must add your own `google-services.json` file to the `app/` directory for Firebase services to work.
4.  Sync the Gradle project to download dependencies.
5.  Run the application on an Android Emulator or Physical Device.

## 🧪 Testing
The project includes instrumentation tests designed to run against the Firebase Emulator Suite to ensure safe and isolated testing.
*   Refer to `AuthEmulatorTest.java` for examples of authentication testing interacting with the local emulator.

## 🤝 Contributing
Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request
