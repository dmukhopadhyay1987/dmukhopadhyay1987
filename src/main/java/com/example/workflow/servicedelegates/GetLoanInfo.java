package com.example.workflow.servicedelegates;

import com.example.workflow.services.LoanInfoService;
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

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		delegateExecution.setVariable("loanResponseDto",
				loanInfoService.getLoan((String) delegateExecution
						.getVariable("loanNumber")));
		delegateExecution.removeVariable("loanNumber");
	}
}
