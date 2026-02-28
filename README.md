# FFXI Stats

A comprehensive real-time player statistics and monitoring service for Final Fantasy XI, featuring live data collection from the game client and a web-based dashboard for viewing player information, experience tracking, buff monitoring, and statistical analysis.

## 📋 Overview

FFXI Stats is a multi-component system that bridges the gap between the FFXI game client and a modern web interface. It consists of:

- **Windower Lua Addon**: Runs inside FFXI to capture real-time player data
- **Java Backend API**: Receives and stores player data with WebSocket support for live updates (built with Dropwizard)
- **Vue.js Web Dashboard**: Interactive frontend for viewing statistics, charts, and player information
- **MongoDB Database**: Persistent storage for player data and chat logs
- **PlayerServiceEmulator**: Python-based emulator that simulates player stat updates without needing a running FFXI client

## 🏗️ Architecture

```
┌─────────────────┐
│  FFXI Client    │
│  (Windower)     │
│                 │
│  PlayerService  │◄─── Lua Addon
│  .lua           │
└────────┬────────┘
         │ HTTP POST
         ▼
┌─────────────────┐        ┌──────────────────────┐
│  Java API       │        │  PlayerServiceEmulator│
│  (Dropwizard)   │◄───────│  (Python)             │
│  Port 8080      │        │  Simulates player     │
│  WS Port 8081   │        │  stat updates for     │
└────────┬────────┘        │  dev / testing        │
         │                 └──────────────────────┘
         ▼
┌─────────────────┐     ┌─────────────────┐
│   MongoDB       │     │   Vue.js App    │
│   Database      │     │   (Vite)        │
└─────────────────┘     └─────────────────┘
                              ▲
                              │
                        Nginx Reverse Proxy
                        (ffxi.eaglejs.io)
```

## 🚀 Features

### Player Monitoring
- **Real-time Stats**: HP, MP, TP, and all combat statistics
- **Character Information**: Job, level, nation, and player details
- **Resistances**: Element and status effect resistance tracking
- **Currencies**: Gil, conquest points, and other in-game currencies
- **Experience Tracking**: Experience points, merit points, and capacity points with progress visualization

### Buff & Status Effects
- **Active Buffs**: Real-time display of all active buffs and debuffs
- **Buff Icons**: Visual representation with tooltips
- **Duration Tracking**: Time remaining on temporary effects

### Chat & Logs
- **Chat Log Capture**: Records in-game chat messages
- **Message Filtering**: Organized by chat type and source

### Data Visualization
- **Charts Dashboard**: Historical data visualization using Chart.js
- **Progress Tracking**: Visual progress bars for experience and vitals
- **Responsive Design**: Mobile-friendly interface with Bootstrap

### UI Features
- **Theme Switching**: Light/dark mode support
- **Fullscreen Mode**: Immersive viewing experience
- **Real-time Updates**: WebSocket connection for live data streaming
- **Navigation**: Multi-view routing with Vue Router

## 📦 Project Structure

```
ffxi-player-service/
├── PlayerService/          # Windower Lua addon
│   ├── PlayerService.lua   # Main addon logic
│   ├── PlayerServiceInterface.lua
│   └── Utils.lua
├── PlayerServiceEmulator/  # Python emulator (dev/testing)
│   ├── emulator.py         # Entry point (CLI)
│   ├── config.json         # Endpoint + timing configuration
│   ├── requirements.txt    # Python dependencies
│   ├── models/
│   │   └── player.py       # Player dataclasses mirroring JavaService model
│   ├── services/
│   │   ├── api_client.py   # HTTP client for all REST endpoints
│   │   └── player_simulator.py  # Per-player simulation logic
│   └── data/
│       └── players.py      # FFXI game data + seeded player profiles
├── service/                # Java backend
│   └── JavaService/
│       ├── build.gradle   # Gradle build config
│       └── src/           # Java source files
├── app/                   # Vue.js frontend
│   ├── src/
│   │   ├── components/    # Vue components
│   │   ├── stores/        # Pinia state management
│   │   ├── views/         # Page views
│   │   ├── router/        # Vue Router config
│   │   └── types/         # TypeScript definitions
│   └── public/            # Static assets
├── build.sh               # Build and deployment script
├── restart-service.sh     # Service restart script
├── start-nginx.sh         # Nginx startup script
├── ffxi.eaglejs.io-ssl.conf  # Nginx configuration
├── io.eaglejs.ffxi.service.plist  # Java service LaunchDaemon
└── com.eaglejs.ffxi.nginx.plist   # Nginx LaunchDaemon
```

