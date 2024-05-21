package com.hand.demo.domain.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * (InvoiceApplyLine)实体类
 *
 * @author alfredo.frangoulis@hand-global.com
 * @since 2024-05-21 14:23:12
 */

@Getter
@Setter
@ApiModel("")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "46324_invoice_apply_line")
public class InvoiceApplyLine extends AuditDomain {
    private static final long serialVersionUID = -60631936854586213L;

    public static final String FIELD_APPLY_LINE_ID = "applyLineId";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_APPLY_HEADER_ID = "applyHeaderId";
    public static final String FIELD_INVOICE_NAME = "invoiceName";
    public static final String FIELD_CONTENT_NAME = "contentName";
    public static final String FIELD_TAX_CLASSIFICATION_NUMBER = "taxClassificationNumber";
    public static final String FIELD_UNIT_PRICE = "unitPrice";
    public static final String FIELD_QUANTITY = "quantity";
    public static final String FIELD_EXCLUDE_TAX_AMOUNT = "excludeTaxAmount";
    public static final String FIELD_TAX_RATE = "taxRate";
    public static final String FIELD_TAX_AMOUNT = "taxAmount";
    public static final String FIELD_TOTAL_AMOUNT = "totalAmount";
    public static final String FIELD_REMARK = "remark";
    public static final String FIELD_ATTRIBUTE1 = "attribute1";
    public static final String FIELD_ATTRIBUTE2 = "attribute2";
    public static final String FIELD_ATTRIBUTE3 = "attribute3";
    public static final String FIELD_ATTRIBUTE4 = "attribute4";
    public static final String FIELD_ATTRIBUTE5 = "attribute5";
    public static final String FIELD_ATTRIBUTE6 = "attribute6";
    public static final String FIELD_ATTRIBUTE7 = "attribute7";
    public static final String FIELD_ATTRIBUTE8 = "attribute8";
    public static final String FIELD_ATTRIBUTE9 = "attribute9";
    public static final String FIELD_ATTRIBUTE10 = "attribute10";
    public static final String FIELD_ATTRIBUTE11 = "attribute11";
    public static final String FIELD_ATTRIBUTE12 = "attribute12";
    public static final String FIELD_ATTRIBUTE13 = "attribute13";
    public static final String FIELD_ATTRIBUTE14 = "attribute14";
    public static final String FIELD_ATTRIBUTE15 = "attribute15";

    @Id
    @GeneratedValue
    private Long applyLineId;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotNull
    private Long tenantId;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotNull
    private Long applyHeaderId;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotBlank
    private String invoiceName;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotBlank
    private String contentName;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotBlank
    private String taxClassificationNumber;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotNull
    private BigDecimal unitPrice;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotNull
    private BigDecimal quantity;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotNull
    private BigDecimal excludeTaxAmount;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotNull
    private BigDecimal taxRate;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotNull
    private BigDecimal taxAmount;

    @ApiModelProperty(value = "${column.comment}", required = true)
    @NotNull
    private BigDecimal totalAmount;

    private String remark;

    private String attribute1;

    private String attribute2;

    private String attribute3;

    private String attribute4;

    private String attribute5;

    private String attribute6;

    private String attribute7;

    private String attribute8;

    private String attribute9;

    private String attribute10;

    private String attribute11;

    private String attribute12;

    private String attribute13;

    private String attribute14;

    private String attribute15;


}

