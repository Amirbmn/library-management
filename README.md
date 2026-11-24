# Library Management System

Run:
1. `docker compose up -d`  (PostgreSQL on :5432, db `library`, user/pass `postgres`)
2. `cd backend && mvn spring-boot:run`  (API on :8080)
3. `cd frontend && npm install && npm run dev`  (UI on :5173)

Default admin: `admin` / `admin123`. Register new accounts for normal users.
Config (loan days, max loans, fine per day): `backend/src/main/resources/application.yml`.
