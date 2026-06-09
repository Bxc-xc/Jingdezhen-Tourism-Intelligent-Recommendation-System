package com.jdz.tourism.service;

import com.jdz.tourism.entity.Marketplace;
import com.jdz.tourism.repository.MarketplaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MarketplaceService {

    @Autowired
    private MarketplaceRepository marketplaceRepository;

    public List<Marketplace> getAllMarketplacesForAdmin() {
        return marketplaceRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    public List<Marketplace> getEnabledMarketplaces() {
        return marketplaceRepository.findAllByEnabledTrueOrderBySortOrderAscIdAsc();
    }

    public Optional<Marketplace> getById(Long id) {
        return marketplaceRepository.findById(id);
    }

    public Marketplace create(Marketplace marketplace) {
        marketplace.setId(null);
        fillCoverIfMissing(marketplace);
        return marketplaceRepository.save(marketplace);
    }

    public Marketplace update(Long id, Marketplace marketplace) {
        Marketplace existing = marketplaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("市集不存在，ID: " + id));

        if (marketplace.getName() != null) existing.setName(marketplace.getName());
        existing.setOpenTime(marketplace.getOpenTime());
        existing.setAddress(marketplace.getAddress());
        existing.setDescription(marketplace.getDescription());
        existing.setCarouselImages(marketplace.getCarouselImages());
        existing.setCoverImage(marketplace.getCoverImage());
        existing.setSortOrder(marketplace.getSortOrder());
        existing.setEnabled(marketplace.getEnabled());

        fillCoverIfMissing(existing);
        return marketplaceRepository.save(existing);
    }

    public void delete(Long id) {
        if (!marketplaceRepository.existsById(id)) {
            throw new RuntimeException("市集不存在，ID: " + id);
        }
        marketplaceRepository.deleteById(id);
    }

    private void fillCoverIfMissing(Marketplace marketplace) {
        if (marketplace == null) return;
        if (marketplace.getCoverImage() != null && !marketplace.getCoverImage().isBlank()) return;
        String imgs = marketplace.getCarouselImages();
        if (imgs == null || imgs.isBlank()) return;
        String first = imgs.split(",")[0].trim();
        if (!first.isBlank()) {
            marketplace.setCoverImage(first);
        }
    }
}


