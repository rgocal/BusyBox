#!/system/bin/sh
########################
# DESCRIPTION:
#   Restart com.android.systemui
#
# NOTE: Run this script as root
#

service call activity 42 s16 com.android.systemui
am startservice -n com.android.systemui/.SystemUIService
