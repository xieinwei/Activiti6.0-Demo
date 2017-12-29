package com.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.test.delegate.ExtensionDelegateForPass;
import com.test.delegate.ExtensionDelegateForReject;

public class ActiviTest01 {

	private ActivitiService activitiService = new ActivitiService();

	@Test
	public void createActTable() {
		ActivitiService au = new ActivitiService();
		au.createTable();
	}

	/**
	 * 流程普通部署方式测试
	 */
	@Test
	public void deployTest() {
		ActivitiService au = new ActivitiService();
		Deployment deploy = au.deploy("contractChangeApproval", "UMS亲幼合同变更审批流程", "RYB.UMS.合同审批流程");
		if (deploy != null) {
			System.out.println("id = " + deploy.getId());
			System.out.println("key = " + deploy.getKey());
			System.out.println("name = " + deploy.getName());
			System.out.println("category = " + deploy.getCategory());
		}
	}

	/**
	 * 流程zip部署方式测试
	 */
	@Test
	public void zipDeployTest() {
		Deployment deploy = activitiService.deploy("zipTest", "员工请假流程", "普通流程");
		if (deploy != null) {
			System.out.println("id = " + deploy.getId());
			System.out.println("key = " + deploy.getKey());
			System.out.println("name = " + deploy.getName());
			System.out.println("category = " + deploy.getCategory());
		}
	}

	/**
	 * 已发布流程列表测试
	 */
	@Test
	public void deployedList() {
		List<ProcessDefinition> processDefinitions = this.activitiService.deployedList(true);
		// List<ProcessDefinition> processDefinitions =
		// this.activitiService.deployedList(false);
		for (ProcessDefinition processDefinition : processDefinitions) {
			System.out.println("id=" + processDefinition.getId());
			System.out.println("version=" + processDefinition.getVersion());
			System.out.println("name=" + processDefinition.getName());
			System.out.println("key=" + processDefinition.getKey());
			System.out.println("category=" + processDefinition.getCategory());
			System.out.println("resourceName=" + processDefinition.getResourceName());
			System.out.println("diagramResourceName=" + processDefinition.getDiagramResourceName());
			System.out.println("++++---++-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");
		}
	}

	/**
	 * 已发布流程分页列表测试
	 */
	@Test
	public void deployedListPage() {
		// List<ProcessDefinition> processDefinitions =
		// this.activitiService.deployedList(true, 1, 2);
		List<ProcessDefinition> processDefinitions = this.activitiService.deployedList(false, 0, 2);
		for (ProcessDefinition processDefinition : processDefinitions) {
			System.out.println("id=" + processDefinition.getId());
			System.out.println("version=" + processDefinition.getVersion());
			System.out.println("name=" + processDefinition.getName());
			System.out.println("key=" + processDefinition.getKey());
			System.out.println("category=" + processDefinition.getCategory());
			System.out.println("resourceName=" + processDefinition.getResourceName());
			System.out.println("diagramResourceName=" + processDefinition.getDiagramResourceName());
			System.out.println("++++++++++++++++++++++++++++++++++");
		}
	}

	/**
	 * 根据流程定义Id启动流程测试
	 */
	@Test
	public void startByProcessDefinitionId() {
		Map<String, Object> varibles = new HashMap<>();
		varibles.put("userId", "李华");
		varibles.put("zjUserId", "张三");
		varibles.put("bmjlUserId", "李四");
		varibles.put("dszUserId", "王五");

		varibles.put("days", 3);
		varibles.put("startDate", "2017-09-16");
		varibles.put("endDate", "2017-09-18");
		String processDefinitioId = "empLeaveRequest:1:4";

		this.activitiService.startProcessInstanceById(processDefinitioId, varibles);
	}

