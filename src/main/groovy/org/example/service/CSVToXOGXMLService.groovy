package org.example.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.model.Department
import groovy.xml.MarkupBuilder
import java.nio.file.Files
import java.nio.file.Paths

class CSVToXOGXMLService {
    private static final Logger logger = LogManager.getLogger(CSVToXOGXMLService)

    // Read the CSV and return a list of Department objects
    List<Department> readCSV(String csvFile) {
        validateCSVFilePath(csvFile)
        InputStream inputStream = locateCSVFile(csvFile)

        if (inputStream == null) {
            logger.error("File not found: {}", csvFile)
            return []
        }

        List<String> lines = readCSVLines(inputStream)
        validateCSVContent(lines)

        return parseCSVLines(lines)
    }

    // Validate the CSV file path
    private void validateCSVFilePath(String csvFile) {
        if (!csvFile || csvFile.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV file path is missing or empty.")
        }
        logger.debug("CSV file path validated: {}", csvFile)
    }

    // Locate the CSV file in the resources folder
    private InputStream locateCSVFile(String csvFile) {
        logger.debug("Locating CSV file: {}", csvFile)
        InputStream inputStream = CSVToXOGXMLService.class.classLoader.getResourceAsStream(csvFile)
        if (inputStream != null) {
            logger.debug("CSV file found: {}", csvFile)
            return inputStream
        }
        logger.warn("CSV file not found in resources: {}", csvFile)
        return null
    }

    // Read lines from the CSV file
    private List<String> readCSVLines(InputStream inputStream) {
        logger.info("Reading lines from input stream.")
        List<String> lines = inputStream.readLines() // You can convert the InputStream to a List of lines
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty.")
        }
        logger.debug("Read {} lines from input stream.", lines.size())
        return lines
    }


    // Validate the content of the CSV file
    private void validateCSVContent(List<String> lines) {
        if (lines.size() < 2) {
            throw new IllegalArgumentException("CSV file must have a header and at least one data row.")
        }
        logger.debug("CSV content validated with {} rows.", lines.size())
    }

    // Parse CSV lines and map them to Department objects
    private List<Department> parseCSVLines(List<String> lines) {
        List<Department> departments = []
        def headers = lines[0].split(",").collect { it.trim() }

        lines[1..-1].each { line ->
            def values = line.split(",").collect { it.trim() }

            // Skip rows where the Ignore column (first column) has "#"
            if (values[0] != "#") {
                departments << mapToDepartment(values)
            }
        }
        logger.info("Successfully parsed {} departments from CSV.", departments.size())
        return departments
    }

    // Map CSV row values to a Department object
    private Department mapToDepartment(List<String> values) {
        return new Department(
                department_code: values[1]?.trim(),
                dept_manager_code: values[2]?.trim() ?:"admin",
                entity: values[3]?.trim() ?:"Corporate",
                short_description: values[4]?.trim() ?: "",
                locationcode: values[5]?.trim() ?:"DE",
                parent_department_code: values[6]?.trim() ?: ""
        )
    }

    // Generate the XOG XML using a flat structure with parent-child references
    String generateXOGXML(List<Department> departmentData) {
        logger.info("Generating XOG XML for {} departments", departmentData.size())
        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.NikuDataBus('xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                'xsi:noNamespaceSchemaLocation': '../xsd/nikuxog_department.xsd') {

            Header(action: 'write', externalSource: 'NIKU', objectType: 'department', version: '15.9')

            Departments {
                // Build a map to quickly access departments by their department_code
                Map<String, Department> departmentMap = departmentData.collectEntries { [it.department_code, it] }

                // Identify root departments (those without a parent or with a parent code not in the map)
                List<Department> rootDepartments = departmentData.findAll { dept ->
                    dept.parent_department_code == null || !departmentMap.containsKey(dept.parent_department_code)
                }

                // Process each root department and its children
                rootDepartments.each { rootDept ->
                    createDepartment(xml, rootDept, departmentMap)
                }
            }
        }
        logger.info("XOG XML generation completed")
        return writer.toString()
    }

    private void createDepartment(MarkupBuilder xml, Department dept, Map<String, Department> departmentMap) {
        logger.info("Generating XML for department: {}", dept.department_code)

        xml.Department(department_code: dept.department_code,
                dept_manager_code: dept.dept_manager_code,
                entity: dept.entity,
                short_description: dept.short_description) {

            Description(dept.short_description)
            logger.debug("Added Description for department: {}", dept.short_description)

            if (dept.locationcode) {
                LocationAssociations {
                    LocationAssociation(locationcode: dept.locationcode)
                }
            }

            // Check for sub-departments using the parent-child reference
            departmentMap.each { parentCode, childDept ->
                if (childDept.parent_department_code == dept.department_code) {
                    logger.info("Found sub-department: {} for parent department: {}", childDept.department_code, dept.department_code)
                    createDepartment(xml, childDept, departmentMap)
                }
            }
        }
    }

    // Save the XML string to a file
    void saveToFile(String fileName, String content) {
        logger.info("Saving XOG XML to file: {}", fileName)
        Files.write(Paths.get(fileName), content.bytes)
        logger.info("File saved successfully: {}", fileName)
    }
}