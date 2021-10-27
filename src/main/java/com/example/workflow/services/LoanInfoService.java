package com.example.workflow.services;

import com.example.workflow.model.LoanResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoanInfoService {

	public LoanResponseDto getLoan(String loanNumber) {
		log.info("Inside >> {}",
				this.getClass().getSimpleName());
		var result = new LoanResponseDto();
		result.setLoan(loanNumber);
		result.setCustomerId("WE4F3W5E4F35E4F");
		result.setRemainingPrincipal(200000.0);
		result.setProductType("JHD");
		return result;
	}
}
