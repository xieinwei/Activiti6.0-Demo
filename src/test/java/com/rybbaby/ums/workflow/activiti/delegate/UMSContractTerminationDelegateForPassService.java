package com.rybbaby.ums.workflow.activiti.delegate;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * 续约协议审批通过的之后委托处理服务
 */
public class UMSContractTerminationDelegateForPassService implements Serializable, JavaDelegate {

	@Override
	public void execute(DelegateExecution arg0) {
		
	}

}