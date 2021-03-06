package org.ccci.idm.rules.services;

import org.ccci.idm.user.UserManager;
import org.ccci.idm.user.ldaptive.dao.io.GroupValueTranscoder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author lee.braddock
 */
public class UserManagerService
{
    static private ApplicationContext applicationContext =
            new ClassPathXmlApplicationContext("classpath*:spring-configuration/*.xml");

    public static UserManager getUserManager()
    {
        return (UserManager) applicationContext.getBean("userManager");
    }

    public static GroupValueTranscoder getGroupValueTranscoder()
    {
        return (GroupValueTranscoder) applicationContext.getBean("groupValueTranscoder");
    }
}
