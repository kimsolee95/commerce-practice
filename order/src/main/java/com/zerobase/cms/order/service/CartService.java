package com.zerobase.cms.order.service;

import com.zerobase.cms.order.client.RedisClient;
import com.zerobase.cms.order.client.user.CustomerDto;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.domain.redis.Cart.Product;
import com.zerobase.cms.order.domain.redis.Cart.ProductItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {

  private final RedisClient redisClient;

  public Cart getCart(Long customerId) {

    Cart cart = redisClient.get(customerId, Cart.class);
    return cart != null ? cart : new Cart();
  }

  public Cart putCart(Long customerId, Cart cart) {
    redisClient.put(customerId, cart);
    return cart;
  }

  public void deleteCart(Long customerId) {
    redisClient.delete(customerId);
  }

  public Cart addCart(Long customerId, AddProductCartForm form) {

    Cart cart = redisClient.get(customerId, Cart.class);

    //장바구니가 없을 경우
    if (cart == null) {
      cart = new Cart();
      cart.setCustomerId(customerId);
    }

    //이전에 동일 상품 있는지 check
    Optional<Product> productOptional = cart.getProducts().stream()
        .filter(product1 -> product1.getId().equals(form.getId()))
        .findFirst();

    if (productOptional.isPresent()) {

      Cart.Product redisProduct = productOptional.get();
      List<ProductItem> items = form.getItems().stream().map(Cart.ProductItem::from)
          .collect(Collectors.toList());
      Map<Long, Cart.ProductItem> redisItemMap = redisProduct.getItems().stream()
          .collect(Collectors.toMap(Cart.ProductItem::getId, item -> item));

      if (!redisProduct.getName().equals(form.getName())) {
        cart.addMessage(redisProduct.getName() + "의 정보가 변경되었습니다. 확인 부탁드립니다.");
      }

      for (Cart.ProductItem item : items) {

        Cart.ProductItem redisItem = redisItemMap.get(item.getId());

        if (redisItem == null) {
          redisProduct.getItems().add(item);
        } else {

          if (!redisItem.getPrice().equals(item.getPrice())) {
            cart.addMessage(redisProduct.getName() + item.getName() + "의 정보가 변경되었습니다. 확인 부탁드립니다.");
          }
          redisItem.setCount(redisItem.getCount() + item.getCount());
        }
      }
    } else {

      Cart.Product product = Cart.Product.from(form);
      cart.getProducts().add(product);
    }

    redisClient.put(customerId, cart);
    return cart;
  }

  public void deleteCartAfterOrderCompleted(CustomerDto customerDto, Cart orderCart) {

    //redis cache delete
    Cart redisCart = getCart(customerDto.getId());

    if (Objects.equals(redisCart, orderCart)) {
      //1. 주문하는 orderCart가 고객의 장바구니 redisCart와 똑같다면 고객 장바구니 redisCart 전체 삭제
      deleteCart(customerDto.getId());
    } else {

      //2. (장바구니 레디스케시 데이터 productItem) - (주문 메서드 인자 값 cart에 대한 검증을 마친 객체 orderCart 내 productItem) 여집합을 redis put
      //인자 값 기준으로 여집합 구하기
      for (int i=0; i<orderCart.getProducts().size(); i++) {

        Cart.Product orderCartProduct = orderCart.getProducts().get(i); //입력받은 cart 내 상품 1개
        Long orderCartProductId = orderCartProduct.getId(); //상품 id
        List<Cart.Product> redisCartProducts = redisCart.getProducts(); //레디스 캐시 내의 상품 리스트

        //만약 상품값을 캐시가 이미 가지고 있다면 ( -> 하위 데이터 상품아이템 값을 지워야 함)
        //1. 하위 데이터인 상품아이템 리스트를 for문 돌려서 동일한 값은 모두 remove
        List<Cart.ProductItem> redisCartProductItems = new ArrayList<>();
        int currentProductIdx = redisCartProducts.indexOf(orderCartProduct);
        if (currentProductIdx == -1) { //상퓸 옵션을 모두 동일하게 하지 않았을 경우임. -> 상품 id 값으로 인덱스 찾아오기

          for (int j=0; j<redisCartProducts.size(); j++) {
            if (orderCartProductId.equals(redisCartProducts.get(j).getId())) {
              currentProductIdx = j;
            }
          }
        }

        for (int j=0; j<orderCartProduct.getItems().size(); j++) {

          redisCartProductItems = redisCartProducts.get(currentProductIdx).getItems();

          Cart.ProductItem orderCartProductItem = orderCartProduct.getItems().get(j);

          if (redisCartProductItems.contains(orderCartProductItem)) {
            redisCart.getProducts().get(currentProductIdx).getItems().remove(orderCartProductItem);
          }
        } //for j

        //2. 하위 데이터인 상품아이템값을 더이상 하나도 가지고 있지 않다면 상위 데이터인 상품을 remove하기
        if (redisCart.getProducts().get(currentProductIdx).getItems().isEmpty()) {
          redisCart.getProducts().remove(currentProductIdx);
        }
      } //for i

      redisCart.getProducts().removeAll(Collections.singletonList(null));
      putCart(customerDto.getId(), redisCart); //redis cache put
    }
  }

}
