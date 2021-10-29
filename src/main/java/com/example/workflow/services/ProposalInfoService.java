package com.example.workflow.services;

import com.example.workflow.model.ProposalRequestDto;
import com.example.workflow.model.ProposalResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class ProposalInfoService {

	public ProposalResponseDto getProposal(ProposalRequestDto proposalRequestDto) {
		log.info("Inside >> {}",
				this.getClass().getSimpleName());
		var result = new ProposalResponseDto();
		result.setLoan(proposalRequestDto.getLoanNum());
		result.setCustomerId(proposalRequestDto.getCustId());
		result.setProposedTerm(new Random().nextInt(30));
		result.setProposedInterestRate(new Random().nextDouble());
		return result;
	}
}
