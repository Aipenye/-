package com.wms.controller;

import com.wms.common.PasswordUtil;
import com.wms.common.Result;
import com.wms.common.SmsProperties;
import com.wms.entity.User;
import com.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SmsProperties smsProperties;
    @Autowired
    private UserService userService;

    private static final String CODE_PREFIX = "sms:code:";
    private static final int CODE_EXPIRE_MINUTES = 5;

    @PostMapping("/sendCode")
    public Result sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }

        // 检查手机号是否已注册
        List<User> existing = userService.lambdaQuery().eq(User::getPhone, phone).list();
        if (!existing.isEmpty()) {
            return Result.fail("该手机号已注册");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(CODE_PREFIX + phone, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 调用阿里云短信SDK发送（需引入aliyun-java-sdk-core依赖后取消注释）
        // sendAliyunSms(phone, code);

        // 开发阶段：将验证码打印到控制台
        System.out.println("【验证码】手机号: " + phone + "，验证码: " + code + "（" + CODE_EXPIRE_MINUTES + "分钟有效）");

        return Result.success("验证码已发送");
    }

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        String name = body.get("name");
        String password = body.get("password");

        if (phone == null || code == null || name == null || password == null) {
            return Result.fail("参数不完整");
        }

        String cachedCode = redisTemplate.opsForValue().get(CODE_PREFIX + phone);
        if (cachedCode == null) {
            return Result.fail("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            return Result.fail("验证码错误");
        }

        List<User> existing = userService.lambdaQuery().eq(User::getPhone, phone).list();
        if (!existing.isEmpty()) {
            return Result.fail("该手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setName(name);
        user.setNo(phone); // 默认以手机号作为账号
        user.setPassword(PasswordUtil.encode(password));
        user.setRoleId(2); // 默认普通用户
        user.setIsvalid("Y");

        redisTemplate.delete(CODE_PREFIX + phone);
        return userService.save(user) ? Result.success("注册成功") : Result.fail("注册失败");
    }

    // 阿里云短信发送（引入SDK后启用）
    // private void sendAliyunSms(String phone, String code) {
    //     DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",
    //             smsProperties.getAccessKeyId(), smsProperties.getAccessKeySecret());
    //     IAcsClient client = new DefaultAcsClient(profile);
    //     SendSmsRequest request = new SendSmsRequest();
    //     request.setPhoneNumbers(phone);
    //     request.setSignName(smsProperties.getSignName());
    //     request.setTemplateCode(smsProperties.getTemplateCode());
    //     request.setTemplateParam("{\"code\":\"" + code + "\"}");
    //     client.getAcsResponse(request);
    // }
}
