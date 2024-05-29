package com.hand.demo.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author alfredo.frangoulis@hand-global.com
 * @since 2024-05-21 14:21:52
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaders 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeaders);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
     */
    void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders);
    List<InvoiceApplyHeader> fuzzySearch(InvoiceApplyHeader invoiceApplyHeader);
    ResponseEntity<InvoiceApplyHeader> deleteById(Long applyHeaderId);
    ResponseEntity<InvoiceApplyHeader> getHeaderDetail(Long applyHeaderId);
    void updateHeaderAmounts(Long applyHeaderId);
}

