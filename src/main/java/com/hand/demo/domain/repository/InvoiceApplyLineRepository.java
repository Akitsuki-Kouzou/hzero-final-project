package com.hand.demo.domain.repository;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import io.swagger.models.auth.In;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * (InvoiceApplyLine)资源库
 *
 * @author marsa.ariqi@hand-global.com
 * @since 2024-05-21 14:20:34
 */
public interface InvoiceApplyLineRepository extends BaseRepository<InvoiceApplyLine> {
    /**
     * 查询
     *
     * @param invoiceApplyLine 查询条件
     * @return 返回值
     */
    List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine);
    List<InvoiceApplyLine> selectListHN(InvoiceApplyLine invoiceApplyLine);
    /**
     * 根据主键查询（可关联表）
     *
     * @param applyLineId 主键
     * @return 返回值
     */
    InvoiceApplyLine selectByPrimary(Long applyLineId);
    List<InvoiceApplyLine> fuzzySearch(InvoiceApplyLine invoiceApplyLine);
    List<InvoiceApplyLine> selectByHeaderId(Long applyHeaderId);
    boolean isValidHeaderId(Long applyHeaderId);
}
