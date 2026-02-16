# UI Integration

## Overview
The JavaService integrates the ffxi-stats UI package from the Nexus npm repository.

## Build Process
The Gradle build automatically handles UI integration:

1. **Placeholder UI**: A basic placeholder HTML page is created during build if the full UI package is not available
2. **Static Assets**: UI files are placed in `build/resources/main/assets/`
3. **Jetty Serving**: Dropwizard's Jetty server serves static assets on port 80

## Installing the Full UI Package

### Option 1: Nexus Repository (Recommended)
The build is configured to pull from:
```
http://eaglejs-mac-mini.local:8091/repository/npm-hosted/ffxi-stats/
```

If authentication is required, configure Nexus to allow anonymous read access or add credentials to `~/.gradle/gradle.properties`:
```properties
nexusUsername=your-username
nexusPassword=your-password
```

### Option 2: Manual Installation
1. Download `ffxi-stats-1.0.0.tgz` from Nexus
2. Extract to `src/main/resources/assets/`
3. Rebuild the project

## Configuration

### Port Configuration
The service runs on port 80 (configured in `config.yml`):
```yaml
server:
  applicationConnectors:
    - type: http
      port: 80
```

### Static Asset Path
Static assets are served from the `/assets` directory in the classpath and mapped to the root path `/`.

## Accessing the UI
Once the service is running:
- Main UI: http://localhost/
- Health Check: http://localhost/health
- API Docs: http://localhost/openapi.json
- WebSocket: ws://localhost/ws/players

## Development
To modify the placeholder UI, edit the `createPlaceholderUI` task in `build.gradle`.
