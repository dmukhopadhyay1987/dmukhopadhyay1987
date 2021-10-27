package com.example.workflow.servicedelegates;

import com.example.workflow.model.LoanResponseDto;
import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.GenericUtilityService;
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
	GenericUtilityService genericUtilityService;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = genericUtilityService.loanNumber(delegateExecution);
		LoanResponseDto loanResponseDto = loanInfoService.getLoan(loanNumber);
		ProcessInfo processInfo = persistenceService.get(
				genericUtilityService.getQualifiedFilePath(
						loanNumber,
						ProcessInfo.class),
				genericUtilityService.processInfoSha(delegateExecution),
				ProcessInfo.class);
		processInfo.setLoanDetails(loanResponseDto);
		genericUtilityService.setBusinessKey(delegateExecution,
				loanNumber,
				persistenceService.save(
						processInfo.getLoanNumber(),
						genericUtilityService.getQualifiedFilePath(processInfo.getLoanNumber(), ProcessInfo.class),
						processInfo,
						delegateExecution.getCurrentActivityName()).getSha());
	}
}