	/**
	 * 根据流程key启动流程测试
	 */
	@Test
	public void startByKey() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("candidateForZSJL", "吴敏");// 流程第一步：招商经理审核
		variables.put("resultForZSJL", true);// 流程第一步审核结果
		variables.put("candidateForDQJL", "曹博");// 流程第二步：大区经理审核
		variables.put("resultForDQJL", true);// 流程第二步审核结果
		variables.put("resultForQYSYBFZR", true);// 流程第三步审核结果
		// 合同所属大区，如果为南方合同，则需要南方风控先审批，审批通过后再由总部风控审批。如果招商经理业务大区为亲幼华南（业务代码：4）则判定为需要南方风控审核
		boolean region = true;
		variables.put("region", region);
		variables.put("resultForNFFK", true);// 流程第四步南方风控审核结果
		variables.put("resultForZBFK", true);// 流程第四步总部风控审核结果
		variables.put("projectType", 2);// 项目类型
		variables.put("resultForQZDDB", true);// 流程第五步审核结果
		variables.put("resultForFWZCB", true);// 流程第五步审核结果
		variables.put("candidateForYEYDDB", "");// 流程第六步：幼儿园督导部审核
		variables.put("resultForYEYDDB", true);// 流程第六步审核结果
        variables.put("resultForKJ", true);//会计审核结果
        variables.put("resultForCWJL", true);//财务经理审核结果
        variables.put("resultForCWFZR", true);//财务负责人审核结果

