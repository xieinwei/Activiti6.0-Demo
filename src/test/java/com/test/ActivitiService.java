package com.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cmd.GetBpmnModelCmd;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDiagramCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;

/**
 * activiti工具类
 *
 * @author xiejinwei
 */
public class ActivitiService {

	private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

	private String diagramsDirName = "diagrams";
	public static final String DEFAULTNAMESPACE = "http://b3mn.org/stencilset/bpmn2.0#";

	public void createTable() {
		ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
		if(defaultProcessEngine!=null) {
			System.out.println("创建成功!");
		}else {
			System.out.println("创建失败!");
		}
	}
	
	/**
	 * 根据流程定义的key重新部署流程
	 *
	 * @param definitionKey
	 *            流程定义key
	 * @return
	 */
	public Deployment reDeploy(String definitionKey) {
		ProcessDefinition processDefinition = this.processEngine.getRepositoryService().createProcessDefinitionQuery()
				.processDefinitionKey(definitionKey).latestVersion().singleResult();
		if (processDefinition != null) {
			Deployment deployment = this.processEngine.getRepositoryService().createDeploymentQuery()
					.deploymentId(processDefinition.getDeploymentId()).singleResult();
			if (deployment != null) {
				String deploymentKey = deployment.getKey();
				String name = deployment.getName();
				String category = deployment.getCategory();
				return this.deploy(deploymentKey, name, category);
			}
		}
		return null;
	}

	/**
	 * 部署流程(首先尝试部署zip文件，若zip文件不存在则会尝试部署bpmn、png文件)
	 *
	 * @param diagramDirName
	 *            设计文件所在目录
	 * @param flowName
	 *            流程名称
	 * @param category
	 *            流程分类名
	 * @return
	 */
	public Deployment deploy(String diagramDirName, String flowName, String category) {
		Class<? extends ActivitiService> clas = this.getClass();
		System.out.println("即将部署流程：" + diagramDirName);
		// 部署对象
		DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment();
		// classpath根路径
		String classPath = clas.getResource("/").getPath();
		// 设计文件目录加设计文件名
		boolean isDeploy = false;
		String diagramsPath = classPath + diagramsDirName + "/" + diagramDirName;
		File diagramDir = new File(diagramsPath);
		if (diagramDir.exists()) {
			File[] zipFiles = diagramDir.listFiles((File dir, String name) -> {
				return name.endsWith(".zip");
			});
			if (zipFiles != null && zipFiles.length > 0) {
				File zipFile = zipFiles[0];
				try (InputStream resourceAsStream = new FileInputStream(zipFile);
						ZipInputStream zipInputStream = new ZipInputStream(resourceAsStream);) {
					deploymentBuilder.addZipInputStream(zipInputStream);
					System.out.println("流程 ZIP 文件添加到部署资源成功!");
					isDeploy = true;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("流程 ZIP 文件添加到部署资源发生异常!");
				}
			} else {
				String formsRelativeDirName = diagramsDirName + "/" + diagramDirName;
				File[] bpmnAndPngFiles = diagramDir.listFiles((File dir, String name) -> {
					return name.endsWith(".bpmn") || name.endsWith(".png");
				});
				if (bpmnAndPngFiles != null && bpmnAndPngFiles.length > 0) {
					for (File bpmnAndPngFile : bpmnAndPngFiles) {
						if (bpmnAndPngFile.exists()) {
							String bpmnAndPngFileName = bpmnAndPngFile.getName();
							if (bpmnAndPngFileName.endsWith(".bpmn") && !isDeploy) {// 只有包含了bpmn文件才可部署
								isDeploy = true;
							}
							deploymentBuilder.addClasspathResource(formsRelativeDirName + "/" + bpmnAndPngFileName);
							System.out.println("流程文件:" + bpmnAndPngFileName + " 添加到部署资源成功!");
						}
					}
				}
				// 表单文件
				File formsDir = new File(diagramDir.getPath() + "/forms");
				if (formsDir.exists() && formsDir.isDirectory()) {
					for (File file : formsDir.listFiles()) {
						deploymentBuilder.addClasspathResource(formsRelativeDirName + "/forms/" + file.getName());
						System.out.println("流程 form 文件:" + file.getName() + " 添加到部署资源成功!");
					}
				}
			}
		}
		if (isDeploy) {
			Deployment deploy = deploymentBuilder.key(diagramDirName)// 流程key
					.name(flowName)// 流程名字
					.category(category)// 流程分类
					.deploy();
			if (deploy == null) {
				System.out.println("流程部署失败!");
			} else {
				System.out.println("流程部署成功!");
			}
			return deploy;
		} else {
			return null;
		}
	}

