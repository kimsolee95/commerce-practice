package com.zerobase.cms.order.application;

import static org.junit.jupiter.api.Assertions.*;

import com.zerobase.cms.order.config.TestRedisConfig;
import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.product.AddProductForm;
import com.zerobase.cms.order.domain.product.AddProductItemForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.service.ProductService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestRedisConfig.class)
class CartApplicationTest {

  @Autowired
  private CartApplication cartApplication;

  @Autowired
  private ProductService productService;

  @Autowired
  private ProductRepository productRepository;

  @Test
  void ADD_TEST() {

    Product product = add_product();
    Product result = productRepository.findWithProductItemsById(product.getId()).get();

    assertNotNull(result);

    //각 필드 검증
    assertEquals(result.getName(), "나이키 에어포스");
    assertEquals(result.getDescription(), "신발");

    assertEquals(result.getProductItems().size(), 3);
    assertEquals(result.getProductItems().get(0).getName(), "나이키 에어포스0");
    assertEquals(result.getProductItems().get(0).getPrice(), 10000);

    //custimer Id 100 일 때, 장바구니 상품 추가 test
    Long customerId = 100L;
    cartApplication.clearCart(customerId);

    Cart cart = cartApplication.addCart(customerId, makeProductCartForm(result));

    //변경이 없으므로 변경알림용 message 0
    assertEquals(cart.getMessages().size(), 0);

    //application 내의 getCart -> refereshCart를 통해 가격변경한 내용이 반영된다.
    cart = cartApplication.getCart(customerId);
    assertEquals(cart.getMessages().size(), 1);
  }

  AddProductCartForm makeProductCartForm(Product product) {

    AddProductCartForm.ProductItem productItem = AddProductCartForm.ProductItem.builder()
        .id(product.getProductItems().get(0).getId())
        .name(product.getProductItems().get(0).getName())
        .count(5)
        .price(20000)
        .build();

    return AddProductCartForm.builder()
        .id(product.getId())
        .sellerId(product.getSellerId())
        .name(product.getName())
        .description(product.getDescription())
        .items(List.of(productItem))
        .build();
  }

  Product add_product() {

    Long sellerId = 1L;
    AddProductForm form = makeProductForm("나이키 에어포스", "신발", 3);
    return productService.addProduct(sellerId, form);
  }


  private static AddProductForm makeProductForm(String name, String description, int itemCount) {

    List<AddProductItemForm> itemForms = new ArrayList<>();
    for (int i=0; i<itemCount; i++) {
      itemForms.add(makeProductItemForm(null, name + i));
    }
    return AddProductForm.builder()
        .name(name)
        .description(description)
        .items(itemForms)
        .build();
  }

  private static final AddProductItemForm makeProductItemForm(Long productId, String name) {

    return AddProductItemForm.builder()
        .productId(productId)
        .name(name)
        .price(10000)
        .count(10)
        .build();
  }




}