package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author ariel.peaceo@hand-global.com
 * @since 2024-05-21 14:06:03
 */
public interface InvoiceApplyHeaderMapper extends BaseMapper<InvoiceApplyHeader> {
    /**
     * 基础查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeader> selectHead(InvoiceApplyHeader invoiceApplyHeader);

    List<InvoiceApplyHeader> selectStatusFailed();
}

