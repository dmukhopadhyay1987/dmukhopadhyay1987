package com.example.workflow.listener;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.GenericUtilityService;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class RenewalProcessStartListener implements ExecutionListener {

	@Autowired
	private PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	private ProcessInfo processInfo;

	@Autowired
	GenericUtilityService genericUtilityService;

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = (String) delegateExecution.getVariable("loanNumber");
		processInfo.setLoanNumber(loanNumber);
		processInfo.setStartDateTime(LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME));
		genericUtilityService.setBusinessKey(delegateExecution,
				loanNumber,
				persistenceService.save(
						processInfo.getLoanNumber(),
						genericUtilityService.getQualifiedFilePath(processInfo.getLoanNumber(), ProcessInfo.class),
						processInfo,
						delegateExecution.getCurrentActivityName()).getSha());
	}
}
