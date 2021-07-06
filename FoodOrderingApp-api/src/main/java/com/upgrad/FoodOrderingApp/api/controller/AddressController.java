package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

// Address Controller Handles all  the Address related endpoints

@CrossOrigin
@RestController
@RequestMapping("/")
public class AddressController {

    @Autowired
    AddressService addressService; //Handles all service related to the Address

    @Autowired
    CustomerService customerService; // Handles all the Service Related to the Customer.


    /* The method handles Address save Related request.It takes the details as per in the SaveAddressRequest
     & produces response in SaveAddressResponse and returns UUID of newly Created Customer Address and Success message else Return error code and error Message.
      */
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST,path = "/address",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(@RequestHeader("authorization") final String authorization, @RequestBody(required = false)SaveAddressRequest saveAddressRequest)throws AuthorizationFailedException, AddressNotFoundException, SaveAddressException {

        //Access the accessToken from the request Header
        String accessToken = authorization.split("Bearer ")[1];

        //Calls customerService getCustomer Method to check the validity of the customer.this methods returns the customerEntity  to be updated  with address.
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        //Creating addressEntity from SaveAddressRequest data.
        AddressEntity addressEntity = new AddressEntity();

        addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        addressEntity.setUuid(UUID.randomUUID().toString());


        //Getting stateEntity using address service method getStateByUUID.
        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());

        //Calls saveAddress method to save the new addressEntity Created.
        AddressEntity savedAddress = addressService.saveAddress(addressEntity,stateEntity);

        //Calls saveCustomerAddressEntity method to save to save foreign Keys of the newly created address and the Customer.
        CustomerAddressEntity customerAddressEntity = addressService.saveCustomerAddressEntity(customerEntity,savedAddress);

        //Creating SaveAddressResponse response
        SaveAddressResponse saveAddressResponse = new SaveAddressResponse()
                .id(savedAddress.getUuid())
                .status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse,HttpStatus.CREATED);
    }

}
