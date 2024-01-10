package com.tiens.meeting.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiens.meeting.repository.mapper.MeetingAttendeeMapper;
import com.tiens.meeting.repository.po.MeetingAttendeePO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author yuwenjie
 * @description 针对表【meeting_attendee(会议与会者表)】的数据库操作Service实现
 * @createDate 2024-01-10 10:39:54
 */
@Service
public class MeetingAttendeeDaoServiceImpl extends ServiceImpl<MeetingAttendeeMapper, MeetingAttendeePO>
    implements MeetingAttendeeDaoService {

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public boolean saveBatch(Collection<MeetingAttendeePO> entityList, int batchSize) {
        try {
            int size = entityList.size();
            int idxLimit = Math.min(batchSize, size);
            int i = 1;
            //保存单批提交的数据集合
            List<MeetingAttendeePO> oneBatchList = new ArrayList<>();
            for (Iterator<MeetingAttendeePO> var7 = entityList.iterator(); var7.hasNext(); ++i) {
                MeetingAttendeePO element = var7.next();
                oneBatchList.add(element);
                if (i == idxLimit) {
                    baseMapper.insertBatchSomeColumn(oneBatchList);
                    //每次提交后需要清空集合数据
                    oneBatchList.clear();
                    idxLimit = Math.min(idxLimit + batchSize, size);
                }
            }
        } catch (Exception e) {
            log.error("saveBatch fail", e);
            return false;
        }
        return true;
    }

}




