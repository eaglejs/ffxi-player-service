# Project and Code Guidelines
Our project is a full-stack application with a clear separation between the frontend and backend. The AI assistant should follow established best practices and coding standards for each respective technology stack.

## Tech Stack
*   **Frontend**: Vue 3 (Composition API, <script setup lang="ts"> and <style scoped lang="scss"> syntax), TypeScript, Pinia for state management, Vue Router, and Bootstrap 5 for styling.
*   **Backend**: Dropwizard (Java, Maven), using Jersey for RESTful APIs, Hibernate for data access, and Liquibase for database migrations.
*   **Scripts**: Using Gradle for build automation and task management.
*   **Database**: MongoDB for storing registration and voting data.
*   **Testing**: Vitest Utils for frontend unit tests, Cypress for E2E tests, and JUnit/AssertJ/Mockito for backend tests.

## General Guidelines
*   Always use type hints/annotations in languages that support them (TypeScript, Java).
*   Ensure all code is well-documented using JSDoc/TSDoc for frontend and Javadoc for backend.
*   Follow RESTful API design principles.
*   Unit tests are required for all new core functionality.
*   Prioritize secure coding practices, including input validation and appropriate error handling.
*   Keep dependencies up to date and clean.
*   Update the README file in the root directoryto reflect the current state of the project, including setup instructions, usage guidelines, and any relevant documentation.
