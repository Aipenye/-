package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.common.PasswordUtil;
import com.wms.entity.Menu;
import com.wms.entity.User;
import com.wms.service.WorkOrderService;
import com.wms.entity.WorkOrder;
import com.wms.service.UserService;
import com.wms.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器：用户管理和管理员管理模块
 * </p>
 *
 * @author linsuwen
 * @since 2023-01-02
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private WorkOrderService workOrderService;

    /*
     * 查询全部用户
     * @author linsuwen
     * @date 2023/1/2 19:26
     */
    @GetMapping("/list")
    public List<User> list(){
        return userService.list();
    }

    /*
     * 根据账号查找用户
     * @author linsuwen
     * @date 2023/1/4 14:53
     */
    @GetMapping("/findByNo")
    public Result findByNo(@RequestParam String no){
        List list = userService.lambdaQuery()
                .eq(User::getNo,no)
                .list();
        return list.size()>0?Result.success(list):Result.fail();
    }

    /*
     * 新增用户
     * @author linsuwen
     * @date 2023/1/2 19:11
     */
    @PostMapping("/save")
    public Result save(@RequestBody User user){
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        return userService.save(user)?Result.success():Result.fail();
    }

    /*
     * 更新用户
     * @author linsuwen
     * @date 2023/1/2 19:11
     */
    @PostMapping("/update")
    public Result update(@RequestBody User user){
        return userService.updateById(user)?Result.success():Result.fail();
    }

    /*
     * 用户登录：登录的时候一并将菜单信息也查询出来
     * @author linsuwen
     * @date 2023/1/3 14:08
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        List<User> list = userService.lambdaQuery()
                .eq(User::getNo, user.getNo())
                .list();

        if (list.size() > 0) {
            User dbUser = list.get(0);
            if (!PasswordUtil.matches(user.getPassword(), dbUser.getPassword())) {
                return Result.fail();
            }
            List<Menu> menuList = menuService.lambdaQuery()
                    .like(Menu::getMenuright, dbUser.getRoleId())
                    .list();
            HashMap res = new HashMap();
            res.put("user", dbUser);
            res.put("menu", menuList);
            return Result.success(res);
        }
        return Result.fail();
    }

    /*
     * 修改用户
     * @author linsuwen
     * @date 2023/1/4 15:02
     */
    @PostMapping("/mod")
    public boolean mod(@RequestBody User user){
        return userService.updateById(user);
    }
    
    /*
     * 新增或修改：存在用户则修改，否则新增用户
     * @author linsuwen
     * @date 2023/1/2 19:12
     */
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody User user){
        if (user.getId() == null && user.getPassword() != null) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }
        return userService.saveOrUpdate(user)?Result.success():Result.fail();
    }

    /*
     * 删除用户
     * @author linsuwen
     * @date 2023/1/2 19:15
     */
    @GetMapping("/del")
    public Result delete(Integer id){
        return userService.removeById(id)?Result.success():Result.fail();
    }

    /*
     * 模糊查询
     * @author linsuwen
     * @date 2023/1/2 19:36
     */
    @PostMapping("/listP")
    public Result query(@RequestBody User user){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(user.getName())){
            wrapper.like(User::getName,user.getName());
        }
        return Result.success(userService.list(wrapper));
    }

    /*
     * 分页查询
     * @author linsuwen
     * @date 2023/1/2 19:48
     */
