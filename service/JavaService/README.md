# FFXI Player Service - Java Service

A Dropwizard-based Java service for managing FFXI player data with WebSocket support and MongoDB integration. This service acts as the backend for tracking real-time player statistics, inventory, and chat logs.

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
├── build.gradle                     # Gradle build configuration
├── gradlew                          # Gradle wrapper script (Unix/Mac)
├── gradlew.bat                      # Gradle wrapper script (Windows)
└── README.md                        # This file
```

## Features

- **REST API**: Comprehensive endpoints for updating and retrieving player state (stats, jobs, currency, etc.).
- **Real-time Updates**: WebSocket support for broadcasting player changes to connected clients.
- **MongoDB Persistence**: Stores player data, chat logs, and history.
- **UI Integration**: Automatically fetches and serves the frontend UI from a Nexus repository.
- **Swagger/OpenAPI**: Auto-generated API documentation.

## API Endpoints

The service exposes the following main resources via REST:

### Player Management (`/player`)
Used primarily for data ingestion (setting state) and specific data retrieval.

- **Initialization**: `POST /player/initialize_player`
- **Status Updates**:
  - `POST /player/set_online`
  - `POST /player/set_status` (Engagement status)
  - `POST /player/set_zone`
- **Stats & Attributes**:
  - `POST /player/set_stats` (STR, DEX, Attack, Defense, etc.)
  - `POST /player/set_hpp` / `POST /player/set_mpp` / `POST /player/set_tp`
  - `POST /player/set_jobs` (Main/Sub levels)
  - `POST /player/set_merits`
  - `POST /player/set_capacity_points` (Job Points)
- **Inventory & Buffs**:
  - `POST /player/set_gil`
  - `POST /player/set_currency1` / `POST /player/set_currency2`
  - `GET /player/get_buffs`
  - `POST /player/set_buffs`
  - `POST /player/refresh_buffs` (Clear buffs)
- **Chat & History**:
  - `GET /player/get_chat_log`
  - `GET /player/get_chat_log_by_type`
  - `POST /player/set_messages`
  - `POST /player/set_exp_history`
  - `POST /player/reset_exp_history`

### Player Lists (`/players`)
- `GET /players/get_players`: List all online players (active in last 60s).
- `GET /players/get_player?playerId={id}`: Retrieve full details for a specific player.

## WebSocket Protocol

**Endpoint**: `ws://localhost/ws/players` (or configured port)

Clients can connect to receive real-time updates. By default, clients receive all broadcasts. To filter updates, clients can send JSON subscription messages.

### Client-to-Server Messages
| Action | Payload | Description |
|--------|---------|-------------|
| `subscribe` | `{"action": "subscribe", "playerId": 123}` | Subscribe to updates for a specific player ID. |
| `unsubscribe` | `{"action": "unsubscribe", "playerId": 123}` | Unsubscribe from a specific player ID. |
| `unsubscribe_all` | `{"action": "unsubscribe_all"}` | Clear all subscriptions (reverts to receiving all broadcasts?). *Note: Implementation clears list, behavior depends on logic.* |
| `ping` | `{"action": "ping"}` | Heartbeat check. Server responds with `pong`. |

### Server-to-Client Messages
- **Updates**: JSON objects containing the updated fields (e.g., `{"playerId": 123, "hpp": 85}`).
- **Heartbeat**: Server sends `ping` frames or text messages to keep connections alive.

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

The JAR file will be created at: `build/libs/ffxi-player-service-x.x.x.jar`

## UI Integration

The build process automatically attempts to fetch and integrate the UI from a Nexus npm repository.

### Configuration
UI package settings can be configured in `gradle.properties` or passed as command line arguments:
- `uiPackageName` (default: `ffxi-stats`)
- `uiPackageVersion`
- `nexusHost` / `nexusPort` / `nexusNpmRepo`

### Manual UI Tasks
```bash
# Run just the UI download and extract tasks
./gradlew downloadUI extractUI
```

## Running the Application

### Development Mode

**Option 1: Standard Run**
Run directly with Gradle.
```bash
./gradlew runDevWatch --args="server src/main/resources/config.yml"
```
*Connect debugger to port 5005.*

**Option 2: Watch Mode (Auto-Reload)**
Watches for changes in `src/main/java` and `src/main/resources` and automatically restarts the server.
```bash
./gradlew clean jar
java -jar build/libs/ffxi-player-service-x.x.x.jar server src/main/resources/config.yml
```

### Production Mode

1. **Build the production JAR:**
   ```bash
   ./gradlew clean jar
   ```

2. **Run:**
   ```bash
   java -jar build/libs/ffxi-player-service-x.x.x.jar server src/main/resources/config.yml
   ```

3. **Run as a background service (production):**
   ```bash
   nohup java -jar build/libs/ffxi-player-service-x.x.x.jar server src/main/resources/config.yml > application.log 2>&1 &
   ```

4. **Check if it's running:**
   ```bash
   ps aux | grep ffxi-player-service
   ```

5. **Stop the service:**
   ```bash
   # Find the process ID (PID) first
   ps aux | grep ffxi-player-service
   # Then kill the process
   kill <PID>
   ```

## Configuration

Edit `src/main/resources/config.yml` to configure:

- **Server ports**: Application (default 80/8080) and Admin (8081).
- **MongoDB URI**: `mongodb://localhost:27017` (default).
- **CORS Settings**: Allowed origins, headers, and methods.

## Endpoints Summary

Once running locally:

- **UI (Root):** http://localhost:8080/
- **API Health Check:** http://localhost:8080/api/health
- **API OpenAPI Spec:** http://localhost:8080/api/openapi.json
- **WebSocket:** ws://localhost:8080/ws/players
- **Admin Interface:** http://localhost:8081/

## Troubleshooting

### Port Already in Use
If you see "Address already in use", check if another instance or service is using port 80, 8080, or 8081.
Modify `config.yml` or the `runDev` task configuration in `build.gradle` to change ports.

### MongoDB Connection
Ensure MongoDB is running locally. The service expects a standard connection at `mongodb://localhost:27017`.

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
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar build/libs/ffxi-player-service-x.x.x.jar server src/main/resources/config.yml
```
Then attach your debugger to port 5005.
