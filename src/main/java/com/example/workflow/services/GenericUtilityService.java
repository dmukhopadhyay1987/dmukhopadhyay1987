package com.example.workflow.services;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class GenericUtilityService {

	public static final String BRANCH_NAME_PREFIX = "irpb/";
	public static final String QUALIFIED_PATH_PREFIX = "ir/data/process/";
	@Autowired
	private String processBusinessKeyDelimiter;

	@Autowired
	private String loanVariableKey;

	public String loanNumber(DelegateExecution delegateExecution) {
		return getBusinessKey(delegateExecution) != null
				? getBusinessKey(delegateExecution).split(processBusinessKeyDelimiter)[0]
				: (String) delegateExecution.getVariable(loanVariableKey);
	}

	public String processInfoSha(DelegateExecution delegateExecution) {
		return getBusinessKey(delegateExecution).split(processBusinessKeyDelimiter)[1];
	}

	private String getBusinessKey(DelegateExecution delegateExecution) {
		return delegateExecution.getProcessInstance().getProcessBusinessKey();
	}

	public void setBusinessKey(DelegateExecution delegateExecution, String loanNumber, String processHead) {
		delegateExecution.getProcessInstance().setProcessBusinessKey(loanNumber
				.concat(processBusinessKeyDelimiter)
				.concat(processHead));
	}

	public String commitMessage(DelegateExecution delegateExecution, boolean mergeCommit) {
		return (mergeCommit ? "Merge " : StringUtils.EMPTY)
				.concat(loanNumber(delegateExecution)).concat(StringUtils.SPACE)
				.concat(delegateExecution.getProcessDefinitionId()).concat(StringUtils.SPACE).concat("[")
				.concat(delegateExecution.getActivityInstanceId()).concat("]");
	}

	public String getQualifiedLoanFilePath(String path, Class<?> c) {
		return QUALIFIED_PATH_PREFIX
				.concat(path)
				.concat("/")
				.concat(getFileNameInRepo(c).toLowerCase(Locale.ROOT))
				.concat(".json");
	}

	public String getBranchName(String loanNumber) {
		return BRANCH_NAME_PREFIX.concat(loanNumber);
	}

	public String retrieveLoanNumber(String branchName) {
		return branchName.replace(BRANCH_NAME_PREFIX, StringUtils.EMPTY);
	}

	private String getFileNameInRepo(Class<?> obj) {
		return obj.getSimpleName();
	}
}
