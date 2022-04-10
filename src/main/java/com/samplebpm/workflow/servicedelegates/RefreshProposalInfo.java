package com.samplebpm.workflow.servicedelegates;

import com.samplebpm.workflow.model.LoanModificationInfo;
import com.samplebpm.workflow.model.LoanStatus;
import com.samplebpm.workflow.model.ProposalRequestDto;
import com.samplebpm.workflow.model.ProposalResponseDto;
import com.samplebpm.workflow.services.IndividualProcessUtilityService;
import com.samplebpm.workflow.services.PersistenceService;
import com.samplebpm.workflow.services.ProposalInfoService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Component
@Slf4j
public class RefreshProposalInfo implements JavaDelegate {

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
		String branchName = individualProcessUtilityService.getBranchName(loanNumber);
		LoanModificationInfo loanModificationInfo = persistenceService.get(qualifiedFilePath,
				branchName,
				LoanModificationInfo.class);
		if (loanModificationInfo.getProposalDetails() == null) {
			loanModificationInfo.setProposalDetails(proposalResponseDto);
			loanModificationInfo.setStatus(LoanStatus.OFFER_GENERATED);
			persistenceService.save(
					branchName,
					qualifiedFilePath,
					loanModificationInfo,
					individualProcessUtilityService.commitMessage(delegateExecution, false));
		}
		delegateExecution.removeVariable(proposalRequestVariableKey);
	}

	@ExceptionHandler({Exception.class})
	private void handleException(Exception e) {
		log.error(e.getCause().getLocalizedMessage());
		throw new BpmnError("renewalError", e.getCause().getLocalizedMessage(), e);
	}
}
