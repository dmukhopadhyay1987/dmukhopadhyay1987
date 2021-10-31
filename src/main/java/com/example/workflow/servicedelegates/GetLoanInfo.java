package com.example.workflow.servicedelegates;

import com.example.workflow.model.LoanResponseDto;
import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.IndividualProcessUtilityService;
import com.example.workflow.services.LoanInfoService;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GetLoanInfo implements JavaDelegate {

	@Autowired
	LoanInfoService loanInfoService;

	@Autowired
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = individualProcessUtilityService.loanNumber(delegateExecution);
		LoanResponseDto loanResponseDto = loanInfoService.getLoan(loanNumber);
		String qualifiedFilePath = individualProcessUtilityService.getQualifiedLoanFilePath(
				loanNumber,
				ProcessInfo.class);
		ProcessInfo processInfo = persistenceService.get(
				qualifiedFilePath,
				individualProcessUtilityService.processInfoSha(delegateExecution),
				ProcessInfo.class);
		if (processInfo.getLoanDetails() == null) {
			processInfo.setLoanDetails(loanResponseDto);
			individualProcessUtilityService.setBusinessKey(delegateExecution,
					loanNumber,
					persistenceService.save(
							individualProcessUtilityService.getBranchName(loanNumber),
							qualifiedFilePath,
							processInfo,
							individualProcessUtilityService.commitMessage(delegateExecution, false)).getSha());
		}
	}
}
