package com.example.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimerEndListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution delegateExecution) {
		log.info("Timer Ended >>> {}",
				delegateExecution.getCurrentActivityName());
	}
}
