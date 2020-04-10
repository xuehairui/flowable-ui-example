package com.promote.app.service;

import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FlowableServiceTest {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    public void test() {
         List<Model> list= repositoryService.createModelQuery().orderByCreateTime().desc().list();
        System.out.println("--------->list:"+list.size());

        List<Deployment> list2 = repositoryService.createDeploymentQuery().orderByDeploymenTime().desc().list();
        System.out.println("--------->list2:"+list2.size());
        list2.forEach(a->{
            System.out.println(a.getParentDeploymentId() + ":"+a.getName() + ":->" + a.getKey());
        });
    }

    @Test
    public void createprocess() {
        Process process = new Process();
        process.setId("process");

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

        SequenceFlow sequenceFlow = new SequenceFlow(startEvent.getId(), createLeave.getId());
        SequenceFlow sequenceFlow2 = new SequenceFlow(createLeave.getId(), auditLeave.getId());
        SequenceFlow sequenceFlow3 = new SequenceFlow(auditLeave.getId(), gateway.getId());
        SequenceFlow sequenceFlow4 = new SequenceFlow(gateway.getId(), endEvent.getId());
        sequenceFlow4.setConditionExpression("${flag == '1'}");
        sequenceFlow4.setName("审核通过");
        SequenceFlow sequenceFlow5 = new SequenceFlow(gateway.getId(), createLeave.getId());
        sequenceFlow5.setName("拒绝");
        sequenceFlow5.setConditionExpression("${flag == '0'}");

        SequenceFlow sequenceFlow6 = new SequenceFlow(auditLeave.getId(), endEvent.getId());
        process.addFlowElement(startEvent);
        process.addFlowElement(endEvent);
        process.addFlowElement(createLeave);
        process.addFlowElement(auditLeave);
        //process.addFlowElement(gateway);
        process.addFlowElement(sequenceFlow);
        process.addFlowElement(sequenceFlow2);
        //process.addFlowElement(sequenceFlow3);
        //process.addFlowElement(sequenceFlow4);
        //process.addFlowElement(sequenceFlow5);
        process.addFlowElement(sequenceFlow6);

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
                .tenantId("intelligentAsset")
                .deploy();
        System.out.println(deploy.getId()+ ":key" + deploy.getKey() + ":time" + deploy.getDeploymentTime());
    }

    @Test
    public void startProcess() {
        ProcessInstance instance = runtimeService.startProcessInstanceById("process:1:c6d16b9e-1696-11ea-bcee-e86a64e3d10e");
        System.out.println("instance:" + instance.getId());
    }

    @Test
    public void startProcess2() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKeyAndTenantId("leaveProcess", "leaveProcess");
        System.out.println("instance:" + instance.getId());
    }

    @Test
    public void createProcess2() {
        Process process = new Process();
        process.setId("leaveProcess12312312123");
        process.setName("leaveProcess12312123");

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


        //MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        //multiInstanceLoopCharacteristics.set

        UserTask auditLeave = new UserTask();
        auditLeave.setId("auditTask");
        auditLeave.setName("领导审批");
        auditLeave.setAssignee("雄霸");


        /*FlowableListener listener = new FlowableListener();
        listener.setEvent("create");
        listener.setImplementation("${}");
        listener.setImplementationType("delegateExpression");*/

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

        //gateway.setIncomingFlows(inList);
        //gateway.setOutgoingFlows(outList);

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

    @Test
    public void complete() {
        taskService.complete("2d392a9c-1738-11ea-9808-e86a64e3d10e");
    }

    @Test
    public void audit() {
        Map<String, Object> map = new HashMap<>();
        map.put("comment", "审核通过");
        map.put("flag", "1");
        taskService.complete("96c3996c-1738-11ea-aede-e86a64e3d10e", map);
    }
}