		String processDefinitionKey = "contractApproval_qy";
		ProcessInstance processInstance = this.activitiService.startProcessInstanceByKey(processDefinitionKey,
				"QYHT-A001", variables);
		System.out.println("processInstanceId:" + processInstance.getId());
		System.out.println("name:" + processInstance.getName());
		System.out.println("bussinessKey:" + processInstance.getBusinessKey());
		System.out.println("deploymentId:" + processInstance.getDeploymentId());
		System.out.println("processDefinitionVersion:" + processInstance.getProcessDefinitionVersion());
		System.out.println("processVariables:" + processInstance.getProcessVariables());
		System.out.println("description:" + processInstance.getDescription());
		System.out.println("processInstanceId:" + processInstance.getProcessInstanceId());
		System.out.println("rootProcessInstanceId:" + processInstance.getRootProcessInstanceId());
	}

	/**
	 * 续约流程启动测试
	 */
	@Test
	public void start1() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("projectType", 2);// 亲子督导审批结果
		variables.put("resultForQZDD", true);// 亲子督导审批结果
		variables.put("resultForFWZCFZR", true);// 服务支持负责人
		variables.put("resultForYEDDFZR", true);// 幼儿督导负责人
		variables.put("resultForQYSYB", true);// 亲幼事业部审批结果
		variables.put("resultForFW", true);// 法务审批结果
		variables.put("delegateForReject", new ExtensionDelegateForReject());//// 驳回的委托处理对象
		variables.put("delegateForPass", new ExtensionDelegateForPass());// 通过时的委托处理对象

		String processDefinitionKey = "contractExtension_qy";
		ProcessInstance processInstance = this.activitiService.startProcessInstanceByKey(processDefinitionKey,
				"QYHT-A001", variables);
		System.out.println("processInstanceId:" + processInstance.getId());
		System.out.println("name:" + processInstance.getName());
		System.out.println("bussinessKey:" + processInstance.getBusinessKey());
		System.out.println("deploymentId:" + processInstance.getDeploymentId());
		System.out.println("processDefinitionVersion:" + processInstance.getProcessDefinitionVersion());
		System.out.println("processVariables:" + processInstance.getProcessVariables());
		System.out.println("description:" + processInstance.getDescription());
		System.out.println("processInstanceId:" + processInstance.getProcessInstanceId());
		System.out.println("rootProcessInstanceId:" + processInstance.getRootProcessInstanceId());
	}

	/**
	 * 合同转让流程启动测试
	 */
	@Test
	public void start2() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("candidateForDQFZR", "张三");// 大区负责人
		variables.put("projectType", 2);// 项目类型（1：亲子园，2：幼儿园）
		variables.put("region", true);
		variables.put("resultForDQFZR", true);// 大区负责人审批结果
		variables.put("resultForNFFK", true);// 南方风控审批结果
		variables.put("resultForZBFK", true);// 风控审批结果
		variables.put("resultForQYSYBFZR", true);// 亲幼事业部负责人审批结果
		variables.put("resultForQZDDFZR", true);// 亲子督导负责人审批结果
		variables.put("resultForYEFWZCBFZR", true);// 幼儿服务支持部负责人审批结果
		variables.put("resultForYEDDFZR", true);// 幼儿督导负责人审批结果
		variables.put("resultForKJ", true);// 会计审批结果
		variables.put("resultForCWJL", true);// 财务经理审批结果
		variables.put("resultForCWFZR", true);// 财务负责人审批结果
		variables.put("resultForFW", true);// 法务审批结果审批结果
		String processDefinitionKey = "contractTransfer_qy";

		ProcessInstance processInstance = this.activitiService.startProcessInstanceByKey(processDefinitionKey,
				"QYHT-A001", variables);
		System.out.println("processInstanceId:" + processInstance.getId());
		System.out.println("name:" + processInstance.getName());
		System.out.println("bussinessKey:" + processInstance.getBusinessKey());
		System.out.println("deploymentId:" + processInstance.getDeploymentId());
		System.out.println("processDefinitionVersion:" + processInstance.getProcessDefinitionVersion());
		System.out.println("processVariables:" + processInstance.getProcessVariables());
		System.out.println("description:" + processInstance.getDescription());
		System.out.println("processInstanceId:" + processInstance.getProcessInstanceId());
		System.out.println("rootProcessInstanceId:" + processInstance.getRootProcessInstanceId());
	}

	/**
	 * 合同变更流程启动测试
	 */
	@Test
	public void start3() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("projectType", 2);// 项目类型（1：亲子园，2：幼儿园）
		variables.put("isMainChange", true);// 是否为主体变更
		variables.put("resultForQZDDFZR", true);// 亲子督导负责人
		variables.put("resultForFWZCFZR", true);// 服务支持负责人
		variables.put("resultForYEDDFZR", true);// 幼儿督导负责人
		variables.put("resultForQYSYBFZR", true);// 亲幼事业部负责人
		variables.put("resultForKJ", true);// 会计
		variables.put("resultForCWJL", true);// 财务经理
		variables.put("resultForCWFZR", true);// 财务负责人
		variables.put("resultForFW", true);// 法务负责人

		String processDefinitionKey = "contractChangeApproval";

		ProcessInstance processInstance = this.activitiService.startProcessInstanceByKey(processDefinitionKey,
				"QYHT-A001", variables);
		System.out.println("processInstanceId:" + processInstance.getId());
		System.out.println("name:" + processInstance.getName());
		System.out.println("bussinessKey:" + processInstance.getBusinessKey());
		System.out.println("deploymentId:" + processInstance.getDeploymentId());
		System.out.println("processDefinitionVersion:" + processInstance.getProcessDefinitionVersion());
		System.out.println("processVariables:" + processInstance.getProcessVariables());
		System.out.println("description:" + processInstance.getDescription());
		System.out.println("processInstanceId:" + processInstance.getProcessInstanceId());
		System.out.println("rootProcessInstanceId:" + processInstance.getRootProcessInstanceId());
	}

	/**
	 * 合同解约流程启动测试
	 */
	@Test
	public void start4() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("candidateForDQFZR", "张三");// 大区负责人
		variables.put("isOpenSchool", true);// 是否已经开园
		variables.put("projectType", 2);// 项目类型（1：亲子园，2：幼儿园）
		variables.put("region", true);// 是否为华南项目
		variables.put("resultForDQFZR", true);// 大区负责人
		variables.put("resultForHNFK", false);// 华南风控
		variables.put("resultForZBFK", true);// 总部风控
		variables.put("resultForQZDDFZR", true);// 亲子督导负责人
		variables.put("resultForFWZCFZR", true);// 服务支持负责人
		variables.put("resultForYEDDFZR", true);// 幼儿督导负责人
		variables.put("resultForQYSYBFZR", true);// 亲幼事业部负责人
		variables.put("resultForKJ", true);// 会计
		variables.put("resultForCWJL",  true);// 财务经理
		variables.put("resultForCWFZR",true);// 财务负责人
		variables.put("resultForFW", true);// 法务负责人

		String processDefinitionKey = "contractTermination_qy";

		ProcessInstance processInstance = this.activitiService.startProcessInstanceByKey(processDefinitionKey,
				"QYHT-A001", variables);
		System.out.println("processInstanceId:" + processInstance.getId());
		System.out.println("name:" + processInstance.getName());
		System.out.println("bussinessKey:" + processInstance.getBusinessKey());
		System.out.println("deploymentId:" + processInstance.getDeploymentId());
		System.out.println("processDefinitionVersion:" + processInstance.getProcessDefinitionVersion());
		System.out.println("processVariables:" + processInstance.getProcessVariables());
		System.out.println("description:" + processInstance.getDescription());
		System.out.println("processInstanceId:" + processInstance.getProcessInstanceId());
		System.out.println("rootProcessInstanceId:" + processInstance.getRootProcessInstanceId());
	}

	/**
	 * 获取流程表单
	 */
	@Test
	public void getStartForm() {
		try {
			String form = this.activitiService.getStartForm("empLeaveRequest:4:7507");
			System.out.println("表单内容：");
			System.out.println(form);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取流程表单
	 */
	@Test
	public void getTaskForm() {
		try {
			System.out.print("请输入任务id:");
			Scanner sc = new Scanner(System.in);
			String taskId = sc.nextLine();
			Map<String, Object> variables = new HashMap<>();
			variables.put("checkRecordId", "");
			variables.put("processInstanceId", "");
			variables.put("contractId", "");
			String form = this.activitiService.getTaskForm(taskId, variables);
			System.out.println("表单内容：");
			System.out.println(form);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取已启动的流程列表
	 */
	@Test
	public void startedList() {
		// List<ProcessInstance> processInstances =
		// this.activitiService.startedList(0, 3);
		List<ProcessInstance> processInstances = this.activitiService.startedList();
		for (ProcessInstance processInstance : processInstances) {
			System.out.println("instanceId:" + processInstance.getId());
			System.out.println("name:" + processInstance.getName());
			System.out.println("bussinessKey:" + processInstance.getBusinessKey());
			System.out.println("deploymentId:" + processInstance.getDeploymentId());
			System.out.println("processDefinitionVersion:" + processInstance.getProcessDefinitionVersion());
			System.out.println("processVariables:" + processInstance.getProcessVariables());
			System.out.println("description:" + processInstance.getDescription());
			System.out.println("processInstanceId:" + processInstance.getProcessInstanceId());
			System.out.println("rootProcessInstanceId:" + processInstance.getRootProcessInstanceId());
			System.out.println("--------------");
		}
	}

	/**
	 * 获取指定流程实例的任务列表
	 */
	@Test
	public void taskList() {
		System.out.print("请输入流程实例id:");
		Scanner sc = new Scanner(System.in);
		String processInstanceId = sc.nextLine();
		List<Task> tasks = this.activitiService.taskList(processInstanceId);
		for (Task task : tasks) {
			System.out.println("taskId:" + task.getId());
			System.out.println("createTime:" + task.getCreateTime());
			System.out.println("name:" + task.getName());
			System.out.println("executionid:" + task.getExecutionId());
		}
		if (tasks.size() == 0) {
			System.out.println("无任务节点!");
		}
	}

	/**
	 * 获取指定流程实例的任务列表
	 */
	@Test
	public void taskList1() {
		System.out.print("请输入流程实例id:");
		Scanner sc = new Scanner(System.in);
		String processInstanceId = sc.nextLine();
		List<String> taskAssigneeIds = new ArrayList<String>();
		taskAssigneeIds.add("大区经理");
		List<Task> tasks = this.activitiService.taskList(processInstanceId, taskAssigneeIds);
		for (Task task : tasks) {
			System.out.println("taskId:" + task.getId());
			System.out.println("createTime:" + task.getCreateTime());
			System.out.println("name:" + task.getName());
			System.out.println("executionid:" + task.getExecutionId());
		}
		if (tasks.size() == 0) {
			System.out.println("无任务节点!");
		}
	}

	/**
	 * 执行完指定的任务节点
	 */
	@Test
	public void complate() {
		try {
			System.out.print("请输入任务id:");
			Scanner sc = new Scanner(System.in);
			String taskId = sc.nextLine();
			Map<String, Object> variables = new HashMap<>();
			System.out.print("请输入条件key:");
			String key = sc.nextLine();
			System.out.print("请输入条件值:");
			String value = sc.nextLine();
			System.out.println(key + "=" + BooleanUtils.toBooleanObject(value));
			variables.put(key, BooleanUtils.toBooleanObject(value));
			this.activitiService.complate(taskId, variables);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void autoComplate() throws IOException {
		System.out.print("请输入流程实例id:");
		Scanner sc = new Scanner(System.in);
		String processInstanceId = sc.nextLine();
		autoComplate(processInstanceId);
		graphicsByInstanceId(processInstanceId);
	}

	/**
	 * 自动执行完指定的任务节点
	 */
	public void autoComplate(String processInstanceId) {
		try {
			List<Task> taskList = this.activitiService.getTaskList(processInstanceId);
			for (Task task : taskList) {
				this.activitiService.complate(task.getId());
			}
			if (taskList != null && !taskList.isEmpty()) {
				Thread.sleep(1000);
				autoComplate(processInstanceId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 审核记录
	 */
	@Test
	public void complateHistoryList() {
		System.out.print("请输入流程实例id:");
		Scanner sc = new Scanner(System.in);
		String processInstanceId = sc.nextLine();
		List<HistoricTaskInstance> complateHistoryList = this.activitiService.complateHistoryList(processInstanceId);
		for (HistoricTaskInstance historicTaskInstance : complateHistoryList) {
			System.out.println("-----------------------------------");
			System.out.println(historicTaskInstance.getId());
			System.out.println(historicTaskInstance.getStartTime());
			System.out.println(historicTaskInstance.getEndTime());
			System.out.println(historicTaskInstance.getName());
		}
	}

	/**
	 * 根据任务节点id绘制图片测试
	 */
	@Test
	public void graphicsByTaskIdTest() {
		try {
			String taskId = "47503";
			InputStream inputStream = this.activitiService.graphicsByTaskId(taskId);
			FileUtils.copyInputStreamToFile(inputStream, new File("E:/bbb.png"));
			IOUtils.closeQuietly(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据流程定义Id绘制图片测试
	 */
	@Test
	public void graphicsByDefinitionId() {
		try {
			System.out.print("请输入流程定义Id:");
			Scanner sc = new Scanner(System.in);
			String definitionId = sc.nextLine();
			InputStream inputStream = this.activitiService.graphicsByDefinitionId(definitionId);
			File file = new File("E:/ccc.png");
			FileUtils.copyInputStreamToFile(inputStream, file);
			System.out.println("生成成功，文件位置：=" + file.getPath());
			IOUtils.closeQuietly(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据流程实例绘制图片测试
	 */
	@Test
	public void graphicsByInstanceId() {
		try {
			System.out.print("请输入流程实例Id:");
			Scanner sc = new Scanner(System.in);
			String instanceId = sc.nextLine();
			graphicsByInstanceId(instanceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void graphicsByInstanceId(String instanceId) throws IOException {
		InputStream inputStream = this.activitiService.graphicsByInstanceId(instanceId);
		if (inputStream != null) {
			File file = new File("E:/ddd1.jpg");
			FileUtils.copyInputStreamToFile(inputStream, file);
			System.out.println("生成成功，文件位置：=" + file.getPath());
			IOUtils.closeQuietly(inputStream);
		}
	}
}
