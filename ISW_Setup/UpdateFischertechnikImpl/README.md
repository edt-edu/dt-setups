# RevPi Control code

## Prerequisites

This project is build using poetry. See [poetry](poetry.md) on instructions
on how to install poetry and run commands.

## Using the code on the RevPi

See [installing on the RevPi](installing_on_revpi.md) for instructions on how
to install the package on the RevPi's.

To run the code on the RevPi, run `python -m revpi --mqtt=<mqtt-address>`.

Arguments:
- Use `-v` or `-vv` do display more debugging information.
- Use `--island=island1|island2` to use the configuration for the given island (`island1` is the default).
- Use `--json` do encode MQTT messages as JSON instead of the DT understood bytearray.

## Testing

All tests reside in the `tests` subdirectory.

Run `poetry run pytest` for basic testing,
or `poetry run pytest --cov=revpi --cov-branch --cov-report=html` to generate the coverage report.

### Mocks

The `tests.mocks.revpimock` module can be used to simulate RevisionPy's `revpimodio2` module.

## Simulation

To simulate the RevisionPy environment, run `poetry run python -m tests.mock_run_main`.

Note: This is only a basic simulation of some functionality.

The simulation expects an MQTT broker running available on localhost.
