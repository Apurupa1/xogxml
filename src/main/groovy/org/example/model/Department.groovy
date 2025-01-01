package org.example.model

class Department {
    String department_code
    String dept_manager_code
    String entity
    String short_description
    String locationcode
    String parent_department_code

    // Constructor that accepts a map of attributes
    Department(Map<String, String> attributes) {
        this.department_code = attributes['department_code']
        this.dept_manager_code = attributes['dept_manager_code']
        this.entity = attributes['entity']
        this.short_description = attributes['short_description']
        this.locationcode = attributes['locationcode']
        this.parent_department_code = attributes['parent_department_code']
    }

    // Additional constructor for direct parameters
    Department(String department_code, String dept_manager_code, String entity, String short_description, String locationcode, String parent_department_code) {
        this.department_code = department_code
        this.dept_manager_code = dept_manager_code
        this.entity = entity
        this.short_description = short_description
        this.locationcode = locationcode
        this.parent_department_code = parent_department_code
    }

    String toString() {
        return "Department[department_code: $department_code, dept_manager_code: $dept_manager_code, entity: $entity, " +
                "short_description: $short_description, locationcode: $locationcode, parent_department_code: $parent_department_code]"
    }
}


