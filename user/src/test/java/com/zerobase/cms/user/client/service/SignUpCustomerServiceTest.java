package com.zerobase.cms.user.client.service;

import static org.junit.jupiter.api.Assertions.*;

import com.zerobase.cms.user.client.domain.SignUpForm;
import com.zerobase.cms.user.client.domain.model.Customer;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

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

    Customer c = service.signUpCustomerService(form);
    assertNotNull(service.signUpCustomerService(form).getId() != null);
  }

}