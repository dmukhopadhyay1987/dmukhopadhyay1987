package com.example.listener;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableKafka
@Slf4j
public class ProcessTriggerEventListener {

	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	List<String> loans;

	@Autowired
	String loansVariableKey;

	@KafkaListener(topics = "loanReadyForRenewal", groupId = "group")
	public void onMessage(String loanNumber) {
		log.info("Received Loan # {}", loanNumber);
		if (loanNumber.equals("START")) {
			loans = new ArrayList<>();
		} else if (!loanNumber.equals("END")) {
			loans.add(loanNumber);
		} else {
			ProcessEngines.getDefaultProcessEngine()
					.getRuntimeService()
					.createProcessInstanceByKey("burstRenewalProcess")
					.setVariable(loansVariableKey, Variables.objectValue(loans)
							.serializationDataFormat(Variables.SerializationDataFormats.JSON)
							.create())
					.execute();
			loans = null;
		}
	}
}
