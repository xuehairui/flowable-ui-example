package com.promote.app.service;

import org.flowable.engine.*;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LeaveTest {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private HistoryService historyService;
    /**
     * 启动流程
     */
    @Test
    public void startprocess() {
        Map<String, Object> map = new HashMap<>();
        map.put("orgList", Arrays.asList(new String[] {"江宁区", "鼓楼区"}));
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("a666", map);
        System.out.println("实例id：" + instance.getId());


    }

    /**
     * 创建请假单
     */
    @Test
    public void create() {
        String taskId = "81bd9e33-20ad-11ea-95ee-e86a64e3d10e";
        Map<String, Object> map = new HashMap<>();
        //map.put("flag", "2");
        //map.put("transfer", "0");
        //map.put("num", 2);
        //String [] userArr = new String[] {"乔峰", "段誉"};
        //map.put("userlist", Arrays.asList(userArr));
        taskService.complete(taskId, map);
    }

    @Test
    public void createGroup() {
        String taskId = "55142f77-1b19-11ea-9204-e86a64e3d10e";
        Map<String, Object> map = new HashMap<>();
        //map.put("transfer", "0");
        //map.put("num", 2);
        String [] orgArr = new String[] {"鼓楼区城管", "江宁区城管"};
        map.put("orgs", Arrays.asList(orgArr));
        taskService.addComment(taskId, null, "审批通过");
        taskService.complete(taskId, map);
    }

    @Test
    public void queryTask() {
        List<Task> taskList = taskService.createTaskQuery().taskCandidateGroup("鼓楼区")./*or().taskCandidateOrAssigned("虚竹").*/orderByTaskCreateTime().asc().active().list();
        for (Task task : taskList) {
            System.out.println("taskId:->" + task.getId() + "::"+ task.getProcessDefinitionId());
            String name = repositoryService.getBpmnModel(task.getProcessDefinitionId()).getFlowElement(task.getTaskDefinitionKey()).getName();
            System.out.println("name->" + name);
            //taskService.claim(task.getId(), "江宁区城管123");
        }


        //


    }

    @Test
    public void moveTest() {
        taskService.addComment("9af5c3e7-20ad-11ea-afa4-e86a64e3d10e", "81bb7b49-20ad-11ea-95ee-e86a64e3d10e", "鼓楼区驳回");
        runtimeService.createChangeActivityStateBuilder().processInstanceId("81bb7b49-20ad-11ea-95ee-e86a64e3d10e")
                .moveActivityIdTo("mutiinstance", "sponsor").changeState();

        //runtimeService.createChangeActivityStateBuilder().processInstanceId("").moveActivityIdsToSingleActivityId()
    }

    /**
     * 城管大队审核通过
     */
    @Test
    public void audit() {
        String taskId = "4fc33800-1fef-11ea-9718-e86a64e3d10e";
        Map<String, Object> map = new HashMap<>();
        map.put("pass", "1");
        taskService.addComment(taskId, null, "审批通过");
        taskService.complete(taskId, map);
    }

}
