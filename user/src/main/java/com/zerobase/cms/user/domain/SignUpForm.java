package com.zerobase.cms.user.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpForm {

  private String email;
  private String name;
  private String password;
  private LocalDate birth;
  private String phone;
}
