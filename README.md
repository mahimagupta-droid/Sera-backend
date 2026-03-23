# 🚨 Smart Emergency Resource Allocator (ERA)

A Spring Boot backend that intelligently allocates hospital resources to emergency cases using a weighted scoring algorithm based on **distance**, **severity**, and **hospital availability**.

---

## 🏗️ Project Structure

```
smart-emergency-allocator/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/era/emergency/
    │   │   ├── SmartEmergencyAllocatorApplication.java
    │   │   ├── controller/
    │   │   │   ├── EmergencyController.java    ← Emergency lifecycle & allocation
    │   │   │   └── HospitalController.java     ← Hospital CRUD
    │   │   ├── service/
    │   │   │   ├── AllocationEngine.java       ← Core AI scoring engine
    │   │   │   ├── EmergencyService.java       ← Emergency business logic
    │   │   │   └── HospitalService.java        ← Hospital business logic
    │   │   ├── model/
    │   │   │   ├── EmergencyRequest.java       ← Emergency JPA entity
    │   │   │   ├── Hospital.java               ← Hospital JPA entity
    │   │   │   ├── Severity.java               ← LOW / MEDIUM / HIGH / CRITICAL
    │   │   │   └── EmergencyStatus.java        ← PENDING / ALLOCATED / IN_TRANSIT …
    │   │   ├── repository/
    │   │   │   ├── EmergencyRequestRepository.java
    │   │   │   └── HospitalRepository.java
    │   │   ├── dto/
    │   │   │   └── EmergencyDTOs.java          ← All request/response DTOs
    │   │   ├── util/
    │   │   │   └── GeoDistanceUtil.java        ← Haversine distance + ETA
    │   │   └── config/
    │   │       ├── DataSeeder.java             ← 7 sample hospitals + 5 emergencies
    │   │       ├── GlobalExceptionHandler.java ← Centralised error handling
    │   │       └── WebConfig.java              ← CORS configuration
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/era/emergency/
            ├── controller/
            │   ├── EmergencyControllerIntegrationTest.java
            │   └── HospitalControllerIntegrationTest.java
            ├── service/
            │   └── AllocationEngineTest.java
            └── util/
                └── GeoDistanceUtilTest.java
```

---

## ⚙️ Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+     |
| Maven| 3.8+    |

---

## 🚀 Run the Application

```bash
# Clone / navigate to project
cd smart-emergency-allocator

# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
```

The server starts on **http://localhost:8080**

H2 Console: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:eradb`
- Username: `sa` | Password: *(empty)*

---

## 📡 REST API Reference

### Emergency Endpoints

#### `POST /emergency` — Submit a new emergency
```json
// Request body
{
  "patientName": "Rahul Sharma",
  "patientAge": 45,
  "severity": "CRITICAL",
  "emergencyType": "Cardiac Arrest",
  "description": "Patient collapsed. CPR in progress.",
  "latitude": 28.5450,
  "longitude": 77.2560,
  "locationAddress": "Lajpat Nagar, New Delhi",
  "reporterContact": "+91-9876543210"
}

