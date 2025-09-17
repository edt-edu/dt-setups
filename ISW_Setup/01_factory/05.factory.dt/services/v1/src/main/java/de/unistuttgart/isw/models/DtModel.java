package de.unistuttgart.isw.models;

public class DtModel {
    private String manufacturerName;
    private String serialNumber;
    private String countryOfOrigin;
    private String hardwareVersion;
    private String softwareVersion;
    private String firmwareVersion;
    
    // In ISO 8601 format date
    private String dateOfManufacture;

    // URI of the product
    private String uriOfTheProduct;


    // empty constructor for deserialization
    protected class DtModal {

    }


    /**
     * Returns the manufacturer name
     * @return
     */
    public String getManufacturerName() {
        return manufacturerName;
    }


    /**
     * Returns the serial number
     * @return
     */
    public String getSerialNumber() {
        return serialNumber;
    }


    /**
     * Returns the country of origin
     * @return
     */
    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }


    /**
     * Returns the hardware version
     * @return
     */
    public String getHardwareVersion() {
        return hardwareVersion;
    }


    /**
     * Returns the software version
     * @return
     */
    public String getSoftwareVersion() {
        return softwareVersion;
    }


    /**
     * Returns the firmware version
     * @return
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }


    /**
     * Returns the date of manufacture in ISO 8601 format
     * @return
     */
    public String getDateOfManufacture() {
        return dateOfManufacture;
    }


    /**
     * Returns the uri of the product
     * @return
     */
    public String getUriOfTheProduct() {
        return uriOfTheProduct;
    }

    /**
     * Creates a new builder for the DtModel
     */
    public static Builder builder() {
        return new Builder();
    }

    
    public static class Builder {
        private String manufacturerName;
        private String serialNumber;
        private String countryOfOrigin;
        private String hardwareVersion;
        private String softwareVersion;
        private String firmwareVersion;
        private String dateOfManufacture;
        private String uriOfTheProduct;

       

        /**
         * Sets the serial number
         * @param serialNumber
         * @return
         */
        public Builder setManufacturerName(String manufacturerName) {
            this.manufacturerName = manufacturerName;
            return this;
        }

        /**
         * Sets the serial number
         * @param serialNumber
         * @return
         */
        public Builder setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        /**
         * Sets the country of origin
         * @param countryOfOrigin
         * @return
         */
        public Builder setCountryOfOrigin(String countryOfOrigin) {
            this.countryOfOrigin = countryOfOrigin;
            return this;
        }

        /**
         * Sets the hardware version
         * @param hardwareVersion
         * @return
         */
        public Builder setHardwareVersion(String hardwareVersion) {
            this.hardwareVersion = hardwareVersion;
            return this;
        }

        /**
         * Sets the software version
         * @param softwareVersion
         * @return
         */
        public Builder setSoftwareVersion(String softwareVersion) {
            this.softwareVersion = softwareVersion;
            return this;
        }

        /**
         * Sets the firmware version
         * @param firmwareVersion
         * @return
         */
        public Builder setFirmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        /**
         * Sets the date of manufacture in ISO 8601 format
         * @param dateOfManufacture
         * @return
         */
        public Builder setDateOfManufacture(String dateOfManufacture) {
            this.dateOfManufacture = dateOfManufacture;
            return this;
        }

        /**
         * Sets the uri of the product
         * @param uriOfTheProduct
         * @return
         */
        public Builder setUriOfTheProduct(String uriOfTheProduct) {
            this.uriOfTheProduct = uriOfTheProduct;
            return this;
        }

        /**
         * Builds the DtModel object
         * @return
         */
        public DtModel build() {
            DtModel dtModel = new DtModel();
            dtModel.manufacturerName = this.manufacturerName;
            dtModel.serialNumber = this.serialNumber;
            dtModel.countryOfOrigin = this.countryOfOrigin;
            dtModel.hardwareVersion = this.hardwareVersion;
            dtModel.softwareVersion = this.softwareVersion;
            dtModel.firmwareVersion = this.firmwareVersion;
            dtModel.dateOfManufacture = this.dateOfManufacture;
            dtModel.uriOfTheProduct = this.uriOfTheProduct;
            return dtModel;
        }
    }
    
}
