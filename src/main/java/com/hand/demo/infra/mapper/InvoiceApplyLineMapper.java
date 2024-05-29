package com.hand.demo.infra.mapper;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author alfredo.frangoulis@hand-global.com
 * @since 2024-05-21 14:23:12
 */
public interface InvoiceApplyLineMapper extends BaseMapper<InvoiceApplyLine> {
    /**
     * 基础查询
     *
     * @param invoiceApplyLine 查询条件
     * @return 返回值
     */
    List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine);
    List<InvoiceApplyLine> fuzzySearch(InvoiceApplyLine invoiceApplyLine);

}

