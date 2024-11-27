#!/bin/sh

launchctl unload -w ~/Library/LaunchAgents/com.eaglejs.ffxi.node.plist
launchctl load -w ~/Library/LaunchAgents/com.eaglejs.ffxi.node.plist
