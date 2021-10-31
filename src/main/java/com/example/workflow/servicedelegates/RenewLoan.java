package com.example.workflow.servicedelegates;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.GenericUtilityService;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RenewLoan implements JavaDelegate {

	@Autowired
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	GenericUtilityService genericUtilityService;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = genericUtilityService.loanNumber(delegateExecution);
		String qualifiedFilePath = genericUtilityService.getQualifiedLoanFilePath(loanNumber, ProcessInfo.class);
		ProcessInfo processInfo = persistenceService.get(
				qualifiedFilePath,
				genericUtilityService.processInfoSha(delegateExecution),
				ProcessInfo.class);
		processInfo.setStatus("Renewed");
		genericUtilityService.setBusinessKey(delegateExecution,
				loanNumber,
				persistenceService.save(
						genericUtilityService.getBranchName(loanNumber),
						qualifiedFilePath,
						processInfo,
						genericUtilityService.commitMessage(delegateExecution, false)).getSha());
	}

}
