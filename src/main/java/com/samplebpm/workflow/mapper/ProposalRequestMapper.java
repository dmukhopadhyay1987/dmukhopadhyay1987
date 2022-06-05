package com.samplebpm.workflow.mapper;

import com.samplebpm.workflow.model.LoanModificationInfo;
import com.samplebpm.workflow.model.LoanResponseDto;
import com.samplebpm.workflow.model.ProposalRequestDto;
import com.samplebpm.workflow.services.IndividualProcessUtilityService;
import com.iwonosql.service.PersistenceService;
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
	PersistenceService<LoanModificationInfo> persistenceService;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@Autowired
	String proposalRequestVariableKey;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = individualProcessUtilityService.loanNumber(delegateExecution);
		LoanResponseDto loanResponseDto = persistenceService.get(
				individualProcessUtilityService.getQualifiedLoanFilePath(loanNumber, LoanModificationInfo.class),
				individualProcessUtilityService.getBranchName(loanNumber),
				LoanModificationInfo.class).getLoanDetails();
		ProposalRequestDto proposalRequestDto = dozerBeanMapper.map(loanResponseDto, ProposalRequestDto.class);
		delegateExecution.setVariable(proposalRequestVariableKey, proposalRequestDto);
		log.info("Mapped");
	}
}
