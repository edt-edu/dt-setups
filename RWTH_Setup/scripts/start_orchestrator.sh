#!/bin/bash
script_dir=$(dirname -- "$( readlink -f -- "$0"; )")

# Function to run backend
run_backend() {
    cd /home/se-rechnerpool2/fischertechnik/mbdo-impl/factoryscada/backend && ./gradlew bootRun --args="--configuration.path=/home/se-rechnerpool2/fischertechnik/mbdo-impl/RWTH_Setup/scripts/factoryscada_rwth.yml" | sed "s/^/[backend] /"
}

# Function to run frontend
run_frontend() {
    cd /home/se-rechnerpool2/fischertechnik/mbdo-impl/factoryscada/frontend && npm run start | sed "s/^/[frontend] /"
}

# Run backend and frontend in the background
run_backend & 
backend_pid=$!

run_frontend &
frontend_pid=$!

# Trap Ctrl+C (INT) or termination signals (TERM) and kill both processes
trap 'kill $backend_pid $frontend_pid; exit' INT TERM

# Wait for both background jobs to complete
wait $backend_pid
wait $frontend_pid
