package com.promote.app.listen;

import org.flowable.task.service.delegate.DelegateTask;

public class TaskListener implements org.flowable.task.service.delegate.TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        /*String result = (String) delegateTask.getVariable("result");
        //ExecutionListner类中设置的拒绝计数变量
        int rejectedCount = (int)delegateTask.getVariable(“reject”);
        if(“reject”.equals(result)){
            //拒绝
            delegateTask.setVariable("rejected", ++rejectedCount);
        }*/
    }
}