//    @PostMapping("/listPage")
//    public Result page(@RequestBody QueryPageParam query){
//        HashMap param = query.getParam();
//        String name = (String)param.get("name");
//
//        Page<User> page = new Page();
//        page.setCurrent(query.getPageNum());
//        page.setSize(query.getPageSize());
//
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        wrapper.like(User::getName,name);
//
//        IPage result = userService.page(page,wrapper);
//        return Result.success(result.getRecords(),result.getTotal());
//    }

    @PostMapping("/listPage")
    public List<User> listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        System.out.println("name=>"+(String)param.get("name"));

        Page<User> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(User::getName,name);


        IPage result = userService.page(page,lambdaQueryWrapper);

        System.out.println("total=>"+result.getTotal());

        return result.getRecords();
    }

    /*
     * 查询功能：根据前端表单输入的信息或者下拉框选择查询用户，并以分页的形式返回前端
     * @author linsuwen
     * @date 2023/1/4 20:28
     */
    @PostMapping("/listPageC1")
    public Result listPageC1(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String sex = (String)param.get("sex");
        String roleId = (String)param.get("roleId");

        Page<User> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(User::getName,name);
        }
        if(StringUtils.isNotBlank(sex)){
            lambdaQueryWrapper.eq(User::getSex,sex);
        }
        if(StringUtils.isNotBlank(roleId)){
            lambdaQueryWrapper.eq(User::getRoleId,roleId);
        }

        IPage result = userService.pageCC(page,lambdaQueryWrapper);

        System.out.println("total=>"+result.getTotal());

        return Result.success(result.getRecords(),result.getTotal());
    }

    // 更新个人信息（昵称/年龄/性别/电话）
    @PostMapping("/updateProfile")
    public Result updateProfile(@RequestBody User user) {
        User update = new User();
        update.setId(user.getId());
        update.setName(user.getName());
        update.setAge(user.getAge());
        update.setSex(user.getSex());
        update.setPhone(user.getPhone());
        return userService.updateById(update) ? Result.success() : Result.fail();
    }

    // 修改密码
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody java.util.Map<String, Object> body) {
        Integer id = (Integer) body.get("id");
        String oldPassword = (String) body.get("oldPassword");
        String newPassword = (String) body.get("newPassword");
        User user = userService.getById(id);
        if (user == null) return Result.fail("用户不存在");
        if (!com.wms.common.PasswordUtil.matches(oldPassword, user.getPassword())) {
            return Result.fail("原密码错误");
        }
        user.setPassword(com.wms.common.PasswordUtil.encode(newPassword));
        return userService.updateById(user) ? Result.success() : Result.fail();
    }

    // 更新工人工作状态
    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestBody java.util.Map<String, Object> body) {
        Integer id = (Integer) body.get("id");
        Integer workStatus = (Integer) body.get("workStatus");
        User user = userService.getById(id);
        if (user == null) return Result.fail();
        // 下班时将该工人未完成工单放回缓冲池
        if (workStatus == 0) {
            workOrderService.lambdaUpdate()
                    .eq(WorkOrder::getWorkerId, id)
                    .eq(WorkOrder::getStatus, 0)
                    .set(WorkOrder::getWorkerId, null)
                    .update();
        }
        user.setWorkStatus(workStatus);
        return userService.updateById(user) ? Result.success() : Result.fail();
    }

    // 查询工人状态和今日剩余休息时间
    @GetMapping("/workerStatus")
    public Result workerStatus(@RequestParam Integer id) {
        User user = userService.getById(id);
        if (user == null) return Result.fail();
        // 每天重置休息时间
        LocalDate today = LocalDate.now();
        if (user.getLastRestDate() == null || !user.getLastRestDate().equals(today)) {
            user.setTodayRestMinutes(0);
            user.setLastRestDate(today);
            userService.updateById(user);
        }
        int remaining = 120 - (user.getTodayRestMinutes() == null ? 0 : user.getTodayRestMinutes());
        HashMap<String, Object> res = new HashMap<>();
        res.put("workStatus", user.getWorkStatus());
        res.put("todayRestMinutes", user.getTodayRestMinutes());
        res.put("remainingRestMinutes", Math.max(0, remaining));
        return Result.success(res);
    }

    // 开始休息（检查剩余时间）
    @PostMapping("/startRest")
    public Result startRest(@RequestBody java.util.Map<String, Object> body) {
        Integer id = (Integer) body.get("id");
        User user = userService.getById(id);
        if (user == null) return Result.fail();
        LocalDate today = LocalDate.now();
        if (user.getLastRestDate() == null || !user.getLastRestDate().equals(today)) {
            user.setTodayRestMinutes(0);
            user.setLastRestDate(today);
        }
        int remaining = 120 - (user.getTodayRestMinutes() == null ? 0 : user.getTodayRestMinutes());
        if (remaining <= 0) {
            return Result.fail("今日休息时间已用完");
        }
        user.setWorkStatus(3);
        userService.updateById(user);
        return Result.success(remaining);
    }

    // 增加今日已休息分钟数（前端计时器到期时调用）
    @GetMapping("/addRestMinutes")
    public Result addRestMinutes(@RequestParam Integer id, @RequestParam Integer minutes) {
        User user = userService.getById(id);
        if (user == null) return Result.fail();
        int current = (user.getTodayRestMinutes() == null ? 0 : user.getTodayRestMinutes());
        user.setTodayRestMinutes(current + minutes);
        userService.updateById(user);
        int remaining = 120 - user.getTodayRestMinutes();
        return Result.success(Math.max(0, remaining));
    }

}
