package com.skripsi.siap_sewa.utils;

import com.skripsi.siap_sewa.dto.ApiResponse;
import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class CommonUtils {
    public ResponseEntity<ApiResponse> setResponse(ErrorMessageEnum errorMessageEnum, Object response){
        return new ResponseEntity<>(
                ApiResponse.builder()
                        .errorSchema(ApiResponse.ErrorSchema.builder()
                                .errorCode(errorMessageEnum.getErrorCode())
                                .errorMessage(errorMessageEnum.getErrorMessage())
                                .build()
                        )
                        .outputSchema(response)
                        .build(),
                errorMessageEnum.getHttpStatus());
    }

    public ResponseEntity<ApiResponse> setValidationErrorResponse(List<Map<String, Object>> validationErrors) {
        return new ResponseEntity<>(
                ApiResponse.builder()
                        .errorSchema(ApiResponse.ErrorSchema.builder()
                                .errorCode(Constant.BAD_REQUEST_CODE)
                                .errorMessage("Request invalid")
                                .build()
                        )
                        .outputSchema(validationErrors)
                        .build(),
                HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ApiResponse> setResponse(String errorCode, String errorMessage, HttpStatus httpStatus, Object response){
        return new ResponseEntity<>(
                ApiResponse.builder()
                        .errorSchema(ApiResponse.ErrorSchema.builder()
                                .errorCode(errorCode)
                                .errorMessage(errorMessage)
                                .build()
                        )
                        .outputSchema(response)
                        .build(),
                httpStatus);
    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    public String generateOtpMessage(String otp) {
        return String.format(
                """
                        Hi, Sobat Sewa.
                        
                        Berikut merupakan one-time passcode (OTP) kamu : %s.
                        
                        OTP akan expired dalam 30 menit.
                        
                        Selamat menggunakan website Pintu Sewa
                        
                        Hormat kami,
                        Tim Pintu Sewa
                """, otp
        );
    }

    public boolean isNull(String parameter){
        return parameter == null || parameter.isEmpty() || parameter.equalsIgnoreCase("");
    }

    public String generateEmailShop(String shopName) {
        return String.format(
                """
                Hi, Sobat Sewa.
                
                Selamat atas pembukaan toko baru Anda, %s!
                Kami sangat senang melihat Anda bergabung dengan komunitas Pintu Sewa.
                
                Kami berharap toko Anda sukses dan dapat menjangkau banyak pelanggan.
                Jangan ragu untuk menghubungi kami jika ada yang bisa kami bantu.
                
                Selamat berjualan dan sukses selalu!
                
                Hormat kami,
                Tim Pintu Sewa
                """, shopName
        );
    }

    public static String getRentDurationName(int code) {
        return switch (code) {
            case 1 -> "Harian";
            case 2 -> "Mingguan";
            case 3 -> "Bulanan";
            case 4 -> "Harian;Mingguan";
            case 5 -> "Mingguan;Bulanan";
            case 6 -> "Harian;Bulanan";
            case 7 -> "Harian;Mingguan;Bulanan";
            default -> "Tidak diketahui";
        };
    }

    public static String getRelativeTimeFromNow(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "waktu tidak diketahui";
        }

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "baru saja";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " menit yang lalu";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " jam yang lalu";
        }

        long days = hours / 24;
        if (days < 30) {
            return days + " hari yang lalu";
        }

        long months = days / 30;
        if (months < 12) {
            return months + " bulan yang lalu";
        }

        long years = months / 12;
        return years + " tahun yang lalu";
    }

}