package com.skripsi.siap_sewa.dto.chat;

import com.skripsi.siap_sewa.enums.MessageTypeEnum;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageRequest {

    private MessageTypeEnum type;
    private String content;
    private String sender;

}