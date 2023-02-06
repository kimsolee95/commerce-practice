package com.zerobase.cms.user.controller;

import com.zerobase.cms.user.application.OrderHistoryApplication;
import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.customer.ChangeBalanceForm;
import com.zerobase.cms.user.domain.customer.CustomerDto;
import com.zerobase.cms.user.domain.customer.OrderHistoryMailInfoForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.exception.ErrorCode;
import com.zerobase.cms.user.service.customer.CustomerBalanceService;
import com.zerobase.cms.user.service.customer.CustomerService;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import com.zerobase.domain.domain.common.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

  private final JwtAuthenticationProvider provider;
  private final CustomerService customerService;
  private final CustomerBalanceService customerBalanceService;
  private final OrderHistoryApplication orderHistoryApplication;

  @GetMapping("/getInfo")
  public ResponseEntity<CustomerDto> getInfo(@RequestHeader(name = "X-AUTH-TOKEN") String token) {

    UserVo vo = provider.getUserVo(token);
    Customer customer = customerService.findByIdAndEmail(vo.getId(), vo.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_CUSTOMER));
    return ResponseEntity.ok(CustomerDto.from(customer));
  }

  @PostMapping("/balance")
  public ResponseEntity<Integer> changeBalance(@RequestHeader(name = "X-AUTH-TOKEN") String token,
                                                @RequestBody ChangeBalanceForm form) {
    UserVo vo = provider.getUserVo(token);
    return ResponseEntity.ok(customerBalanceService.changeBalance(vo.getId(), form).getCurrentMoney());
  }

  @PostMapping("/order/history-mail")
  public ResponseEntity<String> sendOrderHistoryMail(@RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody OrderHistoryMailInfoForm form) {
    return ResponseEntity.ok(orderHistoryApplication.sendOrderHistoryMail(form));
  }

}
