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
	PersistenceService persistenceService;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = (String) delegateExecution
				.getVariable("loanNumber");
		LoanResponseDto loanResponseDto = loanInfoService.getLoan(loanNumber);
//		delegateExecution.setVariable("loanResponseDto",
//				loanResponseDto);
		ProcessInfo processInfo = persistenceService.get(loanNumber, (String) delegateExecution.getVariable("processInfo"));
		processInfo.setLoanDetails(loanResponseDto);
		delegateExecution.setVariable("processInfo", persistenceService.save(
				processInfo,
				delegateExecution.getCurrentActivityName()).getSha());
	}
}
