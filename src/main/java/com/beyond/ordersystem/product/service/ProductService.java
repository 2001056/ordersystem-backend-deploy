package com.beyond.ordersystem.product.service;


import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dtos.ProductCreateDto;
import com.beyond.ordersystem.product.dtos.ProductResDto;
import com.beyond.ordersystem.product.dtos.ProductSearchDto;
import com.beyond.ordersystem.product.dtos.ProductUpdateDto;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${aws.s3.bucket1}")
    private String bucket;

    public ProductService(ProductRepository productRepository, MemberRepository memberRepository, S3Client s3Client,@Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.s3Client = s3Client;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Long save(ProductCreateDto productCreateDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        Product product = productRepository.save(productCreateDto.toEntity(member));
        if(productCreateDto.getProductImage() != null){
            String fileName = "product-"+product.getId()+"-"+productCreateDto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productCreateDto.getProductImage().getContentType())
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(productCreateDto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String imgUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateProfileImageUrl(imgUrl);
        }
//        동시성 문제 해결을 위해 상품등록시 redis에 재고세팅
        redisTemplate.opsForValue().set(String.valueOf(product.getId()), String.valueOf(product.getStockQuantity()));

        return product.getId();
    }

    @Transactional(readOnly = true)
    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto searchDto){
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if(searchDto.getProductName() != null){
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%"+searchDto.getProductName()+"%"));
                }
                if(searchDto.getCategory() != null){
                    predicateList.add(criteriaBuilder.equal(root.get("category"), searchDto.getCategory()));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i=0; i<predicateArr.length; i++){
                    predicateArr[i] = predicateList.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> postList = productRepository.findAll(specification, pageable);
        return postList.map(p->ProductResDto.fromEntity(p));
    }

    @Transactional(readOnly = true)
    public ProductResDto findById(Long id){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("상품정보없음"));
        return ProductResDto.fromEntity(product);
    }

    public void update(Long id , ProductUpdateDto dto){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("상품정보없음"));
        product.updateProduct(dto);

        if(dto.getProductImage()!=null){
//            이미지를 수정하거나 추가하고자 하는 경우 : 삭제 후 추가
//            기존이미지를 파일명으로 삭제
            if (product.getImagePath()!=null) {
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
            String newfileName = "product-"+product.getId()+"-"+dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newfileName)
                    .contentType(dto.getProductImage().getContentType())
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String newimgUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(newfileName)).toExternalForm();
            product.updateProfileImageUrl(newimgUrl);
        }else {
//            이미지를 삭제하고자 하는 경우
            if (product.getImagePath()!=null){
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
        }
    }


}
