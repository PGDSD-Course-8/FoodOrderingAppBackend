package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.common.UtilityProvider;
import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

//This Class handles all service related to the address

@Service
public class AddressService {
    @Autowired
    AddressDao addressDao; //Handles all data related to the addressEntity

    @Autowired
    CustomerAuthDao customerAuthDao; //Handles all data related to the customerAuthEntity

    @Autowired
    UtilityProvider utilityProvider;// It Provides Data Check methods for various cases

    @Autowired
    CustomerAddressDao customerAddressDao; //Handles all Data of CustomerAddressEntity

    @Autowired
    StateDao stateDao; //Handles all data related to the StateEntity


    /* This method is to saveAddress.Takes the Address and state entity and saves the Address to the DB.
    If error throws exception with error code and error message.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity,StateEntity stateEntity)throws SaveAddressException{

        //Checking if any field is empty in the address entity.
        if (addressEntity.getCity() == null || addressEntity.getFlatBuilNo() == null || addressEntity.getPincode() == null || addressEntity.getLocality() == null){
            throw new SaveAddressException("SAR-001","No field can be empty");
        }
        //Checking if pincode is valid
        if(!utilityProvider.isPincodeValid(addressEntity.getPincode())){
            throw new SaveAddressException("SAR-002","Invalid pincode");
        }

        //Setting state to the address
        addressEntity.setState(stateEntity);

        //Passing the addressEntity to addressDao saveAddress method which returns saved address.
        AddressEntity savedAddress = addressDao.saveAddress(addressEntity);

        //returning SavedAddress
        return savedAddress;

    }

    /*This method is to saveCustomerAddressEntity.This method takes Customer Entity and AddressEntity and returns CustomerAddressEntity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity saveCustomerAddressEntity(CustomerEntity customerEntity,AddressEntity addressEntity){

        //Creating new CustomerAddressEntity and setting the data.
        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setCustomer(customerEntity);
        customerAddressEntity.setAddress(addressEntity);

        //Saving the newly Created CustomerAddressEntity in the DB.
        CustomerAddressEntity createdCustomerAddressEntity = customerAddressDao.saveCustomerAddress(customerAddressEntity);
        return createdCustomerAddressEntity;
    }

    /*This method is to getStateByUUID using UUID of state.
    If error throws exception with error code and error message.
     */
    public StateEntity getStateByUUID (String uuid)throws AddressNotFoundException{
        //Calls getStateByUuid od StateDao to get all the State details.
        StateEntity stateEntity = stateDao.getStateByUuid(uuid);
        if(stateEntity == null) {//Checking if its null to return error message.
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }
        return  stateEntity;
    }
}
