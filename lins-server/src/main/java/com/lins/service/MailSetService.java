package com.lins.service;

import com.lins.entity.MailSet;
import com.lins.mapper.MailSetMapper;
import com.lins.util.DateUtil;
import com.lins.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @version v2.3
 * @ClassName:DiskIoStateService.java
 * @author: lins
 * @date: 2019年11月16日
 * @Description: DiskIoStateService.java
 * @Copyright: 2017-2021 lins. All rights reserved.
 */
@Service
public class MailSetService {


    public void save(MailSet MailSet) throws Exception {
        MailSet.setId(UUIDUtil.getUUID());
        MailSet.setCreateTime(DateUtil.getNowTime());
        MailSet.setFromMailName(MailSet.getFromMailName().trim());
        MailSet.setFromPwd(MailSet.getFromPwd().trim());
        MailSet.setToMail(MailSet.getToMail().trim());
        MailSet.setSmtpHost(MailSet.getSmtpHost().trim());
        mailSetMapper.save(MailSet);
    }


    public int deleteById(String[] id) throws Exception {
        return mailSetMapper.deleteById(id);
    }

    public List<MailSet> selectAllByParams(Map<String, Object> params) throws Exception {
        return mailSetMapper.selectAllByParams(params);
    }

    public int updateById(MailSet MailSet) throws Exception {
        return mailSetMapper.updateById(MailSet);
    }


    @Autowired
    private MailSetMapper mailSetMapper;


}
