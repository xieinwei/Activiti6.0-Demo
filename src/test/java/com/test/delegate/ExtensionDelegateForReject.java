package com.test.delegate;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ExtensionDelegateForReject implements Serializable, JavaDelegate {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(DelegateExecution de) {
		String currentActivityId = de.getCurrentActivityId();
		String processInstanceBusinessKey = de.getProcessInstanceBusinessKey();
		String processInstanceId = de.getProcessInstanceId();
		String eventName = de.getEventName();
		System.out.println("currentActivityId:" + currentActivityId);
		System.out.println("processInstanceBusinessKey:" + processInstanceBusinessKey);
		System.out.println("processInstanceId:" + processInstanceId);
		System.out.println("eventName:" + eventName);
		System.out.println("呦西,我正在处理委派给我的任务：修改续约状态为驳回！");
	}

}
