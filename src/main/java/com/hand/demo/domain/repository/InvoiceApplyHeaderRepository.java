package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author ariel.peaceo@hand-global.com
 * @since 2024-05-21 14:06:03
 */
public interface InvoiceApplyHeaderRepository extends BaseRepository<InvoiceApplyHeader> {
    /**
     * 查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeader> selectList(InvoiceApplyHeader invoiceApplyHeader);

    /**
     * 根据主键查询（可关联表）
     *
     * @param applyHeaderId 主键
     * @return 返回值
     */
    InvoiceApplyHeader selectByPrimary(Long applyHeaderId);

    void updateRedis(Long applyHeaderId);

    void deleteRedis(Long applyHeaderId);

    List<InvoiceApplyHeader> selectStatusFailed();
}
