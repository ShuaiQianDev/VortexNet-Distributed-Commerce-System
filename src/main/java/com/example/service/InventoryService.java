package com.example.demo.service;

import com.example.demo.entity.Inventory;
import com.example.demo.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * 检查库存是否充足
     */
    public boolean checkInventory(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);

        if (inventory == null) {
            log.warn("Product not found in inventory: {}", productId);
            return false;
        }

        Integer available = inventory.getQuantity() - inventory.getReservedQuantity();
        boolean result = available >= quantity;

        log.info("Inventory check for product {}: available={}, required={}, result={}",
                productId, available, quantity, result);

        return result;
    }

    /**
     * 预留库存（下单时调用）
     */
    public void reserveInventory(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);

        if (inventory != null) {
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);
            log.info("Inventory reserved for product {}: quantity={}", productId, quantity);
        }
    }

    /**
     * 释放库存预留（支付失败时调用）
     */
    public void releaseInventory(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);

        if (inventory != null) {
            inventory.setReservedQuantity(
                    Math.max(0, inventory.getReservedQuantity() - quantity)
            );
            inventoryRepository.save(inventory);
            log.info("Inventory released for product {}: quantity={}", productId, quantity);
        }
    }

    /**
     * 确认订单（支付成功时调用）
     */
    public void confirmOrder(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);

        if (inventory != null) {
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventory.setReservedQuantity(
                    Math.max(0, inventory.getReservedQuantity() - quantity)
            );
            inventoryRepository.save(inventory);
            log.info("Order confirmed for product {}: quantity={}", productId, quantity);
        }
    }
}
