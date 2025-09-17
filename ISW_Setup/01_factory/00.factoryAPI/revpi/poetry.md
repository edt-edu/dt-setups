# Using poetry

## Installing

### Using the system package manager

Use the system package manager to install poetry.

E.g. on debian systems, run `apt-get install poetry`.

To use poetry run `poetry`.

### Using pip

Use pip (or pip3) to install poetry `pip install poetry`.

To use poetry run `pythom -m poetry` (or `python3 -m poetry`).

## Installing project dependencies

Run `poetry install` to install the project dependencies as given in `pyproject.tmol`.

## Building the python package

Run `poetry build`. The python package will be built and installed to `dist/revpi-<version>-py3-none-any.whl`.

## Run commands in the project environment

Run `poetry run <cmd>`.

E.g.:
- tests using pytest: `poetry run pytest`
- a python console: `poetry run python`

