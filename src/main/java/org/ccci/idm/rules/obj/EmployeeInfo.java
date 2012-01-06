package org.ccci.idm.rules.obj;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ccci.soa.pshr.client.UsEmployeeInfo;
import org.ccci.util.NkUtil;

public class EmployeeInfo
{
    DateFormat df = new SimpleDateFormat("M/d/yy");
    private UsEmployeeInfo wrapped;
    
    public EmployeeInfo(UsEmployeeInfo wrapped)
    {
        super();
        this.wrapped = wrapped;
    }
    public Date getTermDateAsDate() throws ParseException
    {
        if(NkUtil.isBlank(getTermDate())) return null;
        return df.parse(getTermDate());
    }
    public boolean equals(Object arg0)
    {
        return wrapped.equals(arg0);
    }
    public String getActiveStatus()
    {
        return wrapped.getActiveStatus();
    }
    public int hashCode()
    {
        return wrapped.hashCode();
    }
    public void setActiveStatus(String value)
    {
        wrapped.setActiveStatus(value);
    }
    public String getCompany()
    {
        return wrapped.getCompany();
    }
    public void setCompany(String value)
    {
        wrapped.setCompany(value);
    }
    public String getDeptCode()
    {
        return wrapped.getDeptCode();
    }
    public void setDeptCode(String value)
    {
        wrapped.setDeptCode(value);
    }
    public String getDeptDescr()
    {
        return wrapped.getDeptDescr();
    }
    public void setDeptDescr(String value)
    {
        wrapped.setDeptDescr(value);
    }
    public String getDivision()
    {
        return wrapped.getDivision();
    }
    public void setDivision(String value)
    {
        wrapped.setDivision(value);
    }
    public String getEffDt()
    {
        return wrapped.getEffDt();
    }
    public void setEffDt(String value)
    {
        wrapped.setEffDt(value);
    }
    public String getEmplStatus()
    {
        return wrapped.getEmplStatus();
    }
    public void setEmplStatus(String value)
    {
        wrapped.setEmplStatus(value);
    }
    public String getEmployed()
    {
        return wrapped.getEmployed();
    }
    public void setEmployed(String value)
    {
        wrapped.setEmployed(value);
    }
    public String getHrEditOnly()
    {
        return wrapped.getHrEditOnly();
    }
    public void setHrEditOnly(String value)
    {
        wrapped.setHrEditOnly(value);
    }
    public String getJobCode()
    {
        return wrapped.getJobCode();
    }
    public void setJobCode(String value)
    {
        wrapped.setJobCode(value);
    }
    public String getJobDescr()
    {
        return wrapped.getJobDescr();
    }
    public void setJobDescr(String value)
    {
        wrapped.setJobDescr(value);
    }
    public String getLakeHartMailCode()
    {
        return wrapped.getLakeHartMailCode();
    }
    public void setLakeHartMailCode(String value)
    {
        wrapped.setLakeHartMailCode(value);
    }
    public String getLatestHireDate()
    {
        return wrapped.getLatestHireDate();
    }
    public void setLatestHireDate(String value)
    {
        wrapped.setLatestHireDate(value);
    }
    public String getLocationCode()
    {
        return wrapped.getLocationCode();
    }
    public void setLocationCode(String value)
    {
        wrapped.setLocationCode(value);
    }
    public String getLocationDescr()
    {
        return wrapped.getLocationDescr();
    }
    public void setLocationDescr(String value)
    {
        wrapped.setLocationDescr(value);
    }
    public String getMinistryCode()
    {
        return wrapped.getMinistryCode();
    }
    public void setMinistryCode(String value)
    {
        wrapped.setMinistryCode(value);
    }
    public String getMinistryDescr()
    {
        return wrapped.getMinistryDescr();
    }
    public void setMinistryDescr(String value)
    {
        wrapped.setMinistryDescr(value);
    }
    public String getOrigHireDate()
    {
        return wrapped.getOrigHireDate();
    }
    public void setOrigHireDate(String value)
    {
        wrapped.setOrigHireDate(value);
    }
    public String getPaygroup()
    {
        return wrapped.getPaygroup();
    }
    public void setPaygroup(String value)
    {
        wrapped.setPaygroup(value);
    }
    public String getPositionCode()
    {
        return wrapped.getPositionCode();
    }
    public void setPositionCode(String value)
    {
        wrapped.setPositionCode(value);
    }
    public String getStatusCode()
    {
        return wrapped.getStatusCode();
    }
    public void setStatusCode(String value)
    {
        wrapped.setStatusCode(value);
    }
    public String getSubministryCode()
    {
        return wrapped.getSubministryCode();
    }
    public void setSubministryCode(String value)
    {
        wrapped.setSubministryCode(value);
    }
    public String getSubministryDescr()
    {
        return wrapped.getSubministryDescr();
    }
    public void setSubministryDescr(String value)
    {
        wrapped.setSubministryDescr(value);
    }
    public String getSupervisorEmplid()
    {
        return wrapped.getSupervisorEmplid();
    }
    public void setSupervisorEmplid(String value)
    {
        wrapped.setSupervisorEmplid(value);
    }
    public String getSupported()
    {
        return wrapped.getSupported();
    }
    public void setSupported(String value)
    {
        wrapped.setSupported(value);
    }
    public String getTermDate()
    {
        return wrapped.getTermDate();
    }
    public void setTermDate(String value)
    {
        wrapped.setTermDate(value);
    }
    public String getWwcFreeSubFlag()
    {
        return wrapped.getWwcFreeSubFlag();
    }
    public void setWwcFreeSubFlag(String value)
    {
        wrapped.setWwcFreeSubFlag(value);
    }
    public String toString()
    {
        return wrapped.toString();
    }
    
    
    
    
}
