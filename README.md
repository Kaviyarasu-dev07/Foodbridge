# 🌿 FoodBridge — Real-Time Food Rescue Platform

FoodBridge connects surplus food donors (restaurants, stores, individuals) with
NGOs and shelters in real time, so surplus meals reach people instead of the
bin. Donors post available food, nearby NGOs get instantly alerted, and both
sides track their impact over time.

## ✨ Features

- **Real-time listings** — Donors post surplus food with quantity, condition,
  and pickup deadline; NGOs are notified instantly over WebSocket.
- **Role-based dashboards** — Separate views for Donor, NGO, and Admin, each
  with permissions scoped to their role.
- **AI-assisted food estimation** — Upload a photo of the food and the app
  estimates quantity/type using Google Cloud Vision.
- **Live chat between donor and NGO** — Coordinate pickup details in real time.
- **Route optimization** — Suggests efficient pickup routes for NGOs handling
  multiple listings.
- **Impact tracking & leaderboard** — Meals rescued, environmental impact
  certificates, and a leaderboard to encourage participation.
- **Predictive alerts** — Scheduled checks flag expiring listings and surface
  donor patterns.
- **Push notifications** — Web push support so NGOs get alerted even when the
  tab isn't open.
- **JWT-based authentication** with role-based access control (Spring
  Security).
- **Multi-language ready** — i18next-based translation setup (English).

## 🛠 Tech Stack

**Backend**
- Java 17, Spring Boot 2.7
- Spring Security + JWT (jjwt)
- Spring Data JPA (MySQL in production, H2 for local dev)
- Spring WebSocket (STOMP) for real-time listings and chat
- Google Cloud Vision API (food photo estimation)
- ZXing (QR code generation)
- SpringDoc OpenAPI (API docs)

**Frontend**
- React 19 + Vite
- React Router 7
- Axios (with JWT interceptor)
- @stomp/stompjs + SockJS (WebSocket client)
- Chart.js / react-chartjs-2 (impact charts)
- Leaflet / react-leaflet (pickup route maps)
- i18next (translations)
- react-hot-toast (notifications)
- vite-plugin-pwa (installable PWA support)

## 📁 Project Structure

```
Foodbridge/
├── src/main/java/com/foodbridge/     # Spring Boot backend
│   ├── controller/                    # REST + WebSocket endpoints
│   ├── service/                       # Business logic
│   ├── entity/                        # JPA entities
│   ├── repository/                    # Spring Data repositories
│   ├── security/                      # JWT + Spring Security config
│   ├── scheduler/                     # Predictive alerts, expired listings
│   └── dto/                           # Request/response payloads
├── src/main/resources/
│   └── application.properties.example # Copy to application.properties
├── foodbridge-frontend/               # React + Vite frontend
│   └── src/
│       ├── pages/                     # Donor / NGO / Admin dashboards, auth
│       ├── components/                # Reusable UI components
│       └── services/api.js            # Axios instance with JWT interceptor
└── pom.xml
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL (optional — H2 in-memory works out of the box for local dev)

### Backend setup
```bash
# Copy the example config and fill in your own values
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
Edit `application.properties` with your own database credentials, a strong
`jwt.secret`, and (optionally) a Gemini/Google Vision API key for photo
estimation.

```bash
./mvnw spring-boot:run
```
Backend runs on `http://localhost:8080`.

### Frontend setup
```bash
cd foodbridge-frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173`.

### Demo accounts
Seeded automatically on first run (local/H2 profile):

| Role  | Email                  | Password    |
|-------|-------------------------|-------------|
| Donor | donor@foodbridge.com    | password123 |
| NGO   | ngo@foodbridge.com      | password123 |
| Admin | admin@foodbridge.com    | password123 |

## 🔒 Security Notes

- Never commit real values for `application.properties` — it's gitignored.
  Use `application.properties.example` as the template.
- JWT secret, database credentials, and API keys must be set per-environment
  and kept out of version control.

## 📄 License

This project is licensed under the MIT License.
