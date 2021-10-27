package com.example.workflow.mapper;

import com.example.workflow.model.LoanResponseDto;
import com.example.workflow.model.ProcessInfo;
import com.example.workflow.model.ProposalRequestDto;
import com.example.workflow.services.GenericUtilityService;
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
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	GenericUtilityService genericUtilityService;

	@Autowired
	String proposalRequestVariableKey;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = genericUtilityService.loanNumber(delegateExecution);
		LoanResponseDto loanResponseDto = persistenceService.get(
				genericUtilityService.getQualifiedFilePath(loanNumber, ProcessInfo.class),
				genericUtilityService.processInfoSha(delegateExecution),
				ProcessInfo.class).getLoanDetails();
		ProposalRequestDto proposalRequestDto = dozerBeanMapper.map(loanResponseDto, ProposalRequestDto.class);
		delegateExecution.setVariable(proposalRequestVariableKey, proposalRequestDto);
	}
}
