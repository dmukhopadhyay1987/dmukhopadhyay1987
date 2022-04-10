package com.samplebpm.workflow.services;

import com.samplebpm.workflow.model.LoanResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Random;

import static org.apache.commons.lang3.RandomStringUtils.random;

@Service
@Slf4j
public class LoanInfoService {

	public LoanResponseDto getLoan(String loanNumber) {
		log.info("Inside >> {}",
				this.getClass().getSimpleName());
		var result = new LoanResponseDto();
		result.setLoan(loanNumber);
		result.setCustomerId(RandomStringUtils.random(
				10,
				true,
				true).toUpperCase());
		result.setRemainingPrincipal(new BigInteger(
				String.valueOf(
						new Random().nextInt(
								1000000))).doubleValue());
		result.setProductType(random(
				3,
				true,
				false).toUpperCase());
		result.setRemainingTerm(new Random().nextInt(
								30));
		return result;
	}
}
