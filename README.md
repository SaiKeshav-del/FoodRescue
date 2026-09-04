# FoodRescue-1
# 🌱 FoodRescue — Surplus Food Rescue & Distribution Platform

> *"Rescue Food. Reduce Waste. Feed Communities."*

[![Java](https://img.shields.io/badge/Java-11%2B%20%2F%2017%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3.4-38B2AC?logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Production_Ready-brightgreen.svg)]()

**FoodRescue** is a full-stack platform designed to eliminate urban food waste by connecting restaurants, bakeries, supermarkets, and banquet halls with conscious citizens and hunger-relief NGOs in real time.

---

## 🌟 Key Features

### 🛒 Hyperlocal Marketplace
- **Search & Multi-Filters:** Real-time filtering by category (Bakery, Meals, Groceries, Desserts, Dairy), dietary preferences (100% Pure Veg, Non-Veg, Vegan, Jain), price range, and distance radius.
- **Expiring Soon Urgency Meters:** Live ticking countdown timers showing pickup window deadlines.
- **Interactive Vector Map:** Visual hotspot map with interactive price pins.

### 📲 Instant Digital QR Pickup Pass
- **Dynamic QR Generation:** Client-side vector matrix generation on HTML5 Canvas.
- **Verification System:** Store cashiers can scan or enter the 6-digit Reservation ID to mark food as collected and update inventory in real time.

### 🤝 ₹0 NGO Bulk Rescue Portal
- **Zero-Cost Donations:** Hotels and wedding banquet halls can list large catering surplus batches for verified NGOs.
- **Volunteer Logistics:** Direct assignment of volunteer pickup drivers to transport fresh food to local shelter homes.

### 📊 Real-Time Environmental Impact Engine
- Computes savings dynamically:
  - **$CO_2$ Emissions Diverted:** $\approx 1.8\text{ kg } CO_2$ per meal rescued.
  - **Freshwater Conserved:** $\approx 420\text{L}$ water footprint saved per meal.
  - **Citizen Financial Savings:** Avg 60%–75% off MRP.

### 👥 4 Role-Based Dashboards
1. **Citizen Portal:** Active pickup passes, past rescue history, eco-badges, and rewards points.
2. **Restaurant Manager Portal:** Surplus wizard, active inventory controls, QR scanner verifier, and revenue analytics.
3. **NGO Volunteer Portal:** Claim ₹0 bulk food, dispatch volunteers, and generate donor certificates.
4. **Admin Command Center:** Platform KPIs, business KYC approval queue, listing moderation, and live audit logs.

---

## 🛠️ Tech Stack

- **Backend:** Java (JDK 11+) utilizing built-in zero-dependency HTTP microserver, thread-safe concurrent storage (`ConcurrentHashMap`, `CopyOnWriteArrayList`), and REST APIs.
- **Frontend:** HTML5, Tailwind CSS, Lucide Icons, Canvas QR Generator, Canvas Confetti.
- **State Management:** Reactive client state with persistent local caching and real-time synchronization.

---

## 🚀 Quick Start (Run in 10 Seconds)

### Prerequisites
- Any standard Java version installed (Java 11, 17, or 21+).

### Step-by-Step Execution

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/food-rescue.git
   cd food-rescue
