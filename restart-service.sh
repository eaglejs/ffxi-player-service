#!/bin/sh

launchctl unload -w ~/Library/LaunchAgents/com.eaglejs.ffxi.node.plist
cp com.eaglejs.ffxi.node.plist ~/Library/LaunchAgents/
launchctl load -w ~/Library/LaunchAgents/com.eaglejs.ffxi.node.plist
