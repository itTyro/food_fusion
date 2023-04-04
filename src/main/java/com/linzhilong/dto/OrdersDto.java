package com.linzhilong.dto;

import com.linzhilong.entity.OrderDetail;
import com.linzhilong.entity.Orders;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
