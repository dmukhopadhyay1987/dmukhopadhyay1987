package com.example.workflow.listener;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.IndividualProcessUtilityService;
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
public class IndividualProcessEndListener implements ExecutionListener {

	@Autowired
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@Autowired
	String loanVariableKey;

	@Autowired
	String proposalResponseVariableKey;

	@SneakyThrows
	@Override
	public void notify(DelegateExecution delegateExecution) {

		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = individualProcessUtilityService.loanNumber(delegateExecution);
		String qualifiedFilePath = individualProcessUtilityService.getQualifiedLoanFilePath(
				loanNumber,
				ProcessInfo.class);
		ProcessInfo processInfo = persistenceService.get(qualifiedFilePath,
				individualProcessUtilityService.processInfoSha(delegateExecution),
				ProcessInfo.class);
		processInfo.setEndDateTime(LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME));
		processInfo.setHistory(persistenceService.history(qualifiedFilePath,
						c -> c.getCommitDetails().getMessage().contains(loanNumber),
						ProcessInfo.class)
				.stream().peek(h -> h.setHistory(null))
				.collect(Collectors.toList()));
		persistenceService.save(
				individualProcessUtilityService.getBranchName(loanNumber),
				qualifiedFilePath,
				processInfo,
				individualProcessUtilityService.commitMessage(delegateExecution, false));
		persistenceService.merge(
				individualProcessUtilityService.getBranchName(loanNumber),
				individualProcessUtilityService.commitMessage(delegateExecution, true));
		delegateExecution.removeVariable(loanVariableKey);
		delegateExecution.removeVariable(proposalResponseVariableKey);
	}
}
