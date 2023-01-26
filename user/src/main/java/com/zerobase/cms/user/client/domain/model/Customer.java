package com.zerobase.cms.user.client.domain.model;

import com.zerobase.cms.user.client.domain.SignUpForm;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.AuditOverride;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
public class Customer extends BaseEntity {

  @Id
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String email;
  private String name;
  private String password;
  private String phone;
  private LocalDate birth;

  private LocalDateTime verifyExpiredAt;
  private String verificationCode;
  private boolean verify;

  public static Customer from(SignUpForm form) {
    return Customer.builder()
        .email(form.getEmail().toLowerCase(Locale.ROOT))
        .password(form.getPassword())
        .name(form.getName())
        .birth(form.getBirth())
        .phone(form.getPhone())
        .verify(false)
        .build();
  }
}
