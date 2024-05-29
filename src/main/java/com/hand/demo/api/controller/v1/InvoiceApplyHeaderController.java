package com.hand.demo.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
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
import java.util.List;

/**
 * (InvoiceApplyHeader)表控制层
 *
 * @author marsa.ariqi@hand-global.com
 * @since 2024-05-21 14:19:52
 */

@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping
    public ResponseEntity<Page<InvoiceApplyHeader>> list(InvoiceApplyHeader invoiceApplyHeader, @PathVariable Long organizationId, @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
            direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvoiceApplyHeader> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "Get By Id")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping("/{applyHeaderId}")
    public ResponseEntity<InvoiceApplyHeader> detail(@PathVariable Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        return Results.success(invoiceApplyHeader);
    }

    @ApiOperation(value = "details")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping("/details/{applyHeaderId}")
    public ResponseEntity<InvoiceApplyHeader> detailWithLine(@PathVariable Long applyHeaderId) {
        return invoiceApplyHeaderService.detailWithLine(applyHeaderId);
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
        invoiceApplyHeaders.forEach(item -> item.setDelFlag(1));
        for(InvoiceApplyHeader header: invoiceApplyHeaders){
            invoiceApplyHeaderService.deleteRedisCache(header);
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
        }
        return Results.success();
    }

    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @DeleteMapping("/{applyHeaderId}")
    public ResponseEntity<InvoiceApplyHeader> deleteById(@PathVariable Long applyHeaderId) {
        return invoiceApplyHeaderService.deleteById(applyHeaderId);
    }

    @ApiOperation(value = "fuzzy")
    @GetMapping("/search")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    public List<InvoiceApplyHeader> fuzzySearch(InvoiceApplyHeader invoiceApplyHeaders) {
        return invoiceApplyHeaderService.fuzzySearch(invoiceApplyHeaders);
    }

    @ApiOperation(value = "Export")
    @GetMapping("/export")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ExcelExport(InvoiceApplyHeader.class)
    public ResponseEntity<List<InvoiceApplyHeader>> export(
            @RequestParam("exportType") String exportType,
            ExportParam exportParam, HttpServletResponse response, InvoiceApplyHeader invoiceApplyHeader, @PathVariable Long organizationId, @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
            direction = Sort.Direction.ASC) PageRequest pageRequest) {
        Page<InvoiceApplyHeader> page = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return ResponseEntity.ok(page);
    }
}

