package com.skripsi.siap_sewa.dto.transaction;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProcessShippingDetailResponse {
    private String shippingPartner;
    private String estimatedTime;
    private String shippingCode;
    private String customerName;
    private String shippingAddress;
    private List<FlowShippingResponse> ShippingFlow;
}
