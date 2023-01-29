package com.zerobase.cms.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.service.customer.SignUpCustomerService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SignUpCustomerServiceTest {

  @Autowired
  private SignUpCustomerService service;

  @Test
  void signUp() {

    SignUpForm form = SignUpForm.builder()
        .name("testtest")
        .birth(LocalDate.now())
        .email("testtest@gmail.com")
        .password("1")
        .phone("01044443333")
        .build();

    Customer c = service.signUp(form);
    assertNotNull(service.signUp(form).getId() != null);
  }

}