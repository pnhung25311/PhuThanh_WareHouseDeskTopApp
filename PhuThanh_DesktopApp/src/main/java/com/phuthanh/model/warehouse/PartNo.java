package com.phuthanh.model.warehouse;

import java.util.Date;

public class PartNo {
    
    private int partNoAID;
    private String productID;
    private String idpartNo;
    private String partNoID;
    private String nameEnglish;
    private String nameVietNamese;
    private double partNoQty;
    private String parameter;
    private String remark;
    private Date lastTime;

    public PartNo(int partNoAID, String productID, String idpartNo, String partNoID, String nameEnglish,
			String nameVietNamese, double partNoQty, String parameter, String remark, Date lastTime) {
		this.partNoAID = partNoAID;
		this.productID = productID;
		this.idpartNo = idpartNo;
		this.partNoID = partNoID;
		this.nameEnglish = nameEnglish;
		this.nameVietNamese = nameVietNamese;
		this.partNoQty = partNoQty;
		this.parameter = parameter;
		this.remark = remark;
		this.lastTime = lastTime;
	}
	public int getPartNoAID() {
        return partNoAID;
    }
    public void setPartNoAID(int partNoAID) {
        this.partNoAID = partNoAID;
    }
    public String getProductID() {
		return productID;
	}
	public void setProductID(String productID) {
		this.productID = productID;
	}
	public String getIdpartNo() {
		return idpartNo;
	}
	public void setIdpartNo(String idpartNo) {
		this.idpartNo = idpartNo;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	public String getPartNoID() {
        return partNoID;
    }
    public void setPartNoID(String partNoID) {
        this.partNoID = partNoID;
    }
    public String getNameEnglish() {
        return nameEnglish;
    }
    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }
    public String getNameVietNamese() {
        return nameVietNamese;
    }
    public void setNameVietNamese(String nameVietNamese) {
        this.nameVietNamese = nameVietNamese;
    }
    public double getPartNoQty() {
        return partNoQty;
    }
    public void setPartNoQty(double partNoQty) {
        this.partNoQty = partNoQty;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public Date getLastTime() {
        return lastTime;
    }
    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    
}
