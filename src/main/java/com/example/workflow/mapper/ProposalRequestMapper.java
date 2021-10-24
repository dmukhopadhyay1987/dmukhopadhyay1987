package com.example.workflow.mapper;

import com.example.workflow.model.LoanResponseDto;
import com.example.workflow.model.ProposalRequestDto;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "proposalRequestMapper")
@Slf4j
public class ProposalRequestMapper implements JavaDelegate {

	@Autowired
	DozerBeanMapper dozerBeanMapper;

	@Autowired
	PersistenceService persistenceService;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		delegateExecution.getVariables().forEach((k, v) -> log.info("Variables >>> {} :: {}", k, v));
		String loanNumber = (String) delegateExecution.getVariable("loanNumber");
		LoanResponseDto loanResponseDto = persistenceService.get(loanNumber, (String) delegateExecution.getVariable("processInfo")).getLoanDetails();
		ProposalRequestDto proposalRequestDto = dozerBeanMapper.map(loanResponseDto, ProposalRequestDto.class);
		delegateExecution.setVariable("proposalRequestDto", proposalRequestDto);
		delegateExecution.getVariables().forEach((k, v) -> log.info("Variables >>> {} :: {}", k, v));
	}
}
