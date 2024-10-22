# Build and install on the RevPi

## `revpi` package

1. Build the package  
    Run `poetry build`.
    The python package will be built and installed to `dist/revpi-<version>-py3-none-any.whl`.
2. Copy the `.whl` file to the RevPi.
3. Uninstall the old package  
    Run `pip3 uninstall revpi -y` on the RevPi.
4. Installing the updated version  
    Run `pip3 install <package>.whl` on the RevPi.

Steps 3. and 4. can be combined to `pip3 install --no-deps --force-reinstall <package>.whl`.

Step 3. is only necessary if there is no version update in the package.

## Dependencies

To install the required dependencies on the RevPi:

1. Download the package locally `pip3 donwload <package>[<version>]`.
2. Copy the downloaded package (`<package>.tar.gz`) to the RevPi.
3. Install the package on the RevPi (`pip3 install <package>.tar.gz`).