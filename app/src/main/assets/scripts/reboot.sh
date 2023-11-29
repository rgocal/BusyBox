#!/system/bin/sh
########################
# DESCRIPTION:
#   Reboot Android
#
# NOTE: Run this script as root
#

setprop sys.powerctl reboot
sleep 3
reboot # fallback
