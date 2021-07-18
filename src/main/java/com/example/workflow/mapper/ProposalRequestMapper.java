package com.example.workflow.mapper;

import com.example.workflow.model.ProposalRequestDto;
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

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		delegateExecution.getVariables().forEach((k, v) -> log.info("Variables >>> {} :: {}", k, v));
		delegateExecution.setVariable("proposalRequestDto", dozerBeanMapper.map(delegateExecution.getVariable("loanResponseDto"), ProposalRequestDto.class));
		delegateExecution.getVariables().forEach((k, v) -> log.info("Variables >>> {} :: {}", k, v));
	}
}
