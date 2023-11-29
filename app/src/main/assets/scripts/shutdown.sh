#!/system/bin/sh
########################
# DESCRIPTION:
#   Shutdown Android
#
# NOTE: Run this script as root
#

setprop sys.powerctl shutdown
sleep 3
reboot -p # fallback
