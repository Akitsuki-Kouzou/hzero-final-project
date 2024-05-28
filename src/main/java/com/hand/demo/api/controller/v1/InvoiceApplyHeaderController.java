package com.hand.demo.api.controller.v1;

import com.alibaba.fastjson.JSON;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.export.annotation.ExcelExport;
import org.hzero.export.vo.ExportParam;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (InvoiceApplyHeader)表控制层
 *
 * @author ariel.peaceo@hand-global.com
 * @since 2024-05-21 14:06:03
 */

@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @Autowired
    private MessageClient messageClient;

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<Page<InvoiceApplyHeader>> list(InvoiceApplyHeader invoiceApplyHeader, @PathVariable Long organizationId, @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
            direction = Sort.Direction.DESC) PageRequest pageRequest) {
        invoiceApplyHeader.setTenantId(organizationId);
        Page<InvoiceApplyHeader> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping("/{applyHeaderId}")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<InvoiceApplyHeader> detail(@PathVariable Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        return Results.success(invoiceApplyHeader);
    }

    @ApiOperation(value = "创建或更新")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyHeader>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        validObject(invoiceApplyHeaders);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyHeaderService.saveData(invoiceApplyHeaders);
        return Results.success(invoiceApplyHeaders);
    }

    @ApiOperation(value = "删除")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @DeleteMapping
    public ResponseEntity<InvoiceApplyHeader> remove(@RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item -> item.setDelFlag(BaseConstants.Flag.YES));
        invoiceApplyHeaderRepository.batchUpdateOptional(invoiceApplyHeaders, InvoiceApplyHeader.FIELD_DEL_FLAG);
        //  delete from Redis
        invoiceApplyHeaders.forEach(item -> invoiceApplyHeaderRepository.deleteRedis(item.getApplyHeaderId()));
        return Results.success();
    }

    @ApiOperation(value = "EXPORT YO")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping("/export")
    @ExcelExport(InvoiceApplyHeader.class)
    public ResponseEntity<List<InvoiceApplyHeader>> export(@PathVariable Long organizationId,
                                                           ExportParam exportParam, InvoiceApplyHeader invoiceApplyHeader, HttpServletResponse response){
        invoiceApplyHeader.setTenantId(organizationId);
        return Results.success( invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
    }

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping("/send-email")
    public ResponseEntity<List<InvoiceApplyHeader>> send_email() {
        Long tenantId = DetailsHelper.getUserDetails().getTenantId();
        List<InvoiceApplyHeader> task = invoiceApplyHeaderRepository.selectStatusFailed();
        String jsonStr = JSON.toJSONString(task);
        Receiver rcv = new Receiver();
        Map<String, String> args = new HashMap<>();
        args.put("employeeId", "46321");
        args.put("content", jsonStr);
        rcv.setEmail("ariel.peaceo@hand-global.com");
        messageClient.sendEmail(tenantId, "AKI.EMAIL", "46321-TEMPLATE", Collections.singletonList(rcv), args);
        return Results.success(task);
    }
}

