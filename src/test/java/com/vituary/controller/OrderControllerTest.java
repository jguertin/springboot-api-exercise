package com.vituary.controller;

import com.vituary.dto.OrderDTO;
import com.vituary.model.Item;
import com.vituary.model.ItemPriceAdjuster;
import com.vituary.model.Order;
import com.vituary.repository.ItemRepository;
import com.vituary.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class OrderControllerTest {
    private OrderController controller;

    private OrderRepository orderRepository;
    private ItemRepository itemRepository;
    private ItemPriceAdjuster priceAdjuster;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        itemRepository = mock(ItemRepository.class);
        priceAdjuster = mock(ItemPriceAdjuster.class);
        controller = new OrderController(orderRepository, itemRepository, priceAdjuster);
    }

    @Test
    void createGeneratesAnOrderWithAdjustedPricing() {
        Item item = new Item("Item 1", "Item 1 desc", 25, 3, randomUUID());
        OrderDTO input = new OrderDTO(null, item.getId(), null, null);
        User user = new User("user", "password", emptyList());
        UUID newOrderId = randomUUID();

        when(priceAdjuster.getAdjustedPriceForPurchase(any())).thenReturn(37);
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return new Order(order.getItem(), order.getUsername(), order.getPrice(), newOrderId);
        });

        ResponseEntity<OrderDTO> actual = controller.create(input, user);

        assertEquals(Integer.valueOf(37), actual.getBody().getPrice());
        assertEquals(newOrderId, actual.getBody().getId());
        assertEquals(item.getId(), actual.getBody().getItemId());
        assertEquals(user.getUsername(), actual.getBody().getUsername());
        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
    }

    @Test
    void createAttachesItemToOrderAndDecrementsQuantity() {
        Item item = new Item("Item 1", "Item 1 desc", 25, 3, randomUUID());
        OrderDTO input = new OrderDTO(null, item.getId(), null, null);
        User user = new User("user", "password", emptyList());
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(orderRepository.save(any())).then(returnsFirstArg());

        controller.create(input, user);

        verify(orderRepository, times(1)).save(argThat(order -> order.getItem() == item));
        assertEquals(2, item.getQuantity());
    }

    @Test
    void createReturnsBadRequestWhenItemNotFound() {
        OrderDTO input = new OrderDTO(null, randomUUID(), null, null);
        User user = new User("user", "password", emptyList());
        when(itemRepository.findById(any())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(input, user));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getReason().contains(input.getItemId().toString()));
    }

    @Test
    void createReturnsBadRequestWhenItemQuantityIsZero() {
        Item item = new Item("Item 1", "Item 1 desc", 25, 0, randomUUID());
        OrderDTO input = new OrderDTO(null, item.getId(), null, null);
        User user = new User("user", "password", emptyList());
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(input, user));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getReason().contains(input.getItemId().toString()));
    }

    @Test
    void listProvidesDataSourceOrdersAsDTOs() {
        Item item = new Item("Item 1", "Item 1 desc", 25, 3, randomUUID());
        Order order = new Order(item, "user", 30, randomUUID());
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(order));

        List<OrderDTO> actual = controller.list();

        assertEquals(1, actual.size());
        assertEquals(order.getId(), actual.get(0).getId());
        assertEquals(item.getId(), actual.get(0).getItemId());
        assertEquals(order.getUsername(), actual.get(0).getUsername());
        assertEquals(Integer.valueOf(order.getPrice()), actual.get(0).getPrice());
    }
}
