# FFXI Player Service - Java Service

A Dropwizard-based Java service for managing FFXI player data with WebSocket support and MongoDB integration. This service acts as the backend for tracking real-time player statistics, inventory, and chat logs.

## Prerequisites

- **Java 21** (required for building with Gradle)
- **Java 25** (runtime - the application runs on Java 25)
- **MongoDB** (running on localhost:27017 by default)

**Note**: Due to Gradle 8.14 bytecode parsing limitations, builds must be executed with Java 21 as JAVA_HOME, even though the application itself is compiled for and runs on Java 25. Set JAVA_HOME to Java 21 before running Gradle commands:
```bash
export JAVA_HOME=/path/to/java21
./gradlew clean build
```

## Project Structure

```
service/
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
  - `POST /player/set_inventory` (full per-character inventory snapshot, see below)
  - `GET /player/inventory/characters`
  - `GET /player/{playerId}/inventory`
  - `GET /player/inventory/search` (find item ids across characters)
  - `GET /player/{playerId}/inventory/search` (same, scoped to one character)
- **Chat & History**:
  - `GET /player/get_chat_log`
  - `GET /player/get_chat_log_by_type`
  - `POST /player/set_messages`
  - `POST /player/set_exp_history`
  - `POST /player/reset_exp_history`

### Inventory (`/player`)

`POST /player/set_inventory` stores one document per `playerId` in the `inventories` collection (unique index on `playerId`, index on `playerNameLower`). The snapshot **replaces** the stored one, so removed items disappear.

```json
{
  "playerId": 1,
  "playerName": "Sungjinwoo",
  "contentHash": "<sha256 hex>",
  "timestamp": 1761350000,
  "gil": 12345,
  "keyItems": [1, 2, 3],
  "bags": {
    "inventory": [{ "id": 4096, "count": 12 }],
    "wardrobe": [{ "id": 12345, "count": 1 }],
    "slip 01": [{ "id": 27000, "count": 1 }]
  }
}
```

- `playerId`, `playerName`, `contentHash` are required; `gil >= 0`; item `count >= 1`.
- Bag names are opaque keys (new bags/slips are accepted). Names containing `.` or starting with `$` are rejected.
- Responses (text/plain): `200 Inventory: OK` (saved or same `contentHash`, no write), `200 Inventory: STALE` (`timestamp` older than the stored one, ignored), `400` listing every invalid field, `413` for bodies over 1 MB, `500` on database errors. Safe to retry with the same body.
- Characters with no recorded `playerId` (never logged in since the Python service learned ids) are skipped client-side until they log in.

`GET /player/inventory/characters` lists every character with a stored inventory (online or not), sorted by name: `[{"playerId","playerName","gil","updatedAt"}]`.

`GET /player/{playerId}/inventory` returns the stored snapshot (404 if none).

`GET /player/inventory/search` finds item ids across stored snapshots:

| Param | Description |
|-------|-------------|
| `item` | Item id; repeatable and/or comma-separated. Required. |
| `player` | Optional playerId or case-insensitive playerName. 404 if unknown. |
| `bag` | Optional bag filter, repeatable (e.g. `bag=wardrobe&bag=satchel`). |
| `includeKeyItems` | `true` also matches key item ids (reported as bag `keyItems`). Default `false`. |
| `limit` / `offset` | Pagination over players. Default 100, max 500 / 0. |
| `name` | Not supported (400): the backend only stores ids, so resolve names to ids client-side. |

```
GET /api/player/inventory/search?item=4096&item=77&player=sungjinwoo
```
```json
{
  "query": { "item": [4096, 77], "player": "sungjinwoo", "bag": [], "includeKeyItems": false, "limit": 100, "offset": 0 },
  "totalMatches": 1,
  "results": [
    {
      "playerId": 1,
      "playerName": "Sungjinwoo",
      "totalCount": 13,
      "locations": [
        { "bag": "inventory", "itemId": 4096, "count": 12 },
        { "bag": "slip 01", "itemId": 77, "count": 1 }
      ],
      "updatedAt": "2026-10-25T03:56:03.094Z"
    }
  ]
}
```

`itemId` appears on each location only when more than one item id is searched. Results are sorted by `totalCount` desc, then `playerName`; players with no matches are omitted.

### Item Icons (`/items`)

A permanent, write-once store of 32x32 item icons, one `<itemId>.png` per item in `itemIconDir` (config key; defaults to `~/ffxi-data/item-icons`). Keep it outside the build directory so redeploys never wipe it, and back it up with your data. Icons are extracted from the game's own DAT files on the PC running the PlayerService and uploaded here once; nothing is ever overwritten.

- `GET /items/icon/{id}.png`: the icon (`Cache-Control: max-age=31536000, immutable`), or 404 if none is stored yet.
- `PUT /items/icon/{id}.png` (`Content-Type: image/png`): stores the icon. `201 Icon: STORED`, `200 Icon: EXISTS` (unchanged), `400` if the id is outside 0-65535 or the body is not a 32x32 PNG, `413` over 64 KB.
- `POST /items/icons/missing` with `{"ids":[4096,5,6]}`: returns `{"missing":[5,6]}`, the ids with no stored icon (max 5000 ids per call).

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

### Create Executable Fat JAR (Recommended)
Build a self-contained executable JAR with all dependencies bundled (shadowJar):
```bash
./gradlew clean shadowJar
```

The fat JAR file will be created at: `build/libs/ffxi-player-service-x.x.x.jar` (~26 MB)

This JAR includes all dependencies (Dropwizard, MongoDB driver, etc.) and can be run directly:
```bash
java -jar build/libs/ffxi-player-service-x.x.x.jar server src/main/resources/config.yml
```

### Create Standard JAR (Dependencies Required)
```bash
./gradlew clean jar
```

The standard JAR file will be created at: `build/libs/ffxi-player-service-x.x.x.jar`
**Note**: This JAR requires dependencies to be available on the classpath.

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

**Option 2: Direct JAR Execution**
Run the application directly with the fat JAR:
```bash
./gradlew clean shadowJar
java -jar build/libs/ffxi-player-service-x.x.x.jar server src/main/resources/config.yml
```

### Production Mode

1. **Build the production fat JAR:**
   ```bash
   ./gradlew clean shadowJar
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
Check your Java versions:
```bash
java -version      # Should be Java 25 for running the application
echo $JAVA_HOME    # Should point to Java 21 for building with Gradle
```

**Building**: Requires JAVA_HOME to be set to Java 21 due to Gradle 8.14 limitations with Java 25 bytecode parsing.
**Running**: The application itself requires Java 25 runtime.

If builds fail with "Unsupported class file major version 69", set JAVA_HOME to Java 21:
```bash
export JAVA_HOME=/path/to/java21
./gradlew clean shadowJar
```

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
