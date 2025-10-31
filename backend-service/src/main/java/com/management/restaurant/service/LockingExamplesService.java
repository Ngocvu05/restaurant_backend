package com.management.restaurant.service;

import com.management.restaurant.model.Dish;
import com.management.restaurant.model.TableEntity;

import java.math.BigDecimal;

public interface LockingExamplesService {
    Dish updateDishPriceOptimistic(Long dishId, BigDecimal newPrice);
    TableEntity bookTableWithPessimisticLock(Long tableId);
    TableEntity getTableWithPessimisticRead(Long tableId);
    Dish incrementDishVersion(Long dishId);
    void lockTwoTables(Long firstTableId, Long secondTableId);
    Dish incrementOrderCountHybrid(Long dishId);
    TableEntity bookTableWithTimeout(Long tableId);
    Dish getDishWithLock(Long dishId);
}
