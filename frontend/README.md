E-Healthcare Frontend (Vite + React + Tailwind)

Quick start

1. Install dependencies

```bash
cd ehealth-frontend
npm install
```

2. Start dev server

```bash
npm run dev
```

3. Environment

Create a `.env` or set env vars as needed. Example `.env` in the frontend root:

```
VITE_API_URL=http://localhost:8086/api
```

Notes

- Login page calls `POST /api/auth/login` and saves `token` to localStorage.
- Use the seeded admin: `admin@ehealth.com` / `admin123` (change in backend)
- This scaffold includes core components, dashboard, and routing. Continue by wiring list endpoints and CRUD pages.
