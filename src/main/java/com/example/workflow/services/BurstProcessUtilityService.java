package com.example.workflow.services;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class BurstProcessUtilityService {

	public static final String BRANCH_NAME_PREFIX = "irpb/run_";
	public static final String QUALIFIED_PATH_PREFIX = "ir/data/report/";

	@Autowired
	String reportBranchVariableKey;

	@Autowired
	String reportShaVariableKey;

	public String processId(DelegateExecution delegateExecution) {
		return (String) delegateExecution.getVariable(reportBranchVariableKey);
	}

	public String commitMessage(DelegateExecution delegateExecution, boolean mergeCommit) {
		return (mergeCommit ? "Merge " : StringUtils.EMPTY)
				.concat(processId(delegateExecution)).concat(StringUtils.SPACE)
				.concat(delegateExecution.getProcessDefinitionId()).concat(StringUtils.SPACE).concat("[")
				.concat(delegateExecution.getActivityInstanceId()).concat("]");
	}

	public String getQualifiedReportFilePath(String path, Class<?> c) {
		return QUALIFIED_PATH_PREFIX
				.concat(path)
				.concat("/")
				.concat(getFileNameInRepo(c).toLowerCase(Locale.ROOT))
				.concat(".json");
	}

	public String getBranchName(String processId) {
		return BRANCH_NAME_PREFIX.concat(processId);
	}

	private String getFileNameInRepo(Class<?> obj) {
		return obj.getSimpleName();
	}
}
