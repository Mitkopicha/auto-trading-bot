<h1 align="center">Auto Trading Bot — Take-Home Task</h1>

<p align="center">A simple web application that simulates an automated crypto trading bot. All trades are simulated — no real money is used.</p>

---

## Table of contents

- [Overview](#overview)
- [Tech stack](#tech-stack)
- [Features](#features)
- [Dashboard](#dashboard)
- [Project structure](#project-structure)
- [Setup](#setup)
  - [Database (MariaDB)](#database-mariadb)
  - [Backend](#backend)
  - [Frontend](#frontend)
- [How to run](#how-to-run)
- [Notes](#notes)

---

## Overview

This repository contains a minimal web application that demonstrates an automated crypto trading bot. It supports two operating modes:

- Training mode — backtesting on historical price data
- Trading mode — live simulation using current market prices

All activity is simulated; no real funds are used.

## Tech stack

| Layer     | Technology                      |
|----------:|:--------------------------------|
| Frontend  | : HTML, CSS, JavaScript(React)  |
| Backend   | Java, Spring Boot (raw JDBC)    |
| Database  | MariaDB                         |
| Market    | Binance public REST API         |

## Features

- Training (backtest) and Trading (live simulation) modes
- Automated trading logic based on market indicators
- Tracking of:
  - Account balance
  - Portfolio holdings
  - Trade history
  - Equity over time

## Dashboard

- Price chart with BUY / SELL markers
- Trade history table with profit & loss (PnL)
- Portfolio summary
- Controls to start, pause, reset, and switch modes

## Project structure

```
trading_bot_212/
├── README.md
├── db/
│   ├── schema.sql
│   └── seed.sql
├── backend/
└── frontend/
```

## Setup

### Database (MariaDB)

1. Ensure MariaDB is running locally.
2. Create the database:

```sql
CREATE DATABASE trading_bot_212;
```

3. From the project root, load schema and seed data:

```bash
mysql -u root -p trading_bot_212 < db/schema.sql
mysql -u root -p trading_bot_212 < db/seed.sql
```

### Backend

1. Edit database credentials in `backend/src/main/resources/application.properties`.

Example:

```
spring.datasource.url=jdbc:mariadb://localhost:3306/trading_bot_212
spring.datasource.username=root
spring.datasource.password=your_password
```

2. Run the backend:

```bash
cd backend
./mvnw spring-boot:run
```

The backend will be available at:

http://localhost:8080

### Frontend

1. Install dependencies and start the dev server:

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at:

http://localhost:5173

## How to run

1. Start MariaDB and load the database.
2. Start the backend service.
3. Start the frontend dev server.
4. Open the dashboard in your browser and select Training or Trading mode.

## Notes

- The backend uses raw SQL (JDBC) and does not use an ORM.
- Binance public REST API is used for market data and does not require an API key for public endpoints.
- The project is designed for clarity and simplicity as a take-home task.

## ScreenShots 
Trading mode 
<img width="1919" height="1057" alt="image" src="https://github.com/user-attachments/assets/66e8694c-2621-4b0d-b1c5-74e5a3334b7e" />
<img width="675" height="460" alt="Screenshot 2" src="https://github.com/user-attachments/assets/f2ec6ee3-0c4b-4ce3-a697-ccbdec74625a" />


Training mode 
<img width="1873" height="1031" alt="image" src="https://github.com/user-attachments/assets/8f3c6940-6da0-4f66-8bbe-a3d835bc32a7" />
<img width="1887" height="1066" alt="image" src="https://github.com/user-attachments/assets/b333fa85-69f4-42dc-a76b-7d296fc75dc5" />


