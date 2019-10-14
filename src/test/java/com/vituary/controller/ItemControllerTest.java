package com.vituary.controller;

import com.vituary.dto.ItemDTO;
import com.vituary.model.Item;
import com.vituary.model.ItemPriceAdjuster;
import com.vituary.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ItemControllerTest {
    private ItemController controller;

    private ItemRepository repository;
    private ItemPriceAdjuster priceAdjuster;

    @BeforeEach
    void setUp() {
        repository = mock(ItemRepository.class);
        priceAdjuster = mock(ItemPriceAdjuster.class);
        controller = new ItemController(repository, priceAdjuster);
    }

    @Test
    void listProvidesDataSourceItemsAsDTOs() {
        List<Item> items = Arrays.asList(
                new Item("Item 1", "Item 1 desc", 25, 3, randomUUID()),
                new Item("Item 2", "Item 2 desc", 75, 1, randomUUID())
        );
        when(repository.findByQuantityIsGreaterThan(anyInt())).thenReturn(items);

        List<ItemDTO> actual = controller.list();

        assertEquals(items.size(), actual.size());
        assertTrue(actual.stream().anyMatch(itemDTO -> itemDTO.getId() == items.get(0).getId()));
        assertTrue(actual.stream().anyMatch(itemDTO -> itemDTO.getId() == items.get(1).getId()));
    }

    @Test
    void listRetrievesOnlyItemsWithAPositiveQuantity() {
        controller.list();

        verify(repository, times(1)).findByQuantityIsGreaterThan(eq(0));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void listUsesPriceAdjusterOnItems() {
        Item item = new Item("Item 1", "Item 1 desc", 25, 3, randomUUID());
        when(repository.findByQuantityIsGreaterThan(anyInt())).thenReturn(Collections.singletonList(item));
        when(priceAdjuster.getAdjustedPriceForView(any())).thenReturn(37);

        List<ItemDTO> actual = controller.list();

        assertEquals(37, actual.get(0).getPrice());
        verify(priceAdjuster, times(1)).getAdjustedPriceForView(same(item));
        verifyNoMoreInteractions(priceAdjuster);
    }

    @Test
    void readConvertsItemToDTO() {
        UUID id = randomUUID();
        Item item = new Item("Item 1", "Item 1 desc", 25, 3, id);
        when(repository.findById(any())).thenReturn(Optional.of(item));

        ItemDTO actual = controller.read(id);

        assertNotNull(actual);
        assertEquals(item.getId(), actual.getId());
        assertEquals(item.getName(), actual.getName());
        assertEquals(item.getDescription(), actual.getDescription());
        verify(repository, times(1)).findById(eq(id));
    }

    @Test
    void readThrowsExceptionWhenNotFound() {
        UUID id = randomUUID();
        when(repository.findById(any())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.read(id));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getReason().contains(id.toString()));
    }

    @Test
    void readUsesPriceAdjuster() {
        Item item = new Item("Item 1", "Item 1 desc", 25, 3, randomUUID());
        when(repository.findById(any())).thenReturn(Optional.of(item));
        when(priceAdjuster.getAdjustedPriceForView(any())).thenReturn(37);

        ItemDTO actual = controller.read(randomUUID());

        assertEquals(37, actual.getPrice());
        verify(priceAdjuster, times(1)).getAdjustedPriceForView(same(item));
        verifyNoMoreInteractions(priceAdjuster);
    }
}