## 🔧 Technology Stack

### Backend
- **Java** with Dropwizard framework
- **MongoDB** (mongodb-driver-sync)
- **WebSocket** (javax.websocket + Jetty) for real-time communication
- **Swagger** (OpenAPI 3) for API documentation
- **Gradle** as build tool
- **IP Filtering** for protected routes

### Frontend
- **Vue 3** with Composition API
- **TypeScript** for type safety
- **Vite** as build tool and dev server
- **Pinia** for state management
- **Vue Router** for navigation
- **Chart.js** + vue-chartjs for data visualization
- **Bootstrap 5** for UI components
- **SCSS** for styling
- **Axios** for HTTP requests

### Game Integration
- **Windower** addon framework
- **Lua** scripting for FFXI integration

### Development Tools
- **Python 3.8+** for the PlayerServiceEmulator
- **requests** library for HTTP simulation

## 📥 Installation

### Prerequisites
- Java 11+
- Gradle
- MongoDB
- Python 3.8+ (for PlayerServiceEmulator)
- Nginx (optional, for production deployment)
- FFXI with Windower (for game integration)

### Backend Setup

```bash
cd service/JavaService
./gradlew run
```

The API server will run on port 8080 and WebSocket server on port 8081.

### Frontend Setup

```bash
cd app
npm install
npm run dev
```

The development server will run on port 5173 with hot module replacement.

### Lua Addon Installation

1. Copy the `PlayerService` folder to your Windower addons directory:
   ```
   Windower4/addons/PlayerService/
   ```

2. Load the addon in-game:
   ```
   //lua load PlayerService
   ```

3. Configure the addon to point to your API endpoint (edit PlayerServiceInterface.lua)

### PlayerServiceEmulator Setup

The emulator lets you develop and test the frontend and backend **without a running FFXI client**. It simulates multiple players sending realistic stat updates to every REST endpoint.

#### Install dependencies

```bash
cd PlayerServiceEmulator
python3 -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

#### Configure the emulator

Edit `PlayerServiceEmulator/config.json` to point at your running service:

```json
{
  "service": {
    "base_url": "http://localhost:8080"
  },
  "emulator": {
    "update_interval_seconds": 3,
    "online_ping_interval_seconds": 30
  }
}
```

| Key | Description |
|-----|-------------|
| `base_url` | Base URL of the running JavaService REST API |
| `update_interval_seconds` | How often (in seconds) each player tick fires |
| `online_ping_interval_seconds` | How often the emulator sends an `set_online` heartbeat |

#### Run the emulator

```bash
# Continuous simulation (Ctrl-C to stop)
python emulator.py

# Run exactly one tick then exit (smoke test / CI)
python emulator.py --once

# Override update interval from the command line
python emulator.py --interval 1

# Use a custom config file
python emulator.py --config /path/to/my-config.json
```

#### What the emulator simulates

Each simulated player runs a periodic tick that calls the following endpoints:

| Frequency | Endpoints called |
|-----------|-----------------|
| Every tick (~3 s) | `set_hpp`, `set_mpp`, `set_tp`, `set_zone`, `set_buffs`, `set_messages` |
| Every 5 ticks | `set_exp_history`, `set_capacity_points`, `set_merits`, `set_stats` |
| Every 20 ticks | `set_currency1`, `set_currency2`, `set_gil` |
| Every 30 s | `set_online` (heartbeat) |
| On first start | `initialize_player` (skipped if player already exists) |

#### Verifying output

With the Java service running, you should see players appear in the web dashboard within seconds. You can also query the API directly:

```bash
# List all online players
curl http://localhost:8080/players/get_players

