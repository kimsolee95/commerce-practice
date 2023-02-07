package com.zerobase.cms.order.application;

import com.zerobase.cms.order.client.UserClient;
import com.zerobase.cms.order.client.user.ChangeBalanceForm;
import com.zerobase.cms.order.client.user.CustomerDto;
import com.zerobase.cms.order.client.user.OrderHistoryMailInfoForm;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.domain.repository.ProductItemRepository;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.ProductItemService;
import java.util.stream.IntStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderApplication {

  private final CartApplication cartApplication;
  private final UserClient userClient;
  private final ProductItemService productItemService;
  private final ProductItemRepository productItemRepository;

  @Transactional
  public void order(String token, Cart cart) {
    //결제
    //1. 상품들이 모두 주문 가능 상태인지 확인
    //2. 상품 가격 변동이 있는지 확인
    //3. 고객 잔액이 주문 가능 금액인지 확인
    //4. 결제 처리 + 상품 재고 결제한만큼 반영하여 update
    //case 1. 주문 시, 기존 장바구니 내역 비우기
    //case 2. 장바구니 상품 중, 주문하지 않은 상품은 장바구니에 그대로 set.
    Cart orderCart = cartApplication.refreshCart(cart);
    if (orderCart.getMessages().size() > 0) {
      throw new CustomException(ErrorCode.ORDER_FAIL_CHECK_CART);
    }

    CustomerDto customerDto = userClient.getCustomerInfo(token).getBody();

    int totalPrice = getTotalPrice(cart);
    if (customerDto.getBalance() < totalPrice) {
      throw new CustomException(ErrorCode.ORDER_FINAL_NO_MONEY);
    }

    //transaction rollback 처리 어떻게?
    //1. feign 결과값 에 따라서 상품 엔티티를 변경하도록 한다. (1. api 호출 status value 200인가? 2. 호출 후의 유저의 잔액 값 + 다시 DB로 조회해서 가져온 잔액 값 동일할 때만 상품어아탬 더티체킹 수행)
    ResponseEntity<Integer> responseChangeBalance = userClient.changeBalance(token,
        ChangeBalanceForm.builder().from("USER").message("Order").money(-totalPrice).build());
    CustomerDto changedCustomerDto = userClient.getCustomerInfo(token).getBody();

    //wrapper 값 간의 비교는 equals로 해야 값을 비교 한다. (!=은 주소값 비교가 되기 때문에 숫자값의 동일함을 비교하지 못한다.)
    if (!responseChangeBalance.getBody().equals(changedCustomerDto.getBalance())
        || responseChangeBalance.getStatusCodeValue() != 200) {
      throw new CustomException(ErrorCode.ORDER_FAIL_PAYMENT_ERROR);
    }

    StringBuilder productStringBuilder = new StringBuilder();

    for (Cart.Product product : orderCart.getProducts()) {
      for (Cart.ProductItem cartProductItem : product.getItems()) {

        ProductItem productItem = productItemService.getProductItem(cartProductItem.getId());
        productItem.setCount(productItem.getCount() - cartProductItem.getCount());

        productStringBuilder.append(productItem.getName());
        productStringBuilder.append("\n");
      }
    }

    //mail send
    userClient.sendOrderHistoryMail(token,
        OrderHistoryMailInfoForm.builder()
            .email(customerDto.getEmail())
            .itemName(productStringBuilder.toString())
            .money(totalPrice).build());
  }

  private Integer getTotalPrice(Cart cart) {
    return cart.getProducts().stream().flatMapToInt(product -> product.getItems().stream()
            .flatMapToInt(productItem -> IntStream.of(productItem.getPrice() * productItem.getCount())))
        .sum();
  }


}
