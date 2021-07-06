package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.common.UtilityProvider;
import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantCategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantCategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

//This Class handles all service related to the Restaurant.

@Service
public class RestaurantService {

    @Autowired
    RestaurantDao restaurantDao; //Handles all data related to the RestaurantEntity

    @Autowired
    RestaurantCategoryDao restaurantCategoryDao; //Handles all data related to the RestaurantCategoryEntity

    @Autowired
    CategoryDao categoryDao;  //Handles all data related to the CategoryEntity


    /* This method is to get restaurants By Rating and returns list of RestaurantEntity
    If error throws exception with error code and error message.
    */
    public List<RestaurantEntity> restaurantsByRating(){

        //Calls restaurantsByRating of restaurantDao to get list of RestaurantEntity
        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantsByRating();
        return restaurantEntities;
    }

    /* This method is to get restaurants By Name and returns list of RestaurantEntity. its takes restaurant name as the input string.
    If error throws exception with error code and error message.
    */
    public List<RestaurantEntity> restaurantsByName(String restaurantName)throws RestaurantNotFoundException{
        if(restaurantName == null || restaurantName ==""){ //Checking for restaunrant name to be null or empty to throw exception.
            throw new RestaurantNotFoundException("RNF-003","Restaurant name field should not be empty");
        }

        //Calls restaurantsByName of restaurantDao to get list of RestaurantEntity
        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantsByName(restaurantName);
        return restaurantEntities;
    }

    /* This method is to get restaurant By Category and returns list of RestaurantEntity. its takes categoryId as the input string.
     If error throws exception with error code and error message.
     */
    public List<RestaurantEntity> restaurantByCategory(String categoryId) throws CategoryNotFoundException {

        if(categoryId == null || categoryId == ""){ //Checking for categoryId to be null or empty to throw exception.
            throw new CategoryNotFoundException("CNF-001","Category id field should not be empty");
        }

        //Calls getCategoryByUuid of categoryDao to get list of CategoryEntity
        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(categoryId);

        if(categoryEntity == null){//Checking for categoryEntity to be null or empty to throw exception.
            throw new CategoryNotFoundException("CNF-002","No category by this id");
        }

        //Calls getRestaurantByCategory of restaurantCategoryDao to get list of RestaurantCategoryEntity
        List<RestaurantCategoryEntity> restaurantCategoryEntities = restaurantCategoryDao.getRestaurantByCategory(categoryEntity);

        //Creating new restaurantEntity List and add only the restaurant for the corresponding category.
        List<RestaurantEntity> restaurantEntities = new LinkedList<>();
        restaurantCategoryEntities.forEach(restaurantCategoryEntity -> {
            restaurantEntities.add(restaurantCategoryEntity.getRestaurant());
        });
        return restaurantEntities;
    }
}
