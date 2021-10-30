package com.example.workflow.listener;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.GenericUtilityService;
import com.example.workflow.services.PersistenceService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RenewalProcessEndListener implements ExecutionListener {

	@Autowired
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	GenericUtilityService genericUtilityService;

	@Autowired
	String loanVariableKey;

	@Autowired
	String proposalResponseVariableKey;

	@SneakyThrows
	@Override
	public void notify(DelegateExecution delegateExecution) {

		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = genericUtilityService.loanNumber(delegateExecution);
		ProcessInfo processInfo = persistenceService.get(genericUtilityService.getQualifiedFilePath(loanNumber, ProcessInfo.class),
				genericUtilityService.processInfoSha(delegateExecution),
				ProcessInfo.class);
		processInfo.setEndDateTime(LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME));
		processInfo.setHistory(persistenceService.history(processInfo.getLoanNumber(),
						c -> c.getCommitDetails().getMessage().contains(processInfo.getLoanNumber()),
						ProcessInfo.class)
				.stream().peek(h -> h.setHistory(null))
				.collect(Collectors.toList()));
		persistenceService.save(
				processInfo.getLoanNumber(),
				genericUtilityService.getQualifiedFilePath(processInfo.getLoanNumber(), ProcessInfo.class),
				processInfo,
				genericUtilityService.commitMessage(delegateExecution, false));
		persistenceService.merge(processInfo.getLoanNumber(), genericUtilityService.commitMessage(delegateExecution, true));
		delegateExecution.removeVariable(loanVariableKey);
		delegateExecution.removeVariable(proposalResponseVariableKey);
	}
}
