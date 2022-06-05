package com.samplebpm.workflow.listener;

import com.samplebpm.workflow.model.LoanModificationInfo;
import com.samplebpm.workflow.model.LoanStatus;
import com.samplebpm.workflow.services.IndividualProcessUtilityService;
import com.iwonosql.service.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class IndividualProcessStartListener implements ExecutionListener {

	@Autowired
	private PersistenceService<LoanModificationInfo> persistenceService;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@Autowired
	String loanVariableKey;

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = (String) delegateExecution.getVariable(loanVariableKey);
		if (loanNumber.contains("run_")) {
			throw new RuntimeException();
		}
		individualProcessUtilityService.setBusinessKey(delegateExecution,
				loanNumber);
		persistenceService.save(
						individualProcessUtilityService.getBranchName(loanNumber),
						individualProcessUtilityService.getQualifiedLoanFilePath(loanNumber, LoanModificationInfo.class),
						getLoanModification(loanNumber),
						individualProcessUtilityService.commitMessage(delegateExecution, false));
	}

	private LoanModificationInfo getLoanModification(String loanNumber) {
		return new LoanModificationInfo(
				loanNumber, LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME),
				null, null, null,
				LoanStatus.READY_FOR_RENEWAL,
				null);
	}
}
