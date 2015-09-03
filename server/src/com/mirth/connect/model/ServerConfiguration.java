/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.text.DateFormatter;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.directoryresource.DirectoryResourceProperties;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("serverConfiguration")
public class ServerConfiguration implements Serializable, Migratable {
    private String date;
    private List<Channel> channels = null;
    private List<User> users = null;
    private List<AlertModel> alerts = null;
    private List<CodeTemplateLibrary> codeTemplateLibraries = null;
    private ServerSettings serverSettings = null;
    private UpdateSettings updateSettings = null;
    private Map<String, String> globalScripts = null;
    private Map<String, Properties> pluginProperties = null;
    private ResourcePropertiesList resourceProperties = null;

    public List<AlertModel> getAlerts() {
        return this.alerts;
    }

    public void setAlerts(List<AlertModel> alerts) {
        this.alerts = alerts;
    }

    public List<Channel> getChannels() {
        return this.channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public void setServerSettings(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    public UpdateSettings getUpdateSettings() {
        return updateSettings;
    }

    public void setUpdateSettings(UpdateSettings updateSettings) {
        this.updateSettings = updateSettings;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CodeTemplateLibrary> getCodeTemplateLibraries() {
        return codeTemplateLibraries;
    }

    public void setCodeTemplateLibraries(List<CodeTemplateLibrary> codeTemplateLibraries) {
        this.codeTemplateLibraries = codeTemplateLibraries;
    }

    public Map<String, String> getGlobalScripts() {
        return globalScripts;
    }

    public void setGlobalScripts(Map<String, String> globalScripts) {
        this.globalScripts = globalScripts;
    }

    public Map<String, Properties> getPluginProperties() {
        return pluginProperties;
    }

    public void setPluginProperties(Map<String, Properties> pluginProperties) {
        this.pluginProperties = pluginProperties;
    }

    public ResourcePropertiesList getResourceProperties() {
        return resourceProperties;
    }

    public void setResourceProperties(ResourcePropertiesList resourceProperties) {
        this.resourceProperties = resourceProperties;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        DonkeyElement globalScripts = element.getChildElement("globalScripts");

        if (globalScripts != null) {
            for (DonkeyElement entry : globalScripts.getChildElements()) {
                DonkeyElement keyString = entry.getChildElement("string");
                if (keyString.getTextContent().equals("Shutdown")) {
                    keyString.setTextContent("Undeploy");
                }
            }
        }
    }

    @Override
    public void migrate3_2_0(DonkeyElement element) {
        ResourcePropertiesList list = new ResourcePropertiesList();
        DirectoryResourceProperties defaultResource = new DirectoryResourceProperties();
        defaultResource.setId(ResourceProperties.DEFAULT_RESOURCE_ID);
        defaultResource.setName(ResourceProperties.DEFAULT_RESOURCE_NAME);
        defaultResource.setDescription("Loads libraries from the custom-lib folder in the Mirth Connect home directory.");
        defaultResource.setIncludeWithGlobalScripts(true);
        defaultResource.setDirectory("custom-lib");
        list.getList().add(defaultResource);

        try {
            DonkeyElement resourcePropertiesElement = element.addChildElementFromXml(ObjectXMLSerializer.getInstance().serialize(list));
            resourcePropertiesElement.setNodeName("resourceProperties");
        } catch (DonkeyElementException e) {
            throw new SerializerException("Failed to migrate server configuration.", e);
        }
    }

    @Override
    public void migrate3_3_0(DonkeyElement element) {
        DonkeyElement librariesElement = element.addChildElement("codeTemplateLibraries");
        DonkeyElement libraryElement = librariesElement.addChildElement("codeTemplateLibrary");
        libraryElement.setAttribute("version", "3.3.0");
        libraryElement.addChildElement("id", UUID.randomUUID().toString());
        libraryElement.addChildElement("name", "Library 1");
        libraryElement.addChildElement("revision", "1");
        try {
            libraryElement.addChildElementFromXml(ObjectXMLSerializer.getInstance().serialize(Calendar.getInstance())).setNodeName("lastModified");
        } catch (DonkeyElementException e) {
            throw new SerializerException("Failed to migrate code template library last modified date.", e);
        }
        libraryElement.addChildElement("description", "This library was added upon migration to version 3.3.0. It includes all pre-existing\ncode templates, and is set to be included on all pre-existing and new channels.\n\nYou should create your own new libraries and assign code templates to them as you\nsee fit. You should also link libraries to specific channels, so that you're not\nnecessarily including all code templates on all channels all the time.");
        libraryElement.addChildElement("includeNewChannels", "true");
        libraryElement.addChildElement("enabledChannelIds");
        libraryElement.addChildElement("disabledChannelIds");
        try {
            libraryElement.addChildElementFromXml(element.removeChild("codeTemplates").toXml());
        } catch (DonkeyElementException e) {
            throw new SerializerException("Failed to migrate code templates.", e);
        }

        String time = "";
        String interval = "";
        String dayOfWeek = "";
        String dayOfMonth = "1";
        boolean disabled = true;

        DonkeyElement properties = null;
        DonkeyElement pluginProperties = element.getChildElement("pluginProperties");
        if (pluginProperties != null) {
            for (DonkeyElement childElement : pluginProperties.getChildElements()) {
                String entryKey = childElement.getChildElement("string").getTextContent();
                if (entryKey.equals("Data Pruner")) {
                    properties = childElement.getChildElement("properties");

                    for (DonkeyElement property : properties.getChildElements()) {
                        String propertyName = property.getAttributes().item(0).getTextContent();

                        if (propertyName.equals("dayOfWeek")) {
                            dayOfWeek = property.getTextContent();
                        } else if (propertyName.equals("dayOfMonth")) {
                            dayOfMonth = property.getTextContent();
                        } else if (propertyName.equals("interval")) {
                            interval = property.getTextContent();
                        } else if (propertyName.equals("time")) {
                            time = property.getTextContent();
                        }
                    }
                }
            }

            String pollingType = "INTERVAL";
            String pollingHour = "12";
            String pollingMinute = "0";
            boolean isWeekly = true;
            boolean[] activeDays = new boolean[] { true, true, true, true, true, true, true, true };

            disabled = interval.equals("disabled");

            if (!interval.equals("hourly") && !disabled) {
                SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm aa");
                DateFormatter timeFormatter = new DateFormatter(timeDateFormat);
                Date timeDate = null;

                try {
                    timeDate = (Date) timeFormatter.stringToValue(time);
                    Calendar timeCalendar = Calendar.getInstance();
                    timeCalendar.setTime(timeDate);

                    pollingType = "TIME";
                    pollingHour = String.valueOf(timeCalendar.get(Calendar.HOUR_OF_DAY));
                    pollingMinute = String.valueOf(timeCalendar.get(Calendar.MINUTE));

                    isWeekly = interval.equals("weekly");
                    if (isWeekly) {
                        SimpleDateFormat dayDateFormat = new SimpleDateFormat("EEEEEEEE");
                        DateFormatter dayFormatter = new DateFormatter(dayDateFormat);

                        Date dayDate = (Date) dayFormatter.stringToValue(dayOfWeek);
                        Calendar dayCalendar = Calendar.getInstance();
                        dayCalendar.setTime(dayDate);

                        activeDays[dayCalendar.get(Calendar.DAY_OF_WEEK)] = false;
                    }
                } catch (Exception e) {
                }
            }

            try {
                DonkeyElement pollingProperties = new DonkeyElement("<com.mirth.connect.donkey.model.channel.PollConnectorProperties/>");
                pollingProperties.setAttribute("version", "3.3.0");
                pollingProperties.addChildElementIfNotExists("pollingType", pollingType);
                pollingProperties.addChildElementIfNotExists("pollOnStart", "false");
                pollingProperties.addChildElementIfNotExists("pollingFrequency", "3600000");
                pollingProperties.addChildElementIfNotExists("pollingHour", pollingHour);
                pollingProperties.addChildElementIfNotExists("pollingMinute", pollingMinute);
                pollingProperties.addChildElementIfNotExists("cronJobs");

                DonkeyElement advancedProperties = pollingProperties.addChildElementIfNotExists("pollConnectorPropertiesAdvanced");
                advancedProperties.addChildElementIfNotExists("weekly", isWeekly ? "true" : "false");

                DonkeyElement inactiveDays = advancedProperties.addChildElementIfNotExists("inactiveDays");
                if (inactiveDays != null) {
                    for (int index = 0; index < 8; ++index) {
                        inactiveDays.addChildElement("boolean", activeDays[index] ? "true" : "false");
                    }
                }

                advancedProperties.addChildElementIfNotExists("dayOfMonth", dayOfMonth);
                advancedProperties.addChildElementIfNotExists("allDay", "true");
                advancedProperties.addChildElementIfNotExists("startingHour", "8");
                advancedProperties.addChildElementIfNotExists("startingMinute", "0");
                advancedProperties.addChildElementIfNotExists("endingHour", "17");
                advancedProperties.addChildElementIfNotExists("endingMinute", "0");

                DonkeyElement prunerProperty = properties.addChildElementFromXml(ObjectXMLSerializer.getInstance().serialize(pollingProperties.toXml()));
                prunerProperty.setAttribute("name", "pollingProperties");

                DonkeyElement enabledProperty = properties.addChildElement("property", Boolean.toString(!disabled));
                enabledProperty.setAttribute("name", "enabled");
            } catch (Exception e) {
                throw new SerializerException("Failed to migrate Data Pruner properties.", e);
            }
        }
    }
}
