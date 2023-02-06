package com.zerobase.cms.order.application;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartApplication {

  private final ProductSearchService productSearchService;
  private final CartService cartService;

  public Cart addCart(Long customerId, AddProductCartForm form) {

    Product product = productSearchService.getByProductId(form.getId());
    if (product == null) {
      throw new CustomException(ErrorCode.NOT_FOUND_PRODUCT);
    }

    Cart cart = cartService.getCart(customerId);

    if (cart != null && !addAble(cart, product, form)) {
      throw new CustomException(ErrorCode.ITEM_COUNT_NOT_ENOUGH);
    }

    return cartService.addCart(customerId, form);
  }

  public Cart updateCart(Long customerId, Cart cart) {
    //상품의 삭제, 수량 변경이 일어나기 때문에 get Cart 호출
    cartService.putCart(customerId, cart);
    return getCart(customerId);
  }

  public Cart getCart(Long customerId) {
    Cart cart = refreshCart(cartService.getCart(customerId));

    Cart returnCart = new Cart();
    returnCart.setCustomerId(customerId);
    returnCart.setProducts(cart.getProducts());
    returnCart.setMessages(cart.getMessages());
    cart.setMessages(new ArrayList<>());

    cartService.putCart(customerId, cart);
    return returnCart;
    //확인한 메시지는 스팸이 되기 때문에 제거 처리.
  }

  //null setting
  public void clearCart(Long customerId) {
    cartService.putCart(customerId, null);
  }

  //장바구니 상품 추가 후, 상품 가격 or 수량 변동에 대한 처리
  private Cart refreshCart(Cart cart) {

    //1. 상품이나 상품 아이템 정보, 가격, 수량이 변경되었는지 체크 -> 그에 맞는 알람 고객에게 제공
    //2. 상품 수량, 가격을 임의로 변경.
    Map<Long, Product> productMap = productSearchService.getListByProductIds(
        cart.getProducts().stream().map(Cart.Product::getId).collect(Collectors.toList()))
        .stream()
        .collect(Collectors.toMap(Product::getId, product -> product));

    for (int i=0; i<cart.getProducts().size(); i++) {

      Cart.Product cartProduct = cart.getProducts().get(i);

      Product p = productMap.get(cartProduct.getId());
      if (p == null) {
        cart.getProducts().remove(cartProduct);
        i--;
        cart.addMessage(cartProduct.getName() + " 상품이 삭제되었습니다.");
        continue;
      }

      Map<Long, ProductItem> productItemMap = p.getProductItems().stream()
          .collect(Collectors.toMap(ProductItem::getId, productItem -> productItem));

      List<String> tmpMessages = new ArrayList<>();
      for (int j=0; j<cartProduct.getItems().size(); j++) {

        Cart.ProductItem cartProductItem = cartProduct.getItems().get(j);
        ProductItem productItem = productItemMap.get(cartProductItem.getId());

        if (productItem == null) {
          cartProduct.getItems().remove(cartProductItem);
          j--;
          tmpMessages.add(cartProduct.getName() + "옵션이 삭제되었습니다.");
          continue;
        }

        boolean isPriceChanged = false, isCountNotEnough = false;

        if (!cartProductItem.getPrice().equals(productItem.getPrice())) {
          isPriceChanged = true;
          cartProductItem.setPrice(productItem.getPrice());
        }
        if (cartProductItem.getCount() > productItem.getCount()) {
          isCountNotEnough = true;
          cartProductItem.setCount(productItem.getCount());
        }
        if (isPriceChanged && isCountNotEnough) {
          tmpMessages.add(cartProductItem.getName() + "가격변동, 수량이 부족하여 구매 가능한 최대치로 변경되었습니다.");
        } else if (isPriceChanged) {
          tmpMessages.add(cartProductItem.getName() + " 가격이 변동되었습니다.");
        } else if (isCountNotEnough) {
          tmpMessages.add(cartProductItem.getName() + " 수량이 부족하여 구매 가능한 최대치로 변경되었습니다.");
        }
      }

      if (cartProduct.getItems().size() == 0) {

        cart.getProducts().remove(cartProduct);
        i--;
        cart.addMessage(cartProduct.getName() + " 상품이 옵션이 모두 없어져 구매가 불가능합니다.");
        continue;
      } else if (tmpMessages.size() > 0) {

        StringBuilder builder = new StringBuilder();
        builder.append(cartProduct.getName() + " 상품의 변동 사항 : ");

        for (String message : tmpMessages) {

          builder.append(message);
          builder.append(", ");
        }

        cart.addMessage(builder.toString());
      }
    }

    cartService.putCart(cart.getCustomerId(), cart);
    return cart;
  }

  private boolean addAble(Cart cart, Product product, AddProductCartForm form) {

    Cart.Product cartProduct = cart.getProducts().stream()
        .filter(p -> p.getId().equals(form.getId())).findFirst()
        .orElse(Cart.Product.builder().id(product.getId())
            .items(Collections.emptyList()).build());
    //이 시점에서 cart 내의 product가 form 에서 받아온 값을 반영하지 않은 상태
    // --> items null 대신 빈 리스트로 세팅

    Map<Long, Integer> cartItemCountMap = cartProduct.getItems().stream()
        .collect(Collectors.toMap(Cart.ProductItem::getId, Cart.ProductItem::getCount));

    Map<Long, Integer> currentItemCountMap = product.getProductItems().stream()
        .collect(Collectors.toMap(ProductItem::getId, ProductItem::getCount));
    //부정 조건 case 존재 시 false 반환
    return form.getItems().stream().noneMatch(
        formItem -> {
          Integer cartCount = cartItemCountMap.get(formItem.getId()) == null ? 0
              : cartItemCountMap.get(formItem.getId());
          Integer currentCount = currentItemCountMap.get(formItem.getId());
          return formItem.getCount() + cartCount
              > currentCount; //(부정 조건)입력받은 수량 + 카트 내 수량 > 해당 상품의 재고
        });
  }

}
