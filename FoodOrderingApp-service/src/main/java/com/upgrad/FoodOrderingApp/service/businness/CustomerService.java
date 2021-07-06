package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.common.UtilityProvider;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAuthDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

//This Class handles all service related to the Customer

@Service
public class CustomerService {

    @Autowired
    CustomerDao customerDao; //Handles all data related to the CustomerEntity

    @Autowired
    PasswordCryptographyProvider passwordCryptographyProvider; //Provides coding and decoding for the password

    @Autowired
    UtilityProvider utilityProvider; // It Provides Data Check methods for various cases

    @Autowired
    CustomerAuthDao customerAuthDao; //Handles all data related to the customerAuthEntity

    /* This method is to saveCustomer.Takes the customerEntity  and saves the Customer to the DB.
   If error throws exception with error code and error message.
   */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {

        //calls getCustomerByContactNumber method of customerDao to check if customer already exists.
        CustomerEntity existingCustomerEntity = customerDao.getCustomerByContactNumber(customerEntity.getContactNumber());

        if (existingCustomerEntity != null) {//Checking if Customer already Exists if yes throws exception.
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number");
        }

        if (!utilityProvider.isValidSignupRequest(customerEntity)) {//Checking if is Valid Signup Request.
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }

        if (!utilityProvider.isEmailValid(customerEntity.getEmail())) {//Checking if email is valid
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }

        if (!utilityProvider.isContactValid(customerEntity.getContactNumber())) {//Checking if Contact is valid
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }

        if (!utilityProvider.isValidPassword(customerEntity.getPassword())) {//Checking if Password is valid.
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        }

        //If all condition are satisfied the password is encoded using passwordCryptographyProvider and encoded password adn salt is added to the customerentity and persisited.
        String[] encryptedPassword = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedPassword[0]);
        customerEntity.setPassword(encryptedPassword[1]);

        //Calls createCustomer of customerDao to create the customer.
        CustomerEntity createdCustomerEntity = customerDao.createCustomer(customerEntity);

        return createdCustomerEntity;
    }

    /* This method is to authenticate the customer using contact and password  and return the CustomerAuthEntity .
    If error throws exception with error code and error message.
    */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String contactNumber, String password) throws AuthenticationFailedException {
        //Calls getCustomerByContactNumber method of customerDao using contact no.
        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);
        if (customerEntity == null) {//Checking if CustomerEntity Exists
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        //The password is encrypted using the salt stored in the retrived customer entity.
        String encryptedPassword = passwordCryptographyProvider.encrypt(password, customerEntity.getSalt());
        //If password is same as stored in the db the customer is authenticated to customer auth entity is created with new access token using jwtTokenProvider.
        if (encryptedPassword.equals(customerEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setCustomer(customerEntity);

            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);


            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setExpiresAt(expiresAt);
            customerAuthEntity.setUuid(UUID.randomUUID().toString());

            //Calls createCustomerAuth of customerAuthDao and create new CustomerAuthEntity in the DB with accessToken.
            CustomerAuthEntity createdCustomerAuthEntity = customerAuthDao.createCustomerAuth(customerAuthEntity);
            return createdCustomerAuthEntity;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");//when Authenticate fails throws exception.
        }
    }

    /* This method is to logout the customer using accessToken and return the CustomerAuthEntity .
    If error throws exception with error code and error message.
    */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(String accessToken) throws AuthorizationFailedException {

        //Calls getCustomerAuthByAccessToken of customerAuthDao
        CustomerAuthEntity customerAuthEntity = customerAuthDao.getCustomerAuthByAccessToken(accessToken);

        //Paremters are checked as below if the conditions are not satisfied it throws exception.
        if (customerAuthEntity == null) {//Checking if customerAuthEntity exist
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        if (customerAuthEntity.getLogoutAt() != null) {//Checking customerAuthEntity is logout or not
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        if (customerAuthEntity.getExpiresAt().compareTo(now) < 0) {//Checking accessToken Expiry
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        //Setting the logout time to now.
        customerAuthEntity.setLogoutAt(ZonedDateTime.now());

        //Calls customerLogout of customerAuthDao to update the CustomerAuthEntity and logsout the customer.
        CustomerAuthEntity upatedCustomerAuthEntity = customerAuthDao.customerLogout(customerAuthEntity);
        return upatedCustomerAuthEntity;
    }
}
