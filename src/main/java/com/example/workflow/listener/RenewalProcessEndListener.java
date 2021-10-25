package com.example.workflow.listener;

import com.example.workflow.model.ProcessInfo;
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
public class RenewalProcessEndListener implements ExecutionListener {

	@Autowired
	private PersistenceService persistenceService;

	@Autowired
	private ProcessInfo processInfo;

	@Override
	public void notify(DelegateExecution delegateExecution) {

		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = (String) delegateExecution
				.getVariable("loanNumber");
		ProcessInfo processInfo = persistenceService.get(loanNumber, (String) delegateExecution.getVariable("processInfo"));
		processInfo.setEndDateTime(LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME));
		persistenceService.save(
				processInfo,
				delegateExecution.getProcessInstance().getProcessInstanceId());
		persistenceService.merge(processInfo.getProcessingDate(), delegateExecution.getCurrentActivityName());
		delegateExecution.removeVariable("loanNumber");
		delegateExecution.removeVariable("proposalResponseDto");
		delegateExecution.removeVariable("processInfo");
	}
}
