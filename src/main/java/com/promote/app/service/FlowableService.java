package com.promote.app.service;

import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlowableService {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    public void createprocess() {
        Process process = new Process();
        process.setId("leaveProcess");
        process.setName("leaveProcess");

        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        startEvent.setName("开始");

        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        endEvent.setName("结束");

        UserTask createLeave = new UserTask();
        createLeave.setId("createTask");
        createLeave.setName("创建请假单");
        createLeave.setAssignee("聂风");

        UserTask auditLeave = new UserTask();
        auditLeave.setId("auditTask");
        auditLeave.setName("领导审批");
        auditLeave.setAssignee("雄霸");

        ExclusiveGateway gateway = new ExclusiveGateway();
        gateway.setId("gateway");
        gateway.setName("审批网关");

        List<SequenceFlow> inList = new ArrayList<>();
        List<SequenceFlow> outList = new ArrayList<>();


        SequenceFlow sequenceFlow = new SequenceFlow(startEvent.getId(), createLeave.getId());
        SequenceFlow sequenceFlow2 = new SequenceFlow(createLeave.getId(), auditLeave.getId());
        SequenceFlow sequenceFlow3 = new SequenceFlow(auditLeave.getId(), gateway.getId());
        SequenceFlow sequenceFlow4 = new SequenceFlow(gateway.getId(), endEvent.getId());
        sequenceFlow4.setConditionExpression("${flag == '1'}");
        sequenceFlow4.setName("审核通过");
        SequenceFlow sequenceFlow5 = new SequenceFlow(gateway.getId(), createLeave.getId());
        sequenceFlow5.setName("拒绝");
        sequenceFlow5.setConditionExpression("${flag == '0'}");

        inList.add(sequenceFlow3);
        outList.add(sequenceFlow4);
        outList.add(sequenceFlow5);

        gateway.setIncomingFlows(inList);
        gateway.setOutgoingFlows(outList);

        process.addFlowElement(startEvent);
        process.addFlowElement(endEvent);
        process.addFlowElement(createLeave);
        process.addFlowElement(auditLeave);
        process.addFlowElement(gateway);
        process.addFlowElement(sequenceFlow);
        process.addFlowElement(sequenceFlow2);
        process.addFlowElement(sequenceFlow3);
        process.addFlowElement(sequenceFlow4);
        process.addFlowElement(sequenceFlow5);

        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);


        ProcessValidator processValidator = new ProcessValidatorFactory().createDefaultProcessValidator();
        List<ValidationError> errorList = processValidator.validate(bpmnModel);
        if (errorList.size() > 0) {
            throw new RuntimeException("流程有误，请检查后重试");
        }
        String fileName="model_bpmn20.xml";

        //生成自动布局
        new BpmnAutoLayout(bpmnModel).execute();
        Deployment deploy =repositoryService.createDeployment().addBpmnModel(fileName,bpmnModel)
                .tenantId(process.getId())
                .deploy();

        System.out.println("deployId:"+deploy.getId());
    }

    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().finished()
                .processInstanceId(processInstanceId).count() > 0;
    }


    /**
     * 生成流程图
     *
     * @param processId 流程部署id
     */
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        /**
         * 获得当前活动的节点
         */
        String processDefinitionId = "";
        if (this.isFinished(processId)) {// 如果流程已经结束，则得到结束节点
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();

            processDefinitionId=pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId=pi.getProcessDefinitionId();
        }
        List<String> highLightedActivitis = new ArrayList<String>();

        /**
         * 获得活动的节点
         */
        List<HistoricActivityInstance> highLightedActivitList =  historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        for(HistoricActivityInstance tempActivity : highLightedActivitList){
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();

        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "bmp", highLightedActivitis, flows, engconf.getActivityFontName(),
                engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (IOException e) {
            //log.error("操作异常",e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }

    }

    /**
     * 任务历史
     *
     * @param processId 部署id
     */
    public List<HistoricActivityInstance> getHistoryProcess(String processId) {
        List<HistoricActivityInstance> list = historyService // 历史相关Service
                .createHistoricActivityInstanceQuery() // 创建历史活动实例查询
                .processInstanceId(processId) // 执行流程实例id
                .finished()
                .list();
        return list;
    }




}
