package com.example.workflow.listener;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.services.IndividualProcessUtilityService;
import com.example.workflow.services.PersistenceService;
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
	private PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	private ProcessInfo processInfo;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@Autowired
	String loanVariableKey;

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = (String) delegateExecution.getVariable(loanVariableKey);
		if (processInfo.getStartDateTime() == null) {
			processInfo.setStartDateTime(LocalDateTime.now().format(
					DateTimeFormatter.ISO_DATE_TIME));
		}
		processInfo.setLoanNumber(loanNumber);
		if (processInfo.getStatus() == null ||
				(processInfo.getStatus() != null && !processInfo.getStatus().equals("Offer Generated"))) {
			processInfo.setStatus("Ready for Renewal");
		}
		individualProcessUtilityService.setBusinessKey(delegateExecution,
				loanNumber,
				persistenceService.save(
						individualProcessUtilityService.getBranchName(loanNumber),
						individualProcessUtilityService.getQualifiedLoanFilePath(loanNumber, ProcessInfo.class),
						processInfo,
						individualProcessUtilityService.commitMessage(delegateExecution, false)).getSha());
	}
}
