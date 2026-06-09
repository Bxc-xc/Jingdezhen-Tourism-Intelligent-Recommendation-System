package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_favorite")
public class MarketplaceFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "marketplace_id", nullable = false)
    private Marketplace marketplace;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    public MarketplaceFavorite() {}

    public MarketplaceFavorite(User user, Marketplace marketplace) {
        this.user = user;
        this.marketplace = marketplace;
        this.createTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Marketplace getMarketplace() { return marketplace; }
    public void setMarketplace(Marketplace marketplace) { this.marketplace = marketplace; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
