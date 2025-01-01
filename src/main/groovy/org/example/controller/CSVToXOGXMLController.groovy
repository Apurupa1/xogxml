package org.example.controller

import org.example.model.Department
import org.example.service.CSVToXOGXMLService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class CSVToXOGXMLController {
    private static final Logger logger = LogManager.getLogger(CSVToXOGXMLController)

    private final CSVToXOGXMLService service

    CSVToXOGXMLController(CSVToXOGXMLService service) {
        this.service = service
    }

    void processCSV(String csvFile) {
        logger.info("Starting CSV to XOG XML conversion for file: {}", csvFile)

        try {
            List<Department> departmentData = service.readCSV(csvFile)

            if (departmentData.isEmpty()) {
                logger.warn("No data found in CSV. Exiting.")
                return
            }

            String xogXml = service.generateXOGXML(departmentData)
            service.saveToFile("test.xog.xml", xogXml)

            logger.info("XOG XML successfully generated and saved to test.xog.xml")
        } catch (Exception e) {
            logger.error("An error occurred during CSV processing", e)
        }
    }
}


