package com.example.workflow.listener;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class RenewalProcessStartListener implements ExecutionListener {

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private ProcessInfo processInfo;

	@Override
	public void notify(DelegateExecution delegateExecution) {

		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		Assert.assertTrue(delegateExecution.getVariableNames().contains("loanNumber"));
		processInfo.setLoanNumber((String) delegateExecution.getVariable("loanNumber"));
		processInfo.setProcessingDate(LocalDate.now().format(
				DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		processInfo.setStartDateTime(LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME));
		delegateExecution.setVariable("processInfo", persistenceService.save(
				processInfo,
				delegateExecution.getCurrentActivityName()).getSha());
	}
}
