TARGET_IP=192.168.178.201
rsync ./timesynced.cfg pi@$TARGET_IP:/etc/systemd/timesyncd.conf
ssh pi@$TARGET_IP sudo timedatectl set-ntp true && sudo systemctl unmask systemd-timesyncd && sudo systemctl daemon-reload && sudo systemctl restart systemd-timesyncd && systemctl status systemd-timesyncd

