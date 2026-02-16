# FFXI Player Service - Java Service

A Dropwizard-based Java service for managing FFXI player data with WebSocket support and MongoDB integration.

## Prerequisites

- Java 21 or higher
- MongoDB (running on localhost:27017 by default)

## Project Structure

```
JavaService/
├── src/
│   ├── main/
│   │   ├── java/io/eaglejs/ffxi/    # Java source files
│   │   └── resources/
│   │       └── config.yml           # Configuration file
│   └── test/
│       └── java/io/eaglejs/ffxi/    # Test files
├── build.gradle                      # Gradle build configuration
├── gradlew                          # Gradle wrapper script (Unix/Mac)
├── gradlew.bat                      # Gradle wrapper script (Windows)
└── README.md                        # This file
```

## Building the Application

### Clean Build
```bash
./gradlew clean build
```

### Build without Tests
```bash
./gradlew clean build -x test
```

### Run Tests Only
```bash
./gradlew test
```

### Create Executable JAR
```bash
./gradlew clean jar
```

The JAR file will be created at: `build/libs/JavaService.jar`

## UI Integration

The build process automatically attempts to fetch and integrate the UI from a Nexus npm repository.

### Configuration

UI package settings can be configured in `build.gradle`:

```gradle
ext {
    uiPackageName = 'ffxi-stats'
    uiPackageVersion = '1.0.0'
    nexusNpmUrl = 'http://eaglejs-mac-mini.local:8091/repository/npm-hosted'
}
```

### Build Process

During the build:

1. **Download Task** (`downloadUI`): Attempts to download `ffxi-stats` npm package (.tgz) from the Nexus repository
2. **Extract Task** (`extractUI`): Extracts the npm package and copies UI files to `build/resources/main/assets/`
3. **Fallback** (`createPlaceholderUI`): Creates a placeholder UI if the download fails

### Manual Testing

To manually test UI integration when Nexus is available:

```bash
# Clean build with UI download attempt
./gradlew clean build

# Check extracted UI files
ls -la build/resources/main/assets/

# Run just the UI tasks
./gradlew downloadUI extractUI
```

### Accessing the UI

Once the application is running, access the UI at:
- http://localhost:8080/

The UI will either be the downloaded package from Nexus or a placeholder page showing service status and available endpoints.

## Running the Application

### Development Mode

Run directly with Gradle:
```bash
./gradlew run --args="server src/main/resources/config.yml"
```

Or build and run the JAR:
```bash
./gradlew clean jar
java -jar build/libs/JavaService.jar server src/main/resources/config.yml
```

### Production Mode

1. **Build the production JAR:**
   ```bash
   ./gradlew clean jar
   ```

2. **Run with production configuration:**
   ```bash
   java -jar build/libs/JavaService.jar server src/main/resources/config.yml
   ```

3. **Run as a background service (production):**
   ```bash
   nohup java -jar build/libs/JavaService.jar server src/main/resources/config.yml > application.log 2>&1 &
   ```

4. **Check if it's running:**
   ```bash
   ps aux | grep JavaService
   ```

5. **Stop the service:**
   ```bash
   # Find the process ID (PID) first
   ps aux | grep JavaService
   # Then kill the process
   kill <PID>
   ```

## Configuration

Edit `src/main/resources/config.yml` to configure:

- **Server ports:**
  - Application: 8080 (default)
  - Admin: 8081 (default)
- **MongoDB URI:** `mongodb://localhost:27017` (default)

Example configuration:
```yaml
server:
  applicationConnectors:
    - type: http
      port: 8080
  adminConnectors:
    - type: http
      port: 8081

mongoUri: mongodb://localhost:27017
```

## Endpoints

Once running, the following endpoints are available:

- **Application:** http://localhost:8080
- **Health Check:** http://localhost:8081/healthcheck
- **Admin UI:** http://localhost:8081

## Development Tips

### Watch for Changes and Auto-Rebuild
```bash
./gradlew build --continuous
```

### View All Available Tasks
```bash
./gradlew tasks
```

### Clean Generated Files
```bash
./gradlew clean
```

### Check Dependencies
```bash
./gradlew dependencies
```

## Troubleshooting

### Port Already in Use
If you see "Address already in use" error, either:
1. Stop the process using that port
2. Change the port in `config.yml`

### MongoDB Connection Issues
- Ensure MongoDB is running: `brew services list` or `systemctl status mongod`
- Start MongoDB if needed: `brew services start mongodb-community` (Mac)
- Verify connection: `mongosh mongodb://localhost:27017`

### Java Version Issues
Check your Java version:
```bash
java -version
```
Should show Java 21 or higher. If not, install or switch to a compatible version.

## Key Dependencies

- **Dropwizard 2.0.24** - REST framework
- **MongoDB Driver 4.3.4** - Database connectivity
- **Jetty WebSocket** - WebSocket support
- **Swagger 2.1.11** - API documentation
- **JUnit 4.13.2** - Testing framework

## Additional Commands

### Generate IDE Project Files
For IntelliJ IDEA:
```bash
./gradlew idea
```

For Eclipse:
```bash
./gradlew eclipse
```

### Run with Debug
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar build/libs/JavaService.jar server src/main/resources/config.yml
```
Then attach your debugger to port 5005.
