package com.zerobase.cms.order.application;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
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

  private boolean addAble(Cart cart, Product product , AddProductCartForm form) {

    Cart.Product cartProduct = cart.getProducts().stream()
        .filter(p -> p.getId().equals(form.getId())).findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PRODUCT));

    Map<Long, Integer> cartItemCountMap = cartProduct.getItems().stream()
        .collect(Collectors.toMap(Cart.ProductItem::getId, Cart.ProductItem::getCount));

    Map<Long, Integer> currentItemCountMap = product.getProductItems().stream()
        .collect(Collectors.toMap(ProductItem::getId, ProductItem::getCount));
    //부정 조건 case 존재 시 false 반환
    return form.getItems().stream().noneMatch(
        formItem -> {
          Integer cartCount = cartItemCountMap.get(formItem.getId());
          Integer currentCount = currentItemCountMap.get(formItem.getId());
          return formItem.getCount() + cartCount > currentCount; //(부정 조건)입력받은 수량 + 카트 내 수량 > 해당 상품의 재고
        });
  }

}
