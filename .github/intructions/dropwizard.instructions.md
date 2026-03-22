---
applyTo: 'service/**/*.java, service/**/*.xml'
---
# Dropwizard Backend Guidelines
*   **Java Version**: Use Java 25 or later for all backend development.
*   **REST Resources**: Place all Jersey resources in the `com.example.resources` package.
*   **Configuration**: All configuration should be managed via the `config.yml` file and accessed through a `Configuration` class. Include the MongoDB connection URI and database name as configuration properties.
*   **Build Tool**: Use Gradle for build automation. Define tasks for building, testing, and running the application.
*   **All tasks**: build, test, jar, run should successfully execute without errors after each feature.
*   **Database Access**: Use the [MongoDB Java Driver](https://www.mongodb.com/docs/drivers/java/sync/current/) for data persistence. Encapsulate all database operations in repository classes placed in the `com.example.repositories` package. Inject `MongoDatabase` via the Dropwizard managed lifecycle.
*   **Managed MongoDB Client**: Wrap `MongoClient` in a Dropwizard `Managed` object to ensure proper startup and shutdown handling.
*   **Testing**: Write unit tests using JUnit, AssertJ, and Mockito. Use an embedded MongoDB instance (e.g., [Flapdoodle](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo)) for integration tests.
*   **Metrics/Health Checks**: Utilize Dropwizard's built-in [Metrics](https://dropwizard.io) and [Health Checks](https://dropwizard.io) functionality where appropriate. Implement a MongoDB health check by running a `ping` command against the database.
*   **Error Handling**: Implement global exception mappers for consistent API error responses.
*   **Postman**: Update the Postman collection with all new endpoints and ensure it is well-documented for ease of testing and integration.
*   **README**: Update the README file to reflect the current state of the project, including setup instructions, usage guidelines, and any relevant documentation.