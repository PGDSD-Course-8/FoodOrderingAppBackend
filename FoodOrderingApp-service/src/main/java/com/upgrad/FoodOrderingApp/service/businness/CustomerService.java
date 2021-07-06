package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.common.UtilityProvider;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
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
}
