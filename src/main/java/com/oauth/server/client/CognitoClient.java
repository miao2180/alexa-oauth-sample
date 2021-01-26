package com.oauth.server.client;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.oauth.server.data.Patient;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Map;

import static com.oauth.server.constant.Constant.*;

@Component
@Log4j2
public class CognitoClient {

    private AWSCognitoIdentityProvider awsCognitoIdentityProvider;

    @PostConstruct
    public void init() {
        awsCognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(REGION)
                .build();

    }

    public AdminInitiateAuthResult getAuthResult(String poolId, String clientId, Map<String, String> authParams) {
        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withUserPoolId(PATIENT_POOL_ID)
                .withClientId(PATIENT_POOL_CLIENT_ID)
                .withAuthParameters(authParams);

        return awsCognitoIdentityProvider.adminInitiateAuth(authRequest);
    }

    public void createNewUser(final Patient patient) throws AWSCognitoIdentityProviderException {
        final String emailAddr = patient.getEmailAddress().trim();
        final String patientId = patient.getPatientId().trim();
        final String firstName = patient.getFirstName().trim();
        final String lastName = patient.getLastName().trim();
        final String dateOfBirth = patient.getDateOfBirth().trim();
        AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
                .withUserPoolId(PATIENT_POOL_ID)
                .withUsername(patient.getUserName())
                .withUserAttributes(
                        new AttributeType()
                                .withName(EMAIL)
                                .withValue(emailAddr),
                        new AttributeType()
                                .withName("email_verified")
                                .withValue("true"),
                        new AttributeType()
                                .withName(DATE_OF_BIRTH)
                                .withValue(dateOfBirth),
                        new AttributeType()
                                .withName(FIRSTNAME)
                                .withValue(firstName),
                        new AttributeType()
                                .withName(LASTNAME)
                                .withValue(lastName),
                        new AttributeType()
                                .withName(PATIENT_ID)
                                .withValue(patientId)
                );
        try {
            awsCognitoIdentityProvider.adminCreateUser(cognitoRequest);
            log.info("succeed in creating patient");
        } catch (Exception e) {
            log.info("Error in creating patient", e);
            throw new AWSCognitoIdentityProviderException("Error in creating patient");
        }
    }

    /**
     * Find users by input filters
     * @param attrName filter attribute name
     * @param attrValue filter attribute value
     * @return a list of users
     */
    public ListUsersResult getUsersByFilter(String attrName, String attrValue, String userPoolId)
            throws AWSCognitoIdentityProviderException {
        ListUsersRequest listUsersRequest = new ListUsersRequest()
                .withUserPoolId(userPoolId);
        String query = String.format(USERPOOL_FILTER_QUERY, attrName, attrValue);
        log.info("Start to fetch user list by query {}", query);
        listUsersRequest.setFilter(query);
        ListUsersResult usersResults;
        try {
            usersResults = awsCognitoIdentityProvider.listUsers(listUsersRequest);
        } catch (Exception e) {
            log.error("Failed to fetch user lists", e);
            throw new AWSCognitoIdentityProviderException("Failed to fetch user lists");
        }
        return usersResults;
    }
}
