package com.samplebpm.workflow.servicedelegates;

import com.samplebpm.workflow.model.LoanModificationInfo;
import com.samplebpm.workflow.model.LoanStatus;
import com.samplebpm.workflow.services.IndividualProcessUtilityService;
import com.samplebpm.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Component
@Slf4j
public class RenewLoan implements JavaDelegate {

	@Autowired
	PersistenceService<LoanModificationInfo> persistenceService;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = individualProcessUtilityService.loanNumber(delegateExecution);
		String qualifiedFilePath = individualProcessUtilityService.getQualifiedLoanFilePath(loanNumber, LoanModificationInfo.class);
		String branchName = individualProcessUtilityService.getBranchName(loanNumber);
		LoanModificationInfo loanModificationInfo = persistenceService.get(
				qualifiedFilePath,
				branchName,
				LoanModificationInfo.class);
		loanModificationInfo.setStatus(LoanStatus.RENEWED);
		persistenceService.save(
				branchName,
				qualifiedFilePath,
				loanModificationInfo,
				individualProcessUtilityService.commitMessage(delegateExecution, false));
	}

	@ExceptionHandler({Exception.class})
	private void handleException(Exception e) {
		log.error(e.getCause().getLocalizedMessage());
		throw new BpmnError("renewalError", e.getCause().getLocalizedMessage(), e);
	}
}
