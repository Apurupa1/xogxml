package org.example
import groovy.xml.MarkupBuilder
import java.nio.file.Files
import java.nio.file.Paths

class CSVToXOGXML {
    static void main(String[] args) {
        try {
            String csvFile = "test.csv" // File name in resources
            File file = locateCSVFile(csvFile)

            if (!file) {
                println "Error: File not found. Exiting."
                return
            }

            println "Reading CSV file: $file"
            List<Map<String, String>> departmentData = parseCSV(file)

            if (departmentData.isEmpty()) {
                println "No data found in CSV. Exiting."
                return
            }

            println "Generating XOG XML..."
            String xogXml = generateXOGXML(departmentData)

            String outputFileName = "test.xog.xml"
            saveToFile(outputFileName, xogXml)

            println "XOG XML generated and saved to $outputFileName"
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    // Locate the CSV file in the resources folder
    static File locateCSVFile(String csvFile) {
        URL resourceUrl = CSVToXOGXML.class.classLoader.getResource(csvFile)
        if (resourceUrl) {
            File file = new File(resourceUrl.toURI())
            if (file.exists()) {
                return file
            }
        }
        println "Error: File $csvFile not found in the resources folder."
        return null
    }

    // Parse the CSV file into a list of maps
    static List<Map<String, String>> parseCSV(File file) {
        List<Map<String, String>> rows = []
        file.withReader { reader ->
            def lines = reader.readLines()
            def headers = lines[0].split(",").collect { it.trim() } // First line is the header
            lines[1..-1].each { line ->
                def values = line.split(",").collect { it.trim() }
                rows << [id: values[0], name: values[1], parentID: values[2]]
            }
        }
        return rows
    }

    // Generate the XOG XML
    static String generateXOGXML(List<Map<String, String>> departmentData) {
        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.NikuDataBus('xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                'xsi:noNamespaceSchemaLocation': 'xog.xsd') {
            Header(action: 'write', externalSource: 'NIKU', objectType: 'department', version: '15.9') {
                Security {
                    Username("clarityadmin")
                    Password("claritypassword")
                }
            }
            Departments {
                departmentData.each { dept ->
                    Department(id: dept.id, name: dept.name, parentID: dept.parentID ?: "ROOT")
                }
            }
        }
        return writer.toString()
    }

    // Save the XML string to a file
    static void saveToFile(String fileName, String content) {
        Files.write(Paths.get(fileName), content.bytes)
    }
}

