package com.example.workflow.servicedelegates;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.model.ProposalRequestDto;
import com.example.workflow.model.ProposalResponseDto;
import com.example.workflow.services.GenericUtilityService;
import com.example.workflow.services.PersistenceService;
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

	@Autowired
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	GenericUtilityService genericUtilityService;

	@Autowired
	String proposalRequestVariableKey;

	@Autowired
	String proposalResponseVariableKey;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = genericUtilityService.loanNumber(delegateExecution);
		ProposalResponseDto proposalResponseDto = proposalInfoService.getProposal(
				(ProposalRequestDto) delegateExecution
						.getVariable(proposalRequestVariableKey));
		delegateExecution.setVariable(proposalResponseVariableKey,
				proposalResponseDto);
		String qualifiedFilePath = genericUtilityService.getQualifiedLoanFilePath(
				loanNumber,
				ProcessInfo.class);
		ProcessInfo processInfo = persistenceService.get(qualifiedFilePath,
				genericUtilityService.processInfoSha(delegateExecution),
				ProcessInfo.class);
		if (processInfo.getProposalDetails() == null) {
			processInfo.setProposalDetails(proposalResponseDto);
			processInfo.setStatus("Offer Generated");
			genericUtilityService.setBusinessKey(delegateExecution,
					loanNumber,
					persistenceService.save(
							genericUtilityService.getBranchName(loanNumber),
							qualifiedFilePath,
							processInfo,
							genericUtilityService.commitMessage(delegateExecution, false)).getSha());
		}
		delegateExecution.removeVariable(proposalRequestVariableKey);
	}
}
