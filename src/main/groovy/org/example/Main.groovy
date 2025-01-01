package org.example

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.controller.CSVToXOGXMLController
import org.example.service.CSVToXOGXMLService

class Main {
    // Logger for the Main class
    private static final Logger logger = LogManager.getLogger(Main)

    static void main(String[] args) {
        logger.info("Application started")

        try {
            // Dependency Injection
            logger.debug("Initializing CSVToXOGXMLService")
            CSVToXOGXMLService service = new CSVToXOGXMLService()

            logger.debug("Injecting service into CSVToXOGXMLController")
            CSVToXOGXMLController controller = new CSVToXOGXMLController(service)

            // Determine CSV file
            String csvFile = "test.csv";
            logger.info("Using CSV file: {}", csvFile);

            // Process the CSV
            controller.processCSV(csvFile)
            logger.info("CSV processing completed successfully")

        } catch (Exception e) {
            logger.error("An unexpected error occurred", e)
        }

        logger.info("Application finished")
    }
}


