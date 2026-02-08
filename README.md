# FFXI Stats

A comprehensive real-time player statistics and monitoring service for Final Fantasy XI, featuring live data collection from the game client and a web-based dashboard for viewing player information, experience tracking, buff monitoring, and statistical analysis.

## 📋 Overview

FFXI Stats is a multi-component system that bridges the gap between the FFXI game client and a modern web interface. It consists of:

- **Windower Lua Addon**: Runs inside FFXI to capture real-time player data
- **Node.js Backend API**: Receives and stores player data with WebSocket support for live updates
- **Vue.js Web Dashboard**: Interactive frontend for viewing statistics, charts, and player information
- **MongoDB Database**: Persistent storage for player data and chat logs

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
┌─────────────────┐
│  Node.js API    │
│  (Express)      │
│  Port 8080      │◄─── REST API + WebSocket (8081)
└────────┬────────┘
         │
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
├── service/                # Node.js backend
│   ├── app.js             # Express server
│   ├── routes.js          # Route configuration
│   ├── routes/
│   │   ├── Health.js      # Health check endpoint
│   │   └── Player.js      # Player data endpoints
│   └── schemas/
│       ├── Player.js      # MongoDB player schema
│       └── Chat.js        # MongoDB chat schema
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
├── com.eaglejs.ffxi.node.plist   # Node.js LaunchDaemon
└── com.eaglejs.ffxi.nginx.plist  # Nginx LaunchDaemon
```

## 🔧 Technology Stack

### Backend
- **Node.js** with Express 5.x
- **MongoDB** with Mongoose ODM
- **WebSocket** (ws) for real-time communication
- **CORS** enabled for cross-origin requests
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

## 📥 Installation

### Prerequisites
- Node.js 20+
- MongoDB
- Nginx (optional, for production deployment)
- FFXI with Windower (for game integration)

### Backend Setup

```bash
cd service
npm install
npm start
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
- `com.eaglejs.ffxi.node.plist` - Node.js service
- `com.eaglejs.ffxi.nginx.plist` - Nginx service

Install LaunchDaemons:
```bash
sudo cp *.plist /Library/LaunchDaemons/
sudo launchctl load /Library/LaunchDaemons/com.eaglejs.ffxi.node.plist
sudo launchctl load /Library/LaunchDaemons/com.eaglejs.ffxi.nginx.plist
```

### Environment Variables

Backend:
- `PORT` - API server port (default: 8080)
- MongoDB connection string in `routes/Player.js`

## 📡 API Endpoints

### Player Endpoints
- `POST /initialize_player` - Initialize player session
- `POST /set_player_online` - Mark player as online
- `POST /update_player` - Update player data
- `POST /add_chat_message` - Add chat log entry
- `GET /player/:id` - Get player data

### Health Check
- `GET /health` - Service health status

### WebSocket
- `ws://localhost:8081` - Real-time data streaming

## 🎮 Usage

1. **Start FFXI** with Windower
2. **Load the addon**: `//lua load PlayerService`
3. **Start the backend service**: The addon will begin sending data
4. **Open the web dashboard**: Navigate to your configured domain or localhost
5. **View your stats**: Real-time data will appear in the dashboard

## 🧪 Development

### Frontend Development
```bash
cd app
npm run dev      # Start dev server
npm run build    # Build for production
npm run lint     # Lint code
npm run format   # Format code with Prettier
```

### Backend Development
```bash
cd service
npm run dev      # Start with nodemon (auto-reload)
```

### Type Checking
```bash
cd app
npm run type-check
```

## 📝 Version History

- **App**: v0.1.2
- **PlayerService Lua Addon**: v1.2024.5.19

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