package com.jdz.tourism.service;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.RoomType;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    /** 商家端：查自己的房型 */
    public List<RoomType> getRoomTypesByMerchantId(Long merchantId) {
        return roomTypeRepository.findByMerchantId(merchantId);
    }

    /** 管理员端：查所有房型 */
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAllWithMerchant();
    }

    public Optional<RoomType> getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id);
    }

    public RoomType createRoomType(Long merchantId, RoomType roomType) {
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new RuntimeException("商家不存在"));
        roomType.setMerchant(merchant);
        return roomTypeRepository.save(roomType);
    }

    public RoomType updateRoomType(Long id, RoomType patch) {
        RoomType existing = roomTypeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("房型不存在"));
        existing.setName(patch.getName());
        existing.setDescription(patch.getDescription());
        existing.setPrice(patch.getPrice());
        existing.setStock(patch.getStock() != null ? patch.getStock() : existing.getStock());
        existing.setArea(patch.getArea());
        existing.setBedType(patch.getBedType());
        existing.setBreakfast(patch.getBreakfast() != null ? patch.getBreakfast() : existing.getBreakfast());
        if (patch.getImages() != null) existing.setImages(patch.getImages());
        return roomTypeRepository.save(existing);
    }

    public void deleteRoomType(Long id) {
        roomTypeRepository.deleteById(id);
    }
}
