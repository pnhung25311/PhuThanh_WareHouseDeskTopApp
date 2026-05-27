package com.phuthanh.helper.function;

import com.phuthanh.network.ApiClient;

public class SendNotificationByTelegram {
    public String sendTelegramNotification(String message) {
        try {
            ApiClient apiClient = new ApiClient();
            String endpoint = "telegram/send-notification-cart";

            // tạo JSON body đúng format Spring đang nhận
            String jsonBody = message.toString();

            // dùng hàm post có sẵn của bạn
            String response = apiClient.post(endpoint, jsonBody);

            System.out.println("Telegram response: " + response);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending telegram notification: " + e.getMessage();
        }
    }
}
