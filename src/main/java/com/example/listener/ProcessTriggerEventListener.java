package com.example.listener;

import com.example.workflow.model.LoanModificationInfo;
import com.example.workflow.services.IndividualProcessUtilityService;
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
import java.util.stream.Collectors;

@Component
@EnableKafka
@Slf4j
public class ProcessTriggerEventListener {

	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	List<String> loans;

	@Autowired
	String loansVariableKey;

	@Autowired
	PersistenceService<LoanModificationInfo> persistenceService;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@KafkaListener(topics = "loanReadyForRenewal", groupId = "group")
	public void onMessage(String message) {
		log.info("Received Loan # {}", message);
		if (!message.equals("END")) {
			if (loans == null) loans = new ArrayList<>();
			persistenceService.branches().stream()
					.filter(b -> !b.getName().contains("run_"))
					.collect(Collectors.toList())
					.forEach(b -> loans.add(
							individualProcessUtilityService.retrieveLoanNumber(
									b.getName())));
			loans.add(message);
		} else {
			startBurstRenewalProcess();
		}
	}

	private void startBurstRenewalProcess() {
		if(loans!= null && !loans.isEmpty()) {
			ProcessEngines.getDefaultProcessEngine()
					.getRuntimeService()
					.createProcessInstanceByKey("burstRenewalProcess")
					.setVariable(loansVariableKey, Variables.objectValue(loans)
							.serializationDataFormat(Variables.SerializationDataFormats.JSON)
							.create())
					.execute();
		}
	}
}