# Get a specific simulated player
curl "http://localhost:8080/players/get_player?playerId=1001"
```

#### Seeded player profiles

The emulator ships with three pre-configured players (defined in `data/players.py`):

| Player ID | Name | Main Job | Zone |
|-----------|------|----------|------|
| 664658 | ralphina | WAR/MNK | Escha - Zi'Tah |
| 297203 | zulobo | WHM/SCH | Reisenjima |
| 644915 | darkcloud | BLM/RDM | Dynamis - Buburimu |
| 359030 | valefor | SAM/WAR | Escha - Ru'Aun |
| 325554 | miriana | RDM/BLM | Walk of Echoes |
| 269567 | guildenstern | NIN/DNC | Nyzul Isle |

Add more profiles by appending entries to the `PLAYER_PROFILES` list in `data/players.py`.

## 🔨 Building for Production

### Frontend Build

```bash
cd app
npm run build
```

This generates optimized static files in `app/dist/`.

### Automated Deployment

Run the build script to compile and deploy:

```bash
./build.sh
```

This script:
1. Builds the Vue.js frontend
2. Copies built files to `/var/www/ffxi`
3. Restarts the services

## ⚙️ Configuration

### Nginx (Production)
Configure reverse proxy and SSL in `ffxi.eaglejs.io-ssl.conf`

### LaunchDaemons (macOS)
- `io.eaglejs.ffxi.service.plist` - Java service
- `com.eaglejs.ffxi.nginx.plist` - Nginx service

Install LaunchDaemons:
```bash
sudo cp *.plist /Library/LaunchDaemons/
sudo launchctl load /Library/LaunchDaemons/io.eaglejs.ffxi.service.plist
sudo launchctl load /Library/LaunchDaemons/com.eaglejs.ffxi.nginx.plist
```

### Environment Variables

Backend configuration is managed via `service/JavaService/gradle.properties` (see `gradle.example.properties` for reference).

## 🎮 Usage

### With FFXI Client (Production)

1. **Start FFXI** with Windower
2. **Load the addon**: `//lua load PlayerService`
3. **Start the backend service**: The addon will begin sending data
4. **Open the web dashboard**: Navigate to your configured domain or localhost
5. **View your stats**: Real-time data will appear in the dashboard

### Without FFXI Client (Development / Testing)

1. **Start the backend service**: `cd service/JavaService && ./gradlew run`
2. **Start the emulator**: `cd PlayerServiceEmulator && python emulator.py`
3. **Open the web dashboard**: Navigate to `http://localhost:5173`
4. **View simulated stats**: Three player profiles will appear with live updates

## 🧪 Development

### Frontend Development
```bash
cd app
npm run dev           # Start dev server
npm run build         # Build for production
npm run test:unit:ci  # Execute unit tests via Vitest
npm run lint          # Lint code
npm run format        # Format code with Prettier
```

### Backend Development
```bash
cd service/JavaService
./gradlew run                                                           # Start the Java service
./gradlew runDevWatch -Pconfig="server src/main/resources/config.yml"   # Start the Java service with autoload on change
./gradlew test                                                          # Run tests
```

### Emulator Development
```bash
cd PlayerServiceEmulator
source .venv/bin/activate
python emulator.py --once      # Single tick (fast smoke test)
python emulator.py --interval 1  # Fast continuous mode
```

### Building Jar
```bash
cd service/JavaService
./gradlew clean jar
```

### Type Checking
```bash
cd app
npm run type-check
```

## 🔒 Security

- IP filtering on protected routes (192.168.0.0/16)
- CORS configured for allowed origins
- SSL/TLS support via Nginx configuration

## 📄 License

Private project

## 👤 Author

**eaglejs**

---

*Built with ❤️ for the FFXI community*