	/**
	 * 获取启动表单
	 *
	 * @param processDefinitionId
	 *            表单定义id
	 * @return
	 */
	public String getStartForm(String processDefinitionId) {
		Object renderedStartForm = this.processEngine.getFormService().getRenderedStartForm(processDefinitionId);
		return renderedStartForm == null ? "" : renderedStartForm.toString();
	}

	/**
	 * 获取任务节点表单
	 *
	 * @param taskId
	 *            节点Id
	 * @param variables 变量
	 * @return
	 */
	public String getTaskForm(String taskId, Map<String, Object> variables) {
		if (variables != null && !variables.isEmpty()) {
			Task task = this.processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
			String executionId = task.getExecutionId();
			RuntimeService runtimeService = this.processEngine.getRuntimeService();
			runtimeService.setVariables(executionId, variables);
		}
		Object renderedTaskForm = this.processEngine.getFormService().getRenderedTaskForm(taskId);
		return renderedTaskForm == null ? "" : renderedTaskForm.toString();
	}

	/**
	 * 启动流程，通过流程定义的key，每次部署的都是最新版本的流程
	 *
	 * @param processDefinitionKey
	 *            流程定义的key
	 * @param variables
	 *            流程变量
	 * @return 流程实例
	 */
	public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey,
			Map<String, Object> variables) {
		ProcessInstance processInstance = this.processEngine.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
		System.out.println("流程启动成功，启动方式:by Key=" + processDefinitionKey);
		return processInstance;
	}

	/**
	 * 启动流程，通过流程定义的key，每次部署的都是最新版本的流程
	 *
	 * @param processDefinitionKey
	 *            流程定义的key
	 * @return 流程实例
	 */
	public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
		ProcessInstance processInstance = this.processEngine.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);
		System.out.println("流程启动成功，启动方式:by Key=" + processDefinitionKey);
		return processInstance;
	}

	/**
	 * 启动流程，通过流程定义的id启动，可以启动历史版本的流程
	 *
	 * @param processDefinitionId
	 *            流程定义的id
	 * @return 流程实例
	 */
	public ProcessInstance startProcessInstanceById(String processDefinitionId) {
		ProcessInstance processInstance = this.processEngine.getRuntimeService()
				.startProcessInstanceById(processDefinitionId);
		System.out.println("流程启动成功，启动方式:by id=" + processDefinitionId);
		return processInstance;
	}

	/**
	 * 启动流程，通过流程定义的id启动，可以启动历史版本的流程
	 *
	 * @param processDefinitionId
	 *            流程定义的id
	 * @param variables
	 *            流程变量
	 * @return 流程实例
	 */
	public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
		ProcessInstance processInstance = this.processEngine.getRuntimeService()
				.startProcessInstanceById(processDefinitionId, variables);
		System.out.println("流程启动成功，启动方式:by id=" + processDefinitionId);
		return processInstance;
	}

	/**
	 * 结束任务节点
	 *
	 * @param taskId
	 *            任务id
	 */
	public void complate(String taskId) {
		this.processEngine.getTaskService().complete(taskId);
		System.out.println("任务结束成功：taskId:" + taskId);
	}

	/**
	 * 结束任务节点
	 *
	 * @param taskId
	 *            任务id
	 * @param variables
	 *            流程变量
	 */
	public void complate(String taskId, Map<String, Object> variables) {
		this.processEngine.getTaskService().complete(taskId, variables);
		System.out.println("任务结束成功：askId:" + taskId);
	}
	
	public List<Task> getTaskList(String processInstanceId){
		List<Task> list = this.processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
		return list;
	}

	/**
	 * 结束任务节点
	 *
	 * @param taskId
	 *            任务id
	 * @param variables
	 *            流程变量
	 * @param localScpe
	 *            是否为本地流程变量（本地流程变量只能在当前节点使用）
	 */
	public void complate(String taskId, Map<String, Object> variables, boolean localScpe) {
		this.processEngine.getTaskService().complete(taskId, variables, localScpe);
		System.out.println("任务结束成功：askId:" + taskId);
	}

	/**
	 * 已部署流程列表
	 *
	 * @param includeHistoryVersion
	 *            是否包含历史版本
	 * @return 流程定义列表
	 */
	public List<ProcessDefinition> deployedList(boolean includeHistoryVersion) {
		ProcessDefinitionQuery processDefinitionQuery = this.processEngine.getRepositoryService()
				.createProcessDefinitionQuery();
		if (includeHistoryVersion) {
			return processDefinitionQuery.list();
		} else {
			return processDefinitionQuery.latestVersion().list();
		}
	}

	/**
	 * 已部署流程列表
	 *
	 * @param includeHistoryVersion
	 *            是否包含历史版本
	 * @param firstResult
	 *            第一个下标
	 * @param maxResults
	 *            个数
	 * @return 流程定义列表
	 */
	public List<ProcessDefinition> deployedList(boolean includeHistoryVersion, int firstResult, int maxResults) {
		ProcessDefinitionQuery processDefinitionQuery = this.processEngine.getRepositoryService()
				.createProcessDefinitionQuery();
		if (includeHistoryVersion) {
			return processDefinitionQuery.listPage(firstResult, maxResults);
		} else {
			return processDefinitionQuery.latestVersion().listPage(firstResult, maxResults);
		}
	}

	/**
	 * 已启动的流程列表
	 *
	 * @return 流程实例对象列表
	 */
	public List<ProcessInstance> startedList() {
		return this.processEngine.getRuntimeService().createProcessInstanceQuery().list();
	}

	/**
	 * 已启动的流程列表
	 *
	 * @param firstResult
	 *            第一个下标
	 * @param maxResults
	 *            个数
	 * @return 流程实例对象列表
	 */
	public List<ProcessInstance> startedList(int firstResult, int maxResults) {
		return this.processEngine.getRuntimeService().createProcessInstanceQuery().listPage(firstResult, maxResults);
	}

	/**
	 * 获取指定流程实例的任务列表
	 *
	 * @param processInstanceId
	 *            流程实例Id
	 * @return 任务列表
	 */
	public List<Task> taskList(String processInstanceId) {
		return this.processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
	}

	/**
	 * 获取指定流程实例的任务列表
	 * 
	 * @param processInstanceId
	 *            流程实例Id
	 * @param taskAssigneeIds
	 *            流程代理人Id
	 * @return 任务列表
	 */
	public List<Task> taskList(String processInstanceId, List<String> taskAssigneeIds) {
		return this.processEngine.getTaskService().createTaskQuery().taskAssigneeIds(taskAssigneeIds).list();
	}
	
	/**
     * 获取审核记录列表
     * @param processInstanceId 流程实例Id
     * @return
     */
    public List<HistoricTaskInstance> complateHistoryList(String processInstanceId) {
        return this.processEngine.getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).orderByHistoricTaskInstanceEndTime().desc().list();
    }
    
	/**
	 * 根跟进任务节点id绘制图片，传入的任务节点将会用高亮红框圈住显示
	 *
	 * @param taskId
	 *            任务节点Id
	 * @return 流程图片流对象
	 */
	public InputStream graphicsByTaskId(String taskId) {
		if (taskId != null) {
			Task task = this.processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
			return graphicsByInstanceId(task.getProcessInstanceId());
		}
		return null;
	}

	/**
	 * 根据流程实例Id绘制png图片，将会用高亮红框圈住当前所处节点
	 *
	 * @param instanceId
	 *            流程定义Id
	 * @return 流程图片流对象
	 */
	public InputStream graphicsByInstanceId(String instanceId) {
		Command<InputStream> cmd = null;
		if (instanceId != null) {
			List<HistoricActivityInstance> historicActivityInstances = this.processEngine.getHistoryService()
					.createHistoricActivityInstanceQuery().processInstanceId(instanceId)
					.orderByHistoricActivityInstanceStartTime().asc().list();
			cmd = new ProcessInstanceDiagramCmd(instanceId, historicActivityInstances);
		}
		if (cmd != null) {
			InputStream is = null;
			try {
				is = this.processEngine.getManagementService().executeCommand(cmd);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return is;
		}
		return null;
	}

	/**
	 * 根据流程定义Id绘制png图片， 若在流程发布时发布了流程png图片，那么此处将直接返回发布时的png图片的流对象
	 * 若发布时未发布png图片，那么调用此方法将会返回 null
	 *
	 * @param definitionId
	 * @return 流程图片流对象
	 */
	public InputStream graphicsByDefinitionId(String definitionId) {
		Command<InputStream> cmd = null;
		if (definitionId != null) {
			cmd = new GetDeploymentProcessDiagramCmd(definitionId);
		}
		if (cmd != null) {
			InputStream is = this.processEngine.getManagementService().executeCommand(cmd);
			return is;
		}
		return null;
	}

	/**
	 * 流程实例绘制命令
	 */
	public class ProcessInstanceDiagramCmd implements Command<InputStream> {
		/**
		 * 流程实例id
		 **/
		protected String processInstanceId;

		private List<HistoricActivityInstance> historicTaskInstances;

		public ProcessInstanceDiagramCmd(String processInstanceId,
				List<HistoricActivityInstance> historicTaskInstances) {
			this.processInstanceId = processInstanceId;
			this.historicTaskInstances = historicTaskInstances;
		}

		/**
		 * 动态绘制流程图片
		 *
		 * @param commandContext
		 * @return
		 */
		public InputStream execute(CommandContext commandContext) {
			List<String> highLightedActivities = new ArrayList<>();
			ProcessInstance pi = ActivitiService.this.processEngine.getRuntimeService().createProcessInstanceQuery()
					.processInstanceId(processInstanceId).singleResult();
			if (pi != null) {
				highLightedActivities.add(pi.getActivityId());
			}
			if (historicTaskInstances != null) {
				for (HistoricActivityInstance historicActivityInstance : historicTaskInstances) {
					highLightedActivities.add(historicActivityInstance.getActivityId());
				}
			}
			String processDefinitionId;
			try {
				ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
				ExecutionEntity rootExecutionEntity = executionEntityManager
						.findByRootProcessInstanceId(processInstanceId);
				processDefinitionId = rootExecutionEntity.getProcessDefinitionId();
			} catch (Exception e) {
				processDefinitionId = ActivitiService.this.processEngine.getHistoryService()
						.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult()
						.getProcessDefinitionId();
			}
			GetBpmnModelCmd getBpmnModelCmd = new GetBpmnModelCmd(processDefinitionId);
			BpmnModel bpmnModel = getBpmnModelCmd.execute(commandContext);
			List<String> highLightedFlows = getHighLightedFlows(bpmnModel, historicTaskInstances);
			if (highLightedFlows == null) {
				highLightedFlows = Collections.emptyList();
			}
			DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
			String activityFontName = ActivitiService.this.processEngine.getProcessEngineConfiguration()
					.getActivityFontName();
			String annotationFontName = ActivitiService.this.processEngine.getProcessEngineConfiguration()
					.getAnnotationFontName();
			String labelFontName = ActivitiService.this.processEngine.getProcessEngineConfiguration()
					.getLabelFontName();
			InputStream is = generator.generateDiagram(bpmnModel, "jpg", highLightedActivities, highLightedFlows,
					activityFontName, labelFontName, annotationFontName, null, 1.0);
			return is;
		}

		/**
		 * 获取已经执行的流程节点之间的连线集合
		 *
		 * @param bpmnModel
		 *            bpmn模型
		 * @param historicActivityInstances
		 *            已经执行的流程节点
		 * @return
		 */
		public List<String> getHighLightedFlows(BpmnModel bpmnModel,
				List<HistoricActivityInstance> historicActivityInstances) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 24小时制
			List<String> highFlows = new ArrayList<>();// 用以保存高亮的线flowId
			// 对历史流程节点进行遍历
			for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
				// 得到节点定义的详细信息
				FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess()
						.getFlowElement(historicActivityInstances.get(i).getActivityId());
				List<FlowNode> sameStartTimeNodes = new ArrayList<>();// 用以保存后续开始时间相同的节点
				FlowNode sameActivityImpl1 = null;
				HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);// 第一个节点
				HistoricActivityInstance activityImp2_;
				for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
					activityImp2_ = historicActivityInstances.get(k);// 后续第1个节点
					if (activityImpl_.getActivityType().equals("userTask")
							&& activityImp2_.getActivityType().equals("userTask")
							&& df.format(activityImpl_.getStartTime()).equals(df.format(activityImp2_.getStartTime()))) // 都是usertask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
					{

					} else {
						sameActivityImpl1 = (FlowNode) bpmnModel.getMainProcess()
								.getFlowElement(historicActivityInstances.get(k).getActivityId());// 找到紧跟在后面的一个节点
						break;
					}
				}
				sameStartTimeNodes.add(sameActivityImpl1); // 将后面第一个节点放在时间相同节点的集合里
				for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
					HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
					HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
					if (df.format(activityImpl1.getStartTime()).equals(df.format(activityImpl2.getStartTime()))) {// 如果第一个节点和第二个节点开始时间相同保存
						FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess()
								.getFlowElement(activityImpl2.getActivityId());
						sameStartTimeNodes.add(sameActivityImpl2);
					} else {// 有不相同跳出循环
						break;
					}
				}
				List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows(); // 取出节点的所有出去的线
				for (SequenceFlow pvmTransition : pvmTransitions) {// 对所有的线进行遍历
					FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess()
							.getFlowElement(pvmTransition.getTargetRef());// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
					if (sameStartTimeNodes.contains(pvmActivityImpl)) {
						highFlows.add(pvmTransition.getId());
					}
				}
			}
			return highFlows;
		}
	}
}
