package com.test.delegate;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ExtensionDelegateForPass implements Serializable,JavaDelegate {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(DelegateExecution arg0) {
		System.out.println("呦西,我正在处理委派给我的任务：修改续约状态为通过！");
	}

}