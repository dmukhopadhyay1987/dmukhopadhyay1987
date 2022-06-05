package com.samplebpm.workflow.listener;

import com.samplebpm.workflow.model.ReportInfo;
import com.samplebpm.workflow.services.BurstProcessUtilityService;
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
public class BurstProcessStartListener implements ExecutionListener {

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd";
	@Autowired
	PersistenceService<ReportInfo> burstPersistenceService;

	@Autowired
	BurstProcessUtilityService burstProcessUtilityService;

	@Autowired
	String reportBranchVariableKey;

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String dateTime = LocalDateTime.now().format(
				DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
		delegateExecution.setVariable(reportBranchVariableKey, dateTime);
		ReportInfo reportInfo;
		try {
			reportInfo = burstPersistenceService.get(burstProcessUtilityService.getQualifiedReportFilePath(
							dateTime,
							ReportInfo.class),
					burstProcessUtilityService.getBranchName(dateTime),
					ReportInfo.class);
			reportInfo = ReportInfo.builder()
					.startDateTime(reportInfo.getStartDateTime())
					.loanReportInfos(reportInfo.getLoanReportInfos())
					.endDateTime(reportInfo.getEndDateTime())
					.lastRunTime(LocalDateTime.now()
							.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					.build();
		} catch (Exception e) {
			reportInfo = ReportInfo.builder()
					.startDateTime(LocalDateTime.now()
							.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					.lastRunTime(LocalDateTime.now()
							.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					.build();
		}
		burstPersistenceService.save(burstProcessUtilityService.getBranchName(dateTime),
				burstProcessUtilityService.getQualifiedReportFilePath(
						dateTime,
						ReportInfo.class),
				reportInfo,
				burstProcessUtilityService.commitMessage(
						delegateExecution,
						false));
	}
}
