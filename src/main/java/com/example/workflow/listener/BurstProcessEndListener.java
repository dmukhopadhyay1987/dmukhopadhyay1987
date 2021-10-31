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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BurstProcessEndListener implements ExecutionListener {

	@Autowired
	String loansVariableKey;

	@Autowired
	PersistenceService<ReportInfo> persistenceService;

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
		ReportInfo reportInfo = persistenceService.get(burstProcessUtilityService.getQualifiedReportFilePath(
						new String(Base64.getDecoder().decode(
								burstProcessUtilityService.processId(
										delegateExecution))),
						ReportInfo.class),
				burstProcessUtilityService.reportInfoSha(delegateExecution),
				ReportInfo.class);
		ReportInfo.builder()
				.startDateTime(reportInfo.getStartDateTime())
				.loanReportInfos(loanNumbers.stream()
						.map(l -> LoanReportInfo.builder()
								.loanNumber(l)
								.stages(individualPersistenceService.history(
										individualProcessUtilityService.getQualifiedLoanFilePath(l, LoanModificationInfo.class),
												c -> LocalDateTime.parse(c.getCommitDetails().getAuthor().getDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).isAfter(LocalDate.now().atStartOfDay()),
												LoanModificationInfo.class).entrySet().stream()
										.map(lm -> LoanModificationStageInfo.builder()
												.dateTime(lm.getKey())
												.loanModificationInfo(lm.getValue())
												.build())
										.collect(Collectors.toList()))
								.build()
						).collect(Collectors.toList())
				).build();
	}
}
