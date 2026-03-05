package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.service.SseAlarmService;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dtos.OrderCreateDto;
import com.beyond.ordersystem.ordering.dtos.OrderListDto;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String,String> redisTemplate;


    @Autowired
    public OrderingService(OrderingRepository orderingRepository, ProductRepository productRepository, MemberRepository memberRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate redisTemplate) {
        this.orderingRepository = orderingRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
    }

//    동시성제어방법1. 특정 메서드에 한해 격리수준 올리기.
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long create( List<OrderCreateDto> orderCreateDtoList) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        for (OrderCreateDto dto : orderCreateDtoList){
            Product product = productRepository.findByIdForUpdate(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("entity is not found"));
            if (product.getStockQuantity() < dto.getProductCount())
                throw new IllegalArgumentException("재고가모자랍니다");
            product.updateStockQunatity(dto.getProductCount());
            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetailList().add(orderDetail);

        }


        return ordering.getId();
    }


    @Transactional(readOnly = true)
    public List<OrderListDto> findAll(){
        return orderingRepository.findAll().stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<OrderListDto> myorders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return orderingRepository.findAllByMember(member).stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }

}
