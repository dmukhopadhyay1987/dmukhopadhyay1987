package com.samplebpm.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimerStartListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Timer Started >>> {}",
				delegateExecution.getCurrentActivityName());
	}
}
