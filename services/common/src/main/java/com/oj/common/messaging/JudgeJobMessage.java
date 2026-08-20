package com.oj.common.messaging;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgeJobMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long submissionId;
    private boolean priority;
    private Long warRoomId;
}
