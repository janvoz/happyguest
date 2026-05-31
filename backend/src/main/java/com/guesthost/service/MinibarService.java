package com.guesthost.service;

import com.guesthost.dto.MinibarItemDto;
import com.guesthost.dto.OrderLineDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.MinibarItem;
import com.guesthost.model.OrderLine;
import com.guesthost.repository.MinibarItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinibarService {

    private final MinibarItemRepository minibarItemRepository;
    private final PropertyService propertyService;

    public List<MinibarItem> getItemsForHost(String hostEmail, String propertyId) {
        propertyService.getOwnedProperty(hostEmail, propertyId);
        return minibarItemRepository.findAllByPropertyId(propertyId);
    }

    public List<MinibarItem> getPublicItems(String propertyId) {
        return minibarItemRepository.findAllByPropertyId(propertyId);
    }

    public MinibarItem createItem(String hostEmail, MinibarItemDto dto) {
        propertyService.getOwnedProperty(hostEmail, dto.getPropertyId());
        MinibarItem item = MinibarItem.builder()
                .propertyId(dto.getPropertyId())
                .name(dto.getName())
                .price(dto.getPrice())
                .stockCount(dto.getStockCount())
                .imageUrl(dto.getImageUrl())
                .build();
        return minibarItemRepository.save(item);
    }

    public MinibarItem updateItem(String hostEmail, String itemId, MinibarItemDto dto) {
        MinibarItem existing = minibarItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Minibar item not found: " + itemId));
        propertyService.getOwnedProperty(hostEmail, existing.getPropertyId());
        existing.setName(dto.getName());
        existing.setPrice(dto.getPrice());
        existing.setStockCount(dto.getStockCount());
        existing.setImageUrl(dto.getImageUrl());
        return minibarItemRepository.save(existing);
    }

    public void deleteItem(String hostEmail, String itemId) {
        MinibarItem existing = minibarItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Minibar item not found: " + itemId));
        propertyService.getOwnedProperty(hostEmail, existing.getPropertyId());
        minibarItemRepository.delete(existing);
    }

    public List<OrderLine> buildOrderLinesAndDecrementStock(String propertyId, List<OrderLineDto> requestLines) {
        List<OrderLine> lines = new ArrayList<>();
        for (OrderLineDto lineDto : requestLines) {
            MinibarItem item = minibarItemRepository.findById(lineDto.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Minibar item not found: " + lineDto.getItemId()));
            if (!item.getPropertyId().equals(propertyId)) {
                throw new IllegalArgumentException("Minibar item does not belong to the selected property");
            }
            if (item.getStockCount() < lineDto.getQuantity()) {
                throw new IllegalStateException("Not enough stock for item: " + item.getName());
            }
            item.setStockCount(item.getStockCount() - lineDto.getQuantity());
            minibarItemRepository.save(item);
            lines.add(OrderLine.builder()
                    .itemId(item.getId())
                    .name(item.getName())
                    .quantity(lineDto.getQuantity())
                    .unitPrice(item.getPrice())
                    .build());
        }
        return lines;
    }
}
