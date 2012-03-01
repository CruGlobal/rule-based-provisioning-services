package org.ccci.idm.rules.services.factprovider;

import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.ccci.idm.obj.IdentityUser;
import org.ccci.idm.rules.obj.EmployeeInfo;
import org.ccci.idm.rules.services.FactProvider;
import org.ccci.soa.pshr.client.StaffService;
import org.ccci.soa.pshr.client.StaffServiceService;
import org.ccci.soa.pshr.client.UsStaffMember;

public class EmployeeInfoProvider implements FactProvider
{
    private StaffService service;
    private String serviceServerId;
    private String serviceServerSecret;
    private Properties properties;
    
    public EmployeeInfoProvider(Properties properties) throws Exception
    {
        super();
        this.properties = properties;
        String wsdlUrl = properties.getProperty("pshrService.wsdlUrl");
        String namespace = properties.getProperty("pshrService.namespace");
        String serviceName = properties.getProperty("pshrService.serviceName");
        URL wsdl = new URL(wsdlUrl);
        QName serviceQname = new QName(namespace, serviceName);
        
        StaffServiceService locator = new StaffServiceService(wsdl, serviceQname);
        service = locator.getStaffServicePort();
        
        serviceServerId = properties.getProperty("pshrService.serverId");
        serviceServerSecret = properties.getProperty("pshrService.serverSecret");
    }
    
    public EmployeeInfoProvider(StaffService service, Properties properties) throws Exception
    {
        this.service = service;
        this.properties = properties;
        serviceServerId = properties.getProperty("pshrService.serverId");
        serviceServerSecret = properties.getProperty("pshrService.serverSecret");
    }

    @Override
    public String getFactName()
    {
        return EmployeeInfo.class.getSimpleName();
    }

    @Override
    public Class<?> getFactType()
    {
        return EmployeeInfo.class;
    }

    @Override
    public Object getFact(IdentityUser identityUser)
    {
        try
        {
            String emplid = identityUser.getEmployee()==null?null:identityUser.getEmployee().getEmployeeId();
            if(emplid==null) return null;
            UsStaffMember staff = service.getStaff(serviceServerId, serviceServerSecret, emplid);
            if(staff==null) return null;
            return new EmployeeInfo(staff.getEmploymentInfo());
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
