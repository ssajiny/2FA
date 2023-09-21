package com.example.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Controller
@RequestMapping("/otp")
public class OtpController {

    @RequestMapping ("/index")
    public String main() {
        return "otp/index";
    }
    @RequestMapping("/cal")
    public String calOtp(@RequestParam("secretkey") String secretKey, ModelMap model) {
        model.addAttribute("error", secretKey.length()<8);

        long currentTimeMillis = new Date().getTime();
        long minuteInterval = currentTimeMillis / 30000;

        long checkCode = 0;
        try {
            checkCode = checkCode(secretKey, minuteInterval);
        }  catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        model.addAttribute("secretKey", secretKey);
        model.addAttribute("checkCode", checkCode);

        return "otp/cal";
    }

    public String byteArrayToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    private long checkCode(String secret, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }

        Base32 codec = new Base32();
        byte[] decodedKey32 = codec.decode(secret);

        SecretKeySpec signKey = new SecretKeySpec(decodedKey32, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0xF;

        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xFF);
        }

        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;

        return (int)truncatedHash;
    }

    @ResponseBody
    @RequestMapping("/fetchCheck")
    public long fetchCheck(@RequestBody String secretKey) {
        long currentTimeMillis = new Date().getTime();
        long minuteInterval = currentTimeMillis / 30000;
        String key;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(secretKey);
            key = rootNode.get("secretKey").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        long checkCode = 0;
        try {
            checkCode = checkCode(key, minuteInterval);
        }  catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return checkCode;
    }
}
