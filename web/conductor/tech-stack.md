# Technology Stack

## Core Language
- **TypeScript:** Provides strict typing, reducing runtime errors and improving maintainability for complex dashboard data structures.

## Frontend Framework
- **Next.js (React):** A robust framework for building the user interface. It offers excellent performance, routing, and the ability to seamlessly integrate API routes if needed.

## UI & Styling
- **Tailwind CSS:** For rapid UI development with utility classes, ensuring a consistent and easily customizable design system.
- **shadcn/ui (or similar Radix-based components):** For accessible, high-quality, and data-dense UI components like data tables and complex forms.
- **Lucide React:** A library of beautiful, consistent icons used across the dashboard UI.

## State Management & Data Fetching
- **React Query (@tanstack/react-query):** Efficiently manages asynchronous data fetching, caching, and state synchronization, which is critical for real-time fraud data.

## Testing
- **Jest & React Testing Library:** For unit and integration testing of UI components.
- **Cypress or Playwright:** For end-to-end testing of critical investigative workflows.