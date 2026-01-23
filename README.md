Database Setup (MariaDB)
Create a database: CREATE DATABASE trading_bot;
Update database credentials in: backend/src/main/resources/application.properties
Example:
spring.datasource.url=jdbc:mariadb://localhost:3306/trading_bot
spring.datasource.username=root
spring.datasource.password=your_password
Run the SQL schema (provided in the repo): backend/db/schema.sql

Backend – Run Instructions
From the backend folder:
cd backend
mvn spring-boot:run
The backend will start on:
http://localhost:8080

Frontend – Run Instructions
From the frontend folder:
cd frontend
npm install
npm run dev
The frontend will start on:
http://localhost:5173