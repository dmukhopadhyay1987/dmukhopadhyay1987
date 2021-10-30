package com.example.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngines;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@EnableKafka
@Slf4j
public class ProcessTriggerEventListener {

	@KafkaListener(topics = "loanReadyForRenewal", groupId = "group")
	public void onMessage(String loanNumber) {
		log.info("Received Loan # {}", loanNumber);
		ProcessEngines.getDefaultProcessEngine()
				.getRuntimeService()
				.createProcessInstanceByKey("sample-bpm-project-process")
				.setVariable("loanNumber", loanNumber)
				.execute();
	}
}
