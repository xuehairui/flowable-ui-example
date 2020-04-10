package com.promote.app.listen;

import cn.hutool.core.util.ObjectUtil;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;


@Component
public class TaskHandler implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) {
        if (ObjectUtil.isNotEmpty(delegateExecution.getVariable("nrOfCompletedInstances"))) {
            //已完成的次数
            int completedInstance = (int)delegateExecution.getVariable("nrOfCompletedInstances");
            int sum = (int) delegateExecution.getVariable("nrOfInstances");

            String pass = (String) delegateExecution.getVariable("pass");
            if ("0".equals(pass)) {
                delegateExecution.setVariable("result", "0");
            }
            String result = "";
            if (ObjectUtil.isNotEmpty(delegateExecution.getVariable("result"))) {
                result = String.valueOf(delegateExecution.getVariable("result"));
            }
            if (!result.equals("0") && completedInstance == sum) {
                delegateExecution.setVariable("result", "1");
            }
        }
    }
}
