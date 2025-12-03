#!/bin/bash

gnome-terminal --working-directory=/home/mbdo-admin/mbdo/factoryscada --tab -- bash -c "~/mbdo/scripts/open_plcs_tabs.sh; exec bash"