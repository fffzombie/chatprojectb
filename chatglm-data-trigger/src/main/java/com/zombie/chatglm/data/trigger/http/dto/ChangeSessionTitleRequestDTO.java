package com.zombie.chatglm.data.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeSessionTitleRequestDTO {

    private String sessionId;

    private String newTitle;

}
