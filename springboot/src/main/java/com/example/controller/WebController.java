package com.example.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.ExaminationOrderMapper;
import com.example.mapper.ExaminationPackageMapper;
import com.example.mapper.PhysicalExaminationMapper;
import com.example.service.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class WebController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @Resource
    private DoctorService doctorService;

    @Resource
    ExaminationOrderMapper examinationOrderMapper;

    @Resource
    PhysicalExaminationMapper physicalExaminationMapper;

    @Resource
    ExaminationPackageMapper examinationPackageMapper;

    @Resource
    TitleService titleService;

    @Resource
    OfficeService officeService;


    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello () {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account loginAccount = null;
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            loginAccount = adminService.login(account);
        } else if (RoleEnum.USER.name().equals(account.getRole())) {
            loginAccount = userService.login(account);
        } else if (RoleEnum.DOCTOR.name().equals(account.getRole())) {
            loginAccount = doctorService.login(account);
        } else {
            throw new CustomException("500", "非法请求");
        }
        return Result.success(loginAccount);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            adminService.updatePassword(account);
        } else if (RoleEnum.DOCTOR.name().equals(account.getRole())) {
            doctorService.updatePassword(account);
        } else if (RoleEnum.USER.name().equals(account.getRole())) {
            userService.updatePassword(account);
        }
        return Result.success();
    }


    @GetMapping("/getCountData")
    public Result getCountData() {
        List<ExaminationOrder> examinationorders = examinationOrderMapper.selectAll(null);
        // 普通体检金额统计
        Integer physicalExaminationMoney = examinationorders.stream().filter(o -> o.getOrderType().equals("普通体检"))
                .filter(o -> o.getStatus().equals("待检查")|| o.getStatus().equals("待上传报告")|| o.getStatus().equals("已完成"))
                .map(ExaminationOrder::getMoney).reduce(Integer::sum).orElse(0);

        // 套餐体检金额统计
        Integer examinationPackageMoney = examinationorders.stream().filter(o -> o.getOrderType().equals("套餐体检"))
                .filter(o -> o.getStatus().equals("待检查")|| o.getStatus().equals("待上传报告")|| o.getStatus().equals("已完成"))
                .map(ExaminationOrder::getMoney).reduce(Integer::sum).orElse(0);

        // 普通体检订单数量统计
       long physicalExaminationCount = physicalExaminationMapper.selectAll(null).size();
        // 套餐体检订单数量统计
        long examinationPackageCount = examinationPackageMapper.selectAll(null).size();
        HashMap<Object,Object> map = new HashMap<>();

        map.put("physicalExaminationMoney",physicalExaminationMoney);
        map.put("examinationPackageMoney",examinationPackageMoney);
        map.put("physicalExaminationCount",physicalExaminationCount);
        map.put("examinationPackageCount",examinationPackageCount);

        return Result.success(map);
    }

    @GetMapping("/lineData")
    public Result getLineData() {
        List<ExaminationOrder> examinationOrders = examinationOrderMapper.selectAll(null);
        // 获取到一个月的日期
        Date date = new Date();  // 今天
        DateTime start = DateUtil.offsetDay(date, -30);  // 30天之前的日期
        List<DateTime> dateTimeList = DateUtil.rangeToList(start, date, DateField.DAY_OF_YEAR);
        List<String> dateStrList = dateTimeList.stream().map(DateUtil::formatDate).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        ArrayList<Integer> moneyList = new ArrayList<>();
        for (String day : dateStrList) {
            // 统计当天的销售额
            Integer money = examinationOrders.stream().filter(o -> o.getCreateTime().contains(day))
                    .filter(o -> o.getStatus().equals("待检查") || o.getStatus().equals("待上传报告") || o.getStatus().equals("已完成"))
                    .map(ExaminationOrder::getMoney).reduce(Integer::sum).orElse(0);
            moneyList.add(money);
        }
        HashMap<Object, Object> map = new HashMap<>();
        map.put("dateList", dateStrList);
        map.put("moneyList", moneyList);
        return Result.success(map);
    }

    @GetMapping("/pieData")
    public Result getPieData() {
        // 必须包含  name和value2个属性
        List<Title> titleList = titleService.selectAll(null);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Title title : titleList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", title.getName());
            Integer count = doctorService.selectByTitleId(title.getId());
            map.put("value", count);
            list.add(map);
        }
        return Result.success(list);
    }

    @GetMapping("/barData")
    public Result getDarData() {
        List<Office> officeList = officeService.selectAll(null);
        List<String> officeNames = officeList.stream().map(Office::getName).collect(Collectors.toList());
        ArrayList<Integer> list = new ArrayList<>();
        for (Office office : officeList) {
            Integer count = doctorService.selectByOfficeId(office.getId());
            list.add(count);
        }
        HashMap<Object, Object> map = new HashMap<>();
        map.put("officeList", officeNames);
        map.put("countList", list);
        return Result.success(map);
    }
}
