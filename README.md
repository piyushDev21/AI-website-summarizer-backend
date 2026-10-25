# AI Research Assistant

[![CI](https://github.com/piyushDev21/AI-website-summarizer-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/piyushDev21/AI-website-summarizer-backend/actions/workflows/ci.yml)

A Chrome extension that reads the page you are on and, in one click, gives you a summary, the key points, or related topics to research next. A React side panel talks to a Spring Boot API, which prompts Google Gemini.

```
┌──────────────────────────┐   page text    ┌───────────────────────┐   prompt    ┌─────────────┐
│ Chrome side panel        │ ─────────────▶ │ Spring Boot API       │ ──────────▶ │ Gemini API  │
│ React + TypeScript       │ ◀───────────── │ /api/research/process │ ◀────────── │             │
└──────────────────────────┘    Markdown    └───────────────────────┘             └─────────────┘
```

## Features

- **One click on any page.** Uses your highlighted text if there is any, otherwise the page's main content (`<article>`/`<main>`, falling back to the body).
- **Three modes:** a short summary, 5 to 8 key points, or related topics with a reason for each.
- **Research notes** that save automatically, with a button to add any result to your notes.
- **Safe rendering.** Gemini's Markdown is rendered as React elements, never injected as HTML.
- **Clear errors.** The API returns RFC 9457 problem details (missing API key, rate limits, blocked prompts, timeouts) and the panel shows them as they are.
- The API key is sent to Gemini in the `x-goog-api-key` header, not the URL, and CORS accepts only extension pages and localhost.

## Run it locally

You need Java 17+, Node 20+ and a [Gemini API key](https://aistudio.google.com/app/apikey).

**1. Start the API**

```bash
cd backend
export GEMINI_API_KEY=your-key        # PowerShell: $env:GEMINI_API_KEY="your-key"
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`. Optional settings: `GEMINI_MODEL` (default `gemini-2.5-flash`), `PORT`, and `CORS_ALLOWED_ORIGIN_PATTERNS`.

**2. Build and load the extension**

```bash
cd extension
npm install
npm run build
```

Open `chrome://extensions`, turn on **Developer mode**, click **Load unpacked** and choose `extension/dist`. Click the toolbar icon to open the side panel. If the API runs somewhere other than `localhost:8080`, change it under **Settings** in the panel.

## API

`POST /api/research/process`

```json
{ "content": "text from the page…", "operation": "summarize" }
```

`operation` is one of `summarize`, `key-points` or `suggest`. The response is:

```json
{ "operation": "summarize", "result": "Markdown text" }
```

## Project layout

```
backend/     Spring Boot 3.5 API (Java 17)
  research/  controller, request validation, prompt building
  gemini/    Gemini REST client and response parsing
extension/   Chrome MV3 extension (React 18, TypeScript, Vite)
  src/       side panel UI, page reading, API client, Markdown renderer
  public/    manifest, background service worker, icons
```

## Tests

```bash
cd backend && ./mvnw verify
cd extension && npm run build   # type-checks and bundles
```

Backend tests cover request validation, CORS, the Gemini request format (key in header, prompt in body), response parsing, blocked prompts, rate limiting and the missing-key error.
