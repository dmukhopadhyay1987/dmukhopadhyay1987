package com.example.workflow.servicedelegates;

import com.example.workflow.model.LoanResponseDto;
import com.example.workflow.model.ProcessInfo;
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

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = (String) delegateExecution
				.getVariable("loanNumber");
		LoanResponseDto loanResponseDto = loanInfoService.getLoan(loanNumber);
		ProcessInfo processInfo = persistenceService.get(loanNumber, (String) delegateExecution.getVariable("processInfo"), ProcessInfo.class);
		processInfo.setLoanDetails(loanResponseDto);
		delegateExecution.setVariable("processInfo", persistenceService.save(
				processInfo.getLoanNumber(),
				processInfo.getLoanNumber(),
				processInfo,
				delegateExecution.getCurrentActivityName()).getSha());
	}
}
