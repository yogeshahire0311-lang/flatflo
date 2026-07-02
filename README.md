# FlatFlo

> **Find the best rental property across all platforms in one search.**

FlatFlo is a **meta-search engine for rental homes**. Instead of forcing you to open and re-search the same thing on MagicBricks, NoBroker, Housing.com and a dozen other apps, FlatFlo searches all of them at once, shows the results side by side, and sends you straight to the original listing when you find one you like.

Think of it as **Google Flights, but for rental flats.** We don't own the flights (listings) — we help you find and compare them, then hand you off to the airline (the source platform) to book.

---

## 🎯 What FlatFlo Is (and Isn't)

**FlatFlo IS:**
- A **discovery layer** — one search across many rental platforms.
- A **comparison layer** — see price, area, and source side by side.

**FlatFlo is NOT (at least not at first):**
- A place that hosts listings itself.
- A booking or payment platform.
- A broker.

We aggregate and redirect. The actual rental happens on the source platform. This keeps the product simple to launch and avoids the "chicken-and-egg" problem of needing our own inventory on day one.

---

## 😖 The Problem

Right now, someone hunting for a flat (say, a working professional looking in Goregaon East) has to:

- Open **3–5 different apps** — MagicBricks, NoBroker, Housing.com, and others.
- Run the **same search** on each one.
- Manually spot that the **same flat is listed on multiple apps** (duplicates everywhere).
- Try to remember which app had the cheaper price.

The result: it's **slow, repetitive, confusing, and there's no easy way to tell if a deal is actually good.**

---

## 🚀 The Solution — How It Works

A simple three-step flow:

1. **You search once.** Enter your location, budget, and BHK (number of bedrooms).
2. **FlatFlo shows everything.** Listings from all platforms in one list, with prices compared and each result tagged with where it came from.
3. **You click through.** Pick a listing → we send you (deep-link) straight to it on the original platform to continue.

---

## 🧩 Core Features (MVP)

| Feature | What it does |
|---------|--------------|
| **🔍 Unified Search** | One search box. Filter by location, budget, BHK, and furnishing. |
| **📋 Aggregated Results** | Each listing shows: title, price, area, thumbnail, and a **source badge** (which platform it's from). |
| **🔗 Source Redirection** | Click a listing → deep-linked to the original platform to view/contact. |
| **🧬 Basic Duplicate Detection** | The same flat listed on multiple platforms gets **grouped together** instead of shown 3 times. |
| **🗺️ Map View** | See all listings as pins on a map, colour-coded by platform — great for judging location at a glance. |

---

## 🧠 What Makes FlatFlo Different (Our USP)

Anyone can list flats. FlatFlo's edge is the **smart layer** on top (planned for Phase 2):

- **🏷️ "Best Deal" tag** — automatically flag listings that are priced well for their area and size.
- **⚖️ Price comparison** — when the same flat appears on multiple platforms, show which one is cheapest.
- **📊 Area insights** — average rent for a locality, and demand trends over time.

This is the part competitors *don't* do well, and it's what turns FlatFlo from "just a search tool" into "the app that tells you if you're getting a good deal."

---

## 👥 Who It's For

**Primary users (launch focus):**
- Working professionals searching in a specific area (e.g., Goregaon / Malad).
- Families relocating to a new city or neighbourhood.
- Anyone tired of juggling multiple rental apps.

**Secondary users (future phases):**
- Brokers.
- Property owners.

---

## 🗺️ Roadmap

| Phase | Focus |
|-------|-------|
| **Phase 1 — MVP** | Unified search, aggregated results, source redirection, basic duplicate detection, map view. |
| **Phase 2 — Smart Layer** | "Best Deal" tagging, cross-platform price comparison, area insights (avg rent, demand trends). |
| **Phase 3 — Two-sided (future)** | Features for brokers and property owners; possibly hosting listings directly. |

---

## 🛠️ Tech Stack

This is a monorepo with a Java backend and an Angular frontend.

| Layer | Technology |
|-------|------------|
| **Backend** | Java 21, Spring Boot 4.1.0 (Gradle), Lombok |
| **Frontend** | Angular 20 (TypeScript, RxJS) |
| **Spec/Planning** | [Spec Kit](https://github.com/github/spec-kit) (see `.specify/` and `specs/`) |

### Repository Layout

```
source_code/
├── java/flatflo/          # Spring Boot backend (search API, aggregation, listing model)
│   └── src/main/java/com/flatflow/
│       ├── listing/       # Listing domain model (BHK, area, furnishing, status…)
│       └── scrapper/      # Platform scraping / research
├── angular/               # Angular frontend (search UI, results, map view)
├── specs/                 # Feature specifications (Spec Kit)
│   └── 001-listing-aggregator/
└── .specify/              # Spec Kit configuration
```

---

## ▶️ Getting Started

### Backend (Spring Boot)

```bash
cd java/flatflo
./gradlew bootRun        # starts the API server
```

### Frontend (Angular)

```bash
cd angular
npm install
npm start                # runs on http://localhost:4200
```

---

## ❓ Gaps & Open Questions

> **These are decisions we still need to make — flagged so nothing gets silently assumed.** They're organised roughly by how much they affect the product.

### 🔴 Scope & Legal (decide first — these shape everything)

1. **How do we get listings from other platforms?** Scraping? Official APIs? Partnerships? Each has very different legal, technical, and reliability implications. The `scrapper/` folder suggests scraping — but scraping MagicBricks/NoBroker/Housing.com may violate their terms of service. **This is the single biggest risk to the whole business.**
2. **Which platforms do we launch with?** All of MagicBricks + NoBroker + Housing.com, or start with one to prove the concept?
3. **What geography do we cover at launch?** The examples point to **Mumbai (Goregaon/Malad/Thane)**. Is MVP Mumbai-only, or wider?
4. **Rentals only, or also buy/sell?** The vision says rentals — confirming keeps scope tight.

### 🟠 Data & Freshness

5. **How fresh is the data?** Do we scrape live on each search (slow, always current) or cache listings and refresh periodically (fast, but can go stale — showing rented flats)? This is a core UX trade-off.
6. **How do we handle expired/rented listings?** Nothing frustrates users more than clicking a "great deal" that's already gone.
7. **How reliable is duplicate detection?** Matching "the same flat" across platforms is genuinely hard (different photos, slightly different addresses, missing data). What's our matching strategy, and what happens when we get it wrong?

### 🟡 Product & UX

8. **Do users have accounts?** Needed for saved searches, favourites, and alerts — but adds complexity. Is MVP anonymous?
9. **Alerts/notifications?** "Tell me when a new 2BHK under ₹40k appears in Goregaon" is a killer feature — is it MVP or later?
10. **What does "Best Deal" actually mean?** We need a concrete definition (e.g., price vs. area average) before Phase 2 — otherwise it's just a vague badge.

### 🟢 Business Model (not urgent, but eventually)

11. **How does FlatFlo make money?** If we redirect users away to other platforms, where's the revenue? Options: affiliate/referral fees, ads, premium insights, lead-gen to brokers. Worth noting even if it's out of MVP scope.

---

## 📄 License

_TBD._
