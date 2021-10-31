package com.example.workflow.listener;

import com.example.workflow.model.ReportInfo;
import com.example.workflow.services.BurstProcessUtilityService;
import com.example.workflow.services.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class BurstProcessStartListener implements ExecutionListener {

	@Autowired
	PersistenceService<ReportInfo> persistenceService;

	@Autowired
	BurstProcessUtilityService burstProcessUtilityService;

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String dateTime = LocalDateTime.now().format(
				DateTimeFormatter.ISO_DATE_TIME);
		String processId = Base64.getEncoder().encodeToString(dateTime.getBytes(StandardCharsets.UTF_8));
		burstProcessUtilityService.setBusinessKey(delegateExecution,
				processId,
				persistenceService.save(burstProcessUtilityService.getBranchName(processId),
						burstProcessUtilityService.getQualifiedReportFilePath(
								dateTime,
								ReportInfo.class),
						ReportInfo.builder()
								.startDateTime(dateTime)
								.build(),
						burstProcessUtilityService.commitMessage(
								delegateExecution,
								false)).getSha());
	}
}
