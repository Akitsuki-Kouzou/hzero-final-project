package com.hand.demo.app.service.job;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceInfoDTO;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.feign.InvoiceInfoFeign;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisQueueHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@JobHandler("InvokeHeaderTask")
public class InvokeHeaderTask implements IJobHandler {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private RedisQueueHelper redisQueueHelper;

    @Autowired
    private InvoiceInfoFeign invoiceInfoFeign;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        List<InvoiceApplyHeader> failHeader = invoiceApplyHeaderRepository.failHeaders();
        String dataString = JSON.toJSONString(failHeader);
        redisQueueHelper.push("invoice-info-46319", dataString);
        InvoiceInfoDTO invoiceInfoDTO = new InvoiceInfoDTO();
        invoiceInfoDTO.setContent(dataString);
        invoiceInfoDTO.setEmployeeId("46319");
        invoiceInfoFeign.sendInvoiceInfo(invoiceInfoDTO);
        return ReturnT.SUCCESS;
    }
}
