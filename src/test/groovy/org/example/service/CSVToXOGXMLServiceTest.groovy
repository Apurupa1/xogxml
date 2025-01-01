package org.example.service

import groovy.xml.MarkupBuilder;
import org.example.model.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test

import java.nio.file.Files;
import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.*

class CSVToXOGXMLServiceTest {

    private CSVToXOGXMLService service;

    @BeforeEach
    void setUp() {
        service = new CSVToXOGXMLService();
    }

    @Test
    void testGenerateXOGXML_SingleRootDepartment() {
        List<Department> departments = List.of(
                new Department("C", "", "Corporate", "Corporate", "DE", "")
        );

        String xml = service.generateXOGXML(departments);
        assertNotNull(xml);
        assertTrue(xml.contains("<Department department_code='C'"));
    }

    @Test
    void testGenerateXOGXML_Hierarchy() {
        List<Department> departments = List.of(
                new Department("C", "admin", "Corporate", "Corporate", "DE", ""),
                new Department("FCO", "admin", "Corporate", "Corporate Controlling", "DE", "C"),
                new Department("FCO-F", "admin", "Corporate", "Financial Controlling", "DE", "FCO")
        );

        String xml = service.generateXOGXML(departments);
        assertNotNull(xml);
        assertTrue(xml.contains("<Department department_code='C'"));
        assertTrue(xml.contains("<Department department_code='FCO'"));
        assertTrue(xml.contains("<Department department_code='FCO-F'"));
    }

    @Test
    void testSaveToFile() throws IOException {
        String content = "<XOG>Test</XOG>";

        // Create temporary file for testing
        Path tempFile = Files.createTempFile("test_output", ".xml");
        tempFile.toFile().deleteOnExit();

        service.saveToFile(tempFile.toString(), content);

        // Verify file content
        String savedContent = Files.readString(tempFile);
        assertEquals(content, savedContent);
    }

    @Test
    void testParseCSVLines_SkipIgnoredRows() {
        List<String> lines = [
                "Ignore, ID, dept_manager_code, entity, Name, locationcode, ParentDepartmentID",
                "#,Ignored Row,admin,Corporate,Description,DE,Root",
                ",D1,Department 1,admin,Corporate,Description 1,DE,Root",
                ",D2,Department 2,admin,Corporate,Description 2,DE,D1"
        ]

        List<Department> departments = service.parseCSVLines(lines)
        assertNotNull(departments)
        assertEquals(2, departments.size())
        assertEquals("D1", departments[0].department_code)
        assertEquals("D2", departments[1].department_code)
    }

    @Test
    void testCreateDepartment_ValidData() {
        // Mocking MarkupBuilder for the XML generation
        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        Map<String, List<Department>> departmentsByParentCode = [:]

        // Create a department with valid data
        Department dept = new Department("FCO-F", "admin", "Corporate", "Financial Controlling", "DE", "FCO")

        // Call the method to generate XML
        service.createDepartment(xml, dept, departmentsByParentCode)

        // Capture the generated XML output
        String xmlOutput = writer.toString()

        // Print the XML output for debugging
        println("Generated XML Output:\n" + xmlOutput)

        // Adjust the assertions to match single quotes in the XML output
        assertTrue(xmlOutput.contains("<Department department_code='FCO-F'"),
                "Expected department_code 'FCO-F' to be in the XML output")
        assertTrue(xmlOutput.contains("<Description>Financial Controlling</Description>"),
                "Expected description 'Financial Controlling' to be in the XML output")
    }
}

