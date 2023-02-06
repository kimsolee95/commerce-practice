package com.zerobase.cms.order.client.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderHistoryMailInfoForm {

  private String email;
  private String itemName;
  private Integer money;
}