// Response 201
{
  "success": true,
  "message": "Emergency submitted. Awaiting allocation.",
  "data": {
    "emergencyId": 1,
    "patientName": "Rahul Sharma",
    "severity": "CRITICAL",
    "emergencyType": "Cardiac Arrest",
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

**Severity values:** `LOW` | `MEDIUM` | `HIGH` | `CRITICAL`

---

#### `GET /allocate` — Allocate ALL pending emergencies
```json
// Response 200
{
  "success": true,
  "message": "Allocated 5 emergencies",
  "data": [
    {
      "emergencyId": 1,
      "patientName": "Rahul Sharma",
      "severity": "CRITICAL",
      "hospitalId": 2,
      "hospitalName": "AIIMS Delhi",
      "traumaCenter": true,
      "distanceKm": 3.2,
      "etaMinutes": 11,
      "allocationScore": 0.87,
      "scoreBreakdown": "distance=0.97(×0.35) + severity=0.88(×0.45) + availability=0.60(×0.20) + ambulance=0.05",
      "allocationReason": "Selected 'AIIMS Delhi' for CRITICAL emergency..."
    }
  ]
}
```

---

#### `GET /allocate/{id}` — Allocate a single emergency by ID

---

#### `GET /emergency` — List all emergencies

#### `GET /emergency/pending` — List only PENDING emergencies

#### `GET /emergency/{id}` — Get emergency by ID

---

### Hospital Endpoints

#### `POST /hospital` — Register a hospital
```json
// Request body
{
  "name": "Apollo Hospitals",
  "address": "Sarita Vihar, Delhi-Mathura Road",
  "city": "New Delhi",
  "latitude": 28.5355,
  "longitude": 77.2810,
  "totalIcuBeds": 60,
  "availableIcuBeds": 12,
  "totalGeneralBeds": 400,
  "availableGeneralBeds": 80,
  "availableAmbulances": 8,
  "specializations": "Cardiology,Neurology,Trauma",
  "isActive": true,
  "traumaCenter": true
}
```

#### `GET /hospital` — List all hospitals
#### `GET /hospital/active` — List active hospitals only
#### `GET /hospital/{id}` — Get hospital by ID
#### `PUT /hospital/{id}` — Update hospital data

---

## 🧠 Allocation Algorithm

```
score = (0.35 × distanceScore) + (0.45 × severityScore) + (0.20 × availabilityScore) + ambulanceBonus
```

| Factor | Weight | Description |
|--------|--------|-------------|
| Distance | 35% | Haversine distance, normalised against 100 km max. Closer = higher score |
| Severity | 45% | ICU availability weighted by case urgency; trauma bonus for CRITICAL cases |
| Availability | 20% | Overall bed capacity ratio (available / total) |
| Ambulance bonus | +5% | Flat bonus when ambulances are available |

### Severity-specific rules
- **CRITICAL** → Candidates filtered to trauma centres first; ICU availability weighted 50%
- **HIGH** → ICU weighted 40%, general beds 20%
- **MEDIUM** → Balanced ICU/general split
- **LOW** → General bed availability dominates

### ETA Calculation
`ETA = ⌈(distanceKm / 50 km·h⁻¹) × 60⌉ + 5 min dispatch overhead`

All weights are configurable in `application.properties`:
```properties
era.allocation.max-distance-km=100.0
era.allocation.weight.distance=0.35
era.allocation.weight.severity=0.45
era.allocation.weight.availability=0.20
```

---

## 🌱 Sample Data (auto-loaded)

**Hospitals (7):** Apollo Hospitals, AIIMS Delhi, Fortis Escorts, Max Saket, Safdarjung Hospital, BLK-Max (full – stress test), Sir Ganga Ram Hospital

**Emergencies (5):**
| Patient | Severity | Type |
|---------|----------|------|
| Rahul Sharma | CRITICAL | Cardiac Arrest |
| Priya Verma | HIGH | Road Accident |
| Mohan Das | MEDIUM | Stroke |
| Anjali Singh | HIGH | Severe Allergic Reaction |
| Suresh Kumar | LOW | Fracture |

---

## ✅ Running Tests

```bash
mvn test
```

**Test coverage:**
- `AllocationEngineTest` — 6 unit tests covering scoring, trauma preference, distance filtering
- `GeoDistanceUtilTest` — 6 unit tests for Haversine formula and ETA
- `EmergencyControllerIntegrationTest` — 6 integration tests (submit, validate, allocate, 404)
- `HospitalControllerIntegrationTest` — 5 integration tests (register, list, validate, 404)

---

## 📊 Quick API Walkthrough

```bash
# 1. View seeded hospitals
curl http://localhost:8080/hospital

# 2. Submit a new emergency
curl -X POST http://localhost:8080/emergency \
  -H "Content-Type: application/json" \
  -d '{"patientName":"Jane Doe","patientAge":50,"severity":"HIGH","emergencyType":"Stroke","latitude":28.58,"longitude":77.22,"locationAddress":"Connaught Place"}'

# 3. Allocate all pending emergencies
curl http://localhost:8080/allocate

# 4. Check all emergencies
curl http://localhost:8080/emergency
```
