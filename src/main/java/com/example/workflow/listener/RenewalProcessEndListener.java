package com.example.workflow.listener;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.FilePathService;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@Slf4j
public class RenewalProcessEndListener implements ExecutionListener {

	@Autowired
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	FilePathService filePathService;

	@Override
	public void notify(DelegateExecution delegateExecution) {

		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = (String) delegateExecution
				.getVariable("loanNumber");
		ProcessInfo processInfo = persistenceService.get(filePathService.getQualifiedFilePath(loanNumber, ProcessInfo.class), (String) delegateExecution.getVariable("processInfo"), ProcessInfo.class);
		processInfo.setEndDateTime(LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME));
		persistenceService.save(
				processInfo.getLoanNumber(),
				filePathService.getQualifiedFilePath(processInfo.getLoanNumber(), ProcessInfo.class),
				processInfo,
				this.getClass().getSimpleName());
		persistenceService.merge(processInfo.getLoanNumber(), this.getClass().getSimpleName().toLowerCase(Locale.ROOT));
		delegateExecution.removeVariable("loanNumber");
		delegateExecution.removeVariable("proposalResponseDto");
		delegateExecution.removeVariable("processInfo");
	}
}
