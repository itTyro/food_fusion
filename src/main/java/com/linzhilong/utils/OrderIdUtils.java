package com.linzhilong.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class OrderIdUtils {
    public static Long getOrderId(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
        String time = LocalDateTime.now().format(formatter);
        Random r = new Random();
        String rand = (r.nextInt(9000) + 1000) + "";


        long orderId = Long.parseLong(Long.parseLong(time) + rand);
        return orderId;
    }
}
