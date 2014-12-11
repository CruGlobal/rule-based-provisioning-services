package org.ccci.idm.rules.webservice;

import java.util.Properties;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.ccci.idm.authentication.credentials.impl.UsernamePasswordCredentials;
import org.ccci.idm.authentication.handler.impl.PropertyBasedUsernamePasswordAuthHandler;
import org.ccci.idm.authentication.manager.AuthenticationManager;
import org.ccci.idm.authentication.manager.impl.AuthenticationManagerImpl;
import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.rules.services.IdentityDaoFactory;
import org.ccci.idm.rules.services.MetaRuleService;
import org.ccci.idm.rules.services.RuleFilter;
import org.ccci.idm.rules.services.rolemanager.RoleManagerFactoryUserManager;
import org.ccci.util.mail.ErrorEmailer;
import org.ccci.util.properties.CcciProperties.PropertyEncryptionSetup;
import org.ccci.util.properties.PropertiesWithFallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService()
public class RuleProvSvc
{
    private Logger logger = LoggerFactory.getLogger(getClass());
    private AuthenticationManager authenticationManager;
    private Properties properties;
    private MetaRuleService service;
	

	public RuleProvSvc() throws Exception
	{
		properties = loadProperties();
        setupAuthenticationManager();
        service = new MetaRuleService(properties, new RoleManagerFactoryUserManager());
	}
	
    @WebMethod(operationName = "runRules")
    public String runRules(@WebParam(name = "serverId") String serverId, @WebParam(name = "serverSecret") String serverSecret, @WebParam(name = "ssoGuid") String ssoGuid, @WebParam(name = "ruleFilter") RuleFilter ruleFilter) throws Throwable
    {
        logger.debug("web service run rules " + ssoGuid);

        authenticationManager.authenticate(new UsernamePasswordCredentials(serverId, serverSecret));

        logger.debug("web service run rules authenticated " + ssoGuid);

        service.setupDefaultIfNecessary();

        logger.debug("web service run rules set default " + ssoGuid);

        IdentityDAO identityDao = IdentityDaoFactory.getInstance();

        logger.debug("web service run rules got dao " + ssoGuid + " rule filter " +
                (ruleFilter == null ? ruleFilter :
                ruleFilter.getRulesets()));

        try
        {
            service.runRules(identityDao, ssoGuid, ruleFilter);
            logger.debug("web service done run rules got dao " + ssoGuid);
            return "done";
        }
        catch(Throwable e)
        {
            logger.error("web service run rules error " + ssoGuid, e);
            ErrorEmailer.sendErrorToAdmin(properties, e);
            throw e;
        }
        finally
        {
            identityDao.close();
        }
    }
    
    
    public static Properties loadProperties()
    {
        PropertyEncryptionSetup encryptionSetup = new PropertyEncryptionSetup("lco97gf5t7D%Y4bh89%U34IF&l*()$Hg6wRD^j4");
        return new PropertiesWithFallback(encryptionSetup, false, "/apps/apps-config/rules.properties",
                "/ora/apps-config/rules.properties","/rulesDefault.properties");
    }

    private void setupAuthenticationManager()
    {
        authenticationManager = new AuthenticationManagerImpl();
        authenticationManager.addAuthenticationHandler(new PropertyBasedUsernamePasswordAuthHandler(properties, "user."));
    }


}
