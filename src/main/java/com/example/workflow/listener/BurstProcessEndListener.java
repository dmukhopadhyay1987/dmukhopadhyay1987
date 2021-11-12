package com.example.workflow.listener;

import com.example.workflow.model.LoanModificationInfo;
import com.example.workflow.model.LoanModificationStageInfo;
import com.example.workflow.model.LoanReportInfo;
import com.example.workflow.model.ReportInfo;
import com.example.workflow.services.BurstProcessUtilityService;
import com.example.workflow.services.IndividualProcessUtilityService;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BurstProcessEndListener implements ExecutionListener {

	@Autowired
	String loansVariableKey;

	@Autowired
	PersistenceService<ReportInfo> burstPersistenceService;

	@Autowired
	PersistenceService<LoanModificationInfo> individualPersistenceService;

	@Autowired
	BurstProcessUtilityService burstProcessUtilityService;

	@Autowired
	IndividualProcessUtilityService individualProcessUtilityService;

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		List<String> loanNumbers = 	(List<String>) delegateExecution.getVariable(loansVariableKey);
		String processId = burstProcessUtilityService.processId(delegateExecution);
		ReportInfo reportInfo = burstPersistenceService.get(burstProcessUtilityService.getQualifiedReportFilePath(
						processId,
						ReportInfo.class),
				burstProcessUtilityService.getBranchName(processId),
				ReportInfo.class);
		List<LoanReportInfo> loanReportInfos = loanNumbers.stream()
				.map(l -> LoanReportInfo.builder()
						.loanNumber(l)
						.stages(individualPersistenceService.history(
										individualProcessUtilityService.getQualifiedLoanFilePath(l, LoanModificationInfo.class),
										c -> c.getCommitDetails().getAuthor().getDate() != null,
										LoanModificationInfo.class)
								.entrySet().stream()
								.map(lm -> LoanModificationStageInfo.builder()
										.dateTime(lm.getKey())
										.loanModificationInfo(lm.getValue())
										.build())
								.toList())
						.build()
				).collect(Collectors.toList());
		if (reportInfo.getLoanReportInfos() != null) {
			loanReportInfos.addAll(reportInfo.getLoanReportInfos());
		}
		burstPersistenceService.save(burstProcessUtilityService.getBranchName(processId),
				burstProcessUtilityService.getQualifiedReportFilePath(processId, ReportInfo.class),
				ReportInfo.builder()
						.startDateTime(reportInfo.getStartDateTime())
						.loanReportInfos(loanReportInfos
						).endDateTime(
								LocalDateTime.now()
										.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
						.build(),
				burstProcessUtilityService.commitMessage(delegateExecution, false));
		burstPersistenceService.merge(burstProcessUtilityService.getBranchName(
						burstProcessUtilityService.processId(
								delegateExecution)),
				burstProcessUtilityService.commitMessage(delegateExecution,
						true));
		delegateExecution.removeVariable(loansVariableKey);
	}
}
