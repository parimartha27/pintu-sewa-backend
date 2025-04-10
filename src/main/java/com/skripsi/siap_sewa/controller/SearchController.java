package com.skripsi.siap_sewa.controller;


import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse> searchShopAndProduct(@RequestParam String keyword) {
        return searchService.searchShopAndProduct(keyword);
    }
}