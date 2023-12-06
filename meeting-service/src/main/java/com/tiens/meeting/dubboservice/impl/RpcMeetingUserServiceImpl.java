package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.MeetingResourceTypeVO;
import com.tiens.api.vo.VMUserVO;
import com.tiens.china.circle.api.bo.HomepageBo;
import com.tiens.china.circle.api.common.result.Result;
import com.tiens.china.circle.api.dto.HomepageUserDTO;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
import com.tiens.meeting.repository.po.MeetingHostUserPO;
import com.tiens.meeting.repository.po.MeetingLevelResourceConfigPO;
import com.tiens.meeting.repository.service.MeetingHostUserDaoService;
import com.tiens.meeting.repository.service.MeetingLevelResourceConfigDaoService;
import common.enums.MeetingResourceEnum;
import common.enums.VmUserSourceEnum;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class RpcMeetingUserServiceImpl implements RpcMeetingUserService {

    //    @Reference(version = "1.0", mock = "com.tiens.meeting.dubboservice.mock.DubboCommonUserServiceMock")
    @Reference(version = "1.0")
    DubboCommonUserService dubboCommonUserService;

    private final MeetingHostUserDaoService meetingHostUserDaoService;

    private final MeetingLevelResourceConfigDaoService meetingLevelResourceConfigDaoService;

    /**
     * 通过卓越卡号查询用户
     *
     * @param joyoCode
     * @return
     */
    @Override
    public CommonResult<VMUserVO> queryVMUser(String joyoCode, String accid) {
        HomepageBo homepageBo = new HomepageBo();
        homepageBo.setJoyoCode(joyoCode);
        homepageBo.setAccId(accid);
        Result<HomepageUserDTO> dtoResult = null;
        try {
            dtoResult = dubboCommonUserService.queryUserInfoAccId(null, homepageBo);
            if (ObjectUtils.isEmpty(dtoResult.getData())) {
                return CommonResult.success(null);
            }
        } catch (Exception e) {
            log.error("调用VM 查询用户异常");
            return CommonResult.success(null);
        }
        HomepageUserDTO data = dtoResult.getData();
        VMUserVO vmUserVO = BeanUtil.copyProperties(data, VMUserVO.class);
        vmUserVO.setJoyoCode(data.getJoyo_code());
        return CommonResult.success(vmUserVO);
    }

    /**
     * 通过卓越卡号添加用户
     *
     * @param joyoCode
     * @param resourceType
     * @return
     */
    @Override
    @Transactional
    public CommonResult addMeetingHostUser(String joyoCode, Integer resourceType) throws ServiceException {
        //1、查询主持人信息是否存在
        CommonResult<VMUserVO> vmUserVOCommonResult = queryVMUser(joyoCode, "");
        VMUserVO vmUserVO = vmUserVOCommonResult.getData();
        if (ObjectUtil.isEmpty(vmUserVOCommonResult.getData())) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_FOUND_HOST_INFO);
        }
        //校验配置是否合法
        boolean res = checkLevelResource(vmUserVO, resourceType);
        if (!res){
            return CommonResult.error(GlobalErrorCodeConstants.EXIST_HOST_RESOURCE_CONFIGURATION);
        }
        //2、添加到主持人表

        MeetingHostUserPO meetingHostUserPO = wrapperMeetingHostUserPO(vmUserVO,resourceType);
        try {
            meetingHostUserDaoService.save(meetingHostUserPO);
        } catch (DuplicateKeyException e) {
            log.error("accId 重复异常");
            return CommonResult.error(GlobalErrorCodeConstants.EXIST_HOST_INFO);
        }
        //3、添加主持人信息到华为云用户列表中
        boolean result = syncAddHWMeetinguser(vmUserVO);
        return CommonResult.success(result);
    }
    private boolean checkLevelResource(VMUserVO vmUserVO,Integer resourceType){
        if (vmUserVO.getLevelCode()==9){
            if (!(resourceType>=7)){
                //不符合规则
                return false;
            }
        }else{
            //1-8级逻辑处理
            MeetingLevelResourceConfigPO configPO = meetingLevelResourceConfigDaoService.lambdaQuery().eq(MeetingLevelResourceConfigPO::getVmUserLevel, vmUserVO.getLevelCode()).oneOpt().get();
            if (resourceType<=configPO.getResourceType()){
                //不符合规则
                return false;
            }
        }
        return true;
    }
    /**
     * 通过accid添加会议用户
     *
     * @param accid
     * @return
     */
    @Override
    public CommonResult addMeetingCommonUser(String accid) throws ServiceException {
        //通过accid查询用户
        CommonResult<VMUserVO> vmUserVOCommonResult = queryVMUser("", accid);
        VMUserVO vmUserVO = vmUserVOCommonResult.getData();
        if (ObjectUtil.isEmpty(vmUserVOCommonResult.getData())) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_FOUND_HOST_INFO);
        }
        //3、添加主持人信息到华为云用户列表中
        boolean result = false;
        try {
            result = syncAddHWMeetinguser(vmUserVO);
        } catch (Exception e) {
            log.error("添加普通用户发生异常！", e);
        }
        return CommonResult.success(result);
    }

    private boolean syncAddHWMeetinguser(VMUserVO vmUserVO) throws ServiceException {
        AddUserRequest request = new AddUserRequest();
        AddUserDTO body = new AddUserDTO();
//        String mobile = vmUserVO.getMobile();
//        if (ObjectUtil.isNotEmpty(mobile)) {
//            body.withPhone(mobile);
//        }
        //1-买买 2-云购 3 Vshare 4 瑞狮 5意涵永

        body.withName(buildHWName(vmUserVO));
        body.setEmail(vmUserVO.getEmail());
        //userId
        body.withThirdAccount(vmUserVO.getAccid());
        //华为账号为卓越卡号拼接
        body.withAccount(buildHWAccount(vmUserVO));

        request.withBody(body);
        //userId
        try {
            MeetingClient meetingClient = SpringUtil.getBean(MeetingClient.class);
            AddUserResponse response = meetingClient.addUser(request);
            log.info("华为云添加用户结果：{}", JSON.toJSONString(response));
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
//            System.out.println(e.getHttpStatusCode());
//            System.out.println(e.getRequestId());
//            System.out.println(e.getErrorCode());
//            System.out.println(e.getErrorMsg());
            if (e.getErrorCode().equals("USG.201040001")) {
                //账号已经存在
                log.info("账号已存在，无需添加");
                return true;
            }
            log.error("华为云添加用户业务异常", e);
            throw new ServiceException("1000", e.getErrorMsg());
        }
        return true;
    }

    private String buildHWName(VMUserVO vmUserVO) {
        String specificSymbol = "&<>/()' \"";
        String brief = StrUtil.brief(vmUserVO.getNickName(), 64);

        if (StrUtil.containsAny(brief, specificSymbol.toCharArray())) {
            //包含特殊符号，重写昵称
            brief = vmUserVO.getJoyoCode();
        }
        return brief;
    }

    private boolean RemoveHWMeetinguser(List<String> accIds) throws ServiceException {
        BatchDeleteUsersRequest request = new BatchDeleteUsersRequest();

        request.withBody(accIds);
        request.withAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        try {
            MeetingClient meetingClient = SpringUtil.getBean(MeetingClient.class);
            BatchDeleteUsersResponse response= meetingClient.batchDeleteUsers(request);
            log.info("华为云删除用户结果：{}", JSON.toJSONString(response));
        }catch (ServiceResponseException e){
            e.printStackTrace();
            log.error("华为云删除用户业务异常", e);
            throw new ServiceException("1000", e.getErrorMsg());
        }
        return true;
    }

    private String buildHWAccount(VMUserVO vmUserVO) {
        String source = VmUserSourceEnum.getNameByCode(vmUserVO.getSource());
        String joyoCode = ObjectUtil.defaultIfBlank(vmUserVO.getJoyoCode(), "DEFAULT" + RandomUtil.randomNumbers(20));
        return source + "-" + joyoCode;
    }

    private MeetingHostUserPO wrapperMeetingHostUserPO(VMUserVO data, Integer resourceType) {
        MeetingHostUserPO meetingHostUserPO = new MeetingHostUserPO();
        meetingHostUserPO.setAccId(data.getAccid());
        meetingHostUserPO.setPhone(data.getMobile());
        meetingHostUserPO.setEmail(data.getEmail());
        meetingHostUserPO.setName(data.getNickName());
        meetingHostUserPO.setJoyoCode(data.getJoyoCode());
        meetingHostUserPO.setLevel(data.getLevelCode());
        meetingHostUserPO.setResourceType(resourceType);
        return meetingHostUserPO;
    }

    /**
     * 移除主持人
     *
     * @param hostUserId
     * @return
     */
    @Override
    public CommonResult removeMeetingHostUser(Long hostUserId) {
        //1、删除主持人数据库数据
        boolean b = meetingHostUserDaoService.removeById(hostUserId);
        //2、删除华为云用户数据
        return CommonResult.success(b);
    }

    /**
     * 查询主持人信息
     *
     * @param accId
     * @return
     */
    @Override
    public CommonResult<MeetingHostUserVO> queryMeetingHostUser(String accId) {

        Optional<MeetingHostUserPO> meetingHostUserPOOptional =
            meetingHostUserDaoService.lambdaQuery().eq(MeetingHostUserPO::getAccId, accId).oneOpt();

        if (meetingHostUserPOOptional.isPresent()) {
            MeetingHostUserVO meetingHostUserVO =
                BeanUtil.copyProperties(meetingHostUserPOOptional.get(), MeetingHostUserVO.class);
            return CommonResult.success(meetingHostUserVO);
        }
        return CommonResult.success(null);
    }

    /**
     * 分页查询主持人列表
     *
     * @param pageDTOPageParam
     * @return
     */
    @Override
    public PageResult<MeetingHostUserVO> queryPage(PageParam<MeetingHostPageDTO> pageDTOPageParam) {

        Page<MeetingHostUserPO> page = new Page<>(pageDTOPageParam.getPageNum(), pageDTOPageParam.getPageSize());
        MeetingHostPageDTO condition = pageDTOPageParam.getCondition();

        LambdaQueryWrapper<MeetingHostUserPO> queryWrapper = Wrappers.lambdaQuery(MeetingHostUserPO.class)
            .like(ObjectUtil.isNotEmpty(condition.getName()), MeetingHostUserPO::getName, condition.getName())
            .eq(ObjectUtil.isNotEmpty(condition.getJoyoCode()), MeetingHostUserPO::getJoyoCode, condition.getJoyoCode())
            .like(ObjectUtil.isNotEmpty(condition.getPhone()), MeetingHostUserPO::getPhone, condition.getPhone())
            .like(ObjectUtil.isNotEmpty(condition.getEmail()), MeetingHostUserPO::getEmail, condition.getEmail())
            .orderByDesc(MeetingHostUserPO::getCreateTime);
        Page<MeetingHostUserPO> pagePoResult = meetingHostUserDaoService.page(page, queryWrapper);
        List<MeetingHostUserPO> records = pagePoResult.getRecords();
        List<MeetingHostUserVO> meetingHostUserVOS = BeanUtil.copyToList(records, MeetingHostUserVO.class);
        if (ObjectUtil.isNotEmpty(meetingHostUserVOS)){
            meetingHostUserVOS.forEach(meetingHostUserVO -> {
                MeetingResourceEnum byCode = MeetingResourceEnum.getByCode(Optional.ofNullable(meetingHostUserVO.getResourceType()).orElse(0));
                meetingHostUserVO.setResourceNum(byCode.getValue());
            });
        }
        PageResult<MeetingHostUserVO> pageResult = new PageResult<>();
        pageResult.setList(meetingHostUserVOS);
        pageResult.setTotal(pagePoResult.getTotal());
        return pageResult;
    }

    @Override
    public CommonResult<List<MeetingResourceTypeVO>> queryResourceTypes() {
        List<MeetingLevelResourceConfigPO> list = meetingLevelResourceConfigDaoService.lambdaQuery().ne(MeetingLevelResourceConfigPO::getResourceType,0).list();
        if (!ObjectUtil.isNotEmpty(list)){
            return CommonResult.success(null);
        }
        ArrayList<MeetingResourceTypeVO> typeVOS = new ArrayList<>();
        list.forEach(meetingLevelResourceConfigPO -> {
            MeetingResourceTypeVO typeVO = new MeetingResourceTypeVO();
            BeanUtil.copyProperties(meetingLevelResourceConfigPO,typeVO);
            MeetingResourceEnum byCode = MeetingResourceEnum.getByCode(typeVO.getResourceType());
            typeVO.setResourceTypeName(byCode.getDesc());
            typeVOS.add(typeVO);
        });
        return CommonResult.success(typeVOS);
    }
}
