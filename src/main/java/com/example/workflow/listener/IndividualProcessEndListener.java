package com.example.workflow.listener;

import com.example.workflow.model.LoanModificationInfo;
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
	PersistenceService<LoanModificationInfo> persistenceService;

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
				LoanModificationInfo.class);
		LoanModificationInfo loanModificationInfo = persistenceService.get(qualifiedFilePath,
				individualProcessUtilityService.getBranchName(loanNumber),
				LoanModificationInfo.class);
		loanModificationInfo.setEndDateTime(LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME));
		loanModificationInfo.setHistory(persistenceService.mergeHistory(qualifiedFilePath,
						c -> c.getCommitDetails().getMessage().contains(loanNumber),
						LoanModificationInfo.class)
				.stream().peek(h -> h.setHistory(null))
				.collect(Collectors.toList()));
		persistenceService.save(
				individualProcessUtilityService.getBranchName(loanNumber),
				qualifiedFilePath,
				loanModificationInfo,
				individualProcessUtilityService.commitMessage(delegateExecution, false));
		persistenceService.merge(
				individualProcessUtilityService.getBranchName(loanNumber),
				individualProcessUtilityService.commitMessage(delegateExecution, true));
		delegateExecution.removeVariable(loanVariableKey);
		delegateExecution.removeVariable(proposalResponseVariableKey);
	}
}
