package com.example.workflow.listener;

import com.example.workflow.model.ReportInfo;
import com.example.workflow.services.BurstProcessUtilityService;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class BurstProcessStartListener implements ExecutionListener {

	public static final String DATE_TIME_FORMAT = "yyyyMMdd";
	@Autowired
	PersistenceService<ReportInfo> persistenceService;

	@Autowired
	BurstProcessUtilityService burstProcessUtilityService;

	@Autowired
	String reportBranchVariableKey;

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String dateTime = LocalDate.now().format(
				DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
		delegateExecution.setVariable(reportBranchVariableKey, dateTime);
		persistenceService.save(burstProcessUtilityService.getBranchName(dateTime),
				burstProcessUtilityService.getQualifiedReportFilePath(
						dateTime,
						ReportInfo.class),
				ReportInfo.builder()
						.startDateTime(LocalDateTime.now()
								.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
						.build(),
				burstProcessUtilityService.commitMessage(
						delegateExecution,
						false));
	}
}
