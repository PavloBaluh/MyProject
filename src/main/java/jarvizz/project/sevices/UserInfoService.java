package jarvizz.project.sevices;

import jarvizz.project.dao.UserInfoDao;
import jarvizz.project.models.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {
    @Autowired
    UserInfoDao userInfoDao;

    public void save(UserInfo userInfo){
        userInfoDao.save(userInfo);
    }
    public UserInfo get(Integer id){
        return userInfoDao.getOne(id);
    }
}
