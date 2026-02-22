import os
import time
import glob
import json
import requests
import sys
from datetime import datetime

# Configuration
WATCH_DIR = "/mnt/c/Users/SL4X3/repos/ffxi-player-settings/Windower/addons/PlayerService/chat_logs"
API_URL = "http://192.168.5.30:8080/api/player/set_messages"
POLL_INTERVAL = 1.0  # Seconds
HEARTBEAT_INTERVAL = 60  # Seconds
MAX_RETRIES = 3  # Maximum number of retry attempts
INITIAL_RETRY_DELAY = 0.5  # Initial delay in seconds before first retry

# Use a session for connection pooling
session = requests.Session()

def post_message(data):
    """Sends the message data to the REST endpoint with retry logic."""
    retry_delay = INITIAL_RETRY_DELAY
    
    for attempt in range(MAX_RETRIES + 1):
        try:
            response = session.post(API_URL, json=data, timeout=5)
            response.raise_for_status()
            return True
        except requests.exceptions.RequestException as e:
            if attempt < MAX_RETRIES:
                print(f"[{datetime.now().isoformat()}] Send attempt {attempt + 1}/{MAX_RETRIES + 1} failed: {e}. Retrying in {retry_delay}s...", file=sys.stderr)
                time.sleep(retry_delay)
                retry_delay *= 2
            else:
                print(f"[{datetime.now().isoformat()}] Failed to send data after {MAX_RETRIES + 1} attempts: {e}", file=sys.stderr)
                return False
    return False

def process_file(file_path, last_pos):
    """Reads new lines from a file and processes them.
    
    Returns the new cursor position.
    """
    try:
        current_size = os.path.getsize(file_path)
    except OSError as e:
        print(f"[{datetime.now().isoformat()}] Cannot access {os.path.basename(file_path)}: {e}")
        return None

    if current_size < last_pos:
        print(f"[{datetime.now().isoformat()}] File truncated by external process: {os.path.basename(file_path)}. Resetting cursor.")
        last_pos = 0

    if current_size == last_pos:
        return last_pos

    new_pos = last_pos
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            f.seek(last_pos)
            while True:
                line = f.readline()
                if not line:
                    break
                
                # Update position for every line read
                new_pos = f.tell()
                
                # Skip lines that are just null bytes or empty
                content = line.strip().replace('\0', '')
                if not content:
                    continue
                
                try:
                    message_data = json.loads(content)
                    post_message(message_data)
                except json.JSONDecodeError as e:
                    # Only log if it's not just a partial line/garbage
                    if len(content) > 5:
                        print(f"[{datetime.now().isoformat()}] Skipping invalid JSON in {os.path.basename(file_path)}: {e}", file=sys.stderr)
                except Exception as e:
                    print(f"[{datetime.now().isoformat()}] Unexpected error processing line: {e}", file=sys.stderr)

    except (OSError, IOError) as e:
        print(f"[{datetime.now().isoformat()}] Error reading {file_path}: {e}. Will retry.", file=sys.stderr)
        return last_pos

    return new_pos

def main():
    print(f"Starting Chat Parser...")
    print(f"Watching directory: {WATCH_DIR}")
    print(f"Target Endpoint: {API_URL}")

    file_cursors = {}
    last_heartbeat = 0

    # Initial pass
    initial_files = glob.glob(os.path.join(WATCH_DIR, "*.jsonl"))
    for file_path in initial_files:
        try:
            file_cursors[file_path] = os.path.getsize(file_path)
            print(f"Tracking existing file: {os.path.basename(file_path)} (starting at offset {file_cursors[file_path]})")
        except OSError:
            pass

    while True:
        try:
            now = time.time()
            if now - last_heartbeat > HEARTBEAT_INTERVAL:
                print(f"[{datetime.now().isoformat()}] Heartbeat: Watching {len(file_cursors)} files...")
                last_heartbeat = now

            current_files = glob.glob(os.path.join(WATCH_DIR, "*.jsonl"))
            current_files_set = set(current_files)

            for file_path in current_files:
                if file_path not in file_cursors:
                    file_cursors[file_path] = 0
                    print(f"[{datetime.now().isoformat()}] New file detected: {os.path.basename(file_path)}")

                new_pos = process_file(file_path, file_cursors[file_path])
                if new_pos is not None:
                    file_cursors[file_path] = new_pos
                else:
                    # File became inaccessible
                    if file_path in file_cursors:
                        del file_cursors[file_path]

            # Cleanup deleted files
            tracked_files = list(file_cursors.keys())
            for file_path in tracked_files:
                if file_path not in current_files_set:
                    del file_cursors[file_path]
                    print(f"[{datetime.now().isoformat()}] Stopped tracking: {os.path.basename(file_path)}")

            time.sleep(POLL_INTERVAL)

        except KeyboardInterrupt:
            print("\nStopping Chat Parser.")
            break
        except Exception as e:
            print(f"[{datetime.now().isoformat()}] Critical error in main loop: {e}", file=sys.stderr)
            time.sleep(POLL_INTERVAL)

if __name__ == "__main__":
    main()
