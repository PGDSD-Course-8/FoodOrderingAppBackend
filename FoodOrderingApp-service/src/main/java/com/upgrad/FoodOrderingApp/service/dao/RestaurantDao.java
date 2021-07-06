package com.upgrad.FoodOrderingApp.service.dao;


import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

//This Class is created to access DB with respect to Restaurant entity

@Repository
public class RestaurantDao {

    @PersistenceContext
    private EntityManager entityManager;

    //To get the list of restaurant by ratings from db
    public List<RestaurantEntity> restaurantsByRating(){
        try{
            List<RestaurantEntity> restaurantEntities = entityManager.createNamedQuery("restaurantsByRating",RestaurantEntity.class).getResultList();
            return restaurantEntities;
        }catch (NoResultException nre){
            return null;
        }
    }

    //To get restaurant by UUID from db
    public RestaurantEntity getRestaurantByUuid(String uuid) {
        try {
            RestaurantEntity restaurantEntity = entityManager.createNamedQuery("getRestaurantByUuid",RestaurantEntity.class).setParameter("uuid",uuid).getSingleResult();
            return restaurantEntity;
        }catch (NoResultException nre){
            return null;
        }

    }
}
