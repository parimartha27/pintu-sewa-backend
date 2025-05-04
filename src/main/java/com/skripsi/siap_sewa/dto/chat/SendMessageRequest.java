package com.skripsi.siap_sewa.dto.chat;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SendMessageRequest {
    @NotBlank(message = "Message Tidak Boleh Kosong")
    private String message;

    @NotBlank(message = "Type Pengirim tidak boleh kosong")
    private String senderType;

    @NotBlank(message = "Room Chat Id tidak boleh kosong")
    private String roomChatId;
}
