package com.oauth.server.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Patient {
    String userName; // username for patient login
    String patientId; //Identifier for patient
    String emailAddress;
    String firstName;
    String lastName;
    String dateOfBirth;
}