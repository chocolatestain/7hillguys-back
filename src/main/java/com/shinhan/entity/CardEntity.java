package com.shinhan.entity;

import com.shinhan.entity.CardDesignEntity;
import com.shinhan.entity.CardStatus;
import com.shinhan.peoch.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")  // 컬럼 이름을 명시적으로 card_id로 설정
    private Long cardId;

    @Column(unique = true, length = 16)
    private String cardNumber;

    private String enName;

    @Column(length = 4)
    private String password;

    @Column(length = 3)
    private String cvc;

    @Column(length = 5)
    private String issuedDate; // MM/YY

    @Column(length = 5)
    private String expirationDate; // MM/YY

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private Integer monthlyAllowance;
    private Integer tempAllowance;
    private Integer monthlySpent;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "userId")
    private UserEntity user;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CardDesignEntity> cardDesigns;
}
