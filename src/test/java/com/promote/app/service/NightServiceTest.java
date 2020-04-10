package com.promote.app.service;

import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NightServiceTest {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    /**
     * 启动流程
     */
    @Test
    public void startprocess() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("passdaycert");
        System.out.println("实例id：" + instance.getId());
    }

    /**
     * 不调车
     */
    @Test
    public void noTransfer() {
        String taskId = "37acf0ec-1a5e-11ea-9e56-e86a64e3d10e";
        Map<String, Object> map = new HashMap<>();
        map.put("transfer", "0");
        map.put("num", 2);
        map.put("user1", "乔峰");
        map.put("user2", "段誉");
        taskService.complete(taskId, map);
    }

    /**
     * 城管大队审核通过
     */
    @Test
    public void audit() {
        String taskId = "64508dbb-1a5e-11ea-b6d1-e86a64e3d10e";
        Map<String, Object> map = new HashMap<>();
        map.put("pass", "1");
        taskService.complete(taskId, map);
    }

    /**
     * 区城管局审核通过
     */
    @Test
    public void audit2() {
        String taskId = "";
        Map<String, Object> map = new HashMap<>();
        map.put("pass", "1");
        taskService.complete(taskId, map);
    }

    /**
     * 查询代办
     */
    @Test
    public void queryTask() {
        List<Task> taskList = taskService.createTaskQuery().taskCandidateOrAssigned("张三").active().orderByTaskCreateTime().asc().list();
        System.out.println(taskList.size());

    }

}
