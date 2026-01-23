
Original file line number	Diff line number	Diff line change
@@ -1,290 +1,23 @@
# Auto Trading Bot — Take-Home Task
A **simple, clean web application** that simulates an automated crypto trading bot.
All trades are **fully simulated** — **no real money** is used.
This project was built as a **take-home interview task**, focusing on clarity, correctness, and explainable logic rather than complexity.
---
## Table of Contents
* [Overview](#overview)
* [Tech Stack](#tech-stack)
* [Features](#features)
* [Trading Strategy](#trading-strategy)
* [Dashboard](#dashboard)
* [Project Structure](#project-structure)
* [Setup Instructions](#setup-instructions)
  * [Prerequisites](#prerequisites)
  * [Database Setup (MariaDB)](#database-setup-mariadb)
  * [Backend Setup](#backend-setup)
  * [Frontend Setup](#frontend-setup)
* [How to Run the Project](#how-to-run-the-project)
* [Testing](#testing)
* [Notes for Reviewers](#notes-for-reviewers)
---
## Overview
This repository contains a **minimal automated trading bot simulator**. The application supports two operating modes:
* **Training Mode** – Backtesting the trading strategy on historical market data
* **Trading Mode** – Live simulation using current market prices
The goal is to demonstrate:
* Clean backend architecture
* Simple and explainable trading logic
* Raw SQL usage (no ORM)
* A lightweight frontend dashboard for visualisation
---
## Tech Stack
| Layer       | Technology                           |
| ----------- | ------------------------------------ |
| Frontend    | HTML, CSS, JavaScript (React + Vite) |
| Backend     | Java 17+, Spring Boot                |
| Database    | MariaDB                              |
| Persistence | Raw JDBC (JdbcTemplate)              |
| Market Data | Binance Public REST API              |
| Testing     | JUnit 5                              |
---
## Features
* Training (backtest) and Trading (live simulation) modes
* Moving Average Crossover trading strategy
* Simulated BUY / SELL trades
* Account and portfolio tracking
* Trade history persistence
* Equity snapshots over time
* Interactive dashboard with charts
---
## Trading Strategy
The bot uses a **Moving Average Crossover** strategy:
* **Short Moving Average:** 5 periods
* **Long Moving Average:** 20 periods
### Rules
* **BUY** – when the short MA crosses *above* the long MA
* **SELL** – when the short MA crosses *below* the long MA
* **HOLD** – otherwise
Trade size is fixed (percentage of available balance).
All decisions are deterministic and easy to explain.
---
## Dashboard
The frontend dashboard provides:
* Price chart with BUY / SELL markers
* Trade history table with profit & loss
* Portfolio and balance overview
* Controls to:
  * Start / step simulation
  * Switch between Training and Trading modes
  * Reset the system
---
## Project Structure
```
trading_bot_212/
├── README.md
├── db/
│   ├── schema.sql
│   └── seed.sql
├── backend/
│   ├── src/
│   └── pom.xml
├── frontend/
│   ├── src/
│   └── package.json
```
---
## Setup Instructions
### Prerequisites
Make sure you have the following installed:
* **Java 17+**
* **Node.js 18+**
* **MariaDB**
* **Git**
---
## Database Setup (MariaDB)
1. Ensure MariaDB is running locally
2. Create the database:
```sql
CREATE DATABASE trading_bot_212;
```
3. From the **project root**, load the schema and seed data:
```bash
mysql -u root -p trading_bot_212 < db/schema.sql
mysql -u root -p trading_bot_212 < db/seed.sql
```
This creates:
* Accounts
* Portfolio tables
* Trade history tables
* Initial seed data
---
## Backend Setup
1. Navigate to the backend folder:
```bash
cd backend
```
2. Configure database credentials in:
```
backend/src/main/resources/application.properties
```
Database Setup (MariaDB)
Create a database: CREATE DATABASE trading_bot;
Update database credentials in: backend/src/main/resources/application.properties
Example:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/trading_bot_212
spring.datasource.url=jdbc:mariadb://localhost:3306/trading_bot
spring.datasource.username=root
spring.datasource.password=your_password
```
3. Run the backend using the Maven wrapper:
```bash
./mvnw spring-boot:run
```
Run the SQL schema (provided in the repo): backend/db/schema.sql

Backend – Run Instructions
From the backend folder:
cd backend
mvn spring-boot:run
The backend will start on:
```
http://localhost:8080
```

---
## Frontend Setup
1. Navigate to the frontend folder:
```bash
Frontend – Run Instructions
From the frontend folder:
cd frontend
```
2. Install dependencies:
```bash
npm install
```
3. Start the development server:
```bash
npm run dev
```
The frontend will be available at:
```
http://localhost:5173
```
---
## How to Run the Project
1. Start **MariaDB** and load the database
2. Start the **backend service**
3. Start the **frontend dev server**
4. Open the dashboard in your browser
5. Select **Training** or **Trading** mode and observe the bot behaviour
---
## Testing
Basic unit tests are included for the trading logic:
* **JUnit 5** is used
* Tests validate Moving Average crossover behaviour (BUY / SELL)
Run all tests from the backend directory:
```bash
./mvnw test
```
## Results
<img width="1196" height="293" alt="image" src="https://github.com/user-attachments/assets/61fe4d8d-732e-42aa-80d1-ffd531962334" />
---
## Notes for Reviewers
* All trades are simulated (no real funds)
* Raw SQL (JDBC) is used intentionally — no ORM
* Binance public REST API is used (no API key required)
* The project prioritises **clarity, simplicity, and correctness**
* Designed specifically as a **take-home interview task**
--
## ScreenShots 
 ## Trading mode 
<img width="930" height="533" alt="image" src="https://github.com/user-attachments/assets/f67b670f-30f2-4bb0-a79d-ab0310d780c9" />
<img width="1919" height="1057" alt="image" src="https://github.com/user-attachments/assets/66e8694c-2621-4b0d-b1c5-74e5a3334b7e" />
<img width="675" height="460" alt="Screenshot 2" src="https://github.com/user-attachments/assets/f2ec6ee3-0c4b-4ce3-a697-ccbdec74625a" />
## Training mode 
 # Start 
<img width="937" height="530" alt="image" src="https://github.com/user-attachments/assets/fc6390ae-0239-4a97-a78f-839e5ac188ec" />
# Finish 
<img width="1670" height="1066" alt="image" src="https://github.com/user-attachments/assets/dc26af90-1833-4ad5-9a14-a42b618484f3" />
<img width="1010" height="484" alt="image" src="https://github.com/user-attachments/assets/9754b6d2-df0e-486b-9cd9-e47cedc8cbd2" />
## Live Demo Videos: 
The first 8:25 of the video are for Trading mode then Training Mode is tested. I suggested watching on accelerated speed :).
https://youtu.be/eMxZGuw-gRw
Seperate video for Training mode. 
https://youtu.be/WYmNk7y5g3Q
The frontend will start on:
http://localhost:5173
