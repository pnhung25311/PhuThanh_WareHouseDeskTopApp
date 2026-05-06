package com.phuthanh.business.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phuthanh.model.business.ProductBusiness;
import com.phuthanh.network.ApiClient;

public class BusinessService {
    private final ApiClient api = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<ProductBusiness> getAllProducts() throws Exception {
        String json = api.get("business/get-all");
        return mapper.readValue(json, new TypeReference<List<ProductBusiness>>() {
        });
    }
}
