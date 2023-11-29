#!/system/bin/sh
########################
# DESCRIPTION:
#   Reboot Android into bootloader
#
# NOTE: Run this script as root
#

setprop ctl.start pre-recovery
sleep 3
reboot recovery # fallback
