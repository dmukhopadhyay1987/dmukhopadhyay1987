package com.example.workflow.servicedelegates;

import com.example.workflow.model.LoanModificationInfo;
import com.example.workflow.model.LoanStatus;
import com.example.workflow.model.ProposalRequestDto;
import com.example.workflow.model.ProposalResponseDto;
import com.example.workflow.services.IndividualProcessUtilityService;
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
	PersistenceService<LoanModificationInfo> persistenceService;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@Autowired
	String proposalRequestVariableKey;

	@Autowired
	String proposalResponseVariableKey;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = individualProcessUtilityService.loanNumber(delegateExecution);
		ProposalResponseDto proposalResponseDto = proposalInfoService.getProposal(
				(ProposalRequestDto) delegateExecution
						.getVariable(proposalRequestVariableKey));
		delegateExecution.setVariable(proposalResponseVariableKey,
				proposalResponseDto);
		String qualifiedFilePath = individualProcessUtilityService.getQualifiedLoanFilePath(
				loanNumber,
				LoanModificationInfo.class);
		LoanModificationInfo loanModificationInfo = persistenceService.get(qualifiedFilePath,
				individualProcessUtilityService.processInfoSha(delegateExecution),
				LoanModificationInfo.class);
		if (loanModificationInfo.getProposalDetails() == null) {
			loanModificationInfo.setProposalDetails(proposalResponseDto);
			loanModificationInfo.setStatus(LoanStatus.OFFER_GENERATED);
			individualProcessUtilityService.setBusinessKey(delegateExecution,
					loanNumber,
					persistenceService.save(
							individualProcessUtilityService.getBranchName(loanNumber),
							qualifiedFilePath,
							loanModificationInfo,
							individualProcessUtilityService.commitMessage(delegateExecution, false)).getSha());
		}
		delegateExecution.removeVariable(proposalRequestVariableKey);
	}
}
