package com.beyond.ordersystem.ordering.controller;

import com.beyond.ordersystem.ordering.dtos.OrderCreateDto;
import com.beyond.ordersystem.ordering.dtos.OrderDetailDto;
import com.beyond.ordersystem.ordering.dtos.OrderListDto;
import com.beyond.ordersystem.ordering.service.OrderingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;

    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderCreateDto> orderCreateDtoList) throws IOException {
        Long id = orderingService.create(orderCreateDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll(){
        List<OrderListDto> orderListDtoList = orderingService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(orderListDtoList);
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders(){
        List<OrderListDto> orderListDtoList  = orderingService.myorders();
        return ResponseEntity.status(HttpStatus.OK).body(orderListDtoList);
    }

}
