package com.oj.judge.listener;

import com.oj.common.messaging.JudgeJobMessage;
import com.oj.judge.service.JudgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class JudgeJobListener {

    private static final Logger log = LoggerFactory.getLogger(JudgeJobListener.class);

    private final JudgeService judgeService;

    public JudgeJobListener(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @RabbitListener(queues = "${app.queues.practice}")
    public void onPracticeJob(JudgeJobMessage message) {
        log.info("Received practice judge job for submission {}", message != null ? message.getSubmissionId() : null);
        judgeService.judge(message);
    }

    @RabbitListener(queues = "${app.queues.war}")
    public void onWarJob(JudgeJobMessage message) {
        log.info("Received war judge job for submission {}", message != null ? message.getSubmissionId() : null);
        judgeService.judge(message);
    }
}
