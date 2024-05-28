package com.hand.demo.app.service.job;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceInfoFeignDTO;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.feign.InvoiceInfoFeign;
import io.choerodon.core.oauth.DetailsHelper;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisQueueHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JobHandler("invokeHeaderTask")
public class FindHeaderHandlerJob implements IJobHandler {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private RedisQueueHelper redisQueueHelper;

    @Autowired
    private InvoiceInfoFeign invoiceInfoFeign;

    @Autowired
    private MessageClient messageClient;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        try {
//            Run the schedule Task
            Long tenantId = DetailsHelper.getUserDetails().getTenantId();
            List<InvoiceApplyHeader> task = invoiceApplyHeaderRepository.selectStatusFailed();
            String jsonStr = JSON.toJSONString(task);
//            Push the Queue Message
            redisQueueHelper.push("invoice-info-46321", jsonStr);
            InvoiceInfoFeignDTO mess = new InvoiceInfoFeignDTO();
            mess.setContent(jsonStr);
            mess.setEmployeeId("46321");
            invoiceInfoFeign.sentInvoiceInfo(mess);
            Receiver receiver = new Receiver();
            Map<String, String> args = new HashMap<>();
            args.put("employeeId", "46321");
            args.put("content", jsonStr);
            receiver.setEmail("ariel.peaceo@hand-global.com");
            messageClient.sendEmail(tenantId, "AKI.EMAIL", "46321-TEMPLATE", Collections.singletonList(receiver), args);

        }catch (Exception e){
            tool.error(e.getMessage());
            return ReturnT.FAILURE;
        }
        return ReturnT.SUCCESS;
    }
}
