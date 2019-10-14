package com.vituary.controller;

import com.vituary.dto.OrderDTO;
import com.vituary.model.Item;
import com.vituary.model.ItemPriceAdjuster;
import com.vituary.model.Order;
import com.vituary.repository.ItemRepository;
import com.vituary.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private OrderRepository orderRepository;
    private ItemRepository itemRepository;
    private ItemPriceAdjuster priceAdjuster;

    public OrderController(OrderRepository orderRepository, ItemRepository itemRepository, ItemPriceAdjuster priceAdjuster) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.priceAdjuster = priceAdjuster;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<OrderDTO> create(@RequestBody OrderDTO orderDTO, @AuthenticationPrincipal User user) {
        Item item = itemRepository.findById(orderDTO.getItemId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "item not found: " + orderDTO.getItemId()));
        if (item.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inventory for the item is not available: " + item.getId());
        }

        item.decrementQuantity();
        Order newOrder = new Order(
                item,
                user.getUsername(),
                priceAdjuster.getAdjustedPriceForPurchase(item)
        );

        newOrder = orderRepository.save(newOrder);
        return new ResponseEntity<>(mapModelToDTO(newOrder), HttpStatus.CREATED);
    }

    @RequestMapping
    public List<OrderDTO> list() {
        Iterable<Order> orders = orderRepository.findAll();
        return StreamSupport.stream(orders.spliterator(), false)
                .map(this::mapModelToDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO mapModelToDTO(Order order) {
        return new OrderDTO(order.getId(), order.getItem().getId(), order.getUsername(), order.getPrice());
    }
}
