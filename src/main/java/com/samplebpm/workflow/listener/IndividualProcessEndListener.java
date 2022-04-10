package com.samplebpm.workflow.listener;

import com.samplebpm.workflow.model.LoanModificationInfo;
import com.samplebpm.workflow.services.IndividualProcessUtilityService;
import com.samplebpm.workflow.services.PersistenceService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
		List<LoanModificationInfo> list = new ArrayList<>();
		for (LoanModificationInfo h : persistenceService.mergeHistory(qualifiedFilePath,
				c -> c.getCommitDetails().getMessage().contains(loanNumber),
				LoanModificationInfo.class)) {
			h.setHistory(null);
			list.add(h);
		}
		loanModificationInfo.setHistory(list);
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
