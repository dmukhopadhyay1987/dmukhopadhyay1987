package com.samplebpm.workflow.listener;

import com.samplebpm.workflow.model.LoanModificationInfo;
import com.samplebpm.workflow.model.LoanModificationStageInfo;
import com.samplebpm.workflow.model.LoanReportInfo;
import com.samplebpm.workflow.model.ReportInfo;
import com.samplebpm.workflow.services.BurstProcessUtilityService;
import com.samplebpm.workflow.services.IndividualProcessUtilityService;
import com.iwonosql.service.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
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
		Set<String> loanNumbers = 	(Set<String>) delegateExecution.getVariable(loansVariableKey);
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
