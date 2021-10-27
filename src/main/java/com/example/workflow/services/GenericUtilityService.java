package com.example.workflow.services;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class GenericUtilityService {

	@Autowired
	private String processBusinessKeyDelimiter;

	public String loanNumber(DelegateExecution delegateExecution) {
		return getBusinessKey(delegateExecution).split(processBusinessKeyDelimiter)[0];
	}

	public String processInfoSha(DelegateExecution delegateExecution) {
		return getBusinessKey(delegateExecution).split(processBusinessKeyDelimiter)[1];
	}

	public String getBusinessKey(DelegateExecution delegateExecution) {
		return delegateExecution.getProcessInstance().getProcessBusinessKey();
	}

	public void setBusinessKey(DelegateExecution delegateExecution, String loanNumber, String processHead) {
		 delegateExecution.getProcessInstance().setProcessBusinessKey(loanNumber
				.concat(processBusinessKeyDelimiter)
				.concat(processHead));
	}

	public String getQualifiedFilePath(String path, Class<?> c) {
		return path.concat("/")
				.concat(getFileNameInRepo(c).toLowerCase(Locale.ROOT)
						.concat(".json"));
	}

	private String getFileNameInRepo(Class<?> obj) {
		return obj.getSimpleName();
	}
}
