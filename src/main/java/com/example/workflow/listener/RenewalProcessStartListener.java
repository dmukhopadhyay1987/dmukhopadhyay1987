package com.example.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component(value = "renewalProcessStartListener")
@Slf4j
public class RenewalProcessStartListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution delegateExecution) {

		log.info("Inside >>> {}",
				delegateExecution.getCurrentActivityName());
	}
}
