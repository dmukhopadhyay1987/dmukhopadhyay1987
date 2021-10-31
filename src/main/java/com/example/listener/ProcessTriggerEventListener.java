package com.example.listener;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.GenericUtilityService;
import com.example.workflow.services.PersistenceService;
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

	@Autowired
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	GenericUtilityService genericUtilityService;

	@KafkaListener(topics = "loanReadyForRenewal", groupId = "group")
	public void onMessage(String loanNumber) {
		log.info("Received Loan # {}", loanNumber);
		if (loanNumber.equals("START")) {
			loans = new ArrayList<>();
		} else if (loanNumber.equals("FIX")) {
			loans = new ArrayList<>();
			persistenceService.branches().forEach(b -> loans.add(
					genericUtilityService.retrieveLoanNumber(
							b.getName())));
			startBurstRenewalProcess();
		} else if (!loanNumber.equals("END")) {
			loans.add(loanNumber);
		} else {
			startBurstRenewalProcess();
		}
	}

	private void startBurstRenewalProcess() {
		ProcessEngines.getDefaultProcessEngine()
				.getRuntimeService()
				.createProcessInstanceByKey("burstRenewalProcess")
				.setVariable(loansVariableKey, Variables.objectValue(loans)
						.serializationDataFormat(Variables.SerializationDataFormats.JSON)
						.create())
				.execute();
	}
}
