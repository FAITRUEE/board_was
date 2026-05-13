package com.example.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;  // 태그 이름 (예: "여행", "맛집")

    @Column(nullable = false)
    @Builder.Default
    private Integer useCount = 0;  // 사용 횟수 (인기도)

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 사용 횟수 증가
    public void incrementUseCount() {
        this.useCount++;
    }

    // 사용 횟수 감소
    public void decrementUseCount() {
        if (this.useCount > 0) {
            this.useCount--;
        }
    }
}