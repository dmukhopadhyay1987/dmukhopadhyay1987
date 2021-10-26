package com.example.workflow.servicedelegates;

import com.example.workflow.model.ProcessInfo;
import com.example.workflow.model.ProposalRequestDto;
import com.example.workflow.model.ProposalResponseDto;
import com.example.workflow.services.FilePathService;
import com.example.workflow.services.PersistenceService;
import com.example.workflow.services.ProposalInfoService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GetProposalInfo implements JavaDelegate {

	@Autowired
	ProposalInfoService proposalInfoService;

	@Autowired
	PersistenceService<ProcessInfo> persistenceService;

	@Autowired
	FilePathService filePathService;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
		String loanNumber = (String) delegateExecution
				.getVariable("loanNumber");
		ProposalResponseDto proposalResponseDto = proposalInfoService.getProposal((ProposalRequestDto) delegateExecution
				.getVariable("proposalRequestDto"));
		delegateExecution.setVariable("proposalResponseDto",
				proposalResponseDto);
		ProcessInfo processInfo = persistenceService.get(filePathService.getQualifiedFilePath(loanNumber, ProcessInfo.class), (String) delegateExecution.getVariable("processInfo"), ProcessInfo.class);
		processInfo.setProposalDetails(proposalResponseDto);
		delegateExecution.setVariable("processInfo", persistenceService.save(
				processInfo.getLoanNumber(),
				filePathService.getQualifiedFilePath(processInfo.getLoanNumber(), ProcessInfo.class),
				processInfo,
				delegateExecution.getCurrentActivityName()).getSha());
		delegateExecution.removeVariable("proposalRequestDto");
	}
}
