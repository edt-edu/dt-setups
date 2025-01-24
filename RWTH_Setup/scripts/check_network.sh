#!/bin/bash
echo -e -n "Is my IP ok?\t\t\t"
(ifconfig | grep -q 192.168.178.200) && echo "ok" || echo "fail"

echo -e -n "Router\t\t\t\t"
(ping -c 1 192.168.178.1 2>&1 >/dev/null) && echo "ok" || echo "fail"


echo -e -n "RevPI Core 1 available\t\t"
(ping -c 1 192.168.178.201 2>&1 >/dev/null) && echo "ok" || echo "fail"
echo -e -n "RevPI Core 1 controller running\t"
(ssh pi@192.168.178.201 pgrep -f island1_main.py 2>&1 >/dev/null) && echo "ok" || echo "fail"


echo -e -n "RevPI Core 2 available\t\t"
(ping -c 1 192.168.178.202 2>&1 >/dev/null) && echo "ok" || echo "fail"
echo -e -n "RevPI Core 2 controller running\t"
(ssh pi@192.168.178.202 pgrep -f island2_main.py 2>&1 >/dev/null) && echo "ok" || echo "fail"


echo -e -n "RevPI Core 3 available\t\t"
(ping -c 1 192.168.178.203 2>&1 >/dev/null) && echo "ok" || echo "fail"
echo -e -n "RevPI Core 3 controller running\t"
(ssh pi@192.168.178.203 pgrep -f island3_main.py 2>&1 >/dev/null) && echo "ok" || echo "fail"


echo -e -n "RevPI Core 4 available\t\t"
(ping -c 1 192.168.178.204 2>&1 >/dev/null) && echo "ok" || echo "fail"
echo -e -n "RevPI Core 4 controller running\t"
(ssh pi@192.168.178.204 pgrep -f island4_main.py 2>&1 >/dev/null) && echo "ok" || echo "fail"
