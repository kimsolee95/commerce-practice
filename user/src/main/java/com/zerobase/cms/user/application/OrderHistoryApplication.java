package com.zerobase.cms.user.application;

import com.zerobase.cms.user.client.MailgunClient;
import com.zerobase.cms.user.client.mailgun.SendMailForm;
import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.customer.OrderHistoryMailInfoForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.exception.ErrorCode;
import com.zerobase.cms.user.service.customer.SignUpCustomerService;
import com.zerobase.cms.user.service.seller.SignUpSellerService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderHistoryApplication {

  private final MailgunClient mailgunClient;
  private final String TYPE_CUSTOMER = "customer";

  public String sendOrderHistoryMail(OrderHistoryMailInfoForm form) {

    LocalDateTime now = LocalDateTime.now();

    SendMailForm sendMailForm = SendMailForm.builder()
        .from("ahhasolee@naver.com")
        .to(form.getEmail())
        .subject("ORDER HISTORY Email")
        .text(createEmailBody(form.getEmail(), TYPE_CUSTOMER))
        .build();
    mailgunClient.sendEmail(sendMailForm);
    return "주문 내역 알림 메일 발송에 성공하였습니다.";
  }

  private String createEmailBody(String email, String type) {

    StringBuilder builder = new StringBuilder();
    return builder.append("Hello ").append("! your order history info is here. \n\n")
        .append("==== ORDER INFO ==== \n\n")
        .append("==== to ==== \n\n")
        .append(type)
        .append("\n\n")
        .append(email).toString();
  }

}
