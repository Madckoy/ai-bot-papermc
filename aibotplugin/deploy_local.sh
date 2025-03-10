#!/bin/bash

# Variables
PLUGIN_NAME="AIBotPlugin-1.1-SNAPSHOT.jar"
LOCAL_PATH="target/$PLUGIN_NAME"
LOCAL_PATH_TO_DEPLOY="/opt/apps/minecraft/paper/plugins"  # Local plugin directory
LOG_FILE="deploy.log"

# Check if the file exists
if [[ ! -f "$LOCAL_PATH" ]]; then
    echo "❌ Plugin file $LOCAL_PATH not found! Build the project first." | tee -a "$LOG_FILE"
    exit 1
fi

# Show file size and last modification date
FILE_SIZE=$(stat -c%s "$LOCAL_PATH")
FILE_DATE=$(stat -c%y "$LOCAL_PATH")
echo "📦 Plugin Details: Size=${FILE_SIZE} bytes, Last Modified=${FILE_DATE}" | tee -a "$LOG_FILE"

# Copy the file to the local server's plugin directory
echo "🚀 Copying $PLUGIN_NAME to $LOCAL_PATH_TO_DEPLOY" | tee -a "$LOG_FILE"
cp "$LOCAL_PATH" "$LOCAL_PATH_TO_DEPLOY"

# Check if copy was successful
if [[ $? -eq 0 ]]; then
    echo "✅ Successfully copied $PLUGIN_NAME to $LOCAL_PATH_TO_DEPLOY" | tee -a "$LOG_FILE"
else
    echo "❌ Failed to copy the plugin!" | tee -a "$LOG_FILE"
    exit 1
fi

# Optional: Restart Minecraft Server on Localhost
SSH_CMD="sudo systemctl restart minecraft"
echo "🔄 Restarting Minecraft server on localhost..." | tee -a "$LOG_FILE"
sudo $SSH_CMD

if [[ $? -eq 0 ]]; then
    echo "✅ Minecraft server restarted successfully!" | tee -a "$LOG_FILE"
else
    echo "⚠️ Failed to restart the Minecraft server. Check manually." | tee -a "$LOG_FILE"
fi
