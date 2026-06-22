# Roadmap: Audiobook Generation from EPUB via Gemini AI

This roadmap outlines the steps to implement auto-builds in CI and a feature to generate full-cast audiobooks from EPUB files using the Gemini AI API.

## Phase 1: CI/CD & Infrastructure
- [x] **Auto-build APK in CI**: Modified `.github/workflows/ci.yml` to build and upload debug APKs on pushes to the `develop` branch.
- [x] **EPUB Library Integration**: Integrate `epublib` and create `:core:epub` for text extraction.
- [x] **Gemini API Client**: Create `:core:gemini` with a Retrofit client supporting 529 error retries and `Retry-After` headers.
- [ ] **Settings & Persistence**: Add DataStore persistence and UI for Gemini API Key and model selection.

## Phase 2: Background Processing & WorkManager
- [ ] **Setup WorkManager**: Integrate `androidx.work:work-runtime-ktx` and configure it within the app's DI.
- [ ] **Analysis Worker**: Implement a resumable background worker that:
    - Extracts text from EPUB chapters.
    - Sends text to Gemini for character identification (name, gender, age, energy/personality).
    - Persists character data and default generation settings in a Room database.
- [ ] **Generation Worker**: Implement a resumable background worker that:
    - Maps characters to specific Gemini pre-built voices.
    - Applies "tuning" (via prompts or configuration) to match character traits.
    - Handles chunked TTS generation to avoid API limits.
    - Manages audio file storage and assembly.

## Phase 3: Character & Voice Intelligence
- [ ] **Character Extraction Prompting**: Refine prompts to get structured JSON from Gemini with character metadata.
- [ ] **Voice Mapping Logic**: Implement an intelligent mapping system:
    - Assign pre-built voices (Kore, Aoide, etc.) based on gender/age.
    - Implement "tuning" logic to vary speed, pitch, or energy for reused voices.
- [ ] **Generation Settings Defaults**: Extend analysis output so it pre-populates reasonable defaults for generation settings such as character voice assignments, custom word pronunciations, and other tuning controls before generation starts.
- [ ] **Database Schema**: Design Room tables for `Character`, `VoiceMapping`, and `GenerationProgress`.

## Phase 4: UI & Library Integration
- [ ] **EPUB Import Flow**: Add an "Import EPUB" button to the library.
- [ ] **Generation Settings UI**: Add a screen where users can review and tune generation settings before starting generation, including voice assignments, pronunciation overrides, and similar per-book controls.
- [ ] **Generation Locking & Restart UX**: Disable generation settings once generation has started, and provide a clear way to discard in-progress generation and restart if the user wants to change locked settings.
- [ ] **Progress Tracking UI**: Create a screen to monitor analysis and generation status (resumable).
- [ ] **Library Registration**: Automatically register the final generated audio files as a playable Audiobook in the existing library.
- [ ] **Error Handling UI**: Notify users of persistent API failures or missing API keys.

## Phase 5: Final Polish & Testing
- [ ] **Edge Case Testing**: Long books, books with many characters, network interruptions.
- [ ] **API Limit Optimization**: Implement intelligent chunking and caching to minimize API costs/latency.
- [ ] **Pre-commit Checks**: Ensure all tests pass and code matches the project architecture.
