package com.hand.demo.app.service.job;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceInfoFeignDTO;
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
        try{
            List<InvoiceApplyHeader> task = invoiceApplyHeaderRepository.failedHeaderCollections();
            String jsonStr = JSON.toJSONString(task);

            redisQueueHelper.push("invoice-info-46324", jsonStr);
            InvoiceInfoFeignDTO invoiceInfoFeignDTO = new InvoiceInfoFeignDTO();
            invoiceInfoFeignDTO.setContent(jsonStr);
            invoiceInfoFeignDTO.setEmployeeId("46324");
            invoiceInfoFeign.sentInvoiceInfo(invoiceInfoFeignDTO);
        } catch (Exception e) {
            tool.error(e.getMessage());
            return ReturnT.FAILURE;
        }

        return ReturnT.SUCCESS;
    }
}
