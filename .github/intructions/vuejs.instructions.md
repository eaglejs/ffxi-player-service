---
applyTo: 'app/src/**/*.vue, app/src/**/*.js, app/src/**/*.ts'
---
# Vue 3 Frontend Guidelines
*   **Composition API**: Use the Composition API with `<script setup lang="ts">` and `<style scoped lang="scss">` syntax for all new components.
*   **Styling**: Use Bootstrap 5 classes and components for all styling needs. Avoid custom CSS unless absolutely necessary.
*   **State Management**: Use [Pinia](https://vuejs.org) for global state management. Define stores in `src/stores/`.
*   **Structure**: Organize components by feature/module.
*   **Accessibility**: Ensure all components follow WCAG AA standards, using semantic HTML and ARIA attributes.
*   **API Calls**: Use `axios` for all backend API interactions.
*   **Testing**: Write unit tests for all components using Jest and Vue Test Utils.
*   **Code Style**: Follow the official [Vue Style Guide](https://vuejs.org) and use ESLint/Prettier for consistency.
*   **README**: Update the README file to reflect the current state of the project, including setup instructions, usage guidelines, and any relevant documentation.
