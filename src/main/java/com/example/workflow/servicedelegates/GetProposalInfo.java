package com.example.workflow.servicedelegates;

import com.example.workflow.model.ProposalRequestDto;
import com.example.workflow.services.ProposalInfoService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GetProposalInfo implements JavaDelegate {

	@Autowired
	ProposalInfoService proposalInfoService;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		delegateExecution.setVariable("proposalResponseDto",
				proposalInfoService.getProposal((ProposalRequestDto) delegateExecution
						.getVariable("proposalRequestDto")));
		delegateExecution.removeVariable("proposalRequestDto");
	}
}
