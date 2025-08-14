package com.shinhan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shinhan.entity.CardEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "card_design")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDesignEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "design_id")
    private Long designId;

    @Column(name = "layout_id", nullable = false)
    private Integer layoutId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "letter_color", nullable = false)
    private int letterColor;

    @Column(name = "bg_image_url")
    private String bgImageUrl;

    @Column(name = "card_back_color")
    private String cardBackColor;

    @Column(name = "logo_grayscale")
    private boolean logoGrayscale;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "card_id", nullable = false)  // 참조 대상 컬럼과 이름 및 타입이 일치해야 함
    private CardEntity card;
}
