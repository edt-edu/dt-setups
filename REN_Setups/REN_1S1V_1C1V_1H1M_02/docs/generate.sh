#!/bin/bash 

echo "### Compile MachinesSVGMap and use it to generate svg image for the current Setup ###"

currentworkdir=$(pwd)
pushd ../../../references/MachinesSVGMap

echo "### nvm use --lts ###"
. ~/.nvm/nvm.sh use --lts

echo "### npm built ###"
npm install
npm run build:ts
echo "###  Generate ###"

node ./dist/cli.js $currentworkdir/resources/REN_1S1V_1C1V_1H1M_02_machines_config.json $currentworkdir/generated/img/REN_1S1V_1C1V_1H1M_02_machines_config.svg
popd
