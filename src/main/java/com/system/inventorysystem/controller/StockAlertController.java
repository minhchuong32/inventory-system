package com.system.inventorysystem.controller;

import com.system.inventorysystem.dto.BellNotificationDto;
import com.system.inventorysystem.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class StockAlertController {

    private final StockAlertService stockAlertService;

    @GetMapping
    public List<BellNotificationDto> alerts() {
        return stockAlertService.getNotifications();
    }
}