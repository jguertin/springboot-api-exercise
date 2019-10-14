package com.vituary.controller;

import com.vituary.dto.OrderDTO;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

import java.net.HttpRetryException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerIT {
    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void createShouldFailIfNotAuthorized() {
        String rootURL = "http://localhost:" + port + "/";
        // Pull the first item id from list()
        ResponseEntity<List> listResponse = restTemplate.getForEntity(rootURL + "items", List.class);
        Map<String, Object> listItemMap = (Map) listResponse.getBody().get(0);
        UUID itemId = UUID.fromString(listItemMap.get("id").toString());

        // Now purchase the item
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(new OrderDTO(null, itemId, null, null));
        RestClientException ex = assertThrows(RestClientException.class, () -> restTemplate.exchange(rootURL + "orders", HttpMethod.POST, httpEntity, OrderDTO.class));
        assertTrue(ex.getCause() instanceof HttpRetryException);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), ((HttpRetryException) ex.getCause()).responseCode());
    }

    @Test
    void createShouldPassWhenAuthorized() {
        String rootURL = "http://localhost:" + port + "/";
        // Pull the first item id from list()
        ResponseEntity<List> listResponse = restTemplate.getForEntity(rootURL + "items", List.class);
        Map<String, Object> listItemMap = (Map) listResponse.getBody().get(0);
        UUID itemId = UUID.fromString(listItemMap.get("id").toString());

        // Now purchase the item
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic dXNlcjpwYXNzd29yZA=="); // "user:password"
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(new OrderDTO(null, itemId, null, null), headers);
        ResponseEntity<OrderDTO> response = restTemplate.exchange(rootURL + "orders", HttpMethod.POST, httpEntity, OrderDTO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(itemId, response.getBody().getItemId());
    }
}
