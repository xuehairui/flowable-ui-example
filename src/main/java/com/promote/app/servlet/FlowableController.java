package com.promote.app.servlet;

import com.promote.app.service.FlowableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Api("流程管理")
@RestController
public class FlowableController {

    @Autowired
    private FlowableService flowableService;

    @ApiOperation("根据流程ID查询流程图")
    @GetMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        flowableService.genProcessDiagram(httpServletResponse,processId);
    }

}
