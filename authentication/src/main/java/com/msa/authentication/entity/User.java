package com.msa.authentication.entity;

import com.msa.authentication.dto.request.JoinRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "USER")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;

    private String password;

    private String name;

    private String email;

    private String phone;

    @Builder.Default
    private Boolean active = true;

    // 도메인 로직: 비밀번호 검증
    public boolean verifyPassword(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }

    // 도메인 로직: 사용자 활성 상태 확인
    public boolean isActive() {
        return this.active != null && this.active;
    }

    // 도메인 로직: 사용자 정보 유효성 검증
    public boolean isValidUser() {
        return this.username != null && !this.username.trim().isEmpty() &&
               this.password != null && !this.password.trim().isEmpty() &&
               this.email != null && !this.email.trim().isEmpty();
    }

    // 도메인 로직: 사용자 비활성화
    public void deactivate() {
        this.active = false;
    }

    // 도메인 로직: 사용자 활성화
    public void activate() {
        this.active = true;
    }

    // 도메인 로직: 비밀번호 변경 (해싱된 비밀번호로)
    public User changePassword(String newHashedPassword) {
        return User.builder()
                .id(this.id)
                .username(this.username)
                .password(newHashedPassword)
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .active(this.active)
                .build();
    }
}
